package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.Constraint;

public class DistanceConstraint implements Constraint {
	public final Body bodyA, bodyB;
	private float minLength;
	private float maxLength;

	public DistanceConstraint(Body bodyA, Body bodyB, float minLength, float maxLength) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.minLength = minLength;
		this.maxLength = maxLength;
	}
	public DistanceConstraint(Body bodyA, Body bodyB) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		
		float dx = bodyA.x - bodyB.x;
		float dy = bodyA.y - bodyB.y;
		float dist = (float)Math.sqrt(dx * dx + dy * dy);
		this.maxLength = dist;
		this.minLength = dist;
	}

	public DistanceConstraint(Body bodyA, Body bodyB, float length) {
		this(bodyA, bodyB, length, length);
	}

	private void solveLength(float length) {
		float dx = bodyB.x - bodyA.x;
		float dy = bodyB.y - bodyA.y;
		float dist = (float) Math.sqrt(dx * dx + dy * dy);

		if (dist <= 1e-6f) {
			dist = 1e-6f;
		}

		float diff = dist - length;
		float correctionX = dx * diff / dist;
		float correctionY = dy * diff / dist;

		float invMassA = 1.0f / bodyA.mass;
		float invMassB = 1.0f / bodyB.mass;
		float invMassSum = invMassA + invMassB;

		bodyA.x += correctionX * (invMassA / invMassSum);
		bodyA.y += correctionY * (invMassA / invMassSum);
		bodyB.x -= correctionX * (invMassB / invMassSum);
		bodyB.y -= correctionY * (invMassB / invMassSum);
	}

	@Override
	public void project() {
		float dx = bodyB.x - bodyA.x;
		float dy = bodyB.y - bodyA.y;
		float distSqrd = dx * dx + dy * dy;

		if (distSqrd < minLength * minLength) {
			solveLength(minLength);
		} else if (distSqrd > maxLength * maxLength) {
			solveLength(maxLength);
		}
	}
}
