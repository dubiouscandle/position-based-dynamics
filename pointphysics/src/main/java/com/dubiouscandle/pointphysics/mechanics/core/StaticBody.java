package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.Constraint;

public class StaticBody implements Constraint {
	private Body body;
	private float x, y;

	public StaticBody(Body body, float x, float y) {
		this.body = body;
		this.x = x;
		this.y = y;
	}

	public StaticBody(Body body) {
		this(body, body.x, body.y);
	}

	public void setLocation(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void project() {
		body.x = x;
		body.y = y;
	}
}
