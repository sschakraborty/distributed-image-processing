package com.dimage.verticles;

import java.io.IOException;

import com.dimage.App;
import com.dimage.ImageContext;
import com.dimage.processor.LaplaceFilter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class NegativeLaplaceFilterVerticle extends AbstractVerticle {
	public static int[][] kernel = {
			{ -1, -1, -1 },
			{ -1,  8, -1 },
			{ -1, -1, -1 }
	};
	
	@Override
	public void start() {
		EventBus eb = vertx.eventBus();
		
		eb.consumer("forward-pipeline", message -> {
			if(message.body() instanceof byte[]) {
				try {
					ImageContext obj = ImageContext.readBytes((byte[]) message.body());
					App.getThreadPool().submit(new LaplaceFilter(obj, eb, kernel, "reverse-pipeline"));
				} catch(IOException e) {
					App.errln(e.getLocalizedMessage());
					App.errln(e.getMessage());
					App.flushErr();
				}
			}
		});
	}
}
