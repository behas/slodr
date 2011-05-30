package eu.europeana.lod.data;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

import eu.europeana.lod.util.AcceptHeaderHandler;
import eu.europeana.lod.util.AcceptHeaderHandler.ContentType;
import eu.europeana.lod.util.AcceptHeaderHandler.MimeTypePattern;

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

	// TODO: find out how to set servlet params in tests and get rid of configs.

	protected String website = "http://version1.europeana.eu/web/lod/";

	protected String sparqlEndpoint = "http://data.mminf.univie.ac.at/sparql";
	
	protected String resourcePrefix = "http://data.europeana.eu";

	@Override
	public void init() throws ServletException {

		getServletContext().log("Initializing Europeana LOD Servlet");

		if (getServletConfig().getInitParameter("website") != null) {
			website = getServletConfig().getInitParameter("website");
		}

		if (getServletConfig().getInitParameter("sparqlEndpoint") != null) {
			sparqlEndpoint = getServletConfig().getInitParameter(
					"sparqlEndpoint");
		}

		if (getServletConfig().getInitParameter("resourcePrefix") != null) {
			resourcePrefix = getServletConfig().getInitParameter(
					"resourcePrefix");
		}

	
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// wrap the response
		EuropeanaResponse response = new EuropeanaResponse(resp);

		// try to wrap the request; if fails -> URI not supported
		EuropeanaRequest request = null;
		try {
			request = new EuropeanaRequest(req);
		} catch (ServletException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
		}

		// distinguish between document (HTML) and data (RDF) requests
		if (request.isDocumentRequest()) {
			handleDocumentRequest(request, response);
		} else if (request.isDataRequest()) {
			handleDataRequest(request, response);
		} else {
			// send a 406 Not acceptable error
			response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE,
					"Accept header "
							+ request.getHeader("Accept") + "not supported");

		}

	}

	
	/**
	 * Handles all document (HTML) requests 
	 * 
	 */
	private void handleDocumentRequest(EuropeanaRequest request, EuropeanaResponse response) {
		
		if (request.isRootRequest()) {
			// redirect to the project website
			response.setRedirectTo(website);
			response.setEuropeanaContentType(AcceptHeaderHandler.ContentType.HTML);
		} else {
			// redirect to the item page in the Europeana portal
			String targetURI = request.getDocumentInformationResource();
			response.setRedirectTo(targetURI);
			response.setEuropeanaContentType(AcceptHeaderHandler.ContentType.HTML);
		}
		
	}
	
	
	/**
	 * Handles all requests that require the delivery of RDF
	 * 
	 */
	private void handleDataRequest(EuropeanaRequest request,
			EuropeanaResponse response) throws ServletException, IOException {

		
		if (request.isRootRequest()) {
			// TODO: deliver void description
			response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
			response.setContentType("text/plain");
			response.getOutputStream().print(
					"Data requests on root URI are not supported yet.");
		} else {
			
			// set response content type (common for IR and NIR)
			MimeTypePattern prefMimeType = request.getPreferredAcceptMimeType();
			
			ContentType contentType = AcceptHeaderHandler.getContentType(prefMimeType);
			
			response.setContentType(contentType.toString());

			
			if (request.isInformationResourceRequest()) {
				// deliver data in some RDF serialization
				// Retrieve the non-information resource URI
				String resourceURI = request.getNonInformationResourceURI(this.resourcePrefix);

				// Retrieve the model
				String query = "DESCRIBE <" + resourceURI + ">";

				QueryEngineHTTP endpoint = new QueryEngineHTTP(sparqlEndpoint, query);

				Model model = endpoint.execDescribe();

				if (!model.isEmpty()) {

					response.addHeader("Vary", "Accept");

					response.writeModel(model, contentType.toString());

				} else {

					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}

			} else {
				// redirect to the information resource
				String targetURI = request.getDataInformationResource();
				response.setRedirectTo(targetURI);


			}
			
		}

	}


	private static final long serialVersionUID = 2734874416627565075L;

}