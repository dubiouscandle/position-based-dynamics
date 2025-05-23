package com.dubiouscandle.pointphysics;

public class Body {
	public float x, y;
	public float radius;
	public float mass;

	private float px, py;
	private float fx, fy;

	private float vx, vy;

	public float vx() {
		return vx;
	}

	public float vy() {
		return vy;
	}

	public Body(float radius, float mass) {
		this.radius = radius;
		this.mass = mass;
	}

	@Override
	public String toString() {
		return "[" + hashCode() + ", position <" + x + "," + y + ">, position_difference <" + (x - px) + "," + (y - py)
				+ ">]";
	}

	public void zeroVelocity() {
		px = x;
		py = y;
		vx = 0;
		vy = 0;
	}

	public void step(float h) {
		float nx = x * 2 - px + fx * h * h / mass;
		float ny = y * 2 - py + fy * h * h / mass;

		vx = (nx - x) / h;
		vy = (ny - y) / h;

		px = x;
		py = y;
		x = nx;
		y = ny;

		fx = 0;
		fy = 0;
	}

	public void applyForce(float fx, float fy) {
		this.fx += fx;
		this.fy += fy;
	}

	public void translate(float tx, float ty) {
		x += tx;
		y += ty;
		px += tx;
		py += ty;
	}

	public void setVelocity(float vx, float vy, float stepInterval) {
		px = x - vx * stepInterval;
		py = y - vy * stepInterval;
	}
}