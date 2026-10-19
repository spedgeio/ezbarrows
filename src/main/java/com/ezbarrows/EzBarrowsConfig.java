package com.ezbarrows;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.Color;

@ConfigGroup("ezbarrows")
public interface EzBarrowsConfig extends Config {
    @ConfigItem(
            keyName = "hideKilled",
            name = "Hide Killed Brothers",
            description = "Hide the icon once that brother has been killed this run.",
            position = 1
    )
    default boolean hideKilled() {
        return true;
    }

    @ConfigItem(
            keyName = "showPath",
            name = "Show Tunnel Path",
            description = "Draw the shortest route to the chest through the doors you can open.",
            position = 2
    )
    default boolean showPath() {
        return true;
    }

    @ConfigItem(
            keyName = "pathColour",
            name = "Tunnel Path Colour",
            description = "Colour of the route line in the tunnels.",
            position = 3
    )
    default Color pathColour() {
        return Color.CYAN;
    }
}
