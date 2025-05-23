package com.dubiouscandle.pointphysics.mechanics.core;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.Constraint;

/**
 * angles are normalized between 0 and 2pi so anything more that that does not result in wrap around behavior
 */
public class AngleConstraint implements Constraint {
	private static final float PI = (float) Math.PI;
	private static final float TAU = (float) (Math.PI * 2);

	private Body bodyA, bodyB, bodyC;
	private float angle;

	public AngleConstraint(Body bodyA, Body bodyB, Body bodyC, float angle) {
		this.bodyA = bodyA;
		this.bodyB = bodyB;
		this.bodyC = bodyC;
		this.angle = angle;
	}

	@Override
	public void project() {
		float curAngle = curAngle();
		projectTo(curAngle, angle);
	}

	private void projectTo(float curAngle, float targetAngle) {
		float diff = targetAngle - curAngle;
		if (diff > PI)
			diff -= TAU;
		if (diff < -PI)
			diff += TAU;

		float halfDiff = diff / 2f;

		float baX = bodyA.x - bodyB.x;
		float baY = bodyA.y - bodyB.y;
		float bcX = bodyC.x - bodyB.x;
		float bcY = bodyC.y - bodyB.y;

		float sin = (float) Math.sin(halfDiff);
		float cos = (float) Math.cos(halfDiff);

		float newBAX = cos * baX - sin * baY;
		float newBAY = sin * baX + cos * baY;
		float newBCX = cos * bcX + sin * bcY;
		float newBCY = -sin * bcX + cos * bcY;

		float dAX = newBAX - baX;
		float dAY = newBAY - baY;
		float dCX = newBCX - bcX;
		float dCY = newBCY - bcY;

		float imA = 1 / bodyA.mass;
		float imB = 1 / bodyB.mass;
		float imC = 1 / bodyC.mass;

		float sum = imA + imB + imC;
		if (sum == 0)
			return;

		float factorA = imA / sum;
		float factorC = imC / sum;

		bodyA.x += dAX * factorA;
		bodyA.y += dAY * factorA;

		bodyC.x += dCX * factorC;
		bodyC.y += dCY * factorC;

		bodyB.x -= (dAX * factorA + dCX * factorC);
		bodyB.y -= (dAY * factorA + dCY * factorC);
	}

	private float curAngle() {
		float baX = bodyA.x - bodyB.x;
		float baY = bodyA.y - bodyB.y;

		float bcX = bodyC.x - bodyB.x;
		float bcY = bodyC.y - bodyB.y;

		float angleA = (float) Math.atan2(baY, baX);
		float angleC = (float) Math.atan2(bcY, bcX);

		float angle = angleA - angleC;

		if (angle > Math.PI) {
			angle -= 2 * Math.PI;
		} else if (angle < -Math.PI) {
			angle += 2 * Math.PI;
		}

		return angle;
	}

}
