package com.ezbarrows;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class EzBarrowsOverlay extends Overlay
{
	// Height above the tile to draw the icon, in local units
	private static final int ICON_Z_OFFSET = 150;
	private static final double ICON_SCALE = 0.8;

	private final Client client;
	private final EzBarrowsPlugin plugin;
	private final EzBarrowsConfig config;
	private final SpriteManager spriteManager;
	private final Map<Integer, BufferedImage> iconCache = new HashMap<>();

	@Inject
	EzBarrowsOverlay(Client client, EzBarrowsPlugin plugin, EzBarrowsConfig config, SpriteManager spriteManager)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.spriteManager = spriteManager;
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

			BufferedImage sprite = spriteManager.getSprite(brother.getPrayerSpriteId(), 0);
			if (sprite == null)
			{
				continue;
			}

			BufferedImage icon = iconCache.computeIfAbsent(brother.getPrayerSpriteId(), key -> scaledIcon(sprite));
			Point location = Perspective.getCanvasImageLocation(client, entry.getKey().getLocalLocation(), icon, ICON_Z_OFFSET);
			if (location != null)
			{
				graphics.drawImage(icon, location.getX(), location.getY(), null);
			}
		}
		return null;
	}

	static BufferedImage scaledIcon(BufferedImage sprite)
	{
		return ImageUtil.resizeImage(sprite,
			(int) Math.round(sprite.getWidth() * ICON_SCALE),
			(int) Math.round(sprite.getHeight() * ICON_SCALE));
	}
}
