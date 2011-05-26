package eu.europeana.lod.data;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import eu.europeana.lod.util.AcceptHeaderHandler;
import eu.europeana.lod.util.AcceptHeaderHandler.MimeTypePattern;


/**
 * This class encapsulates the logic of the Europeana Linked Data URI design and
 * provides access methods to request information.
 * 
 * @author haslhofer
 * @author cesareconcordia
 * 
 */
public class EuropeanaRequest extends HttpServletRequestWrapper {
	

	private AcceptHeaderHandler acceptHandler;
	
	public static final String EUROPEANA_DATA_BASE_URL = "http://data.europeana.eu";

	public static final String EUROPEANA_DATA_BASE_HOME_PAGE_URL = "/index.html";
	public static final String EUROPEANA_HTML_BASE_URL = "http://www.europeana.eu/portal/record/";
	
	public static String AGGR_EU_PATH = "/aggregation/europeana/";
	public static String AGGR_PR_PATH = "/aggregation/provider/";
	public static String PROXY_EU_PATH = "/proxy/europeana/";
	public static String PROXY_PR_PATH = "/proxy/provider/";
	public static String RM_PATH = "/rm/europeana/";
	public static String ITEM_PATH = "/item/";
	
	public static String IR_PATH = "/data";

	public EuropeanaRequest(HttpServletRequest request) {
		super(request);
		
		String acceptHeader = getHeader("accept");
		
		acceptHandler = new AcceptHeaderHandler(acceptHeader);
		
		
	}

	
	/**
	 * Returns true if the URI path is empty -> it is a root request
	 */
	public boolean isRootRequest() {
		
		if (getRequestURI() == null || getRequestURI().equalsIgnoreCase("/")) {
			return true;
		}
		
		return false;
	}
	
	
	/**
	 * Returns the preferred mime-type given in the HTTP Accept header field
	 * 
	 * @return
	 */
	public String getPreferredAcceptMimeType() {
		
		return acceptHandler.getPreferredMimeType();
		
	}
	
	
	
	public String getEuropeanaID() {

		//TODO: improve code; e.g., by regex
		
		String uri = getRequestURI();
		
		if(uri.equalsIgnoreCase("/")) {
			return(EUROPEANA_DATA_BASE_HOME_PAGE_URL);
		}
		
		
		// if it is a IR request, strip off IR_PATH prefix
		if(uri.startsWith(IR_PATH)) {
			uri = uri.substring(IR_PATH.length(), uri.length() - 1);
		}

		if (uri.startsWith(AGGR_EU_PATH)) {
			return uri.substring(AGGR_EU_PATH.length(), uri.length());
		} else if (uri.startsWith(AGGR_PR_PATH)) {
			return uri.substring(AGGR_PR_PATH.length(), uri.length());
		} else if (uri.startsWith(PROXY_EU_PATH)) {
			return uri.substring(PROXY_EU_PATH.length(), uri.length());
		} else if (uri.startsWith(PROXY_PR_PATH)) {
			return uri.substring(PROXY_PR_PATH.length(), uri.length());
		} else if (uri.startsWith(RM_PATH)) {
			return uri.substring(RM_PATH.length(), uri.length());
		} else if (uri.startsWith(ITEM_PATH)) {
			return uri.substring(ITEM_PATH.length(), uri.length());
		} else {
			return null;
		}

	}

	/**
	 * Returns whether or not the request asks for a human-readable resource representation
	 * 
	 * @return true if human-readable
	 */
	public boolean isDocumentRequest() {
		
		String mimeType = acceptHandler.getPreferredMimeType();
		
		boolean documentRequest = MimeTypePattern.matchMIMEType(mimeType, MimeTypePattern.HTML);
		
		return documentRequest;
	}

	/**
	 * Returns whether or not the request asks for a machine-readable resource representation
	 * 
	 * @return true if machine-readable
	 */
	public boolean isDataRequest() {
		
		String mimeType = acceptHandler.getPreferredMimeType();

		boolean dataRequest = MimeTypePattern.matchMIMEType(mimeType, MimeTypePattern.RDF, MimeTypePattern.TTL, 
				MimeTypePattern.N3);
		
		return dataRequest;
	}
	
	/**
	 * Returns the non-information resource URI for a given information resource URI
	 * 
	 * e.g., 
	 * 
	 * http://data.europeana.eu/data/rm/europeana/00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154
	 * -->
	 * http://data.europeana.eu/rm/europeana/00000/E2AAA3C6DF09F9FAA6F951FC4C4A9CC80B5D4154
	 * 
	 * @return
	 */
	public String getNonInformationResourceURI() {
		
		// the request is a IR request
		if(getRequestURI().startsWith(IR_PATH)) {
			// chop off the /data prefix
			return EUROPEANA_DATA_BASE_URL + getRequestURI().substring(IR_PATH.length(), getRequestURI().length());
			
		} else {
			
			// the requst is already an NIR request
			return EUROPEANA_DATA_BASE_URL + getRequestURI();
		}
		
	}
	

	public String getHTMLInformationResource() {
				
		String europeanaID = getEuropeanaID();
		
		return "http://www.europeana.eu/portal/record/" + europeanaID + ".html";
	}
	

	public String getRDFInformationResource() {
		
		return EUROPEANA_DATA_BASE_URL + EuropeanaRequest.IR_PATH + getRequestURI();
	}

	
	
	/**
	 * This enum maps URI-path prefixes to EDM Resource types
	 * 
	 * @author haslhofer
	 *
	 */
	public enum ResourceType {
		
		PROXY_PROVIDER("/proxy/provider"),
		PROXY_EUROPEANA("/proxy/europeana"),
		AGGREGATION_PROVIDER("/aggregation/provider"),
		AGGREGATION_EUROPEANA("/aggregation/europeana"),
		RM("/rm/europeana"),
		ITEM("/item");
		
		private String pathPrefix;
		
		private ResourceType(String pathPrefix) {
			this.pathPrefix = pathPrefix;
		}
		
		@Override
		public String toString() {
			return pathPrefix;
		}
		
	}
	


}