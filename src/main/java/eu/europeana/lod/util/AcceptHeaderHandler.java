package eu.europeana.lod.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parses and gives convenient (ordered) access to HTTP Accept Header
 * values.
 * 
 * The mime-types are internally stored in a ordered tree map, having the mime-type
 * with the highest q-value as first entry and the mime-type with the lowest q-value
 * as last entry.
 * 
 * The parsing mechanism follows
 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.1
 * 
 * @author haslhofer
 */
public class AcceptHeaderHandler extends TreeMap<Float, List<String>> {

	/**
	 * We assume that HTML is default if no or an empty accept header is given
	 */
	public static String DEFAULT_MIME_TYPE = "text/html";

	/**
	 * Creates a new AcceptHeaderHandler instance
	 * 
	 * @param acceptHeader
	 */
	public AcceptHeaderHandler(String acceptHeader) {
		
		super(Collections.reverseOrder());
		
		if (acceptHeader == null || acceptHeader.equals("")) {
			List<String> mimeTypeList = new ArrayList<String>();
			mimeTypeList.add(DEFAULT_MIME_TYPE);
			put(new Float(1.0), mimeTypeList);
		} else {
			parseAcceptHeader(acceptHeader.toLowerCase());
		}
	}

	/**
	 * Returns the supported mime-type with highest priority; q-values omitted
	 * 
	 * Returns NULL if the mime-type is unknown
	 * 
	 * @return
	 */
	public MimeTypePattern getPreferredMimeType() {
		
		for (Float key : keySet()) {
			
			for (String mimeType: get(key)) {
				
				MimeTypePattern mt = MimeTypePattern.getMatchingMimeType(mimeType);
				
				if (mt != null) {
					return mt;
				}
								
			}
			
		}
		
		return null;
	}

	/**
	 * Parses HTTP accept headers and puts them, ordered by q-value, into a map.
	 * 
	 */
	private void parseAcceptHeader(String acceptHeader){

		String[] mimeTypes = acceptHeader.trim().split(",");
		for (String mimeType : mimeTypes) {
			parseMimeType(mimeType);
		}

	}

	/**
	 * Parses a single mime-type entry and puts it into the map
	 * 
	 * e.g., application/rdf+xml; q=0.8
	 * 
	 * 0.8 => application/rdf+xml
	 * 
	 */
	private void parseMimeType(String mimeType){

		String[] tokens = mimeType.trim().split(";");

		// the token must be at the first place
		String mt = tokens[0].trim();

		// the default qValue is 1
		Float qValue = new Float(1);

		// search all other tokens for q parameter indicator
		for (int i = 1; i < tokens.length; i++) {

			String parameter = tokens[i].trim();

			if (parameter.startsWith("q=")) {
				String value = parameter.substring(2, parameter.length());
				try {
					qValue = Float.parseFloat(value);
				} catch (NumberFormatException ne) {
					// ignore the qvalue
				}

			}

		}
				
		// put the qValue / mime-type pair into the map
		
		if(containsKey(qValue)) {
			get(qValue).add(mt);
		} else {
			List<String> mimeTypeList = new ArrayList<String>();
			mimeTypeList.add(mt);
			put(qValue, mimeTypeList);
		}
		

	}
	
	
	/**
	 * Returns the response content type for a given request mime type pattern 
	 */
	public static ContentType getContentType(MimeTypePattern mimeTypePattern) {

		return mimeTypeContentTypeMap.get(mimeTypePattern);
	
	}
	
	
	/**
	 * Returns the response content type for a given request mime type 
	 */
	public static ContentType getContentType(String acceptHeader) {
		
		MimeTypePattern mt = getMimeType(acceptHeader);
		
		return getContentType(mt);
		
	}
	
	
	/**
	 * Returns the matching mime type class for a g given accept header 
	 */
	public static MimeTypePattern getMimeType(String acceptHeader) {

		return new AcceptHeaderHandler(acceptHeader).getPreferredMimeType();

	}
	
	
	/**
	 * Mapping between the request mime-types (Accept header) and the response content-types 
	 */
	public static Map<MimeTypePattern,ContentType> mimeTypeContentTypeMap = new HashMap<MimeTypePattern, ContentType> ();
	
	static {
		
		mimeTypeContentTypeMap.put(MimeTypePattern.RDF, ContentType.RDF);
		mimeTypeContentTypeMap.put(MimeTypePattern.TTL, ContentType.TTL);
		mimeTypeContentTypeMap.put(MimeTypePattern.N3, ContentType.N3);
		mimeTypeContentTypeMap.put(MimeTypePattern.HTML, ContentType.HTML);
		
	}
	
	/**
	 * This enum maps mime-type patterns to document serialization formats 
	 * 
	 * @author haslhofer
	 *
	 */
	public enum MimeTypePattern {

		RDF(".*rdf.*|.*rdf\\/xml.*|.*application\\/rdf\\+xml.*"),
		HTML(".*application\\/xml.*|.*text\\/html.*|.*application\\/xhtml\\+xml.*|\\*\\/\\*"),
		TTL(".*ttl.*|.*text\\/turtle.*|.*application\\/x\\-turtle.*|.*application\\/turtle.*|.*text\\/rdf\\+turtle.*"),
		N3(".*n3.*|.*text\\/n3.*|.*text\\/rdf\\+n3.*");

		private String value;

		MimeTypePattern(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
		/**
		 * Returns whether a given String matches a given (list of) mime-type patterns
		 * 
		 * @param acceptHeader
		 * @param mimeTypes
		 * @return
		 */
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

		
		/**
		 * Returns the first matching mime-type pattern for a given accept header string.
		 * 
		 * Returns null, if there is no matching pattern
		 * 
		 * @param acceptHeader
		 * @return
		 */
		public static MimeTypePattern getMatchingMimeType(String acceptHeader) {
		
			for(MimeTypePattern pattern: values()) {
				
				if (matchMIMEType(acceptHeader, pattern)) {
					return pattern;
				}
				
			}
			
			return null;
		}
		
		
		
	}
	
	
	/**
	 * MimeTypes returned in Europeana LOD responses
	 * 
	 * @author haslhofer
	 * 
	 */
	public enum ContentType {

		RDF("application/rdf+xml; charset=UTF-8"),
		HTML("text/html"),
		TTL("text/turtle; charset=UTF-8"),
		N3("text/n3; charset=UTF-8");

		private String value;

		ContentType(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

	}


	private static final long serialVersionUID = 6496628828622396063L;

}
