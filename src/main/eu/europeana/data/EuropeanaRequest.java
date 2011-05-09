package eu.europeana.data;

import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * This class encapsulates the data.europeana.eu URI design issues
 * 
 * @author haslhofer
 * @author cesareconcordia
 * 
 */
public class EuropeanaRequest extends HttpServletRequestWrapper {
	
	private String acceptHeader="";

	public static final String EUROPEANA_DATA_BASE_URL = "http://data.europeana.eu";

	public static final String EUROPEANA_HTML_BASE_URL = "http://www.europeana.eu/portal/record/";
	
	public static String AGGR_EU_PATH = "/aggregation/europeana/";
	public static String AGGR_PR_PATH = "/aggregation/provider/";
	public static String PROXY_EU_PATH = "/proxy/europeana/";
	public static String PROXY_PR_PATH = "/proxy/provider/";
	public static String RM_PATH = "/rm/europeana/";
	public static String ITEM_PATH = "/item";
	
	public static String IR_PATH = "/data";

	public EuropeanaRequest(HttpServletRequest request) {
		super(request);
		acceptHeader=getAcceptValues();
	}

	public boolean isValidRequest() {
		// TODO: run some basic regex test
		return (!acceptHeader.equals(""));
	}

	public String getEuropeanaID() {

		//TODO: improve code; e.g., by regex
		
		String uri = getRequestURI();
		
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
		//return MimeTypePattern.matchMIMEType(getHeader("accept"), MimeTypePattern.HTML, MimeTypePattern.XHTML);
		return MimeTypePattern.matchMIMEType(acceptHeader, MimeTypePattern.HTML);
	}

	/**
	 * Returns whether or not the request asks for a machine-readable resource representation
	 * 
	 * @return true if machine-readable
	 */
	public boolean isDataRequest() {
		//return MimeTypePattern.matchMIMEType(getHeader("accept"), MimeTypePattern.RDF,
			//	MimeTypePattern.APPLICATIONRDF, MimeTypePattern.TTL, MimeTypePattern.N3);
		return MimeTypePattern.matchMIMEType(acceptHeader, MimeTypePattern.RDF, MimeTypePattern.TTL, 
				MimeTypePattern.N3);
		
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
		
		return "http://www.europeana.eu/portal/record/" + getEuropeanaID() + ".html";
	}
	

	public String getRDFInformationResource() {
		
		return EUROPEANA_DATA_BASE_URL + EuropeanaRequest.IR_PATH + getRequestURI();
	}

	
	
	
	/**
	 * This class handles the mimetype definitions and 
	 * 
	 * @author haslhofer
	 *
	 */
	public enum MimeTypePattern {

		RDF(".*rdf\\/xml.*|.*application\\/rdf\\+xml.*"),
		HTML(".*text\\/html.*|.*application\\/xhtml\\+xml.*"),
		TTL(".*ttl.*|.*text\\/turtle.*"),
		N3(".*text\\/n3.*");

		private String value;

		MimeTypePattern(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
		public static boolean matchMIMEType(String acceptHeader, MimeTypePattern... mimeTypes) {
			for (MimeTypePattern mimeType : mimeTypes) {
				Pattern pattern = Pattern.compile(mimeType.getValue());
				Matcher matcher = pattern.matcher(acceptHeader);

				if (matcher.matches()) {
					return true;
				}
			}
			return false;
		}

		
		
	}
	
	// returns the list of accept headers with the higher q value
	
	private String getAcceptValues(){
		String acceptHeader=getHeader("accept");
    	StringTokenizer st = new StringTokenizer(acceptHeader, ",");
    	
    	String headers="";
    	Float candidate_priority=new Float(0);
    	while (st.hasMoreTokens()){
    		StringTokenizer value= new StringTokenizer(st.nextToken(), ";");
    		if (value.countTokens()==1){
    			candidate_priority=new Float(1);
    			headers+=" "+value.nextToken()+" ";
    		}
    		else{
    			String temp="";
    			if (value.countTokens()>1){
    				temp=value.nextToken();
    				String temppr=value.nextToken();
    				if (temppr.startsWith("q=")){
    					Float pr_num= new Float(temppr.substring(2).trim());
    					if (candidate_priority.compareTo(pr_num)<0){
    						candidate_priority = pr_num;
    						headers=" ";
    						headers+=value.nextToken()+" ";
    					}
    					if (candidate_priority.compareTo(pr_num)==0){
    						candidate_priority = pr_num;
    						headers+=value.nextToken()+" ";
    					}
    				}
    			}
    		}
    		
    	}
    	return headers.trim();
    }

	
	

}