package com.ezbarrows;

import net.runelite.api.CollisionDataFlag;
import net.runelite.api.Point;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

class TunnelPathfinder
{
	// Cardinals first so straight moves are preferred over diagonals of equal length
	private static final int[][] DIRECTIONS = {
		{0, 1}, {1, 0}, {0, -1}, {-1, 0},
		{1, 1}, {1, -1}, {-1, -1}, {-1, 1}
	};

	private final int[][] flags;
	private final Set<Point> openDoors;

	TunnelPathfinder(int[][] flags, Set<Point> openDoors)
	{
		this.flags = flags;
		this.openDoors = openDoors;
	}

	// Breadth-first search over scene tiles; empty if no goal tile is reachable
	List<Point> findPath(Point start, Predicate<Point> isGoal)
	{
		Map<Point, Point> cameFrom = new HashMap<>();
		Deque<Point> queue = new ArrayDeque<>();
		cameFrom.put(start, start);
		queue.add(start);

		while (!queue.isEmpty())
		{
			Point current = queue.poll();
			if (isGoal.test(current))
			{
				return buildPath(cameFrom, current);
			}
			for (int[] direction : DIRECTIONS)
			{
				Point next = new Point(current.getX() + direction[0], current.getY() + direction[1]);
				if (!cameFrom.containsKey(next) && canMove(current, direction[0], direction[1]))
				{
					cameFrom.put(next, current);
					queue.add(next);
				}
			}
		}
		return Collections.emptyList();
	}

	private boolean canMove(Point from, int dx, int dy)
	{
		if (dx != 0 && dy != 0)
		{
			// Diagonals need both of the cardinal routes round the corner to be clear
			Point horizontal = new Point(from.getX() + dx, from.getY());
			Point vertical = new Point(from.getX(), from.getY() + dy);
			return canMove(from, dx, 0) && canMove(from, 0, dy)
				&& canMove(horizontal, 0, dy) && canMove(vertical, dx, 0);
		}

		Point to = new Point(from.getX() + dx, from.getY() + dy);
		if (!inBounds(to))
		{
			return false;
		}

		boolean throughDoor = openDoors.contains(from) || openDoors.contains(to);
		if (!throughDoor && ((flag(from) & wallFlag(dx, dy)) != 0 || (flag(to) & wallFlag(-dx, -dy)) != 0))
		{
			return false;
		}
		return openDoors.contains(to) || (flag(to) & CollisionDataFlag.BLOCK_MOVEMENT_FULL) == 0;
	}

	private static int wallFlag(int dx, int dy)
	{
		if (dx == 1)
		{
			return CollisionDataFlag.BLOCK_MOVEMENT_EAST;
		}
		if (dx == -1)
		{
			return CollisionDataFlag.BLOCK_MOVEMENT_WEST;
		}
		return dy == 1 ? CollisionDataFlag.BLOCK_MOVEMENT_NORTH : CollisionDataFlag.BLOCK_MOVEMENT_SOUTH;
	}

	private boolean inBounds(Point point)
	{
		return point.getX() >= 0 && point.getY() >= 0 && point.getX() < flags.length && point.getY() < flags[0].length;
	}

	private int flag(Point point)
	{
		return flags[point.getX()][point.getY()];
	}

	private static List<Point> buildPath(Map<Point, Point> cameFrom, Point end)
	{
		LinkedList<Point> path = new LinkedList<>();
		Point point = end;
		path.addFirst(point);
		// The start tile maps to itself
		while (!point.equals(cameFrom.get(point)))
		{
			point = cameFrom.get(point);
			path.addFirst(point);
		}
		return path;
	}
}
