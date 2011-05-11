package eu.europeana.lod.data;

import eu.europeana.lod.util.VelocityHelper;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.context.Context;

/**
 * This servlet redirects document (HTML) requests to the Europeana Portal and
 * data (RDF) request to the corresponding information resource URI
 * 
 * @author haslhofer
 * @author cesareconcordia
 * 
 */
public class ConnegServlet extends HttpServlet {

	// just some sample resources for testing
	// sample rm:
	// http://localhost:8080/rm/europeana/00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154
	// sample agg:
	// http://localhost:8080/aggregation/europeana/00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154

	// TODO: handle externally in web.xml

	public void init() {

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		EuropeanaRequest request = new EuropeanaRequest(req);

		// check if the request is valid at all
		if (!request.isValidRequest()) {
			// TODO: HTML messages should be put on template files
			send404(resp, req.getRequestURI(),
					"Unknown resource " + request.getRequestURI());
			return;
		}

		// parse the Europeana ID from the URL
		String europeanaID = request.getEuropeanaID();

		// if we cannot parse it -> no need to continue
		if (europeanaID == null) {
			// TODO: HTML messages should be put on template files
			send404(resp, req.getRequestURI(),
					"Cannot parse Europeana ID from the resource URI "
							+ request.getRequestURI());
			return;
		}

		if (request.isDocumentRequest()) {

			String targetURI = request.getHTMLInformationResource();
			resp.setStatus(303);
			resp.setContentType(request.getHeader("accept"));
			resp.addHeader("Location", targetURI);

		} else if (request.isDataRequest()) {

			String targetURI = request.getRDFInformationResource();
			resp.setStatus(303);
			resp.setContentType(request.getHeader("accept"));
			resp.addHeader("Location", targetURI);

		} else {
			// TODO: HTML messages should be put on template files
			send404(resp,
					req.getRequestURI(),
					"The requested resource does not exist on this server, or no information about it is available.");
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