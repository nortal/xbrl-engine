package com.nortal.xblr.metamodel.generator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import com.nortal.xbrl.metamodel.util.MetamodelUtil;

public class App implements Runnable {

	private static final Logger logger = Logger.getLogger(App.class);
	private static final String DEFAULT_ENTRY_POINT = "/schema/schema.xsd";
	private static final String DEFAULT_OUTPUT_PATH = "metamodel."
			+ new SimpleDateFormat("yyMMdd.HHmmss").format(new Date()) + ".xml";
	private static final boolean BUILD_LABELS_AND_RESOURCES = true;

	private final String entryPoint;
	private final String outputPath;

	public App(String entryPoint, String outputPath) {
		this.entryPoint = entryPoint;
		this.outputPath = outputPath;
	}

	@Override
	public void run() {
		MetamodelBuilder builder = new MetamodelBuilder(entryPoint, BUILD_LABELS_AND_RESOURCES);
		XbrlMetamodel metamodel;
		try {
			metamodel = builder.build();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			builder.dispose();
		}
		try (FileOutputStream fos = new FileOutputStream(new File(outputPath))) {
			fos.write(((ByteArrayOutputStream) MetamodelUtil.serialize(metamodel)).toByteArray());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		String entryPoint = (args.length > 0) ? args[0] : DEFAULT_ENTRY_POINT;
		String outputPath = (args.length > 1) ? args[1] : DEFAULT_OUTPUT_PATH;
		logger.info("Application started, entry point " + entryPoint + " output path " + outputPath);
		App runner = new App(entryPoint, outputPath);
		try {
			runner.run();
			logger.info("Application was successful.");
		}
		catch (RuntimeException e) {
			logger.error(e);
			e.printStackTrace();
		}
		logger.info("Application finished in (secs): " + (System.currentTimeMillis() - startTime) / 1000);
	}

}
