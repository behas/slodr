package eu.europeana.lod.data;

import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import eu.europeana.lod.data.EuropeanaRequest.MimeTypePattern;
import eu.europeana.lod.util.VelocityHelper;

/**
 * This servlet handles all requests received by the Europeana Linked Data
 * Pilot.
 * 
 * It forwards HTML request on EDM resources to the Europeana portal and answers
 * RDF request using a remote SPARQL endpoint.
 * 
 * @author haslhofer
 * @author cesareconcordia
 * 
 */
public class EuropeanaLODServlet extends HttpServlet {

	private String DOC_WEB_PAGE = "http://version1.europeana.eu/web/lod/";

	private String SPARQL_ENDPOINT = "http://data.mminf.univie.ac.at/sparql";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// wrap the request and response into Europeana-specific request classes
		EuropeanaRequest request = new EuropeanaRequest(req);

		EuropeanaResponse response = new EuropeanaResponse(resp);

		// check if the request is valid at all
		if (!request.isValidRequest()) {
			send404(resp, req.getRequestURI(), "This is not a valid request!");
			return;
		}

		if (req.getRequestURI().startsWith("/data")) {

			handleDataRequest(request, response);
			return;
		}

		// if request targets root...
		if (request.isRootRequest()) {

			// redirect to documentation page if document request
			if (request.isDocumentRequest()) {

				response.setRedirectTo(DOC_WEB_PAGE);
				response.setEuropeanaContentType(EuropeanaResponse.ContentType.HTML);

				// deliver void description if data request
			} else if (request.isDataRequest()) {

				// TODO: deliver void description
				resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
				resp.setContentType("text/plain");
				resp.getOutputStream().print(
						"Data requests on root URI are not supported yet.");
			}

			return;

		}

		// parse the Europeana ID from the URL
		String europeanaID = request.getEuropeanaID();

		// if we cannot parse it -> no need to continue
		if (europeanaID == null) {
			// TODO: HTML messages should be put on template files
			send404(resp, req.getRequestURI(), "The Europeana ID is null!");

			return;
		}

		if (request.isDocumentRequest()) {

			String targetURI = request.getHTMLInformationResource();
			response.setRedirectTo(targetURI);
			response.setEuropeanaContentType(EuropeanaResponse.ContentType.HTML);

			return;

		} else if (request.isDataRequest()) {

			String targetURI = request.getRDFInformationResource();
			response.setRedirectTo(targetURI);

			resp.setContentType(request.getPreferredAcceptMimeType());

			return;

		} else {
			// TODO: HTML messages should be put on template files
			send404(resp,
					req.getRequestURI(),
					"The requested resource does not exist on this server, or no information about it is available.");
		}

	}

	/**
	 * Handles all requests that require the delivery of RDF
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	private void handleDataRequest(EuropeanaRequest request,
			EuropeanaResponse response) throws ServletException, IOException {

		// Retrieve the non-information resource URI
		String resourceURI = request.getNonInformationResourceURI();

		// Retrieve the model
		String query = "DESCRIBE <" + resourceURI + ">";

		QueryEngineHTTP endpoint = new QueryEngineHTTP(SPARQL_ENDPOINT, query);

		Model model = endpoint.execDescribe();


		if (!model.isEmpty()) {

			response.addHeader("Vary", "Accept");

			String requestedMimeType = request.getPreferredAcceptMimeType();

			response.setContentType(requestedMimeType + "; charset=UTF-8");

			getWriter(requestedMimeType).write(model, response);

			response.getOutputStream().flush();

		} else {

			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

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

	protected void send404(HttpServletResponse resp, String resourceURI,
			String msg) throws IOException {
		resp.setStatus(404);
		VelocityHelper template = new VelocityHelper(getServletContext(), resp);
		Context context = template.getVelocityContext();
		context.put("project_name", "Europeana LOD pilot");
		context.put("title", "404 Not Found");
		context.put("msg", msg);
		if (resourceURI != null) {
			context.put("uri", resourceURI);
		}
		template.renderXHTML("404.vm");
	}

	private static final long serialVersionUID = 2734874416627565075L;

}