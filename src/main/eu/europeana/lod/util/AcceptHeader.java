package eu.europeana.lod.util;

import java.util.StringTokenizer;

public class AcceptHeader {
	
// returns the list of accept headers with the higher q value
	private String acceptHeader="";
	
	public AcceptHeader(String header){
		acceptHeader=header;
	}
	
	public String getValues(){
		return getAcceptValues();
		
	}
	
	private String getAcceptValues(){
		String headers="";
		
		if ((acceptHeader==null) || acceptHeader.trim().equals(""))
			return "text/html";
    	StringTokenizer st = new StringTokenizer(acceptHeader, ",");
    	
    	
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
    				temppr=temppr.trim();
    				if (temppr.startsWith("q=")){
    					Float pr_num= new Float(temppr.substring(2).trim());
    					if (candidate_priority.compareTo(pr_num)<0){
    						candidate_priority = pr_num;
    						headers=" ";
    						//headers+=value.nextToken()+" ";
    						headers+=temp+" ";
    					}
    					if (candidate_priority.compareTo(pr_num)==0){
    						candidate_priority = pr_num;
    						//headers+=value.nextToken()+" ";
    						headers+=temp+" ";
    					}
    				}
    			}
    		}
    		
    	}
    	return headers.trim();
    }


}
