package eu.europeana.lod.data;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

public class ConnegServletTest {

	private EuropeanaTester tester;

	@Before
	public void startServlet() throws Exception {
		tester = new EuropeanaTester();
		tester.setContextPath("/");
		tester.addServlet(ConnegServlet.class, "/*");
		tester.start();
	}

	@After
	public void stopServlet() throws Exception {
		tester.stop();
	}

	@Test
	public void getResourceMapDocument() throws Exception {

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
			return "[" + getStatus() + "]" + "[" + getContentType() + "]" + "["
					+ getLocation() + "]";
		}

	}

}
