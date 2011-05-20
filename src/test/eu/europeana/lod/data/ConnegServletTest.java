package eu.europeana.lod.data;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

public class ConnegServletTest {

	public static String HOST = "data.europeana.eu";
	
	public static String SAMPLE_OBJECT_ID = "00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154";
	
	public static String SAMPLE_RM_REQUEST = "/rm/europeana/" + SAMPLE_OBJECT_ID;
	
	public static String SAMPLE_HTML_RESPONSE = "http://www.europeana.eu/portal/record/" + SAMPLE_OBJECT_ID + ".html";
	
	
	private ServletTester tester;
	
	@Before
	public void startServlet() throws Exception {
		tester = new ServletTester();
		tester.setContextPath("/");
		tester.addServlet(ConnegServlet.class, "/*");
		tester.start();
	}
	
	@After
	public void stopServlet() throws Exception {
		tester.stop();
	}
	
	
	@Test
	public void doGetNonInformationResource() throws Exception{
	
		// test with no mime-type
		
		// test with HTML mime-type
		
		// test with RDF mime-types
		
		HttpTester request = new HttpTester();
		request.setMethod("GET");
		request.setHeader("Host", HOST);
		request.setURI(SAMPLE_RM_REQUEST);
		request.setVersion("HTTP/1.1");
		
		HttpTester response = new HttpTester();
		response.parse(tester.getResponses(request.generate()));
		
		System.out.println(response.getHeader("Location"));
		
		assertEquals(303, response.getStatus());
		assertEquals(SAMPLE_HTML_RESPONSE, response.getHeader("Location"));
		
	}
	
	
	
	
	
}
