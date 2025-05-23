package com.dubiouscandle.pointphysics.mechanics.core;

import java.util.ArrayList;
import java.util.List;

public class StaticEdge {
	public final float x1, y1, x2, y2;
	public final float thickness;

	public StaticEdge(float x1, float y1, float x2, float y2, float thickness) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.thickness = thickness;
	}

	public static class PolygonBuilder {
		private int numVertices = 0;
		private float px, py;
		private ArrayList<StaticEdge> edges = new ArrayList<StaticEdge>();
		private float thickness;

		public PolygonBuilder(float thickness) {
			this.thickness = thickness;
		}

		public void addVertex(float x, float y) {
			if (numVertices > 0) {
				edges.add(new StaticEdge(x, y, px, py, thickness));
			}

			px = x;
			py = y;
			numVertices++;
		}

		public List<StaticEdge> edges() {
			return edges;
		}
	}
}
