package experiments.gravity;

import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.Force;
import com.dubiouscandle.pointphysics.Solver;
import com.dubiouscandle.pointphysics.mechanics.core.BallConstraint;

public class GravitySutff {
	public static void main(String[] args) {
		float frameInterval = 1f / 60;

		BodyList bodyList = new BodyList();
		Random random = new Random(34434);
		Solver simulation = new Solver(bodyList, frameInterval);

		float d = 3000;
		float w = 200;
		float im = 0;
		for (int i = 0; i < 2000; i++) {
			Body body = new Body(1f, 1f);
			body.x = random.nextFloat(-w, w);
			body.y = random.nextFloat(-w, w);

			body.zeroVelocity();
			bodyList.add(body);
		}

		simulation.addForce(new Force() {
			@Override
			public void apply() {
				for (int i = 0; i < bodyList.size(); i++) {
					Body body = bodyList.get(i);

					float dx = body.x;
					float dy = body.y;

					float distSquared = dx * dx + dy * dy;

					if (distSquared <= 1e-6f) {
						continue;
					}

					float dist = (float) Math.sqrt(distSquared);

					float f = 100 * body.mass / distSquared;
					
					f = Math.max(f, 10);
					
					float fx = f * dx / dist;
					float fy = f * dy / dist;

					
					body.fx -= fx;
					body.fy -= fy;
				}
			}
		});

		simulation.addConstraint(new BallConstraint(bodyList));
		simulation.addForce(new Force() {
			@Override
			public void apply() {
				for (int i = 0; i < bodyList.size(); i++) {
					for (int j = i + 1; j < bodyList.size(); j++) {
						Body bodyA = bodyList.get(i);
						Body bodyB = bodyList.get(j);

						float dx = bodyA.x - bodyB.x;
						float dy = bodyA.y - bodyB.y;

						float distSquared = dx * dx + dy * dy;

						if (distSquared <= 1e-6f) {
							continue;
						}

						float dist = (float) Math.sqrt(distSquared);

						float f = 100 * bodyA.mass * bodyB.mass / distSquared;
						float fx = f * dx / dist;
						float fy = f * dy / dist;

						bodyA.fx -= fx;
						bodyA.fy -= fy;
						bodyB.fx += fx;
						bodyB.fy += fy;
					}
				}
			}
		});

		JPanel panel = new RenderPanel(bodyList);

		JFrame frame = new JFrame();
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		long t0 = System.nanoTime() - 4_000_000_000L;
		int frameCount = 0;
		float speed = 1f;

		while (true) {
			int requiredFrames = (int) ((System.nanoTime() - t0) / 1_000_000_000.0 * speed / frameInterval);

			while (frameCount < requiredFrames) {
				if (frameCount % 60 == 0) {
					Body body = new Body(10f, 10f);
					body.x = -10_000f;
					body.y = 0;

					body.zeroVelocity();

					body.px -= 10;

					bodyList.add(body);
				}

				simulation.step(8);
				frameCount++;
			}
			panel.repaint();

			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
