package eu.europeana.lod.data;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

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

		// wrap the response
		EuropeanaResponse response = new EuropeanaResponse(resp);

		// try to wrap the request; if fails -> URI not supported
		EuropeanaRequest request = null;
		try {
			request = new EuropeanaRequest(req);
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Unknown URI "
					+ req.getRequestURI());
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

			String prefMimeType = request.getPreferredAcceptMimeType();
			
			String contentType = EuropeanaResponse.getResponseContentType(prefMimeType).toString();
			
			response.setContentType(contentType);
			
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

			response.writeModel(model, requestedMimeType);

		} else {

			response.sendError(HttpServletResponse.SC_NOT_FOUND);
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