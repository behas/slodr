package eu.europeana.lod.data;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import eu.europeana.lod.util.AcceptHeaderHandler;
import eu.europeana.lod.util.AcceptHeaderHandler.MimeTypePattern;

/**
 * This class encapsulates the logic of the Europeana Linked Data URI design and
 * provides access methods to request information.
 * 
 * Depending on the mime-type a request can ask for data (RDF, TTL, N3) or
 * documents (HTML)
 * 
 * Depending on the URI path a request can ask for a non-information resource or
 * an information resource (/data)
 * 
 * If a request has no URI path it is considered a root request
 * 
 * @author haslhofer
 * @author cesareconcordia
 * 
 */
public class EuropeanaRequest extends HttpServletRequestWrapper {

	/* CONSTANTS */

	public static String IR_PATH = "/data";

	public static final String EUROPEANA_HTML_BASE_URL = "http://www.europeana.eu/portal/record/";

	public static enum ResourceType {

		PROXY_PROVIDER("/proxy/provider/"),
		PROXY_EUROPEANA("/proxy/europeana/"),
		AGGREGATION_PROVIDER("/aggregation/provider/"),
		AGGREGATION_EUROPEANA("/aggregation/europeana/"),
		RM("/rm/europeana/"),
		ITEM("/item/");

		private String pathPrefix;

		private ResourceType(String pathPrefix) {
			this.pathPrefix = pathPrefix;
		}

		/**
		 * Returns the matching resource type for a given request URI
		 * 
		 * Returns null, if there is no matching resource type
		 * 
		 */
		public static ResourceType getResourceType(String requestURI) {

			for (ResourceType resourceType : values()) {

				if (requestURI.startsWith(resourceType.toString())) {
					return resourceType;
				}

			}

			return null;
		}

		@Override
		public String toString() {
			return pathPrefix;
		}

	}

	/* Info parsed from request */

	private AcceptHeaderHandler acceptHandler;

	private String host;

	private ResourceType resourceType;

	private String europeanaID;

	private boolean rootRequest = false;

	private boolean informationResourceRequest = false;

	public EuropeanaRequest(HttpServletRequest request) throws ServletException {
		super(request);

		// parse Accept Header Field
		String acceptHeader = getHeader("accept");
		this.acceptHandler = new AcceptHeaderHandler(acceptHeader);

		// retrieve request host
		this.host = getServerName();

		// parse Europeana URI path
		if (getRequestURI() == null || getRequestURI().equalsIgnoreCase("/")) {
			this.rootRequest = true;
		} else {
			parseRequestURI(getRequestURI());
		}

	}

	/**
	 * Parses info from the request by sequentially chopping of requestURI paths
	 */
	private void parseRequestURI(String requestURI) throws ServletException {

		// check whether the request asks for an information or non-information
		// resource
		if (requestURI.startsWith(IR_PATH)) {
			this.informationResourceRequest = true;
			requestURI = requestURI.substring(IR_PATH.length(),
					requestURI.length());
		}

		// determine resource type
		ResourceType resourceType = ResourceType.getResourceType(requestURI);
		if (resourceType == null) {
			throw new ServletException("Invalid resource type in request URI "
					+ getRequestURI());
		}
		this.resourceType = resourceType;
		requestURI = requestURI.substring(resourceType.toString().length(),
				requestURI.length());

		// determine europeanaID -> the remainder of the request URI
		if (requestURI.length() == 0) {
			throw new ServletException("Invalid europeanaID in request URI "
					+ getRequestURI());
		}
		this.europeanaID = requestURI;

	}

	/**
	 * Returns true if the URI path is empty -> it is a root request
	 */
	public boolean isRootRequest() {

		return this.rootRequest;
	}

	/**
	 * Returns whether or not a request asks for an information resource
	 */
	public boolean isInformationResourceRequest() {

		return this.informationResourceRequest;
	}

	
	/**
	 * Returns the request's europeanaID
	 */
	public String getEuropeanaID() {
		return this.europeanaID;
	}
	
	
	/**
	 * Returns the preferred mime-type given in the HTTP Accept header field
	 * 
	 * @return
	 */
	public String getPreferredAcceptMimeType() {

		return acceptHandler.getPreferredMimeType();

	}

	/**
	 * Returns whether or not the request asks for a human-readable resource
	 * representation
	 * 
	 * @return true if human-readable
	 */
	public boolean isDocumentRequest() {

		String mimeType = acceptHandler.getPreferredMimeType();

		boolean documentRequest = MimeTypePattern.matchMIMEType(mimeType,
				MimeTypePattern.HTML);

		return documentRequest;
	}

	/**
	 * Returns whether or not the request asks for a machine-readable resource
	 * representation
	 * 
	 * @return true if machine-readable
	 */
	public boolean isDataRequest() {

		String mimeType = acceptHandler.getPreferredMimeType();

		boolean dataRequest = MimeTypePattern.matchMIMEType(mimeType,
				MimeTypePattern.RDF, MimeTypePattern.TTL, MimeTypePattern.N3);

		return dataRequest;
	}

	/**
	 * Returns the requested resource type (europeana/provider
	 * proxy/aggregation/item/rm)
	 * 
	 * @return
	 */
	public ResourceType getResourceType() {

		return this.resourceType;

	}

	/**
	 * Returns the non-information resource URI for a given information resource
	 * URI
	 * 
	 * e.g.,
	 * 
	 * http://data.europeana.eu/data/rm/europeana/00000/
	 * E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154 -->
	 * http://data.europeana.eu/rm/europeana
	 * /00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154
	 * 
	 * @return
	 */
	public String getNonInformationResourceURI() {

		// the request is a IR request
		if (getRequestURI().startsWith(IR_PATH)) {
			// chop off the /data prefix
			return "http://"
					+ host
					+ getRequestURI().substring(IR_PATH.length(),
							getRequestURI().length());

		} else {

			// the requst is already an NIR request
			return "http://" + host + getRequestURI();
		}

	}

	public String getHTMLInformationResource() {

		return EUROPEANA_HTML_BASE_URL + europeanaID + ".html";
	}

	public String getRDFInformationResource() {

		return "http://" + host + EuropeanaRequest.IR_PATH + getResourceType() + getEuropeanaID();
	}

}