package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Constraint;
import com.dubiouscandle.pointphysics.util.BVH;
import com.dubiouscandle.pointphysics.util.BVH.QueryCallback;

public class StaticEdges implements Constraint {
	private BVH<StaticEdge> bvh = new BVH<>();
	private BodyList bodyList;
	private Callback callback = new Callback();

	public StaticEdges(BodyList bodyList) {
		this.bodyList = bodyList;
	}

	public void addEdge(StaticEdge edge) {
		float x1 = Math.min(edge.x1, edge.x2) - edge.thickness;
		float y1 = Math.min(edge.y1, edge.y2) - edge.thickness;
		float x2 = Math.max(edge.x1, edge.x2) + edge.thickness;
		float y2 = Math.max(edge.y1, edge.y2) + edge.thickness;

		bvh.add(x1, y1, x2, y2, edge);
		bvh.rebuild();
	}

	private class Callback implements QueryCallback<StaticEdge> {
		Body currentBody;

		@Override
		public boolean reportEntry(float x1, float y1, float x2, float y2, StaticEdge e) {
			solveBody(currentBody, e, e.thickness);
			return true;
		}

	}

	public void solveBody(Body body, StaticEdge e, float edgeThickness) {
		float x0 = e.x1;
		float y0 = e.y1;
		float x1 = e.x2;
		float y1 = e.y2;

		float dx = x1 - x0;
		float dy = y1 - y0;

		float px = body.x - x0;
		float py = body.y - y0;

		float edgeLenSq = dx * dx + dy * dy;
		float t = (dx * px + dy * py) / edgeLenSq;
		t = Math.max(0, Math.min(1, t));

		float closestX = x0 + t * dx;
		float closestY = y0 + t * dy;

		float dxBody = body.x - closestX;
		float dyBody = body.y - closestY;
		float distSq = dxBody * dxBody + dyBody * dyBody;

		float minDist = body.radius + edgeThickness;
		if (distSq < minDist * minDist && distSq > 0f) {
			float dist = (float) Math.sqrt(distSq);
			float correction = minDist - dist;

			float nx = dxBody / dist;
			float ny = dyBody / dist;

			body.x += nx * correction;
			body.y += ny * correction;
		}
	}

	@Override
	public void project() {
		for (int i = 0; i < bodyList.size(); i++) {
			Body body = bodyList.get(i);
			float x = body.x;
			float y = body.y;
			float r = body.radius;

			float x1 = x - r;
			float y1 = y - r;
			float x2 = x + r;
			float y2 = y + r;

			callback.currentBody = body;
			bvh.query(x1, y1, x2, y2, callback);
		}
	}

}
