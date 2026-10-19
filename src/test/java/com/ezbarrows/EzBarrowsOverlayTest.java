package com.ezbarrows;

import org.junit.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

public class EzBarrowsOverlayTest
{
	@Test
	public void iconIsScaledToEightyPercentPlusOutline()
	{
		BufferedImage icon = EzBarrowsOverlay.outlinedIcon(circleSprite(), Color.GREEN);

		assertEquals(24, icon.getWidth());
		assertEquals(24, icon.getHeight());
	}

	@Test
	public void outlineFollowsTheShapeNotTheImageBounds()
	{
		BufferedImage icon = EzBarrowsOverlay.outlinedIcon(circleSprite(), Color.GREEN);

		// Shape edge gets outlined, image corners stay clear
		assertEquals(Color.GREEN.getRGB(), icon.getRGB(12, 0));
		assertEquals(Color.GREEN.getRGB(), icon.getRGB(0, 12));
		assertEquals(0, icon.getRGB(0, 0) >>> 24);
		assertEquals(Color.WHITE.getRGB(), icon.getRGB(12, 12));
	}

	// 25x25 transparent image with an opaque circle filling it, shaped like a prayer icon
	private static BufferedImage circleSprite()
	{
		BufferedImage sprite = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < 25; x++)
		{
			for (int y = 0; y < 25; y++)
			{
				if ((x - 12) * (x - 12) + (y - 12) * (y - 12) <= 144)
				{
					sprite.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		return sprite;
	}
}
