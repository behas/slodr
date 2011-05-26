package eu.europeana.lod.data;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * This class provides methods for constructing Europeana LOD-specific responses
 * 
 * @author haslhofer
 * 
 */
public class EuropeanaResponse extends HttpServletResponseWrapper {

	public EuropeanaResponse(HttpServletResponse response) {
		super(response);

	}
	
	
	/**
	 * Sets the response's content type
	 * 
	 * @param contentType
	 */
	public void setEuropeanaContentType(ContentType contentType) {
		setContentType(contentType.toString());
	}
	
	/**
	 * Add redirect instruction to HTTP Response
	 * 
	 * @param location
	 */
	public void setRedirectTo(String location) {
		setStatus(303);
		setHeader("Location", location);
	}
	

	/**
	 * MimeTypes returned in Europeana LOD responses
	 * 
	 * @author haslhofer
	 * 
	 */
	public enum ContentType {

		RDF("application/rdf+xml"), HTML("text/html"), TTL("text/turtle"), N3(
				"text/n3");

		private String value;

		ContentType(String value) {
			this.value = value;
		}

		public String toString() {
			return value;
		}

	}

}
