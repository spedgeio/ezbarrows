package com.ezbarrows;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class EzBarrowsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(EzBarrowsPlugin.class);
		RuneLite.main(args);
	}
}
