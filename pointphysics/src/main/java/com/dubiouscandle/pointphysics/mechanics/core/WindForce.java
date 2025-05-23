package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Force;

public class WindForce implements Force {
	private final float wx, wy;
	private final float dragCoefficient;
	private final BodyList bodyList;

	public WindForce(BodyList bodyList, float wx, float wy, float dragCoefficient) {
		this.bodyList = bodyList;
		this.wx = wx;
		this.wy = wy;
		this.dragCoefficient = dragCoefficient;
	}

	public void applyTo(Body body) {
		float rvx = body.vx() - wx;
		float rvy = body.vy() - wy;
		float mag = (float) Math.sqrt(rvx * rvx + rvy * rvy);

		if (mag < 1e-6f) {
			return;
		}

		float fx = -rvx * dragCoefficient;
		float fy = -rvy * dragCoefficient;

		body.applyForce(fx, fy);
	}

	@Override
	public void apply() {
		for (int i = 0; i < bodyList.size(); i++) {
			applyTo(bodyList.get(i));
		}
	}
}
