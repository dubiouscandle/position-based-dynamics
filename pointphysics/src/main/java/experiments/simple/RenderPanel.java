package experiments.simple;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Random;

import javax.swing.JPanel;

import com.dubiouscandle.pointphysics.Body;
import com.dubiouscandle.pointphysics.BodyList;
import com.dubiouscandle.pointphysics.mechanics.core.StaticEdge;

public class RenderPanel extends JPanel {
	private static final long serialVersionUID = -5769428947675129568L;
	private BodyList bodyList;
	
	private HashMap<Float, Color> colors = new HashMap<>();
	private Color getColor(float radius) {
		if(colors.containsKey(radius)) {
			return colors.get(radius);
		}else {
			Random random = new Random(Float.hashCode(radius)+4321983);
			Color color = new Color(Color.HSBtoRGB(random.nextFloat(), 1, 1));
			colors.put(radius, color);
			return color;
		}
		
	}
	
	private float cameraX = 0, cameraY = 0, worldWidth = 100, worldHeight = 100;

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
		float scale = (scaleX + scaleY) * 0.5f;
		for (Body body : bodyList) {
			float x = body.x;
			float y = body.y;
			float r = body.radius;
			g2.setColor(getColor(r));

			int screenX = (int) ((x - cameraX) * scaleX + getWidth() / 2);
			int screenY = (int) (getHeight() / 2 - (y - cameraY) * scaleY);

			int diameterX = Math.max((int) (r * 2 * scaleX), 3);
			int diameterY = Math.max((int) (r * 2 * scaleY), 3);

			g2.fillOval(screenX - diameterX / 2, screenY - diameterY / 2, diameterX, diameterY);
		}
		g2.setColor(new Color(0, 255, 0, 255)); // Use green for edges

		for (StaticEdge edge : Simpel.edges) {
			// compute end‐points in screen space
			int x1 = (int) ((edge.x1 - cameraX) * scaleX + getWidth() / 2);
			int y1 = (int) (getHeight() / 2 - (edge.y1 - cameraY) * scaleY);
			int x2 = (int) ((edge.x2 - cameraX) * scaleX + getWidth() / 2);
			int y2 = (int) (getHeight() / 2 - (edge.y2 - cameraY) * scaleY);

			// world‐space half‐thickness → full thickness in pixels
			float pixelThickness = edge.thickness * 2f * scale;

			// set stroke to that thickness
			Stroke stroke = new BasicStroke(pixelThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			g2.setStroke(stroke);
			g2.drawLine(x1, y1, x2, y2);
		}

	}

}
