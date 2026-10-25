package com.ezbarrows;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

import java.awt.Color;

@ConfigGroup("ezbarrows")
public interface EzBarrowsConfig extends Config
{
	@ConfigItem(
		keyName = "hideKilled",
		name = "Hide Killed Brothers",
		description = "Hide the portrait once that brother has been killed this run.",
		position = 1
	)
	default boolean hideKilled()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showBrotherNames",
		name = "Show Brother Names",
		description = "Name each mound: green if alive, red if killed, blue once found to be the tunnel.",
		position = 2
	)
	default boolean showBrotherNames()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showPath",
		name = "Show Tunnel Path",
		description = "Draw the shortest route to the chest through the doors you can open.",
		position = 3
	)
	default boolean showPath()
	{
		return true;
	}

	@ConfigItem(
		keyName = "pathColour",
		name = "Tunnel Path Colour",
		description = "Colour of the route line in the tunnels.",
		position = 4
	)
	default Color pathColour()
	{
		return Color.CYAN;
	}

	@ConfigItem(
		keyName = "showPotential",
		name = "Show Reward Potential",
		description = "Show your reward potential and what's left to kill to reach your cap.",
		position = 5
	)
	default boolean showPotential()
	{
		return true;
	}

	@Range(max = RewardPotential.MAX_POINTS)
	@ConfigItem(
		keyName = "pointsCap",
		name = "Points Cap",
		description = "Reward points to get as close to as possible without going over, out of 1012. 874 is 6 brothers, 2 skeletons and 1 bloodworm; 880 is 86.95%.",
		position = 6
	)
	default int pointsCap()
	{
		return 874;
	}

	@ConfigItem(
		keyName = "highlightMonsters",
		name = "Highlight Target Monsters",
		description = "Outline tunnel monsters you still need to kill to get closest to your cap.",
		position = 7
	)
	default boolean highlightMonsters()
	{
		return true;
	}

	@ConfigItem(
		keyName = "monsterColour",
		name = "Target Monster Colour",
		description = "Colour of the outline on target monsters.",
		position = 8
	)
	default Color monsterColour()
	{
		return new Color(0x3399ff);
	}

	@ConfigItem(
		keyName = "highlightChest",
		name = "Highlight Chest When Ready",
		description = "Outline the chest once every brother is dead and no more kills would get you closer to your cap.",
		position = 9
	)
	default boolean highlightChest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "chestColour",
		name = "Ready Chest Colour",
		description = "Colour of the outline on the chest when it's ready to loot.",
		position = 10
	)
	default Color chestColour()
	{
		return Color.GREEN;
	}
}
