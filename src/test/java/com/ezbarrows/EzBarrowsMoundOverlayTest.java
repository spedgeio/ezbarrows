package com.ezbarrows;

import org.junit.Test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EzBarrowsMoundOverlayTest
{
	@Test
	public void aliveBrotherIsGreen()
	{
		assertEquals(Color.GREEN, EzBarrowsMoundOverlay.nameColour(false, false));
	}

	@Test
	public void killedBrotherIsRedEvenIfTunnel()
	{
		assertEquals(Color.RED, EzBarrowsMoundOverlay.nameColour(true, false));
		assertEquals(Color.RED, EzBarrowsMoundOverlay.nameColour(true, true));
	}

	@Test
	public void tunnelBrotherIsBlue()
	{
		assertEquals(new Color(0x3399ff), EzBarrowsMoundOverlay.nameColour(false, true));
	}

	@Test
	public void lastBrotherAliveIsTheTunnel()
	{
		assertEquals(BarrowsBrother.KARIL, EzBarrowsPlugin.onlyBrotherAlive(brother -> brother != BarrowsBrother.KARIL));
	}

	@Test
	public void tunnelIsUnknownWithSeveralAlive()
	{
		assertNull(EzBarrowsPlugin.onlyBrotherAlive(brother -> brother == BarrowsBrother.AHRIM));
	}
}
