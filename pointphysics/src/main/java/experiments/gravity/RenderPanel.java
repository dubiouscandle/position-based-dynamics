package experiments.gravity;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;

public class RenderPanel extends JPanel {
	private static final long serialVersionUID = -5769428947675129568L;
	private BodyList bodyList;

	private float cameraX = 0, cameraY = 0, worldWidth = 30000, worldHeight = 30000;

	public RenderPanel(BodyList bodyList) {
		this.bodyList = bodyList;

		setPreferredSize(new Dimension(800, 800));
		setBackground(Color.black);

		MouseListener mouseListener = new MouseListener();
		this.addMouseMotionListener(mouseListener);
		this.addMouseWheelListener(mouseListener);
	}

	private class MouseListener implements MouseWheelListener, MouseMotionListener {
		private float px, py;

		@Override
		public void mouseDragged(MouseEvent e) {
			float size = Math.min(getWidth(), getHeight());
			float scaleX = size / worldWidth;
			float scaleY = size / worldHeight;
			float x = e.getX() / scaleX;
			float y = e.getY() / scaleY;

			cameraX += (px - x);
			cameraY -= (py - y);

			px = x;
			py = y;
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			float size = Math.min(getWidth(), getHeight());
			float scaleX = size / worldWidth;
			float scaleY = size / worldHeight;
			px = e.getX() / scaleX;
			py = e.getY() / scaleY;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			final float sens = 0.1f;
			worldWidth *= 1 + sens * (float) e.getPreciseWheelRotation();
			worldHeight *= 1 + sens * (float) e.getPreciseWheelRotation();

			float size = Math.min(getWidth(), getHeight());
			float scaleX = size / worldWidth;
			float scaleY = size / worldHeight;
			px = e.getX() / scaleX;
			py = e.getY() / scaleY;
		}

	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		float size = Math.min(getWidth(), getHeight());
		float scaleX = size / worldWidth;
		float scaleY = size / worldHeight;
		g2.setColor(new Color(0, 0, 255, 255));

		for (Body body : bodyList) {
			float x = body.x;
			float y = body.y;
			float r = body.radius;

			int screenX = (int) ((x - cameraX) * scaleX + getWidth() / 2);
			int screenY = (int) (getHeight() / 2 - (y - cameraY) * scaleY);

			int diameterX = Math.max((int) (r * 2 * scaleX), 3);
			int diameterY = Math.max((int) (r * 2 * scaleY), 3);

			g2.fillOval(screenX - diameterX / 2, screenY - diameterY / 2, diameterX, diameterY);
		}
	}

}
