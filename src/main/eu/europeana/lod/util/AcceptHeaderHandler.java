package eu.europeana.lod.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import eu.europeana.lod.data.EuropeanaRequest.MimeTypePattern;

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
	 * We assume that HTML is default (not a 406 response)
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
	 * Returns null if none of the mime-types is supported
	 * 
	 * @return
	 */
	public String getPreferredMimeType() {
		
		for (Float key : keySet()) {
			
			for (String mimeType: get(key)) {
				
				if (MimeTypePattern.matchMIMEType(mimeType, 
						MimeTypePattern.HTML,
						MimeTypePattern.RDF,
						MimeTypePattern.N3,
						MimeTypePattern.TTL)) {
					
					return mimeType;
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

	private static final long serialVersionUID = 6496628828622396063L;

}
