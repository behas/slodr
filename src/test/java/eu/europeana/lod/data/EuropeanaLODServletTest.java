package eu.europeana.lod.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

import eu.europeana.lod.data.EuropeanaRequest.ResourceType;
import eu.europeana.lod.util.AcceptHeaderHandler;
import eu.europeana.lod.util.AcceptHeaderHandler.ContentType;

/**
 * This class tests and verifies the functionality of the EuropeanaLOD Servlet.
 * 
 * @author haslhofer
 * 
 */
public class EuropeanaLODServletTest {

	private EuropeanaTester tester;

	@Before
	public void startServlet() throws Exception {

		tester = new EuropeanaTester();
		tester.setContextPath("/");
		tester.addServlet(EuropeanaLODServlet.class, "/*");

		tester.start();
	}

	@After
	public void stopServlet() throws Exception {
		tester.stop();
	}

	/* The following test-cases reflect real-world requests and responses */

	/**
	 * Bug report Antoine 26.05 / URI serialization problem in RDF/XML output
	 */
	@Test
	public void dataRequstEncodingProblemTest() throws Exception {

		// http://data.europeana.eu/aggregation/europeana/92037/25F9104787668C4B5148BE8E5AB8DBEF5BE5FE03

		ResourceType resourceType = ResourceType.AGGREGATION_EUROPEANA;
		String itemID = "92037/25F9104787668C4B5148BE8E5AB8DBEF5BE5FE03";
		String acceptHeader = "application/rdf+xml";

		executeIRDataRequest(resourceType, itemID, acceptHeader);

	}

	/**
	 * Bug report Antoine 24.05 / qValue problem
	 */
	@Test
	public void simpleQValueTest() throws Exception {

		// http://data.europeana.eu/rm/europeana/00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154

		ResourceType resourceType = ResourceType.RM;
		String itemID = "00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154";
		String acceptHeader = "application/rdf+xml; q=0.8, text/html; q=0.2";

		executeNIRDataRequest(resourceType, itemID, acceptHeader);

	}

	/**
	 * A simple demo test
	 */
	@Test
	public void simpleRequestResponseTest() throws Exception {

		EuropeanaTestRequest request = new EuropeanaTestRequest();
		request.setHTTPRequestURI("http://data.europeana.eu/rm/europeana/00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154");
		request.setAccept("text/html");

		EuropeanaTestResponse expected_response = new EuropeanaTestResponse();
		expected_response.setStatus(303);
		expected_response.setContentType("text/html");
		expected_response
				.setLocation("http://www.europeana.eu/portal/record/00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154.html");

		EuropeanaTestResponse response = tester.getEuropeanaResponse(request);

		assertEquals(expected_response, response);

	}

	@Test
	public void testRootDocumentRequest() throws Exception {

		EuropeanaTestRequest request = new EuropeanaTestRequest();
		request.setHTTPRequestURI("http://data.europeana.eu/");
		request.setAccept("text/html");

		EuropeanaTestResponse expected_response = new EuropeanaTestResponse();
		expected_response.setStatus(303);
		expected_response.setContentType("text/html");
		expected_response.setLocation("http://version1.europeana.eu/web/lod/");

		EuropeanaTestResponse response = tester.getEuropeanaResponse(request);

		assertEquals(expected_response, response);

	}

	@Test
	public void testRootDataRequest() throws Exception {

		EuropeanaTestRequest request = new EuropeanaTestRequest();
		request.setHTTPRequestURI("http://data.europeana.eu/");
		request.setAccept("text/turtle");

		EuropeanaTestResponse expected_response = new EuropeanaTestResponse();
		expected_response.setStatus(303);
		expected_response.setContentType(ContentType.TTL.toString());

		EuropeanaTestResponse response = tester.getEuropeanaResponse(request);

		assertEquals(expected_response, response);

	}

	/*
	 * The following tests were derived from real-world requests and responses
	 * and should verify the servlet's functionality
	 */

	// the itemIDs to be tested
	private String[] itemIDs = new String[] { "00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154" };

	// the resource types to be tested
	private EuropeanaRequest.ResourceType[] resourceTypes = new EuropeanaRequest.ResourceType[] {
			ResourceType.AGGREGATION_EUROPEANA,
			ResourceType.AGGREGATION_PROVIDER, ResourceType.ITEM,
			ResourceType.RM, ResourceType.PROXY_EUROPEANA,
			ResourceType.PROXY_PROVIDER };

	// the accept headers for document requests to be tested
	private String[] documentAcceptHeaders = new String[] {
			"text/html", // the simplest case
			"application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5", // Google
																											// Chrome
			"image/gif, image/jpeg, "
					+ "image/pjpeg, image/pjpeg, application/x-shockwave-flash, "
					+ "application/xaml+xml, application/vnd.ms-xpsdocument, "
					+ "application/x-ms-xbap, application/x-ms-application, "
					+ "application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*" // IE
																											// 8
	};

	// the accept headers for data requests to be tested
	private String[] dataAcceptHeaders = new String[] { "application/rdf+xml" };

	private static Map<EuropeanaRequest.ResourceType, String> resourceTypeRDFMapping = new HashMap<EuropeanaRequest.ResourceType, String>();

	static {
		resourceTypeRDFMapping.put(ResourceType.AGGREGATION_EUROPEANA,
				"http://www.europeana.eu/schemas/edm/EuropeanaAggregation");
		resourceTypeRDFMapping.put(ResourceType.PROXY_EUROPEANA,
				"http://www.openarchives.org/ore/terms/Proxy");
		resourceTypeRDFMapping.put(ResourceType.AGGREGATION_PROVIDER,
				"http://www.openarchives.org/ore/terms/Aggregation");
		resourceTypeRDFMapping.put(ResourceType.PROXY_PROVIDER,
				"http://www.openarchives.org/ore/terms/Proxy");
		resourceTypeRDFMapping.put(ResourceType.RM,
				"http://www.openarchives.org/ore/terms/ResourceMap");
		resourceTypeRDFMapping.put(ResourceType.ITEM, null);

	}

	/**
	 * Tests non-information document requests for all combinations of itemIDs,
	 * resourceTypes, and document accept headers
	 */
	@Test
	public void testDocumentRequest() throws Exception {

		for (String itemID : itemIDs) {

			for (ResourceType resourceType : resourceTypes) {

				for (String acceptHeader : documentAcceptHeaders) {
					executeDocumentRequest(resourceType, itemID, acceptHeader);
				}

			}

		}

	}

	/**
	 * Tests non-information data requests for all combinations of itemIDs,
	 * resourceTypes, and data accept headers
	 */
	@Test
	public void testNonInformationDataRequest() throws Exception {

		for (String itemID : itemIDs) {

			for (ResourceType resourceType : resourceTypes) {

				for (String acceptHeader : dataAcceptHeaders) {
					executeNIRDataRequest(resourceType, itemID, acceptHeader);
				}

			}

		}

	}

	/**
	 * Tests information data requests for all combinations of itemIDs,
	 * resourceTypes, and data accept headers
	 */
	@Test
	public void testInformationDataRequest() throws Exception {

		for (String itemID : itemIDs) {

			for (ResourceType resourceType : resourceTypes) {

				for (String acceptHeader : dataAcceptHeaders) {
					executeIRDataRequest(resourceType, itemID, acceptHeader);
				}

			}

		}

	}

	/**
	 * Executes and verifies non-information resource document requests for any
	 * combination of resourceType, itemID, and document accept-header
	 */
	private void executeDocumentRequest(ResourceType resourceType,
			String itemID, String acceptHeader) throws Exception {

		String httpRequestURI = "http://data.europeana.eu" + resourceType
				+ itemID;

		EuropeanaTestRequest request = new EuropeanaTestRequest();
		request.setHTTPRequestURI(httpRequestURI);
		request.setAccept(acceptHeader);

		EuropeanaTestResponse expected_response = new EuropeanaTestResponse();
		expected_response.setStatus(303);
		expected_response.setContentType(AcceptHeaderHandler.ContentType.HTML
				.toString());
		expected_response.setLocation("http://www.europeana.eu/portal/record/"
				+ itemID + ".html");

		EuropeanaTestResponse response = tester.getEuropeanaResponse(request);

		assertEquals("NIR document request: " + httpRequestURI,
				expected_response, response);

	}

	/**
	 * Executes and verifies non-information resource data request for any
	 * combination of resourceType, itemID, and data accept-header
	 */
	private void executeNIRDataRequest(ResourceType resourceType,
			String itemID, String acceptHeader) throws Exception {

		String httpRequestURI = "http://data.europeana.eu" + resourceType
				+ itemID;

		EuropeanaTestRequest request = new EuropeanaTestRequest();
		request.setHTTPRequestURI(httpRequestURI);
		request.setAccept(acceptHeader);

		ContentType responseContentType = AcceptHeaderHandler.getContentType(acceptHeader);
		
		EuropeanaTestResponse expected_response = new EuropeanaTestResponse();
		expected_response.setStatus(303);
		expected_response.setContentType(responseContentType.toString());
		expected_response.setLocation("http://data.europeana.eu/data"
				+ resourceType + itemID);

		EuropeanaTestResponse response = tester.getEuropeanaResponse(request);

		assertEquals("NIR data request: " + httpRequestURI, expected_response,
				response);

	}

	/**
	 * Executes and verifies information resource data requests for any
	 * combination of resourceType, itemID, and data accept-header
	 */
	private void executeIRDataRequest(ResourceType resourceType, String itemID,
			String acceptHeader) throws Exception {

		String httpRequestURI = "http://data.europeana.eu" + "/data"
				+ resourceType + itemID;

		EuropeanaTestRequest request = new EuropeanaTestRequest();
		request.setHTTPRequestURI(httpRequestURI);
		request.setAccept(acceptHeader);

		EuropeanaTestResponse expected_response = new EuropeanaTestResponse();
		expected_response.setStatus(200);
		expected_response.setContentType(acceptHeader + "; charset=UTF-8");

		EuropeanaTestResponse response = tester.getEuropeanaResponse(request);

		// this checks just the headers
		assertEquals("IR data request: " + httpRequestURI, expected_response,
				response);

		// now check the body
		String content = response.getContent();

		String nirURI = "http://data.europeana.eu" + resourceType
				+ itemID;

		checkRDFContent(content, nirURI, resourceType);

	}

	/**
	 * Verfies that a given content can be parsed into an RDF model, that it
	 * contains at least some (> 0) triples and exactly one triple
	 * 
	 * [uri][rdf:type][type]
	 * 
	 * @param uri
	 * @param type
	 * @return
	 */
	private void checkRDFContent(String content, String uri,
			ResourceType resourceType) {

		Reader reader = new StringReader(content);

		Model model = ModelFactory.createDefaultModel();
		model.read(reader, "");

		assertTrue("The returned model contains some triples", model.size() > 0);

		String rdfType = resourceTypeRDFMapping.get(resourceType);

		if (rdfType != null) {

			Statement typeStatement = ResourceFactory.createStatement(
					ResourceFactory.createResource(uri), RDF.type,
					ResourceFactory.createResource(rdfType));

			assertTrue(
					"The returned model contains the correct type statement for "
							+ resourceType, model.contains(typeStatement));

		}

	}

	/**
	 * Europeana Request / Response mock tester
	 * 
	 * @author haslhofer
	 * 
	 */
	private class EuropeanaTester extends ServletTester {

		public EuropeanaTester() {
			super();
		}

		public EuropeanaTestResponse getEuropeanaResponse(
				EuropeanaTestRequest request) {

			EuropeanaTestResponse response = new EuropeanaTestResponse();

			try {
				response.parse(super.getResponses(request.generate()));
			} catch (Exception e) {
				throw new RuntimeException("Could not parse request");
			}

			return response;

		}

	}

	/**
	 * A simple wrapper for mock Europeana Requests
	 * 
	 * @ haslhofer
	 */
	private class EuropeanaTestRequest extends HttpTester {

		public EuropeanaTestRequest() {
			super();
			super.setMethod("GET");
			super.setVersion("HTTP/1.1");
		}

		public void setHTTPRequestURI(String httpRequestURI) {

			URI request = null;
			try {
				request = new URI(httpRequestURI);
			} catch (URISyntaxException e) {
				throw new RuntimeException("Cannot parse HTTP Request "
						+ httpRequestURI);
			}

			super.setHeader("Host", request.getHost());
			super.setURI(request.getPath());

		}

		public void setAccept(String acceptHeader) {
			setHeader("Accept", acceptHeader);
		}

		@Override
		public String toString() {
			try {
				return super.generate();
			} catch (IOException e) {
				throw new RuntimeException("Cannot serialize request to string");
			}
		}

	}

	/**
	 * A simple wrapper for mock Europeana Responses
	 * 
	 * @author haslhofer
	 * 
	 */
	private class EuropeanaTestResponse extends HttpTester {

		public EuropeanaTestResponse() {
			super();
		}

		public void setContentType(String contentType) {
			super.setHeader("Content-Type", contentType);
		}

		public String getContentType() {
			return super.getHeader("Content-Type");
		}

		public void setLocation(String location) {
			super.setHeader("Location", location);
		}

		public String getLocation() {
			return super.getHeader("Location");
		}

		@Override
		public boolean equals(Object o) {

			if (!(o instanceof EuropeanaTestResponse)) {
				return false;
			}

			EuropeanaTestResponse other = (EuropeanaTestResponse) o;

			// check status equality
			if (!(this.getStatus() == other.getStatus())) {
				return false;
			}

			// check content-type equality
			if (this.getContentType() != null) {

				if (other.getContentType() == null) {
					return false;
				}

				if (!this.getContentType().equals(other.getContentType())) {
					return false;
				}

			}

			// check location equality
			if (this.getLocation() != null) {

				if (other.getLocation() == null) {
					return false;
				}

				if (!this.getLocation().equals(other.getLocation())) {
					return false;
				}
			}

			return true;

		}

		@Override
		public String toString() {
			return "\n[" + getStatus() + "]" + "\n[" + getContentType() + "]"
					+ "\n[" + getLocation() + "]";
		}

	}
}
