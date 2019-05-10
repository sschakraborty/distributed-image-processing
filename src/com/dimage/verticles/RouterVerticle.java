package com.dimage.verticles;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import com.dimage.App;
import com.dimage.DisplayFrame;
import com.dimage.ImageContext;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class RouterVerticle extends AbstractVerticle {
	@Override
	public void start() {
		// There are two things we need to distribute.
		// One is an image that needs to be broken up.
		// Other is the code.
		
		// Reading the image
		
		EventBus eb = vertx.eventBus();
		
		if(App.getDirPath() != null && App.getImage() != null) {
			BufferedImage image = App.getImage();
			BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics2D graphics = result.createGraphics();
			
			
			// Instantiate a JFrame and a JPanel
			DisplayFrame frame = new DisplayFrame(result);
			frame.setVisible(true);
			
			
			eb.consumer("reverse-pipeline", message -> {
				if(message.body() instanceof byte[]) {
					try {
						ImageContext context = ImageContext.readBytes((byte[]) message.body());
						graphics.drawImage(context.getImage(), context.getStartX(), context.getStartY(), null);
						frame.repaint();
					} catch(IOException e) {
						App.errln(e.getLocalizedMessage());
						App.errln(e.getMessage());
						App.flushErr();
					}
				}
			});
			
			byte[][][] imageArray = App.getContexts();
			for(int i = 0; i < App.getDivisionNum(); i++) {
				for(int j = 0; j < App.getDivisionNum(); j++) {
					eb.send("forward-pipeline", imageArray[i][j]);
				}
			}
		}
	}
}