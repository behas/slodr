package eu.europeana.lod.data;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import eu.europeana.lod.util.AcceptHeaderHandler;
import eu.europeana.lod.util.AcceptHeaderHandler.MimeTypePattern;

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

		MimeTypePattern preferedMimeType = handler.getPreferredMimeType();

		assertEquals(MimeTypePattern.HTML, preferedMimeType);

		handler = new AcceptHeaderHandler(null);

		preferedMimeType = handler.getPreferredMimeType();

		assertEquals(MimeTypePattern.HTML, preferedMimeType);

	}

	@Test
	public void singleMimeType() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler("text/html");

		MimeTypePattern preferedMimeType = handler.getPreferredMimeType();

		assertEquals(MimeTypePattern.HTML, preferedMimeType);

	}

	@Test
	public void mimeTypesWithQValues() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler(
				"application/rdf+xml; q=0.8, text/html; q=0.2");

		assertEquals(MimeTypePattern.RDF, handler.getPreferredMimeType());

		handler = new AcceptHeaderHandler(
				"application/rdf+xml; q=0.2, text/html; q=0.7");

		assertEquals(MimeTypePattern.HTML, handler.getPreferredMimeType());

	}

	@Test
	public void unsupportedHighestMimeType() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler(
				"image/png; q=0.8, text/html; q=0.2");

		assertEquals(MimeTypePattern.HTML, handler.getPreferredMimeType());

	}

	@Test
	public void testGoogleChromeHeaders() throws Exception {

		AcceptHeaderHandler handler = new AcceptHeaderHandler(
				"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

		assertEquals(MimeTypePattern.HTML, handler.getPreferredMimeType());

	}

	@Test
	public void testIE8Headers() throws Exception {

		String acceptHeader = "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*";

		AcceptHeaderHandler handler = new AcceptHeaderHandler(acceptHeader);
		
		assertEquals(MimeTypePattern.HTML, handler.getPreferredMimeType());

	}
	
	@Test
	public void testEqualHeaders() throws Exception {
		
		
		String acceptHeader = "text/html;level=1, application/rdf+xml; q=1, text/html, */*";
		
		AcceptHeaderHandler handler = new AcceptHeaderHandler(acceptHeader);
		
		assertEquals(MimeTypePattern.HTML, handler.getPreferredMimeType());
		
	}
	
	
}
