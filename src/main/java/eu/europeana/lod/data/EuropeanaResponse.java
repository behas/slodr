package eu.europeana.lod.data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

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

	
	public static Map<MimeTypePattern,ContentType> mimeTypeContentTypeMap = new HashMap<MimeTypePattern, ContentType> ();
	
	static {
		
		mimeTypeContentTypeMap.put(MimeTypePattern.RDF, ContentType.RDF);
		mimeTypeContentTypeMap.put(MimeTypePattern.TTL, ContentType.TTL);
		mimeTypeContentTypeMap.put(MimeTypePattern.N3, ContentType.N3);
		mimeTypeContentTypeMap.put(MimeTypePattern.HTML, ContentType.HTML);
		
	}
	
	
	
	/**
	 * MimeTypes returned in Europeana LOD responses
	 * 
	 * @author haslhofer
	 * 
	 */
	public enum ContentType {

		RDF("application/rdf+xml; charset=UTF-8"), HTML("text/html"), TTL("text/turtle; charset=UTF-8"), N3(
				"text/n3; charset=UTF-8");

		private String value;

		ContentType(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

	}


	/**
	 * Returns the response content-type for a given request mime-type
	 * 
	 * @param requestMimeType
	 * @return
	 */
	public static ContentType getResponseContentType(String requestMimeType) {
		
		MimeTypePattern mimeTypePattern = MimeTypePattern.getMatchingMimeType(requestMimeType);
		
		if (mimeTypePattern == null) {
			return ContentType.HTML;
		} else {
			return mimeTypeContentTypeMap.get(mimeTypePattern);
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
			writer.setProperty("allowBadURIs", "true");
			writer.write(
					model,
					new OutputStreamWriter(response.getOutputStream(), "utf-8"),
					null);
		}
	}

	

}
