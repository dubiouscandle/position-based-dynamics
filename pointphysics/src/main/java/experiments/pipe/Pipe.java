package experiments.pipe;

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
import com.dubiouscandle.pointphysics.mechanics.core.LinearDamping;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdge;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdge.PolygonBuilder;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdges;

public class Pipe {
	public static ArrayList<StaticEdge> edges = new ArrayList<StaticEdge>();

	public static void main(String[] args) {
		float frameInterval = 1f / 60;

		BodyList bodyList = new BodyList();
		Random random = new Random(34434);
		Solver simulation = new Solver(bodyList, frameInterval);
		float s = 1500;
		float w = 300;
		for (int i = 0; i < 4000; i++) {
			Body body = new Body(100, 1f);
			body.x = random.nextFloat(s * 2f, s * 6f);
			body.y = random.nextFloat(s * 5, s * 30) - 30;
			body.zeroVelocity();
			bodyList.add(body);
		}
		{
			Body body = new Body(100, 1f);
			body.x = -10000;
			body.y = 1000;
			body.zeroVelocity();
			bodyList.add(body);
		}
		{
			Body body = new Body(300, 1000f);
			body.x = +2000;
			body.y = 10000;
			body.zeroVelocity();
			bodyList.add(body);
		}
		{
			PolygonBuilder polygonBuilder = new PolygonBuilder(w);
			for (int resolution = 16, i = 0; i <= resolution; i++) {
				float angle = (float) Math.PI * i / resolution * 2 / 3;

				polygonBuilder.addVertex(2 * s * (float) Math.cos(angle), -2 * s * (float) Math.sin(angle));
			}

			for (StaticEdge edge : polygonBuilder.edges()) {
				edges.add(edge);
			}
		}
		{
			PolygonBuilder polygonBuilder = new PolygonBuilder(w);
			for (int resolution = 16, i = 0; i <= resolution; i++) {
				float angle = (float) Math.PI * i / resolution * 2 / 3;

				polygonBuilder.addVertex(s * (float) Math.cos(angle), -s * (float) Math.sin(angle));
			}

			for (StaticEdge edge : polygonBuilder.edges()) {
				edges.add(edge);
			}
		}
		{
			PolygonBuilder polygonBuilder = new PolygonBuilder(w);
			for (int resolution = 16, i = 0; i <= resolution; i++) {
				float angle = (float) Math.PI * i / resolution * 1.5f;

				polygonBuilder.addVertex(s * (float) Math.cos(angle) * 10 - s * 13,
						-s * (float) Math.sin(angle) * 10 - s * 10);
			}

			for (StaticEdge edge : polygonBuilder.edges()) {
				edges.add(edge);
			}
		}

		float fun = s * 160;
		edges.add(new StaticEdge(s * 2, 0, s * 2 + fun, s + fun, w));
//		edges.add(new StaticEdge(s, s * 2, s - s * 1, s * 2 + s * 3, e, w));
		edges.add(new StaticEdge(s, s + fun, s, 0, w));
//		edges.add(new StaticEdge(s * 2, s, s * 2, 0, e, w));
//		edges.add(new StaticEdge(-s, s * 2, -s, 0, e, w));
//		edges.add(new StaticEdge(-s * 2, s * 2, -s * 2, 0, e, w));
//		for (int i = 0; i < 80; i++) {
//			float f = 10f;
//			edges.add(new StaticEdge(0 + i * f, -s, -s + i * f, 0, e));
//			edges.add(new StaticEdge(0 + i * f, -s, s + i * f, 0, e));
//			edges.add(new StaticEdge(-s + i * f, s, -s + i * f, 0, e));
//			edges.add(new StaticEdge(s + i * f, s, s + i * f, 0, e));
//
//		}

		simulation.addConstraint(new CircularWorld(bodyList, 0, 0, s * 50));
		
		StaticEdges ec = new StaticEdges(bodyList);

		for (StaticEdge edge : edges) {
			ec.addEdge(edge);
		}

		simulation.addConstraint(new BallConstraint(bodyList));
		simulation.addConstraint(ec);

		simulation.addForce(new ConstantForce(bodyList, 0, -1000f));
		simulation.addForce(new LinearDamping(bodyList, 0.01f));

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
				simulation.step(8);
				frameCount++;
			}

			panel.paintImmediately(0, 0, panel.getWidth(), panel.getHeight());
		}
	}
}
