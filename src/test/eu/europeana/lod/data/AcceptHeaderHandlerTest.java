package eu.europeana.lod.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.europeana.lod.util.AcceptHeaderHandler;

/**
 * This class tests the functionality of the Accept-Header mime-type parser
 * 
 * @author haslhofer
 * 
 */
public class AcceptHeaderHandlerTest {

	@Test
	public void testNullandEmpty() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler("");

		String preferedMimeType = handler.getPreferredMimeType();

		assertEquals("text/html", preferedMimeType);

		handler = new AcceptHeaderHandler(null);

		preferedMimeType = handler.getPreferredMimeType();

		assertEquals("text/html", preferedMimeType);

	}

	@Test
	public void singleMimeType() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler("text/html");

		String preferedMimeType = handler.getPreferredMimeType();

		assertEquals("text/html", preferedMimeType);

	}

	@Test
	public void mimeTypesWithQValues() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler(
				"application/rdf+xml; q=0.8, text/html; q=0.2");

		assertEquals("application/rdf+xml", handler.getPreferredMimeType());

		handler = new AcceptHeaderHandler(
				"application/rdf+xml; q=0.2, text/html; q=0.7");

		assertEquals("text/html", handler.getPreferredMimeType());

	}

	@Test
	public void unsupportedHighestMimeType() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler(
				"image/png; q=0.8, text/html; q=0.2");

		assertEquals("text/html", handler.getPreferredMimeType());

	}

	@Test
	public void testGoogleChromeHeaders() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler(
				"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

		System.out.println(handler);

		assertEquals("application/xml", handler.getPreferredMimeType());

	}

}