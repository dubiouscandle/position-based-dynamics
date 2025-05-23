package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.Force;

public class Spring implements Force {
	private float length, coefficient;
	private Body bodyA, bodyB;

	public Spring(Body bodyA, Body bodyB, float length, float coefficient) {
		this.length = length;
		this.coefficient = coefficient;
		
		this.bodyA = bodyA;
		this.bodyB = bodyB;
	}

	@Override
	public void apply() {
		float dx = bodyA.x - bodyB.x;
		float dy = bodyA.y - bodyB.y;

		float mag = (float) Math.sqrt(dx * dx + dy * dy);

		if (mag == 0) {
			return;
		}

		float displacement = mag - length;

		float f = -displacement * coefficient;
		dx /= mag;
		dy /= mag;

		bodyA.applyForce(f * dx, f * dy);
		bodyB.applyForce(-f * dx, -f * dy);
	}
}