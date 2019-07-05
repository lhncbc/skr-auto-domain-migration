/**
 * 
 */
package gov.nih.nlm.semmed.util;

import org.displaytag.decorator.ColumnDecorator;
import org.displaytag.exception.DecoratorException;

/**
 * @author hkilicoglu
 *
 */
public class PubmedLinkDecorator implements ColumnDecorator {

	// --------------------------------------------------------- Instance Variables
//	private static Log log = LogFactory.getLog(InitializeSearchAction.class);
	
	/* (non-Javadoc)
	 * @see org.displaytag.decorator.ColumnDecorator#decorate(java.lang.Object)
	 */
	public String decorate(Object columnValue) throws DecoratorException {
        String id = (String) columnValue;
        if (id.indexOf('.') > 0 ) {
        	String pmid = id.substring(0,id.indexOf('.'));
        	String rest = id.substring(id.indexOf('.'));
        	if (id.startsWith("NCT")) 
            	return "<a href=\"http://www.clinicaltrials.gov/ct/show/" + 
    				pmid + "\" target=\"_blank\">" + pmid + "</a>" + rest;
            else 
            	return "<a href=\"http://ncbi.nlm.nih.gov/entrez/query.fcgi?" + 
        			"cmd=Retrieve&db=pubmed&dopt=AbstractPlus&list_uids=" + pmid + 
        			"\" target=\"_blank\">" + pmid + "</a>" + rest;
        } else {
        	if (id.startsWith("NCT")) 
            	return "<a href=\"http://www.clinicaltrials.gov/ct/show/" + 
					id + "\" target=\"_blank\">" + id + "</a>";
        	else 
	        	return "<a href=\"http://ncbi.nlm.nih.gov/entrez/query.fcgi?" + 
	    		"cmd=Retrieve&db=pubmed&dopt=AbstractPlus&list_uids=" + id + 
	    		"\" target=\"_blank\">" + id + "</a>";        	
        }
	}

}
