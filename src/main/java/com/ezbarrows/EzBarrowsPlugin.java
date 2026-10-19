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
import net.runelite.api.Prayer;
import net.runelite.api.TileObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WallObjectDespawned;
import net.runelite.api.events.WallObjectSpawned;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@PluginDescriptor(
        name = "EzBarrows",
        description = "Barrows prayer icons on each sarcophagus and the shortest tunnel route to the chest",
        tags = {"barrows", "prayer", "pvm", "crypt", "tunnel", "path"}
)
public class EzBarrowsPlugin extends Plugin {
    private static final Set<Integer> CHEST_IDS = ImmutableSet.of(
            ObjectID.BARROWS_STONE_CHEST, ObjectID.BARROWS_STONE_CHEST_CLOSED, ObjectID.BARROWS_STONE_CHEST_OPEN);

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

    private final Map<GameObject, BarrowsBrother> sarcophagi = new HashMap<>();

    @Getter(AccessLevel.PACKAGE)
    private Map<GameObject, BarrowsBrother> reachableSarcophagi = Collections.emptyMap();

    @Getter(AccessLevel.PACKAGE)
    private List<Point> path = Collections.emptyList();

    @Getter(AccessLevel.PACKAGE)
    private Set<Prayer> activePrayers = Collections.emptySet();

    private final Set<TileObject> doors = new HashSet<>();
    // Door locks are fixed per run, so remember unlocked doors even while they're mid-open
    private final Set<Point> unlockedDoorTiles = new HashSet<>();
    private GameObject chest;

    @Override
    protected void startUp() {
        overlayManager.add(overlay);
        overlayManager.add(pathOverlay);
    }

    @Override
    protected void shutDown() {
        overlayManager.remove(overlay);
        overlayManager.remove(pathOverlay);
        reset();
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
    public void onGameTick(GameTick event) {
        activePrayers = findActivePrayers();

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
    }

    // Server values only change on the tick, so prayer flicking doesn't flash the outline
    private Set<Prayer> findActivePrayers() {
        Set<Prayer> active = EnumSet.noneOf(Prayer.class);
        for (BarrowsBrother brother : BarrowsBrother.values()) {
            if (client.getServerVarbitValue(brother.getPrayer().getVarbit()) == 1) {
                active.add(brother.getPrayer());
            }
        }
        return active;
    }

    // Each crypt is walled off, so only the coffin in the player's own crypt is reachable
    private Map<GameObject, BarrowsBrother> findReachableSarcophagi(int[][] flags, Point start, int plane) {
        TunnelPathfinder pathfinder = new TunnelPathfinder(flags, Collections.emptySet());
        Map<GameObject, BarrowsBrother> reachable = new HashMap<>();
        sarcophagi.forEach((object, brother) -> {
            // Collision maps are per floor, so coffins on other floors can't be checked
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
        activePrayers = Collections.emptySet();
        doors.clear();
        unlockedDoorTiles.clear();
        chest = null;
        path = Collections.emptyList();
    }

    // Tunnel door IDs are one contiguous range covering every door and state
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
