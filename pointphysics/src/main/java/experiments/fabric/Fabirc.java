package experiments.fabric;

import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Solver;
import com.dubiouscandle.pointphysics.mechanics.core.ConstantForce;
import com.dubiouscandle.pointphysics.mechanics.core.DistanceConstraint;
import com.dubiouscandle.pointphysics.mechanics.core.Spring;
import com.dubiouscandle.pointphysics.mechanics.core.StaticBody;

public class Fabirc {
	public static ArrayList<Body> edges1 = new ArrayList<>();
	public static ArrayList<Body> edges2 = new ArrayList<>();

	public static void main(String[] args) {
		float frameInterval = 1f / 120;

		BodyList bodyList = new BodyList();
		Random random = new Random(344314);
		Solver simulation = new Solver(bodyList, frameInterval);

		Body[][] bodies = new Body[50][50];

		float minS = 0f;
		float maxS = 10f;
		float s = 10;
		float e = 100;
		float s1 = 0;
		float im = 1f;
		for (int i = 0; i < bodies.length; i++) {
			for (int j = 0; j < bodies[i].length; j++) {
				bodies[i][j] = new Body(1f, 1f);

				bodies[i][j].x = j * s;
				bodies[i][j].y = i * s;
				
				bodies[i][j].setVelocity(i, j, frameInterval);
				
				if (i > 0) {
					simulation.addForce(new Spring(bodies[i - 1][j], bodies[i][j], s1, e));
					simulation.addConstraint(new DistanceConstraint(bodies[i - 1][j], bodies[i][j], minS, maxS));
					edges1.add(bodies[i][j]);
					edges2.add(bodies[i - 1][j]);
				} else {
					simulation.addConstraint(new StaticBody(bodies[i][j], j * s, 0));
				}
				if (j > 0) {
//					simulation.addForce(new Spring(bodies[i][j - 1], bodies[i][j], s1, e));
					simulation.addConstraint(new DistanceConstraint(bodies[i][j - 1], bodies[i][j], minS, maxS));
					edges1.add(bodies[i][j - 1]);
					edges2.add(bodies[i][j]);
				}

				bodyList.add(bodies[i][j]);
				bodies[i][j].x += random.nextFloat(0, im);
				bodies[i][j].y += random.nextFloat(0, im);
			}
		}

//		solver.addForce(new WindForce(bodyList, 0, 1000, 0.03f));
		simulation.addForce(new ConstantForce(bodyList, 0, -100f));
//		solver.addForce(new LinearDamping(bodyList, -0.003f));
		JPanel panel = new RenderPanel(bodyList);

		JFrame frame = new JFrame();
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		long t0 = System.nanoTime();
		int frameCount = 0;
		float speed = 1f;

		while (true) {
			int requiredFrames = (int) ((System.nanoTime() - t0) / 1_000_000_000.0 * speed / frameInterval);

			while (frameCount < requiredFrames) {
				simulation.step(32);
				frameCount++;
			}

			panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
		}
	}
}
