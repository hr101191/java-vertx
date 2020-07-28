package com.hurui.micrometermetrics.prometheusmetricsservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hurui.micrometermetrics.verticles.MainVerticle;

import io.vertx.core.Launcher;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;

public class PrometheusMetricsServiceApplication extends Launcher {

	private static final Logger logger = LoggerFactory.getLogger(new Object() { }.getClass().getEnclosingClass());
	
	public static void main(String[] args) {
		
		logger.info("Initializing Vertx Command Launcher...");
		String[] newArgs = new String[args.length + 2];
		newArgs[0] = "run";
	    newArgs[1] = MainVerticle.class.getName();
	    System.arraycopy(args, 0, newArgs, 2, args.length);
	    
	    logger.info("Executing the program with the following command line arguements:");	    
	    for(String str : newArgs) {
	    	logger.info("args: " + str);
	    }
	    logger.info("Deploying Main Verticle...");

		new PrometheusMetricsServiceApplication().dispatch(newArgs);
	}
	
    @Override
    public void beforeStartingVertx(VertxOptions options) {
        // Customize the options
       	logger.info("Configuring Prometheus options...");
		// Deploy with embedded server: prometheus metrics will be automatically exposed,
	    // independently from any other HTTP server defined
	    MicrometerMetricsOptions micrometerMetricsOptions = new MicrometerMetricsOptions()
		    		.setPrometheusOptions(new VertxPrometheusOptions()
		    		//.setStartEmbeddedServer(true)
		    		//.setEmbeddedServerOptions(new HttpServerOptions().setPort(9090))
		    		.setEnabled(true)
	    		)
	    		.setEnabled(true);
	    options.setMetricsOptions(micrometerMetricsOptions);
    }
}
