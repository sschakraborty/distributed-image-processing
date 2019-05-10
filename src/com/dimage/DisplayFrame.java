package com.dimage;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class DisplayFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private final BufferedImage image;
	private double scale = 1.0;
	private int sx, sy;
	
	private void trX(int x, boolean zoomOut) {
		sx = (zoomOut) ? (int) (x * (0.1) + 0.9 * sx) : (int) (x * (-0.1) + 1.1 * sx);
	}
	
	private void trY(int x, boolean zoomOut) {
		sy = (zoomOut) ? (int) (x * (0.1) + 0.9 * sy) : (int) (x * (-0.1) + 1.1 * sy);
	}
	
	public DisplayFrame(BufferedImage image) {
		setSize(750, 500);
		setLayout(new BorderLayout());
		setLocation(10, 10);
		setResizable(true);
		setTitle("Image Display Frame - Distributed Image Processing");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.image = image;
		this.scale = 1.0;
		this.sx = 0;
		this.sy = 0;
		
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int x = e.getX();
				int y = e.getY();
				int delta = e.getWheelRotation();
				
				// Clear viewport
				getGraphics().clearRect(sx, sy, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale));
				
				if(delta < 0) {
					scale *= 1.1;
					trX(x, false);
					trY(y, false);
				} else {
					scale /= 1.1;
					trX(x, true);
					trY(y, true);
				}
				
				repaint();
			}
		});
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(image, sx, sy, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), null);
	}
}
