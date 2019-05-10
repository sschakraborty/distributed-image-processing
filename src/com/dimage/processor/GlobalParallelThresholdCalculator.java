package com.dimage.processor;

import java.awt.Color;
import java.awt.image.BufferedImage;

import com.dimage.App;
import com.dimage.ImageContext;

import io.vertx.core.eventbus.EventBus;

public class GlobalParallelThresholdCalculator implements Runnable {
	private final BufferedImage image;
	private final EventBus eb;
	private final String replyId;
	private int threshold, value;
	
	public GlobalParallelThresholdCalculator(ImageContext imageContext, EventBus eb, String replyId) {
		this.image = imageContext.getImage();
		this.threshold = -1;
		this.value = -1;
		this.eb = eb;
		this.replyId = replyId;
	}
	
	@Override
	public void run() {
		int[] histogram = new int[256];
		int[] cumulHist = new int[256];
		int[] multiHist = new int[256];
		
		// First pass
		// Create the histogram
		// for all the pixels
		// Set the value to threshold.
		
		for(int i = 0; i < image.getWidth(); i++) {
			for(int j = 0; j < image.getHeight(); j++) {
				Color color = new Color(image.getRGB(i, j));
				int lum = (color.getRed() + color.getBlue() + color.getGreen()) / 3;
				if(lum >= 0 && lum <= 255) {
					histogram[lum]++;
				}
			}
		}
		
		cumulHist[0] = histogram[0];
		multiHist[0] = 0;
		for(int i = 1; i < cumulHist.length; i++) {
			cumulHist[i] = cumulHist[i - 1] + histogram[i];
			multiHist[i] = multiHist[i - 1] + histogram[i] * i;
		}
		
		try {
			for(int i = 1; i <= 254; i++) {
				double Wb = cumulHist[i];
				double Wf = cumulHist[cumulHist.length - 1] - cumulHist[i - 1];
				Wb = Wb / (image.getWidth() * image.getHeight());
				Wf = Wf / (image.getWidth() * image.getHeight());
				
				double mub = multiHist[i];
				if(cumulHist[i] != 0) {
					mub /= cumulHist[i];
				}
				
				double muf = multiHist[multiHist.length - 1] - multiHist[i - 1];
				if(cumulHist[cumulHist.length - 1] - cumulHist[i - 1] != 0) {
					muf /= (cumulHist[cumulHist.length - 1] - cumulHist[i - 1]);
				}
				
				int val = (int) (Wb * Wf * Math.pow(mub - muf, 2));
				if(val > value) {
					value = val;
					threshold = i;
				}
			}
		} catch(Exception e) {
			App.errln("Error in core");
			App.errln(e.getMessage());
			App.errln(e.getLocalizedMessage());
			App.flushErr();
			System.exit(0);
		}
		
		try {
			eb.send(replyId, threshold);
		} catch(Exception e) {
			App.errln(e.getLocalizedMessage());
			App.errln(e.getMessage());
			App.flushErr();
		}
	}
}