package com.ezbarrows;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

class RewardPotential
{
	static final int MAX_POINTS = 1012;
	private static final int MAX_KILLS_EACH = 6;

	// Tunnel monsters are worth their combat level
	@Getter
	@RequiredArgsConstructor
	enum TunnelMonster
	{
		SKELETON("Skeleton", 77),
		BLOODWORM("Bloodworm", 52),
		CRYPT_SPIDER("Crypt spider", 56),
		GIANT_CRYPT_SPIDER("Giant crypt spider", 79),
		CRYPT_RAT("Crypt rat", 43),
		GIANT_CRYPT_RAT("Giant crypt rat", 76);

		private final String displayName;
		private final int points;

		// Matches on combat level too, as the tunnel monsters are worth exactly their level
		static TunnelMonster fromNpc(String name, int combatLevel)
		{
			for (TunnelMonster monster : values())
			{
				if (monster.displayName.equalsIgnoreCase(name) && monster.points == combatLevel)
				{
					return monster;
				}
			}
			return null;
		}
	}

	// Combat levels of everything killed, plus 2 per brother
	static int currentPoints(int killedMonsterCombat, int brothersKilled)
	{
		return killedMonsterCombat + brothersKilled * 2;
	}

	// Truncated, not rounded, to 2 decimal places
	static double percent(int points)
	{
		return Math.floor(points * 10000.0 / MAX_POINTS) / 100;
	}

	// Kills that get as close to the cap as possible without going over; empty if none fit
	static Map<TunnelMonster, Integer> killsToCap(int points)
	{
		Map<TunnelMonster, Integer> best = new EnumMap<>(TunnelMonster.class);
		if (points <= 0)
		{
			return best;
		}
		search(TunnelMonster.values(), 0, new int[TunnelMonster.values().length], points, best, null);
		return best;
	}

	private static int[] search(TunnelMonster[] monsters, int index, int[] counts, int needed,
		Map<TunnelMonster, Integer> best, int[] bestScore)
	{
		if (index == monsters.length)
		{
			int total = 0;
			int kills = 0;
			for (int i = 0; i < monsters.length; i++)
			{
				total += counts[i] * monsters[i].getPoints();
				kills += counts[i];
			}
			// Going over the cap is worse than any amount under it
			if (total > needed)
			{
				return bestScore;
			}
			int[] score = {needed - total, kills};
			if (bestScore == null || Arrays.compare(score, bestScore) < 0)
			{
				best.clear();
				for (int i = 0; i < monsters.length; i++)
				{
					if (counts[i] > 0)
					{
						best.put(monsters[i], counts[i]);
					}
				}
				return score;
			}
			return bestScore;
		}
		for (int count = 0; count <= MAX_KILLS_EACH; count++)
		{
			counts[index] = count;
			bestScore = search(monsters, index + 1, counts, needed, best, bestScore);
		}
		counts[index] = 0;
		return bestScore;
	}

	static int total(Map<TunnelMonster, Integer> kills)
	{
		return kills.entrySet().stream().mapToInt(kill -> kill.getKey().getPoints() * kill.getValue()).sum();
	}
}
