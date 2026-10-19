package com.ezbarrows;

import com.ezbarrows.RewardPotential.TunnelMonster;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RewardPotentialTest
{
	private static final int ALL_BROTHERS = Arrays.stream(BarrowsBrother.values()).mapToInt(BarrowsBrother::getRewardPoints).sum();

	@Test
	public void allSixBrothersAreWorth668()
	{
		assertEquals(668, ALL_BROTHERS);
	}

	@Test
	public void eightHundredAndEightyPointsIs86Point95Percent()
	{
		assertEquals(86.95, RewardPotential.percent(880), 0.0001);
	}

	@Test
	public void currentPointsAddTwoPerBrother()
	{
		// Dharok (115) and a bloodworm (52) killed
		assertEquals(169, RewardPotential.currentPoints(167, 1));
	}

	@Test
	public void defaultCapIsTwoSkeletonsAndABloodworm()
	{
		Map<TunnelMonster, Integer> kills = RewardPotential.killsToCap(874 - ALL_BROTHERS);

		assertEquals(2, (int) kills.get(TunnelMonster.SKELETON));
		assertEquals(1, (int) kills.get(TunnelMonster.BLOODWORM));
		assertEquals(2, kills.size());
	}

	@Test
	public void eightHundredAndEightyCapIsReachedExactlyWithThreeKills()
	{
		Map<TunnelMonster, Integer> kills = RewardPotential.killsToCap(880 - ALL_BROTHERS);

		assertEquals(212, RewardPotential.total(kills));
		assertEquals(3, kills.values().stream().mapToInt(Integer::intValue).sum());
	}

	@Test
	public void afterThreeAccidentalBloodwormsACryptRatFitsUnderTheCap()
	{
		// 874 cap, all brothers to come, 3 bloodworms (156) already killed: 50 left
		Map<TunnelMonster, Integer> kills = RewardPotential.killsToCap(874 - ALL_BROTHERS - 156);

		assertEquals(43, RewardPotential.total(kills));
	}

	@Test
	public void neverGoesOverTheCap()
	{
		// A bloodworm (52) would be closer to 51, but it's over
		assertEquals(43, RewardPotential.total(RewardPotential.killsToCap(51)));
		assertEquals(99, RewardPotential.total(RewardPotential.killsToCap(100)));
		for (int room = 0; room <= 400; room++)
		{
			assertTrue(RewardPotential.total(RewardPotential.killsToCap(room)) <= room);
		}
	}

	@Test
	public void stopsWhenNoMonsterFits()
	{
		assertTrue(RewardPotential.killsToCap(42).isEmpty());
	}

	@Test
	public void ratsAreSuggestedWhenTheyFitBest()
	{
		Map<TunnelMonster, Integer> kills = RewardPotential.killsToCap(119);

		assertEquals(1, (int) kills.get(TunnelMonster.CRYPT_RAT));
		assertEquals(1, (int) kills.get(TunnelMonster.GIANT_CRYPT_RAT));
		assertEquals(2, kills.size());
	}

	@Test
	public void nothingSuggestedAtOrOverTheCap()
	{
		assertTrue(RewardPotential.killsToCap(0).isEmpty());
		assertTrue(RewardPotential.killsToCap(-10).isEmpty());
	}
}
