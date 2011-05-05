package eu.europeana.lod.servlets;

import org.apache.commons.lang.StringUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;


import de.fuberlin.wiwiss.pubby.Configuration;
import de.fuberlin.wiwiss.pubby.MappedResource;
import de.fuberlin.wiwiss.pubby.ModelResponse;
import de.fuberlin.wiwiss.pubby.ModelTranslator;
import de.fuberlin.wiwiss.pubby.ResourceDescription;
import de.fuberlin.wiwiss.pubby.vocab.FOAF;
import eu.europeana.lod.spaqrl.RDFdata;
import eu.europeana.lod.spaqrl.RemoteSPARQLDataSource;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


	public class LODRoot extends BaseServlet {
	
	   
	    private static final String LOD_PILOT_REL_URL = "/";
	    private RemoteSPARQLDataSource sds= new RemoteSPARQLDataSource("http://sparql.mminf.univie.ac.at/","http://data.europeana.eu");
	    private RDFdata rdfData = new RDFdata();
	
	    
	    @Override
	    public void init(ServletConfig config) throws ServletException {
	    
	    	super.init(config);
	    }
	
	    @Override
	    protected boolean doGet(String relativeURI,
				HttpServletRequest request, HttpServletResponse response,
				Configuration config)
				throws IOException, ServletException  {
	    	
	    	//PrintWriter out = response.getWriter();
	    	MappedResource resource = config.getMappedResourceFromRelativeWebURI(
					relativeURI, false);
	    	String hpHeaders=getHigherAcceptValues(request.getHeader("accept"));
	    	if (hpHeaders==null)
	    		return false;
	    	
	    	 if (matchRedirect(hpHeaders, Header.RDF, Header.APPLICATIONRDF, Header.N3, Header.TTL)) {
	    		 	ModelTranslator model = new ModelTranslator(sds.getResourceDescription("http://data.europeana.eu/"+relativeURI), config);
	    		 // Check if resource exists in dataset
	    			if (model.getTranslated().size() == 0) {
	    				response.setStatus(404);
	    				response.setContentType("text/plain");
	    				response.getOutputStream().println("Nothing known about <" + resource.getWebURI() + ">");
	    				return true;
	    			}
	    			Model description= rdfData.get(model.getTranslated(), resource);
	    			Resource document = description.getResource(addQueryString(resource.getDataURL(), request));
	    			document.addProperty(FOAF.primaryTopic, getResourceDescription(resource).getResource(resource.getWebURI()));
	    			document.addProperty(RDFS.label, 
	    					"RDF description of " + 
	    					new ResourceDescription(resource, description, config).getLabel());
	    			resource.getDataset().addDocumentMetadata(description, document);
	    			resource.getDataset().addMetadataFromTemplate(description, resource, getServletContext());
	    			ModelResponse server = new ModelResponse(description, request, response);
	    			server.serve();
	    		 	//redirectToPubby(relativeURI, config, request, response);
		        	return true ;
		        }
	  
	    	 
	        if (matchRedirect(hpHeaders, Header.HTML, Header.XHTML)) {
	            
	        	
	        	if (request.getRequestURI().trim().equalsIgnoreCase(LOD_PILOT_REL_URL)){
	        		redirectToLOD(response);
	        		return true;
	        	}
	        	if (!(processUri(request.getRequestURI()).trim()).equalsIgnoreCase("")){
	        		response.setStatus(303);
	        		redirectToEuropeana(processUri(request.getRequestURI()).trim(), response);
	        	}
	        	else
	        		redirectToEuropeana(response);
	        	return true;
	        }
	      
	        redirectToLOD(response);
	       // out.close();
			return true;
	
	    }
	
	    public String checkRedirect(HttpServletRequest request){
	    	
	    	Enumeration <String> en=  request.getHeaderNames();
	    	String res="";
	    	for (;en.hasMoreElements();){
	    		String header=en.nextElement();
	    		
	    		res+=" "+header+" "+request.getHeader(header)+" <br>";
	    	}
	    		
	    	return res;	    	
	    }
	    /**
	     * Matches the request Accept header with the provided rules. The rules are  regular
	     * expressions defined in Header.
	     *
	     * @param requestHeader The Accept header value.
	     * @param headers   The Header rules.
	     * @return Does theHeader values match the Header rule?
	     */
	   
	    public boolean matchRedirect(String requestHeader, Header... headers) {
	        for (Header header : headers) {
	            Pattern pattern = Pattern.compile(header.getValue());
	            Matcher matcher = pattern.matcher(requestHeader);
	          
	            if (matcher.matches()) {
	               return true;
	            }
	        }
	        return false;
	    }
	    
	    private String getHigherAcceptValues(String requestHeader){
	    	StringTokenizer st = new StringTokenizer(requestHeader, ",");
	    	
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
	    	return headers;
	    }
	    
	    private String processUri(String Uri){
	    	int r=Uri.lastIndexOf("/");
	    	if (r>0){
	    		String recordId=Uri.substring(r+1);
	    		String collId="";
	    		int c=(Uri.substring(0, r)).lastIndexOf("/");
	    		if (c>0){
	    			collId=Uri.substring(c+1, r);
	    		}
	    		return " http://www.europeana.eu/portal/record/"+collId+"/"+recordId+".html";
	    	
	    	}
	    	return "";
	    }
	
	   
	
	    public void redirectToEuropeana(HttpServletResponse response) throws IOException {
	    	//out.println("redirect"+hpHeaders+" "+processUri(request.getRequestURI()));
	        String url = "http://europeana.eu/";
	        response.sendRedirect(url);
	    }
	    
	    private void redirectToEuropeana(String uri, HttpServletResponse response) throws IOException {
	    	
	        response.sendRedirect(uri);
	    }
	    
	    private void redirectToLOD(HttpServletResponse response) throws IOException {
	        String url = "http://data.europeana.eu/index.html"; 
	        response.sendRedirect(url);
	    }
	    
	    private void redirectToPubby(String relativeURI, Configuration config, HttpServletRequest request, HttpServletResponse response) throws IOException {
	      
	       // response.sendRedirect(url);
	        try {
	        	
	        	if (relativeURI.startsWith("static/")) {
	    			getServletContext().getNamedDispatcher("default").forward(request, response);
	    			return;
	    		}
	    		
	    		// If index resource is defined, redirect requests for the index page to it
	    		if ("".equals(relativeURI) && config.getIndexResource() != null) {
	    			response.sendRedirect(config.getIndexResource().getWebURI());
	    			return;
	    		}
	    		
	    		// Assume it's a resource URI -- will produce 404 if not
	    		getServletContext().getNamedDispatcher("WebURIServlet").forward(request, response);
	    		return ;
			} catch (ServletException e) {
				
				e.printStackTrace();
			}
	    }
	
	  
	    public enum Header {
	    	
	        RDF(".*rdf\\/xml.*"),
	        HTML(".*text\\/html.*"),
	        APPLICATIONRDF(".*application\\/rdf\\+xml.*"),
	        XHTML(".*application\\/xhtml.*"),
	        TTL(".*ttl.*"),
	        N3(".*n3.*");
	        
	        private String value;
	
	        Header(String value) {
	            this.value = value;
	        }
	
	        public String getValue() {
	            return value;
	        }
	    }
	}