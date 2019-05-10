package com.dimage.verticles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import com.dimage.App;
import com.dimage.ImageContext;
import com.dimage.processor.GlobalParallelBinarizor;
import com.dimage.processor.GlobalParallelThresholdCalculator;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

public class GPBNodeVerticle extends AbstractVerticle {
	public static enum METRIC {
		ARITHMETIC_MEAN, HARMONIC_MEAN, QUADRATIC_MEAN, STAT_MEDIAN;
	}
	
	private final METRIC metric;
	
	public GPBNodeVerticle(METRIC metric) {
		this.metric = metric;
	}
	
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
		ArrayList<Integer> list = new ArrayList<>(App.getSegmentNum());
		ArrayList<ImageContext> context = new ArrayList<>();
		
		eb.consumer("forward-pipeline", message -> {
			if(message.body() instanceof byte[]) {
				try {
					ImageContext obj = ImageContext.readBytes((byte[]) message.body());
					context.add(obj);
					App.getThreadPool().submit(new GlobalParallelThresholdCalculator(obj, eb, "gpb-internal-threshold-submit"));
				} catch(IOException e) {
					App.errln(e.getLocalizedMessage());
					App.errln(e.getMessage());
					App.flushErr();
				}
			}
		});
		
		eb.consumer("gpb-internal-threshold-submit", message -> {
			int k = (int) message.body();
			list.add(k);
			
			if(list.size() == App.getSegmentNum()) {
				int th = 0;
				
				if(metric == METRIC.ARITHMETIC_MEAN) {
					App.prln("Using arithmetic mean metric");
					App.flush();
					for(int p : list) {
						th += p;
					}
					th /= list.size();
				}
				
				if(metric == METRIC.HARMONIC_MEAN) {
					App.prln("Using harmonic mean metric");
					App.flush();
					double sum = 0.0;
					for(int p : list) {
						sum += (1.0 / p);
					}
					th = (int) (list.size() / sum);
				}
				
				if(metric == METRIC.STAT_MEDIAN) {
					App.prln("Using statistical median metric");
					App.flush();
					Collections.sort(list);
					if((list.size() & 1) == 0) {
						int m = list.size() >> 1;
						int s = list.get(m - 1) + list.get(m);
						th = s >> 1;
					} else {
						int m = list.size() >> 1;
						th = list.get(m);
					}
				}
				
				if(metric == METRIC.QUADRATIC_MEAN) {
					App.prln("Using quadratic mean metric");
					App.flush();
					long s = 0;
					for(int p : list) {
						s += p * p;
					}
					s /= list.size();
					th = (int) Math.sqrt(s);
				}
				
				App.pr("Calculated threshold: ");
				App.prln(th + "");
				App.flush();
				
				for(ImageContext con : context) {
					App.getThreadPool().submit(new GlobalParallelBinarizor(con, eb, "reverse-pipeline", th));
				}
			}
		});
	}
}