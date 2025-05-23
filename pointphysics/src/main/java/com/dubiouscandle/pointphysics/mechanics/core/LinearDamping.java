package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Force;

public class LinearDamping implements Force {
	private final float dragCoefficient;
	private final BodyList bodyList;

	public LinearDamping(BodyList bodyList, float dragCoefficient) {
		this.bodyList = bodyList;
		this.dragCoefficient = dragCoefficient;
	}

	@Override
	public void apply() {
		for (int i = 0; i < bodyList.size(); i++) {
			Body body = bodyList.get(i);

			float vx = body.vx();
			float vy = body.vy();
			float mag = (float) Math.sqrt(vx * vx + vy * vy);

			if (mag < 1e-6f) {
				return;
			}

			float fx = -vx * dragCoefficient / mag;
			float fy = -vy * dragCoefficient / mag;

			body.applyForce(fx, fy);
		}
	}
}
