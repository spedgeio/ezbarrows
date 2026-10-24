package com.ezbarrows;

import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EzBarrowsPotentialOverlay extends OverlayPanel
{
	private final Client client;
	private final EzBarrowsConfig config;
	private int cachedRoom = Integer.MIN_VALUE;
	private Map<RewardPotential.TunnelMonster, Integer> cachedKills = Collections.emptyMap();

	@Inject
	EzBarrowsPotentialOverlay(Client client, EzBarrowsPlugin plugin, EzBarrowsConfig config)
	{
		super(plugin);
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Player player = client.getLocalPlayer();
		if (!config.showPotential() || player == null
			|| !EzBarrowsPlugin.isAtBarrows(player))
		{
			return null;
		}

		List<BarrowsBrother> alive = Arrays.stream(BarrowsBrother.values())
			.filter(brother -> client.getVarbitValue(brother.getKilledVarbit()) == 0)
			.collect(Collectors.toList());
		int points = RewardPotential.currentPoints(client.getVarbitValue(VarbitID.BARROWS_KILLED_MONSTER),
			BarrowsBrother.values().length - alive.size());
		int cap = config.pointsCap();

		panelComponent.getChildren().add(TitleComponent.builder().text("Reward Potential").build());
		addLine("Current", formatPoints(points), Color.WHITE);
		addLine("Cap", formatPoints(cap), Color.WHITE);

		if (points >= cap)
		{
			addLine(points == cap ? "Cap reached" : "Over cap", "", points == cap ? Color.GREEN : Color.RED);
			return super.render(graphics);
		}

		// Remaining brothers are always counted; only tunnel monsters are fitted under the cap
		int brothersLeft = alive.stream().mapToInt(BarrowsBrother::getRewardPoints).sum();
		int room = cap - points - brothersLeft;
		// Only search again when the room left changes
		if (room != cachedRoom)
		{
			cachedRoom = room;
			cachedKills = RewardPotential.killsToCap(room);
		}
		for (Map.Entry<RewardPotential.TunnelMonster, Integer> kill : cachedKills.entrySet())
		{
			addLine(kill.getKey().getDisplayName(), "x" + kill.getValue(), Color.YELLOW);
		}
		if (cachedKills.isEmpty())
		{
			addLine("Monsters", "stop here", Color.GREEN);
		}
		int projected = points + brothersLeft + RewardPotential.total(cachedKills);
		addLine("Projected", formatPoints(projected), projected > cap ? Color.RED : Color.WHITE);
		return super.render(graphics);
	}

	private void addLine(String left, String right, Color colour)
	{
		panelComponent.getChildren().add(LineComponent.builder()
			.left(left)
			.right(right)
			.leftColor(colour)
			.rightColor(colour)
			.build());
	}

	private static String formatPoints(int points)
	{
		return String.format("%.2f%% (%d)", RewardPotential.percent(points), points);
	}
}
