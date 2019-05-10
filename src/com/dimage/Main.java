package com.dimage;

import com.dimage.verticles.GPBNodeVerticle;
import com.dimage.verticles.GPBNodeVerticle.METRIC;
import com.dimage.verticles.LPBNodeVerticle;
import com.dimage.verticles.LocalContrastStretcherVerticle;
import com.dimage.verticles.NegativeLaplaceFilterVerticle;
import com.dimage.verticles.NegativeLaplaceSharpenerVerticle;
import com.dimage.verticles.PositiveLaplaceFilterVerticle;
import com.dimage.verticles.PositiveLaplaceSharpenerVerticle;
import com.dimage.verticles.RouterVerticle;

import io.vertx.core.Vertx;

public class Main {
	public static void main(String[] args) {
		try {
			Vertx vertx = Vertx.vertx();
			App.prln("Backbone instantiated...");
			App.prln("Enter option for processor type to use: ");
			App.prln("\t(1). Local Parallel Binarizor (LPB)");
			App.prln("\t(2). Global Parallel Binarizor (GPB)");
			App.prln("\t(3). Negative Laplace Filter (NLF)");
			App.prln("\t(4). Positive Laplace Filter (PLF)");
			App.prln("\t(5). Local Contrast Stretcher (LCS)");
			App.prln("\t(6). Negative Laplace Sharpener (NLS)");
			App.prln("\t(7). Positive Laplace Sharpener (PLS)");
			App.pr("Enter option: ");
			App.flush();
			String bin = App.readLine();
			
			if(bin.equalsIgnoreCase("LPB") || bin.equalsIgnoreCase("1")) {
				vertx.deployVerticle(new LPBNodeVerticle(), res1 -> {
					if(res1.succeeded()) {
						vertx.deployVerticle(new RouterVerticle(), res2 -> {
							if(res2.succeeded()) {
								App.prln("Deployed master router (RouterVerticle)...");
								App.flush();
							}
						});
					}
				});
			} else if(bin.equalsIgnoreCase("GPB") || bin.equalsIgnoreCase("2")) {
				App.prln("Enter threshold aggregation metric type");
				App.prln("\t(1). Arithmetic Mean (AM) - default");
				App.prln("\t(2). Harmonic Mean (HM)");
				App.prln("\t(3). Quadratic Mean (QM)");
				App.prln("\t(4). Statistical Median (MED)");
				App.pr("Enter option: ");
				App.flush();
				String opt = App.readLine();
				
				METRIC metric = METRIC.ARITHMETIC_MEAN;
				
				if(opt.equalsIgnoreCase("HM") || opt.equalsIgnoreCase("2")) {
					metric = METRIC.HARMONIC_MEAN;
				}
				
				if(opt.equalsIgnoreCase("QM") || opt.equalsIgnoreCase("3")) {
					metric = METRIC.QUADRATIC_MEAN;
				}
				
				if(opt.equalsIgnoreCase("MED") || opt.equalsIgnoreCase("4")) {
					metric = METRIC.STAT_MEDIAN;
				}
				
				vertx.deployVerticle(new GPBNodeVerticle(metric), res1 -> {
					if(res1.succeeded()) {
						vertx.deployVerticle(new RouterVerticle(), res2 -> {
							if(res2.succeeded()) {
								App.prln("Deployed master router (RouterVerticle)...");
								App.flush();
							}
						});
					}
				});
			} else if(bin.equalsIgnoreCase("NLF") || bin.equalsIgnoreCase("3")) {
				vertx.deployVerticle(new NegativeLaplaceFilterVerticle(), res1 -> {
					if(res1.succeeded()) {
						vertx.deployVerticle(new RouterVerticle(), res2 -> {
							if(res2.succeeded()) {
								App.prln("Deployed master router (RouterVerticle)...");
								App.flush();
							}
						});
					}
				});
			} else if(bin.equalsIgnoreCase("PLF") || bin.equalsIgnoreCase("4")) {
				vertx.deployVerticle(new PositiveLaplaceFilterVerticle(), res1 -> {
					if(res1.succeeded()) {
						vertx.deployVerticle(new RouterVerticle(), res2 -> {
							if(res2.succeeded()) {
								App.prln("Deployed master router (RouterVerticle)...");
								App.flush();
							}
						});
					}
				});
			} else if(bin.equalsIgnoreCase("LCS") || bin.equalsIgnoreCase("5")) {
				vertx.deployVerticle(new LocalContrastStretcherVerticle(), res1 -> {
					if(res1.succeeded()) {
						vertx.deployVerticle(new RouterVerticle(), res2 -> {
							if(res2.succeeded()) {
								App.prln("Deployed master router (RouterVerticle)...");
								App.flush();
							}
						});
					}
				});
			} else if(bin.equalsIgnoreCase("NLS") || bin.equalsIgnoreCase("6")) {
				vertx.deployVerticle(new NegativeLaplaceSharpenerVerticle(), res1 -> {
					if(res1.succeeded()) {
						vertx.deployVerticle(new RouterVerticle(), res2 -> {
							if(res2.succeeded()) {
								App.prln("Deployed master router (RouterVerticle)...");
								App.flush();
							}
						});
					}
				});
			} else if(bin.equalsIgnoreCase("PLS") || bin.equalsIgnoreCase("7")) {
				vertx.deployVerticle(new PositiveLaplaceSharpenerVerticle(), res1 -> {
					if(res1.succeeded()) {
						vertx.deployVerticle(new RouterVerticle(), res2 -> {
							if(res2.succeeded()) {
								App.prln("Deployed master router (RouterVerticle)...");
								App.flush();
							}
						});
					}
				});
			} else {
				App.errln("Unknown processor type");
				App.flushErr();
				System.exit(0);
			}
		} catch(Exception e) {
			App.errln(e.getLocalizedMessage());
			App.errln(e.getMessage());
			App.errln(e.getCause().toString());
			App.flushErr();
		}
	}
}