package com.nortal.xblr.metamodel.generator.store;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.xbrlapi.data.Store;
import org.xbrlapi.loader.Loader;
import org.xbrlapi.loader.LoaderImpl;
import org.xbrlapi.sax.EntityResolver;
import org.xbrlapi.sax.EntityResolverImpl;
import org.xbrlapi.utilities.XBRLException;
import org.xbrlapi.xlink.XLinkProcessor;
import org.xbrlapi.xlink.XLinkProcessorImpl;
import org.xbrlapi.xlink.handler.XBRLCustomLinkRecogniserImpl;
import org.xbrlapi.xlink.handler.XBRLXLinkHandlerImpl;

import com.google.common.io.Files;

public class StoreLocal extends ThreadLocal<Store> {

	private static final Logger logger = Logger.getLogger(StoreLocal.class);

	private final String entryPoint;
	private final File cacheRoot = Files.createTempDir();
	private final Map<Store, AtomicInteger> referenceCounts = new HashMap<>();

	public StoreLocal(String entryPoint) {
		this.entryPoint = entryPoint;
	}

	@Override
	protected Store initialValue() {
		long startTime = System.currentTimeMillis();
		XBRLXLinkHandlerImpl xlinkHandler = new XBRLXLinkHandlerImpl();
		XBRLCustomLinkRecogniserImpl clr = new XBRLCustomLinkRecogniserImpl();
		XLinkProcessor xlinkProcessor = new XLinkProcessorImpl(xlinkHandler, clr);

		try {
			HashMap<URI, URI> uriMap = new HashMap<>();
			uriMap.put(URI.create("http://www.xbrlapi.org/xml/schemas/s4s.xsd"), getClass().getClassLoader().getResource("s4s.xsd").toURI());

			EntityResolver entityResolver = new EntityResolverImpl(cacheRoot, uriMap);
			Store store = new org.xbrlapi.data.dom.StoreImpl();
			Loader loader = new LoaderImpl(store, xlinkProcessor, entityResolver);
			loader.setEntityResolver(entityResolver);
			xlinkHandler.setLoader(loader);
			loader.discover(getUri(entryPoint));
			logger.info("Store initialized in " + (System.currentTimeMillis() - startTime) / 1000 + " seconds");
			return loader.getStore();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (XBRLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Store get() {
		Store store = super.get();
		synchronized (referenceCounts) {
			AtomicInteger referenceCount = referenceCounts.get(store);
			if (referenceCount == null) {
				referenceCount = new AtomicInteger(0);
				referenceCounts.put(store, referenceCount);
			}
			referenceCount.incrementAndGet();
		}
		return store;
	}

	public void release() {
		Store store = super.get();
		synchronized (referenceCounts) {
			AtomicInteger referenceCount = referenceCounts.get(store);
			if (referenceCount == null) {
				return;
			}
			if (referenceCount.decrementAndGet() == 0) {
				try {
					store.close();
				}
				catch (XBRLException ignored) {
				}
			}
		}
	}

	private String getUri(String filename) {
		try {
			return "file:///" + new File(filename).getCanonicalFile().toURI().getRawPath();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
