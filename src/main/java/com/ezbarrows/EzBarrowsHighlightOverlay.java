package com.ezbarrows;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Map;

public class EzBarrowsHighlightOverlay extends Overlay
{
	private static final int OUTLINE_WIDTH = 2;
	private static final int OUTLINE_FEATHER = 2;

	private final Client client;
	private final EzBarrowsPlugin plugin;
	private final EzBarrowsConfig config;
	private final ModelOutlineRenderer outlineRenderer;

	@Inject
	EzBarrowsHighlightOverlay(Client client, EzBarrowsPlugin plugin, EzBarrowsConfig config, ModelOutlineRenderer outlineRenderer)
	{
		this.client = client;
		this.plugin = plugin;
		this.config = config;
		this.outlineRenderer = outlineRenderer;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Map<RewardPotential.TunnelMonster, Integer> kills = plugin.getSuggestedKills();
		if (config.highlightMonsters() && !kills.isEmpty())
		{
			for (NPC npc : client.getTopLevelWorldView().npcs())
			{
				RewardPotential.TunnelMonster monster = RewardPotential.TunnelMonster.fromNpc(npc.getName(), npc.getCombatLevel());
				if (monster != null && kills.containsKey(monster) && !npc.isDead())
				{
					outlineRenderer.drawOutline(npc, OUTLINE_WIDTH, config.monsterColour(), OUTLINE_FEATHER);
				}
			}
		}

		GameObject chest = plugin.getChest();
		if (config.highlightChest() && plugin.isReadyToLoot() && chest != null)
		{
			outlineRenderer.drawOutline(chest, OUTLINE_WIDTH, config.chestColour(), OUTLINE_FEATHER);
		}
		return null;
	}
}
