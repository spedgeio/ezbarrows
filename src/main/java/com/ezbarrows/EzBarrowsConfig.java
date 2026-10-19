package com.ezbarrows;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("ezbarrows")
public interface EzBarrowsConfig extends Config {
    @ConfigItem(
            keyName = "correctColour",
            name = "Correct Prayer Colour",
            description = "Border colour when you're using the right protection prayer.",
            position = 1
    )
    default Color correctColour() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "incorrectColour",
            name = "Wrong Prayer Colour",
            description = "Border colour when you're not using the right protection prayer.",
            position = 2
    )
    default Color incorrectColour() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "hideKilled",
            name = "Hide Killed Brothers",
            description = "Hide the icon once that brother has been killed this run.",
            position = 3
    )
    default boolean hideKilled() {
        return true;
    }

    @ConfigItem(
            keyName = "showPath",
            name = "Show Tunnel Path",
            description = "Draw the shortest route to the chest through the doors you can open.",
            position = 4
    )
    default boolean showPath() {
        return true;
    }

    @ConfigItem(
            keyName = "pathColour",
            name = "Tunnel Path Colour",
            description = "Colour of the route line in the tunnels.",
            position = 5
    )
    default Color pathColour() {
        return Color.CYAN;
    }
}
