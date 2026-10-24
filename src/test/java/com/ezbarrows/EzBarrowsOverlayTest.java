package com.ezbarrows;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

public class EzBarrowsOverlayTest
{
	@Test
	public void everyBrotherHasAPortrait()
	{
		for (BarrowsBrother brother : BarrowsBrother.values())
		{
			BufferedImage portrait = EzBarrowsOverlay.loadPortrait(brother);

			assertEquals(brother.getDisplayName(), 52, portrait.getHeight());
		}
	}
}
