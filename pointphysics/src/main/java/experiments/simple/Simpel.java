package experiments.simple;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Solver;
import com.dubiouscandle.pointphysics.mechanics.core.AngleConstraint;
import com.dubiouscandle.pointphysics.mechanics.core.BallConstraint;
import com.dubiouscandle.pointphysics.mechanics.core.CircularWorld;
import com.dubiouscandle.pointphysics.mechanics.core.ConstantForce;
import com.dubiouscandle.pointphysics.mechanics.core.DistanceConstraint;
import com.dubiouscandle.pointphysics.mechanics.core.LinearDamping;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdge;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdges;

public class Simpel {
	public static ArrayList<StaticEdge> edges = new ArrayList<>();

	public static void main(String[] args) {
		float frameInterval = 1f / 70;

		float r = 50;
		BodyList bodyList = new BodyList();
		Random random = new Random(34434);
		Solver solver = new Solver(bodyList, frameInterval);

		for (int j = 0; j < 400; j++) {
//			Body b = new Body(3, 1);
//			bodyList.add(b);
//
//			b.x = random.nextFloat(-500, 500);
//			b.y = random.nextFloat(-500, 500);
//
//			while (b.x * b.x + b.y * b.y > 300 * 300) {
//				b.x = random.nextFloat(-500, 500);
//				b.y = random.nextFloat(-500, 500);
//			}

//			b.zeroVelocity();
			addDude(bodyList, solver, 30, j * 30 + 30);
		}

		System.out.println(bodyList);

		StaticEdges se = new StaticEdges(bodyList);
		solver.addConstraint(se);
//		edges.add(new StaticEdge(-10000, 0, 1000, 0, 3));
//		edges.add(new StaticEdge(1500, 500, 1000, 0, 3));

		for (StaticEdge edge : edges) {
			se.addEdge(edge);
		}

		solver.addConstraint(new BallConstraint(bodyList));

		solver.addConstraint(new CircularWorld(bodyList, 0, 0, 300));
		solver.addForce(new ConstantForce(bodyList, 0, -100));
		solver.addForce(new LinearDamping(bodyList, 10f));

		JPanel panel = new RenderPanel(bodyList);

		JFrame frame = new JFrame();
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		long t0 = System.nanoTime();
		int frameCount = 0;

		Body[] b2 = new Body[120];
		{
			for (int i = 0; i < b2.length; i++) {
				Body body = new Body(6f, 30);
				b2[i] = body;
				body.x = -5000;
				body.y = body.radius;
				body.setVelocity(-2, -30, frameInterval);
				bodyList.add(body);
			}
		}

		while (true) {
			int requiredFrames = (int) ((System.nanoTime() - t0) / 1_000_000_000.0 / frameInterval);

			while (frameCount < requiredFrames) {
				float s = 0.5f;
				float ra = 300;
				float t = frameCount * frameInterval;
				for (int i = 0; i < b2.length; i++) {
					Body body = b2[i];
					float angle = (float) Math.PI * i / b2.length;

					float ra1 = ra * (float) Math.cos(t * s);
					body.x = (float) Math.cos(angle * 2) * ra1;
					body.y = (float) Math.sin(angle * 2) * ra1;
//					body.x = (float) (Math.cos(angle) * ra * Math.cos(t * s +  (float) Math.PI * i / b2.length));
//					body.y = (float) (Math.sin(angle) * ra * Math.cos(t * s +  (float) Math.PI * i / b2.length));
				}
				solver.step(16);
				frameCount++;
			}

			panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
		}
	}

	private static void addDude(BodyList bodyList, Solver solver, float x, float y) {
		Body head = new Body(2, 1);
		Body torso1 = new Body(1.5f, 1f);
		Body torso2 = new Body(1.5f, 1f);
		Body hand1 = new Body(1.5f, 1f);
		Body hand2 = new Body(1.5f, 1f);
		Body leg1 = new Body(1.5f, 1f);
		Body leg2 = new Body(1.5f, 1f);

		head.x = x;
		head.y = y;

		torso1.x = x;
		torso1.y = y - 3;

		torso2.x = x;
		torso2.y = y - 6;

		hand1.x = x - 3;
		hand1.y = y;
		hand2.x = x + 3;
		hand2.y = y;

		leg1.x = x - 3;
		leg1.y = y - 6;
		leg2.x = x + 3;
		leg2.y = y - 6;

		solver.addConstraint(new AngleConstraint(head, torso1, torso2, (float) Math.PI));
		solver.addConstraint(new AngleConstraint(hand1, torso1, torso2, -(float) Math.PI * 2 / 3));
		solver.addConstraint(new AngleConstraint(hand2, torso1, torso2, +(float) Math.PI * 2 / 3));
		solver.addConstraint(new AngleConstraint(leg1, torso2, torso1, -(float) Math.PI * 2 / 3));
		solver.addConstraint(new AngleConstraint(leg2, torso2, torso1, +(float) Math.PI * 2 / 3));

		solver.addConstraint(new DistanceConstraint(head, torso1, 5));
		solver.addConstraint(new DistanceConstraint(torso2, torso1, 3));
		solver.addConstraint(new DistanceConstraint(torso1, hand1, 3));
		solver.addConstraint(new DistanceConstraint(torso1, hand2, 3));
		solver.addConstraint(new DistanceConstraint(torso2, leg1, 3));
		solver.addConstraint(new DistanceConstraint(torso2, leg2, 3));

		head.zeroVelocity();
		torso1.zeroVelocity();
		torso2.zeroVelocity();
		hand1.zeroVelocity();
		hand2.zeroVelocity();
		leg1.zeroVelocity();
		leg2.zeroVelocity();

		bodyList.add(head);
		bodyList.add(torso1);
		bodyList.add(torso2);
		bodyList.add(hand1);
		bodyList.add(hand2);
		bodyList.add(leg1);
		bodyList.add(leg2);
	}
}
