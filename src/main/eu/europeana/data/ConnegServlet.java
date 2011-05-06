package eu.europeana.data;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet handles the 
 * 
 * @author haslhofer
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
			// TODO: return 404
		}

		// parse the Europeana ID from the URL
		String europeanaID = request.getEuropeanaID();

		// if we cannot parse it -> no need to continue
		if (europeanaID == null) {
			// TODO: return 404
		}

		
		if (request.isDocumentRequest()) {

			System.out.println("Received Document Request");
			
			String targetURI = request.getHTMLInformationResource();
			resp.setStatus(303);
			resp.setContentType(request.getHeader("accept"));
			resp.addHeader("Location", targetURI);

		} else if (request.isDataRequest()) {

			System.out.println("Received Data Request");
			
			String targetURI = request.getRDFInformationResource();
			resp.setStatus(303);
			resp.setContentType(request.getHeader("accept"));
			resp.addHeader("Location", targetURI);
			
			
		} else {
			// TODO: whatever else came in here, return 404
		}

	}

	private static final long serialVersionUID = 2734874416627565075L;

}