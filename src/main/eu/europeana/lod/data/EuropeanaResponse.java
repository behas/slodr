package eu.europeana.lod.data;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;

import eu.europeana.lod.util.AcceptHeaderHandler.MimeTypePattern;

/**
 * This class provides methods for constructing Europeana LOD-specific responses
 * 
 * @author haslhofer
 * 
 */
public class EuropeanaResponse extends HttpServletResponseWrapper {

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

	
	
	/**
	 * Creates a new Europeana response
	 * 
	 * @param response
	 */
	public EuropeanaResponse(HttpServletResponse response) {
		super(response);

	}
	
	
	/**
	 * Sets the response's content type
	 * 
	 * @param contentType
	 */
	protected void setEuropeanaContentType(ContentType contentType) {
		setContentType(contentType.toString());
	}
	
	/**
	 * Add redirect instruction to HTTP Response
	 * 
	 * @param location
	 */
	protected void setRedirectTo(String location) {
		setStatus(303);
		setHeader("Location", location);
	}
	

	
	protected void writeModel(Model model, String requestedMimeType) throws IOException{
		
		getWriter(requestedMimeType).write(model, this);

		getOutputStream().flush();

		
	}
	
	/*
	 * The following code parts are taken from pubby
	 * 
	 * https://github.com/cygri/pubby/
	 */

	private ModelWriter getWriter(String mediaType) {

		if (MimeTypePattern.matchMIMEType(mediaType, MimeTypePattern.RDF))
			return new RDFXMLWriter();
		if (MimeTypePattern.matchMIMEType(mediaType, MimeTypePattern.TTL))
			return new TurtleWriter();
		if (MimeTypePattern.matchMIMEType(mediaType, MimeTypePattern.N3))
			return new NTriplesWriter();

		return new NTriplesWriter();
	}

	private interface ModelWriter {
		void write(Model model, HttpServletResponse response)
				throws IOException;
	}

	private class NTriplesWriter implements ModelWriter {
		public void write(Model model, HttpServletResponse response)
				throws IOException {
			model.getWriter("N-TRIPLES").write(model,
					response.getOutputStream(), null);
		}
	}

	private class TurtleWriter implements ModelWriter {
		public void write(Model model, HttpServletResponse response)
				throws IOException {
			model.getWriter("TURTLE").write(model, response.getOutputStream(),
					null);
		}
	}

	private class RDFXMLWriter implements ModelWriter {
		public void write(Model model, HttpServletResponse response)
				throws IOException {
			RDFWriter writer = model.getWriter("RDF/XML-ABBREV");
			writer.setProperty("showXmlDeclaration", "true");
			writer.setProperty("blockRules", "propertyAttr");
			writer.write(
					model,
					new OutputStreamWriter(response.getOutputStream(), "utf-8"),
					null);
		}
	}

	

}
