package com.ezbarrows;

import net.runelite.api.CollisionDataFlag;
import net.runelite.api.Point;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TunnelPathfinderTest
{
	private static final int SIZE = 10;
	private static final int WALL_X = 4;

	@Test
	public void takesDiagonalsAcrossOpenGround()
	{
		List<Point> path = new TunnelPathfinder(new int[SIZE][SIZE], Collections.emptySet())
			.findPath(new Point(0, 0), tile -> tile.equals(new Point(3, 3)));

		assertEquals(4, path.size());
		assertEquals(new Point(0, 0), path.get(0));
		assertEquals(new Point(3, 3), path.get(3));
	}

	@Test
	public void wallBlocksTheRoute()
	{
		List<Point> path = new TunnelPathfinder(walledGrid(), Collections.emptySet())
			.findPath(new Point(1, 2), tile -> tile.getX() == 8);

		assertTrue(path.isEmpty());
	}

	@Test
	public void routeGoesThroughAnOpenDoor()
	{
		Point door = new Point(WALL_X, 7);
		List<Point> path = new TunnelPathfinder(walledGrid(), Set.of(door))
			.findPath(new Point(1, 2), tile -> tile.getX() == 8);

		assertTrue(path.contains(door));
		assertTrue(path.contains(new Point(WALL_X + 1, 7)));
	}

	@Test
	public void doesNotCutDiagonallyPastBlockedCorners()
	{
		int[][] flags = new int[SIZE][SIZE];
		flags[1][0] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;
		flags[0][1] = CollisionDataFlag.BLOCK_MOVEMENT_FULL;

		List<Point> path = new TunnelPathfinder(flags, Collections.emptySet())
			.findPath(new Point(0, 0), tile -> tile.equals(new Point(1, 1)));

		assertTrue(path.isEmpty());
	}

	@Test
	public void chestGoalIsAnyTileTouchingTheChest()
	{
		assertTrue(EzBarrowsPlugin.isNextTo(new Point(4, 5), new Point(5, 5), new Point(6, 6)));
		assertTrue(EzBarrowsPlugin.isNextTo(new Point(7, 7), new Point(5, 5), new Point(6, 6)));
		assertFalse(EzBarrowsPlugin.isNextTo(new Point(3, 5), new Point(5, 5), new Point(6, 6)));
	}

	// Wall along the east edge of column WALL_X, splitting the grid in two
	private static int[][] walledGrid()
	{
		int[][] flags = new int[SIZE][SIZE];
		for (int y = 0; y < SIZE; y++)
		{
			flags[WALL_X][y] |= CollisionDataFlag.BLOCK_MOVEMENT_EAST;
			flags[WALL_X + 1][y] |= CollisionDataFlag.BLOCK_MOVEMENT_WEST;
		}
		return flags;
	}
}
