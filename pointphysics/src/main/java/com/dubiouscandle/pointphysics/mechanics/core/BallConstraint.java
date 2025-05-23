package com.dubiouscandle.pointphysics.mechanics.core;

import java.util.function.Consumer;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Constraint;
import com.dubiouscandle.pointphysics.util.Quadtree;
import com.dubiouscandle.pointphysics.util.Quadtree.Cell;

public class BallConstraint implements Constraint {
	public final Quadtree quadtree;
	private Consumer<Cell> consumer = new ContactHandler();

	public BallConstraint(BodyList bodyList) {
		quadtree = new Quadtree(bodyList, 16, 16);
	}

	@Override
	public void project() {
		quadtree.generate();

		quadtree.forEachCell(consumer);
	}

	private class ContactHandler implements Consumer<Cell> {
		@Override
		public void accept(Cell cell) {
			int len = cell.size();

			for (int i = 0; i < len; i++) {
				for (int j = i + 1; j < len; j++) {
					handleCollision(cell.getBody(i), cell.getBody(j));
				}
			}
		}
	}

	public void handleCollision(Body bodyA, Body bodyB) {
		float dx = bodyA.x - bodyB.x;
		float dy = bodyA.y - bodyB.y;
		float r = bodyA.radius + bodyB.radius;
		float distSquared = dx * dx + dy * dy;

		if (distSquared > r * r || distSquared <= 1e-8f) {
			return;
		}

		float dist = (float) Math.sqrt(dx * dx + dy * dy);

		float penetration = r - dist;
		float correctionX = dx * penetration / dist;
		float correctionY = dy * penetration / dist;

		float invMassA = 1.0f / bodyA.mass;
		float invMassB = 1.0f / bodyB.mass;
		float invMassSum = invMassA + invMassB;

		bodyA.x += correctionX * (invMassA / invMassSum);
		bodyA.y += correctionY * (invMassA / invMassSum);
		bodyB.x -= correctionX * (invMassB / invMassSum);
		bodyB.y -= correctionY * (invMassB / invMassSum);
	}
}
