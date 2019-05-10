package com.dimage.verticles;

import java.io.IOException;
import java.util.Random;

import com.dimage.App;
import com.dimage.ImageContext;
import com.dimage.processor.LocalParallelBinarizor;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class LPBNodeVerticle extends AbstractVerticle {
	@Override
	public void start() {
		StringBuilder rand = new StringBuilder();
		Random random = new Random();
		for(int i = 0; i < 15; i++) {
			rand.append((char) ('A' + random.nextDouble() * ('Z' - 'A')));
		}
		String randId = rand.toString();
		App.prln("Deployed verticle with ID: " + randId);
		App.flush();
		
		EventBus eb = vertx.eventBus();
		
		eb.consumer("forward-pipeline", message -> {
			if(message.body() instanceof byte[]) {
				try {
					ImageContext obj = ImageContext.readBytes((byte[]) message.body());
					App.getThreadPool().submit(new LocalParallelBinarizor(obj, eb, "reverse-pipeline"));
				} catch(IOException e) {
					App.errln(e.getLocalizedMessage());
					App.errln(e.getMessage());
					App.flushErr();
				}
			}
		});
	}
}