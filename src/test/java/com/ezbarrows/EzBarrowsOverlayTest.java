package com.ezbarrows;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

public class EzBarrowsOverlayTest
{
	@Test
	public void iconIsScaledToEightyPercent()
	{
		BufferedImage icon = EzBarrowsOverlay.scaledIcon(new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB));

		assertEquals(20, icon.getWidth());
		assertEquals(20, icon.getHeight());
	}
}
