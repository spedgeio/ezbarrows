package com.ezbarrows;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class EzBarrowsMoundOverlay extends Overlay {
    private static final int SURFACE_REGION_ID = 14131;
    // Height above the mound to draw the name, in local units
    private static final int NAME_Z_OFFSET = 50;
    private static final Color ALIVE_COLOUR = Color.GREEN;
    private static final Color KILLED_COLOUR = Color.RED;
    private static final Color TUNNEL_COLOUR = new Color(0x3399ff);

    private final Client client;
    private final EzBarrowsPlugin plugin;
    private final EzBarrowsConfig config;

    @Inject
    EzBarrowsMoundOverlay(Client client, EzBarrowsPlugin plugin, EzBarrowsConfig config) {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Player player = client.getLocalPlayer();
        if (!config.showBrotherNames() || player == null
                || player.getWorldLocation().getRegionID() != SURFACE_REGION_ID) {
            return null;
        }

        graphics.setFont(FontManager.getRunescapeBoldFont());
        for (BarrowsBrother brother : BarrowsBrother.values()) {
            LocalPoint mound = LocalPoint.fromWorld(client.getTopLevelWorldView(), brother.getMound());
            if (mound == null) {
                continue;
            }

            Point location = Perspective.getCanvasTextLocation(client, graphics, mound, brother.getDisplayName(), NAME_Z_OFFSET);
            if (location != null) {
                boolean killed = client.getVarbitValue(brother.getKilledVarbit()) != 0;
                OverlayUtil.renderTextLocation(graphics, location, brother.getDisplayName(),
                        nameColour(killed, brother == plugin.getTunnelBrother()));
            }
        }
        return null;
    }

    static Color nameColour(boolean killed, boolean tunnel) {
        if (killed) {
            return KILLED_COLOUR;
        }
        return tunnel ? TUNNEL_COLOUR : ALIVE_COLOUR;
    }
}
