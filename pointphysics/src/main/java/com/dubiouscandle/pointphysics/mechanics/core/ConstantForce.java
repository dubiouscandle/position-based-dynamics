package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Force;

public class ConstantForce implements Force {
	private final float ax, ay;
	private final BodyList bodyList;

	public ConstantForce(BodyList bodyList, float ax, float ay) {
		this.ax = ax;
		this.ay = ay;
		this.bodyList = bodyList;
	}

	@Override
	public void apply() {
		for (int i = 0; i < bodyList.size(); i++) {
			Body body = bodyList.get(i);

			body.applyForce(ax * body.mass, ay * body.mass);
		}
	}

}
