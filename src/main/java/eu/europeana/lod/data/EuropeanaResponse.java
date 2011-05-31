package eu.europeana.lod.data;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import eu.europeana.lod.util.AcceptHeaderHandler.ContentType;
import eu.europeana.lod.util.AcceptHeaderHandler.MimeTypePattern;

/**
 * This class provides methods for sending Europeana LOD responses. It supports
 * two types of responses:
 * 
 * - redirects to other resources (using a certain HTTP content-type field) -
 * RDF data responsed serialized in the format requested by the HTTP accept
 * header
 * 
 * @author haslhofer
 * 
 */
public class EuropeanaResponse extends HttpServletResponseWrapper {

	/**
	 * Creates a new Europeana response
	 */
	public EuropeanaResponse(HttpServletResponse response) {
		super(response);

	}

	/**
	 * Sends a redirect to a given resource location, having a certain content
	 * type
	 */
	public void sendRedirectTo(String location, ContentType contentType) {
		setStatus(303);
		setHeader("Location", location);
		setContentType(contentType.toString());

	}

	/**
	 * Sends RDF data about a given resource, retrieved from a given SPARQL
	 * endpoint, serialized according to a given content-type
	 */
	public void sendData(String resourceURI, String sparqlEndpoint,
			ContentType contentType) throws IOException {

		setContentType(contentType.toString());

		Model model = retrieveModel(resourceURI, sparqlEndpoint);

		if (model.isEmpty()) {
			sendError(HttpServletResponse.SC_NOT_FOUND, "Could not retrieve "
					+ resourceURI);
			return;
		}

		addHeader("Vary", "Accept");

		writeModel(model, contentType.toString());

	}

	/* RDF writing stuff */

	/**
	 * Retrieve a model for a given resource from a given SPARQL endpoint and
	 * make sure that all BNode descriptions are included
	 */
	private Model retrieveModel(String resourceURI, String sparqlEndpoint) {

		// Retrieve the model
		String query = "DESCRIBE <" + resourceURI + ">";

		QueryEngineHTTP endpoint = new QueryEngineHTTP(sparqlEndpoint, query);

		Model model = endpoint.execDescribe();

		// A temporary model for possible BNode descriptions
		Model bNodeDescriptions = ModelFactory.createDefaultModel();
		
		// A list of statements having BNodes as values
		List<Statement> bNodeStatements = new ArrayList<Statement>();
		
		// Retrieve the models for all possible blank node values
		StmtIterator stmtIter = model.listStatements();
		while (stmtIter.hasNext()) {
			Statement stmt = stmtIter.next();
			if (stmt.getObject().isAnon()) {
				Resource subject = stmt.getSubject();
				Property property = stmt.getPredicate();
				Model bNodeDescription = retrieveBNodeDescription(subject,
						property, sparqlEndpoint);
				
				// add BNode description to return model
				bNodeDescriptions.add(bNodeDescription);
				
				// store statement for later deletion
				bNodeStatements.add(stmt);

			}
		}
		
		if (! bNodeDescriptions.isEmpty()) {
			model.remove(bNodeStatements);
			model.add(bNodeDescriptions);
		}

		return model;

	}

	/**
	 * Returns the description for a bnode which is the object in a triple
	 * having the given resourceURI as subject and the given propertURI
	 */
	private Model retrieveBNodeDescription(Resource subject, Property property,
			String sparqlEndpoint) {

		String query = "DESCRIBE ?x WHERE { <" + subject.getURI() + "> <"
				+ property.getURI() + "> ?x . FILTER (isBlank(?x)) }";

		QueryEngineHTTP endpoint = new QueryEngineHTTP(sparqlEndpoint, query);

		Model model = endpoint.execDescribe();

		return model;

	}

	private void writeModel(Model model, String requestedMimeType)
			throws IOException {

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
