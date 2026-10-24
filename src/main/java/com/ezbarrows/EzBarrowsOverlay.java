package com.ezbarrows;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

public class EzBarrowsOverlay extends Overlay
{
	// Height above the tile to draw the portrait, in local units
	private static final int PORTRAIT_Z_OFFSET = 150;

	private final Client client;
	private final EzBarrowsPlugin plugin;
	private final EzBarrowsConfig config;
	private final Map<BarrowsBrother, BufferedImage> portraits = new EnumMap<>(BarrowsBrother.class);

	@Inject
	EzBarrowsOverlay(Client client, EzBarrowsPlugin plugin, EzBarrowsConfig config)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		for (Map.Entry<GameObject, BarrowsBrother> entry : plugin.getReachableSarcophagi().entrySet())
		{
			BarrowsBrother brother = entry.getValue();
			if (config.hideKilled() && client.getVarbitValue(brother.getKilledVarbit()) != 0)
			{
				continue;
			}

			BufferedImage portrait = portraits.computeIfAbsent(brother, EzBarrowsOverlay::loadPortrait);
			Point location = Perspective.getCanvasImageLocation(client, entry.getKey().getLocalLocation(), portrait, PORTRAIT_Z_OFFSET);
			if (location != null)
			{
				graphics.drawImage(portrait, location.getX(), location.getY(), null);
			}
		}
		return null;
	}

	static BufferedImage loadPortrait(BarrowsBrother brother)
	{
		return ImageUtil.loadImageResource(EzBarrowsOverlay.class, brother.getPortraitPath());
	}
}
