package com.ezbarrows;

import net.runelite.api.gameval.ObjectID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BarrowsBrotherTest
{
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
