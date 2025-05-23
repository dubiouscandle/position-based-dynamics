package experiments.cannon;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Solver;
import com.dubiouscandle.pointphysics.mechanics.core.BallConstraint;
import com.dubiouscandle.pointphysics.mechanics.core.CircularWorld;
import com.dubiouscandle.pointphysics.mechanics.core.ConstantForce;
import com.dubiouscandle.pointphysics.mechanics.core.DistanceConstraint;
import com.dubiouscandle.pointphysics.mechanics.core.LinearDamping;
import com.dubiouscandle.pointphysics.mechanics.core.Spring;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdge;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdges;

public class cannon {
	public static ArrayList<StaticEdge> edges = new ArrayList<>();
	public static ArrayList<Body> joints1 = new ArrayList<>();
	public static ArrayList<Body> joints2 = new ArrayList<>();
	public static CircularWorld cw;

	public static void main(String[] args) {
		float frameInterval = 1f / 60;

		float r = 50;
		BodyList bodyList = new BodyList();
		Random random = new Random(34434);
		Solver solver = new Solver(bodyList, frameInterval);

		addDude(bodyList, solver, 0, 2000);

//		for (int j = 0; j < 3; j++) {
//			for (int i = 0; i < 10; i++) {
//				float ra = 10f;
//				int w = 10;
//				createBlock(bodyList, solver, 2 * j * w * ra, ra * 3 + 2 * ra * w * i, w, w, ra);
//			}
//		}
		System.out.println(bodyList.size());

		StaticEdges se = new StaticEdges(bodyList);
		solver.addConstraint(se);
		edges.add(new StaticEdge(-30000, -300, 30000, -300, 300));

		for (StaticEdge edge : edges) {
			se.addEdge(edge);
		}
		cw = new CircularWorld(bodyList, 0, 0, 3090000);
		solver.addConstraint(cw);
		solver.addConstraint(new BallConstraint(bodyList));

		solver.addForce(new ConstantForce(bodyList, 0, -100));
		solver.addForce(new LinearDamping(bodyList, 0.3f));

		JPanel panel = new RenderPanel(bodyList);

		JFrame frame = new JFrame();
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		long t0 = System.nanoTime();
		int frameCount = 0;

		while (true) {
			int requiredFrames = (int) ((System.nanoTime() - t0) / 1_000_000_000.0 / frameInterval);

			while (frameCount < requiredFrames) {
				if (frameCount >= 200 && frameCount <= 210) {
					Body b = new Body(600, 50f);

					bodyList.add(b);

					b.x = -1000;
					b.y = 600;
					b.zeroVelocity();
					b.setVelocity(-40, -3, frameInterval);
				}

				solver.step(16);
				frameCount++;
			}

			panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
		}
	}

	private static Body[][] createBlock(BodyList bodyList, Solver solver, float x, float y, int width, int height,
			float r) {
		Body[][] bodies = new Body[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				bodies[i][j] = new Body(r, 1);

				bodies[i][j].x = x + i * r * 2;
				bodies[i][j].y = y + j * r * 2;
				bodies[i][j].zeroVelocity();
				bodyList.add(bodies[i][j]);

				if (i > 0) {
					solver.addConstraint(new DistanceConstraint(bodies[i - 1][j], bodies[i][j], r * 2));
				}
				if (j > 0) {
					solver.addConstraint(new DistanceConstraint(bodies[i][j - 1], bodies[i][j], r * 2));
				}
				if (i > 0 && j > 0) {
					solver.addConstraint(
							new DistanceConstraint(bodies[i - 1][j - 1], bodies[i][j], r * 2 * (float) Math.sqrt(2)));
				}
			}
		}
		return bodies;
	}

	private static void addDude(BodyList bodyList, Solver solver, float tx, float ty) {
		int c = 0;

		ArrayList<Body> allBodies = new ArrayList<>();
		for (float[] points : Points.points) {
			Body[] bodies = new Body[points.length / 2];
			for (int i = 0; i < points.length / 2; i++) {
				float x = points[2 * i] * 4 + tx;
				float y = points[2 * i + 1] * 4 + ty;
				bodies[i] = new Body(9f, 3);
				bodies[i].translate(x, y);
				allBodies.add(bodies[i]);
				bodyList.add(bodies[i]);
				c++;
			}

			for (int i = 0; i < bodies.length; i++) {
				solver.addConstraint(new DistanceConstraint(bodies[i], bodies[(i + 1) % bodies.length], 18));
				joints1.add(bodies[i]);
				joints2.add(bodies[(i + 1) % bodies.length]);
				c++;
			}

			for (int i = 0; i < bodies.length; i++) {
				for (int j = 0; j < 70; j++) {
					float curLength = (float) Math.hypot(bodies[i].x - bodies[(i + j) % bodies.length].x, bodies[i].y - bodies[(i + j) % bodies.length].y);
					solver.addForce(new Spring(bodies[i], bodies[(i + j) % bodies.length], curLength, 30));
					joints1.add(bodies[i]);
					joints2.add(bodies[j % bodies.length]);
					c++;
				}
			}

		}
//
//		for (int i = 0; i < allBodies.size(); i++) {
//			for (int j = i + 1; j < allBodies.size(); j++) {
//				float curLength = Body.dist(allBodies.get(i), allBodies.get(j));
//
//				if (curLength < 150) {
//					solver.addForce(new Spring(allBodies.get(i), allBodies.get(j), curLength, 300));
//					joints1.add(allBodies.get(i));
//					joints2.add(allBodies.get(j));
//				}
//			}
//		}

		System.out.println(c);
	}

	private static void createJoints(Solver solver, Body... bodies) {
		for (int i = 0; i < bodies.length; i++) {
			for (int j = i + 1; j < bodies.length; j++) {
				float dx = bodies[i].x - bodies[j].x;
				float dy = bodies[i].y - bodies[j].y;

				DistanceConstraint dc = new DistanceConstraint(bodies[i], bodies[j],
						(float) Math.sqrt(dx * dx + dy * dy));
//				joints.add(dc);

				solver.addConstraint(dc);
			}
		}
	}
}
