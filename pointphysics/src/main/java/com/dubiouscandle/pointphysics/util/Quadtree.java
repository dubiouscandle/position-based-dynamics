package com.dubiouscandle.pointphysics.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;

public class Quadtree {
	private final int maxDepth;
	private final int maxBodies;
	private final BodyList bodyList;

	private ArrayList<Cell> cells = new ArrayList<>();

	public Quadtree(BodyList bodyList, int maxDepth, int maxBodies) {
		super();
		this.bodyList = bodyList;
		this.maxDepth = maxDepth;
		this.maxBodies = maxBodies;
	}

	public Quadtree(BodyList bodyList) {
		this(bodyList, 8, 12);
	}

	public void generate() {
		cells.clear();

		float x1 = Float.POSITIVE_INFINITY;
		float y1 = Float.POSITIVE_INFINITY;
		float x2 = Float.NEGATIVE_INFINITY;
		float y2 = Float.NEGATIVE_INFINITY;
		float minCellRadius = Float.POSITIVE_INFINITY;

		for (int i = 0; i < bodyList.size(); i++) {
			Body body = bodyList.get(i);

			x1 = Math.min(x1, body.x);
			y1 = Math.min(y1, body.y);
			x2 = Math.max(x2, body.x);
			y2 = Math.max(y2, body.y);

			minCellRadius = Math.min(minCellRadius, body.radius);
		}

		Cell root;
		float cx = (x1 + x2) * 0.5f;
		float cy = (y1 + y2) * 0.5f;
		float halfSize = 0.5f * Math.max(x2 - x1, y2 - y1) + 1e-6f;
		root = new Cell(bodyList.size(), 0, cx - halfSize, cy - halfSize, halfSize);

		for (int i = 0; i < bodyList.size(); i++) {
			root.indices[i] = i;
		}

		if (!shouldSplit(root, minCellRadius)) {
			cells.add(root);
			return;
		}

		ArrayDeque<Cell> stack = new ArrayDeque<>();
		stack.add(root);

		while (!stack.isEmpty()) {
			Cell cur = stack.poll();

			float newRadius = cur.radius * 0.5f;

			int cell1Count = 0;
			int cell2Count = 0;
			int cell3Count = 0;
			int cell4Count = 0;
			for (int i = 0; i < cur.indices.length; i++) {
				int index = cur.indices[i];
				Body body = bodyList.get(index);
				if (circleSquareIntersects(body.x, body.y, body.radius, cur.x + cur.radius, cur.y + cur.radius,
						newRadius)) {
					cell1Count++;
				}
				if (circleSquareIntersects(body.x, body.y, body.radius, cur.x, cur.y + cur.radius, newRadius)) {
					cell2Count++;
				}
				if (circleSquareIntersects(body.x, body.y, body.radius, cur.x, cur.y, newRadius)) {
					cell3Count++;
				}
				if (circleSquareIntersects(body.x, body.y, body.radius, cur.x + cur.radius, cur.y, newRadius)) {
					cell4Count++;
				}
			}

			if (cell1Count > 0) {
				float minCell1Radius = Float.POSITIVE_INFINITY;
				Cell cell1 = new Cell(cell1Count, cur.depth + 1, cur.x + cur.radius, cur.y + cur.radius, newRadius);
				for (int i = 0; i < cur.indices.length; i++) {
					int index = cur.indices[i];
					Body body = bodyList.get(index);
					if (circleSquareIntersects(body.x, body.y, body.radius, cur.x + cur.radius, cur.y + cur.radius,
							newRadius)) {
						minCell1Radius = Math.min(minCell1Radius, body.radius);
						cell1.indices[--cell1Count] = index;
					}
				}

				if (shouldSplit(cell1, minCell1Radius)) {
					stack.add(cell1);
				} else {
					cells.add(cell1);
				}
			}
			if (cell2Count > 0) {
				float minCell2Radius = Float.POSITIVE_INFINITY;
				Cell cell2 = new Cell(cell2Count, cur.depth + 1, cur.x, cur.y + cur.radius, newRadius);
				for (int i = 0; i < cur.indices.length; i++) {
					int index = cur.indices[i];
					Body body = bodyList.get(index);
					if (circleSquareIntersects(body.x, body.y, body.radius, cur.x, cur.y + cur.radius, newRadius)) {
						minCell2Radius = Math.min(minCell2Radius, body.radius);
						cell2.indices[--cell2Count] = index;
					}
				}

				if (shouldSplit(cell2, minCell2Radius)) {
					stack.add(cell2);
				} else {
					cells.add(cell2);
				}
			}
			if (cell3Count > 0) {
				float minCell3Radius = Float.POSITIVE_INFINITY;
				Cell cell3 = new Cell(cell3Count, cur.depth + 1, cur.x, cur.y, newRadius);
				for (int i = 0; i < cur.indices.length; i++) {
					int index = cur.indices[i];
					Body body = bodyList.get(index);
					if (circleSquareIntersects(body.x, body.y, body.radius, cur.x, cur.y, newRadius)) {
						minCell3Radius = Math.min(minCell3Radius, body.radius);
						cell3.indices[--cell3Count] = index;
					}
				}
				if (shouldSplit(cell3, minCell3Radius)) {
					stack.add(cell3);
				} else {
					cells.add(cell3);
				}
			}
			if (cell4Count > 0) {
				float minCell4Radius = Float.POSITIVE_INFINITY;
				Cell cell4 = new Cell(cell4Count, cur.depth + 1, cur.x + cur.radius, cur.y, newRadius);
				for (int i = 0; i < cur.indices.length; i++) {
					int index = cur.indices[i];
					Body body = bodyList.get(index);
					if (circleSquareIntersects(body.x, body.y, body.radius, cur.x + cur.radius, cur.y, newRadius)) {
						minCell4Radius = Math.min(minCell4Radius, body.radius);
						cell4.indices[--cell4Count] = index;
					}
				}
				if (shouldSplit(cell4, minCell4Radius)) {
					stack.add(cell4);
				} else {
					cells.add(cell4);
				}
			}
		}
	}

	private boolean circleSquareIntersects(float cx, float cy, float r, float sx, float sy, float squareRadius) {
		float closestX = Math.max(sx, Math.min(cx, sx + squareRadius * 2));
		float closestY = Math.max(sy, Math.min(cy, sy + squareRadius * 2));
		float dx = cx - closestX;
		float dy = cy - closestY;
		return dx * dx + dy * dy <= r * r;
	}

	protected boolean shouldSplit(Cell cell, float minCellRadius) {
		return cell.depth < maxDepth && cell.size() > maxBodies && cell.radius > minCellRadius;
	}

	public void forEachCell(Consumer<Cell> consumer) {
		for (int i = 0; i < cells.size(); i++) {
			consumer.accept(cells.get(i));
		}
	}

	public class Cell {
		private int[] indices;
		private int depth;
		public float x, y, radius;

		public Cell(int capacity, int depth, float x, float y, float radius) {
			this.depth = depth;
			this.x = x;
			this.y = y;
			this.radius = radius;
			indices = new int[capacity];
		}

		public Body getBody(int index) {
			return bodyList.get(indices[index]);
		}

		public int size() {
			return indices.length;
		}

		@Override
		public String toString() {
			return Arrays.toString(indices);
		}
	}
}
