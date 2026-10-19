package com.ezbarrows;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.CollisionData;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@PluginDescriptor(
        name = "EzBarrows",
        description = "Barrows helper: prayer icons on sarcophagi, brother names on mounds, a reward potential tracker and the tunnel route to the chest",
        tags = {"barrows", "prayer", "pvm", "crypt", "tunnel", "path", "mound", "brothers", "reward", "potential", "chest"}
)
public class EzBarrowsPlugin extends Plugin {
    private static final Set<Integer> CHEST_IDS = ImmutableSet.of(
            ObjectID.BARROWS_STONE_CHEST, ObjectID.BARROWS_STONE_CHEST_CLOSED, ObjectID.BARROWS_STONE_CHEST_OPEN);
    private static final String HIDDEN_TUNNEL_TEXT = "You've found a hidden tunnel";

    @Inject
    private Client client;

    @Inject
    private EzBarrowsConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private EzBarrowsOverlay overlay;

    @Inject
    private EzBarrowsPathOverlay pathOverlay;

    @Inject
    private EzBarrowsMoundOverlay moundOverlay;

    @Inject
    private EzBarrowsPotentialOverlay potentialOverlay;

    private final Map<GameObject, BarrowsBrother> sarcophagi = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    private Map<GameObject, BarrowsBrother> reachableSarcophagi = Collections.emptyMap();

    @Getter(AccessLevel.PACKAGE)
    private List<Point> path = Collections.emptyList();

    // Kept across scene loads until the chest is looted
    @Getter(AccessLevel.PACKAGE)
    private BarrowsBrother tunnelBrother;
    private int lastKilledCount;

    private final Set<TileObject> doors = new HashSet<>();
    // Remembered so doors still count as unlocked while opening
    private final Set<Point> unlockedDoorTiles = new HashSet<>();
    private GameObject chest;

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
        overlayManager.add(pathOverlay);
        overlayManager.add(moundOverlay);
        overlayManager.add(potentialOverlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        overlayManager.remove(pathOverlay);
        overlayManager.remove(moundOverlay);
        overlayManager.remove(potentialOverlay);
        reset();
        tunnelBrother = null;
    }

    @Subscribe
    public void onGameObjectSpawned(GameObjectSpawned event) {
        GameObject object = event.getGameObject();
        BarrowsBrother brother = BarrowsBrother.fromSarcophagus(object.getId());
        if (brother != null) {
            sarcophagi.put(object, brother);
        } else if (CHEST_IDS.contains(object.getId())) {
            chest = object;
        } else if (isDoor(object.getId())) {
            doors.add(object);
        }
    }

    @Subscribe
    public void onGameObjectDespawned(GameObjectDespawned event) {
        GameObject object = event.getGameObject();
        sarcophagi.remove(object);
        doors.remove(object);
        if (object == chest) {
            chest = null;
        }
    }

    @Subscribe
    public void onWallObjectSpawned(WallObjectSpawned event) {
        if (isDoor(event.getWallObject().getId())) {
            doors.add(event.getWallObject());
        }
    }

    @Subscribe
    public void onWallObjectDespawned(WallObjectDespawned event) {
        doors.remove(event.getWallObject());
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() == GameState.LOADING) {
            reset();
        }
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        // Looting the chest ends the run
        if (event.getGroupId() == InterfaceID.BARROWS_REWARD) {
            tunnelBrother = null;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        Player player = client.getLocalPlayer();
        WorldView worldView = client.getTopLevelWorldView();
        CollisionData[] collisionMaps = worldView.getCollisionMaps();
        if (player == null || collisionMaps == null) {
            reachableSarcophagi = Collections.emptyMap();
            path = Collections.emptyList();
            return;
        }

        int[][] flags = collisionMaps[worldView.getPlane()].getFlags();
        Point start = toScenePoint(player.getLocalLocation());
        reachableSarcophagi = findReachableSarcophagi(flags, start, worldView.getPlane());
        path = config.showPath() ? findPathToChest(flags, start) : Collections.emptyList();
        updateTunnelBrother();
    }

    private void updateTunnelBrother() {
        Predicate<BarrowsBrother> isKilled = brother -> client.getVarbitValue(brother.getKilledVarbit()) != 0;
        int killedCount = (int) Arrays.stream(BarrowsBrother.values()).filter(isKilled).count();
        // Kills only drop back to zero when a new run starts
        if (killedCount == 0 && lastKilledCount > 0) {
            tunnelBrother = null;
        }
        lastKilledCount = killedCount;

        // The hidden tunnel prompt only appears in the tunnel brother's crypt
        Widget dialogue = client.getWidget(InterfaceID.Objectbox.TEXT);
        if (dialogue != null && !dialogue.isHidden() && dialogue.getText() != null
                && dialogue.getText().contains(HIDDEN_TUNNEL_TEXT) && reachableSarcophagi.size() == 1) {
            tunnelBrother = reachableSarcophagi.values().iterator().next();
        }
        if (tunnelBrother == null) {
            tunnelBrother = onlyBrotherAlive(isKilled);
        }
    }

    // With five brothers killed, the one left must be in the tunnel
    static BarrowsBrother onlyBrotherAlive(Predicate<BarrowsBrother> isKilled) {
        List<BarrowsBrother> alive = Arrays.stream(BarrowsBrother.values())
                .filter(isKilled.negate())
                .collect(Collectors.toList());
        return alive.size() == 1 ? alive.get(0) : null;
    }

    // Crypts are walled off, so only the player's own coffin is reachable
    private Map<GameObject, BarrowsBrother> findReachableSarcophagi(int[][] flags, Point start, int plane) {
        TunnelPathfinder pathfinder = new TunnelPathfinder(flags, Collections.emptySet());
        Map<GameObject, BarrowsBrother> reachable = new HashMap<>();
        sarcophagi.forEach((object, brother) -> {
            // Collision maps only cover the current floor
            if (object.getPlane() == plane && !pathfinder.findPath(start,
                    tile -> isNextTo(tile, object.getSceneMinLocation(), object.getSceneMaxLocation())).isEmpty()) {
                reachable.put(object, brother);
            }
        });
        return reachable;
    }

    private List<Point> findPathToChest(int[][] flags, Point start) {
        if (chest == null || doors.isEmpty()) {
            return Collections.emptyList();
        }

        doors.stream()
                .filter(this::isUnlocked)
                .map(door -> toScenePoint(door.getLocalLocation()))
                .forEach(unlockedDoorTiles::add);
        Point chestMin = chest.getSceneMinLocation();
        Point chestMax = chest.getSceneMaxLocation();

        return new TunnelPathfinder(flags, unlockedDoorTiles)
                .findPath(start, tile -> isNextTo(tile, chestMin, chestMax));
    }

    private boolean isUnlocked(TileObject door) {
        ObjectComposition definition = client.getObjectDefinition(door.getId());
        if (definition.getImpostorIds() != null) {
            definition = definition.getImpostor();
        }
        return definition != null && (definition.getId() == ObjectID.BARROWS_DOOR_UNLOCKED_L
                || definition.getId() == ObjectID.BARROWS_DOOR_UNLOCKED_R);
    }

    private void reset() {
        sarcophagi.clear();
        reachableSarcophagi = Collections.emptyMap();
        doors.clear();
        unlockedDoorTiles.clear();
        chest = null;
        path = Collections.emptyList();
    }

    // Every tunnel door and state falls in this ID range
    static boolean isDoor(int objectId) {
        return objectId >= ObjectID.BARROWS_DOOR_INACTIVE_R && objectId <= ObjectID.BARROWS_DOOR_P_L;
    }

    static boolean isNextTo(Point tile, Point min, Point max) {
        return tile.getX() >= min.getX() - 1 && tile.getX() <= max.getX() + 1
                && tile.getY() >= min.getY() - 1 && tile.getY() <= max.getY() + 1;
    }

    private static Point toScenePoint(LocalPoint localPoint) {
        return new Point(localPoint.getSceneX(), localPoint.getSceneY());
    }

    @Provides
    EzBarrowsConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(EzBarrowsConfig.class);
    }
}
