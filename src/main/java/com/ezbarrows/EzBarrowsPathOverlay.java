package com.ezbarrows;

import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;

public class EzBarrowsPathOverlay extends Overlay
{
	private static final BasicStroke PATH_STROKE = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	private final Client client;
	private final EzBarrowsPlugin plugin;
	private final EzBarrowsConfig config;

	@Inject
	EzBarrowsPathOverlay(Client client, EzBarrowsPlugin plugin, EzBarrowsConfig config)
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
		List<Point> path = plugin.getPath();
		if (path.size() < 2)
		{
			return null;
		}

		WorldView worldView = client.getTopLevelWorldView();
		graphics.setStroke(PATH_STROKE);
		graphics.setColor(config.pathColour());

		Point previous = null;
		for (Point tile : path)
		{
			LocalPoint localPoint = LocalPoint.fromScene(tile.getX(), tile.getY(), worldView);
			Point canvas = Perspective.localToCanvas(client, localPoint, worldView.getPlane());
			if (previous != null && canvas != null)
			{
				graphics.drawLine(previous.getX(), previous.getY(), canvas.getX(), canvas.getY());
			}
			previous = canvas;
		}
		return null;
	}
}
