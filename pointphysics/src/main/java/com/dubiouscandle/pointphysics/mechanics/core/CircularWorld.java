package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Constraint;

public class CircularWorld implements Constraint {
	private BodyList bodyList;
	public float cx, cy, radius;

	public CircularWorld(BodyList bodyList, float cx, float cy, float radius) {
		this.bodyList = bodyList;
		this.cx = cx;
		this.cy = cy;
		this.radius = radius;
	}

	@Override
	public void project() {
		for (int i = 0; i < bodyList.size(); i++) {
			Body body = bodyList.get(i);
			handleFor(body);
		}
	}

	protected void handleFor(Body body) {
		float dx = body.x - cx;
		float dy = body.y - cy;

		float effectiveRadius = radius - body.radius;
		float distSquared = dx * dx + dy * dy;
		if (distSquared <= effectiveRadius * effectiveRadius) {
			return;
		}

		float dist = (float) Math.sqrt(distSquared);
		float correction = (dist - effectiveRadius);
		body.x -= dx / dist * correction;
		body.y -= dy / dist * correction;
	}

}
