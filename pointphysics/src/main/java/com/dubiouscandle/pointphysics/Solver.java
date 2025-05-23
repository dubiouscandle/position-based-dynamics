package com.dubiouscandle.pointphysics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Solver {
	private BodyList bodyList = new BodyList();
	private ArrayList<Constraint> constraints = new ArrayList<>();
	private ArrayList<Force> forces = new ArrayList<>();
	private Random random;
	private final float frameInterval;

	public Solver(BodyList bodyList, float frameInterval) {
		this.bodyList = bodyList;
		this.frameInterval = frameInterval;
		random = new Random(-210123371228935446L);
	}

	public Solver(BodyList bodyList) {
		this(bodyList, 0);
	}

	public void step(int constraintIterations) {
		for (Force force : forces) {
			force.apply();
		}

		for (int i = 0; i < bodyList.size(); i++) {
			bodyList.get(i).step(frameInterval);
		}

		for (int i = 0; i < constraintIterations; i++) {
			Collections.shuffle(constraints, random);
			for (Constraint constraint : constraints) {
				constraint.project();
			}
		}
	}

	public void addConstraint(Constraint constraint) {
		constraints.add(constraint);
	}

	public void addForce(Force force) {
		forces.add(force);
	}

}
