package com.dimage.processor;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.dimage.App;
import com.dimage.ImageContext;

import io.vertx.core.eventbus.EventBus;

public class LaplaceFilter implements Runnable {
	private final ImageContext context;
	private final EventBus eb;
	private final int[][] kernel;
	private final String replyId;
	
	public LaplaceFilter(ImageContext context, EventBus eb, int[][] kernel, String replyId) {
		this.context = context;
		this.eb = eb;
		this.kernel = kernel;
		this.replyId = replyId;
	}
	
	private static Color color(int rgb) {
		return new Color(rgb);
	}

	@Override
	public void run() {
		BufferedImage image = context.getImage();
		int maxRed = Integer.MIN_VALUE, maxGreen = Integer.MIN_VALUE, maxBlue = Integer.MIN_VALUE;
		int minRed = Integer.MAX_VALUE, minGreen = Integer.MAX_VALUE, minBlue = Integer.MAX_VALUE;
		int[][] redMap = new int[image.getWidth()][image.getHeight()];
		int[][] greenMap = new int[image.getWidth()][image.getHeight()];
		int[][] blueMap = new int[image.getWidth()][image.getHeight()];
		
		// Looping for over 2D bitmap pixels keeping 1 pixel padding
		try {
			for(int i = 1; i < image.getWidth() - 1; i++) {
				for(int j = 1; j < image.getHeight() - 1; j++) {
					int sumRed = 0, sumGreen = 0, sumBlue = 0;
					for(int k = -1; k <= 1; k++) {
						for(int l = -1; l <= 1; l++) {
							Color c = color(image.getRGB(i + k, j + l));
							sumRed += kernel[k + 1][l + 1] * c.getRed();
							sumBlue += kernel[k + 1][l + 1] * c.getBlue();
							sumGreen += kernel[k + 1][l + 1] * c.getGreen();
						}
					}
					
					redMap[i][j] = sumRed;
					greenMap[i][j] = sumGreen;
					blueMap[i][j] = sumBlue;
					
					maxRed = Math.max(sumRed, maxRed);
					maxBlue = Math.max(sumBlue, maxBlue);
					maxGreen = Math.max(sumGreen, maxGreen);
					
					minRed = Math.min(sumRed, minRed);
					minBlue = Math.min(sumBlue, minBlue);
					minGreen = Math.min(sumGreen, minGreen);
				}
			}
			
			
			// Stretching
			for(int i = 0; i < image.getWidth(); i++) {
				for(int j = 0; j < image.getHeight(); j++) {
					if(maxRed - minRed != 0)
						redMap[i][j] = ((255 * (redMap[i][j] - minRed)) / (maxRed - minRed));
					else
						redMap[i][j] = 0;
					
					if(maxBlue - minBlue != 0)
						blueMap[i][j] = ((255 * (blueMap[i][j] - minBlue)) / (maxBlue - minBlue));
					else
						blueMap[i][j] = 0;
					
					if(maxGreen - minGreen != 0)
						greenMap[i][j] = ((255 * (greenMap[i][j] - minGreen)) / (maxGreen - minGreen));
					else
						greenMap[i][j] = 0;
					
					image.setRGB(i, j, new Color(redMap[i][j], greenMap[i][j], blueMap[i][j]).getRGB());
				}
			}
		} catch(Exception e) {
			App.errln(e.getLocalizedMessage());
			App.errln(e.getMessage());
			App.flushErr();
		}
		
		try {
			eb.send(replyId, context.getBytes());
		} catch(Exception e) {
			App.errln(e.getMessage());
			App.errln(e.getLocalizedMessage());
			App.flushErr();
		}
	}
}
