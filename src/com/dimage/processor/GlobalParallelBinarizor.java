package com.dimage.processor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.dimage.App;
import com.dimage.ImageContext;

import io.vertx.core.eventbus.EventBus;

public class GlobalParallelBinarizor implements Runnable {
	private final BufferedImage image;
	private final ImageContext imageContext;
	private final EventBus eb;
	private final String replyId;
	private int threshold;
	
	public GlobalParallelBinarizor(ImageContext imageContext, EventBus eb, String replyId, int threshold) {
		this.imageContext = imageContext;
		this.image = imageContext.getImage();
		this.threshold = threshold;
		this.eb = eb;
		this.replyId = replyId;
	}
	
	@Override
	public void run() {
		for(int i = 0; i < image.getWidth(); i++) {
			for(int j = 0; j < image.getHeight(); j++) {
				Color color = new Color(image.getRGB(i, j));
				int lum = (color.getRed() + color.getBlue() + color.getGreen()) / 3;
				if(lum >= 0 && lum <= 255) {
					if(lum <= threshold) {
						// Set black color
						image.setRGB(i, j, 0x000000);
					} else {
						image.setRGB(i, j, 0xFFFFFF);
					}
				}
			}
		}
		
		try {
			eb.send(replyId, imageContext.getBytes());
		} catch(IOException e) {
			App.errln(e.getLocalizedMessage());
			App.errln(e.getMessage());
			App.flushErr();
		}
	}
}