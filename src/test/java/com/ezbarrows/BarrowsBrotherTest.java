package com.ezbarrows;

import net.runelite.api.Prayer;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.SpriteID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BarrowsBrotherTest
{
	@Test
	public void meleeBrothersUseProtectFromMelee()
	{
		for (BarrowsBrother brother : new BarrowsBrother[]{BarrowsBrother.DHAROK, BarrowsBrother.GUTHAN, BarrowsBrother.TORAG, BarrowsBrother.VERAC})
		{
			assertEquals(Prayer.PROTECT_FROM_MELEE, brother.getPrayer());
			assertEquals(SpriteID.Prayeron.PROTECT_FROM_MELEE, brother.getPrayerSpriteId());
		}
	}

	@Test
	public void ahrimUsesProtectFromMagic()
	{
		assertEquals(Prayer.PROTECT_FROM_MAGIC, BarrowsBrother.AHRIM.getPrayer());
		assertEquals(SpriteID.Prayeron.PROTECT_FROM_MAGIC, BarrowsBrother.AHRIM.getPrayerSpriteId());
	}

	@Test
	public void karilUsesProtectFromMissiles()
	{
		assertEquals(Prayer.PROTECT_FROM_MISSILES, BarrowsBrother.KARIL.getPrayer());
		assertEquals(SpriteID.Prayeron.PROTECT_FROM_MISSILES, BarrowsBrother.KARIL.getPrayerSpriteId());
	}

	@Test
	public void findsBrotherBySarcophagus()
	{
		assertEquals(BarrowsBrother.DHAROK, BarrowsBrother.fromSarcophagus(ObjectID.BARROW_DHAROK_SARCOPHAGUS));
		assertEquals(BarrowsBrother.KARIL, BarrowsBrother.fromSarcophagus(ObjectID.BARROW_KARIL_SARCOPHAGUS));
	}

	@Test
	public void unknownObjectIsNotASarcophagus()
	{
		assertNull(BarrowsBrother.fromSarcophagus(ObjectID.NTK_SARCOPHAGUS));
	}
}
