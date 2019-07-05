package gov.nih.nlm.semmed.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author rodriguezal
 *
 */
public class PubMedIDParser extends DefaultHandler {

	private boolean inQueryKey = false;
	private boolean inWebEnv = false;
	private boolean inID = false;
	private boolean inCount = false;
	private static Log log = LogFactory.getLog(PubMedIDParser.class);

	private StringBuffer sb = new StringBuffer();
	private StringBuffer sbCount = null;
	private int[] ids;
	private String webEnv;
	private String queryKey;
	private int count = -1;
	private int current = 0;
	private int maxReturn = Integer.MAX_VALUE;

	public PubMedIDParser(int max){
		if(max>0)
			maxReturn=max;
	}

	public int[] getIds(){
		return ids;
	}

	public String getWebEnv(){
		if(webEnv != null)
			return "WebEnv="+webEnv+"&query_key="+queryKey;
		else
			return "query_key="+queryKey;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException{
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false

        if (eName.equalsIgnoreCase("id")) {
        	sb = new StringBuffer();
        	inID = true;
	} else if (eName.equalsIgnoreCase("webenv"))
        	inWebEnv = true;
        else if (eName.equalsIgnoreCase("querykey"))
        	inQueryKey = true;
        else if (count<0 && eName.equalsIgnoreCase("count")) {
        	sbCount = new StringBuffer();
        	inCount = true;
        }
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException{
		String eName = localName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false

        if (eName.equalsIgnoreCase("id")) {
        	if(sb != null && sb.toString().trim().equals("") != true)
        		ids[current++] = Integer.parseInt(sb.toString().trim());
        		// log.debug("PubMed Id: " + sb.toString().trim());
        	inID = false;
        }
        else if (eName.equalsIgnoreCase("webenv")){
        	inWebEnv = false;
        	webEnv = sb.toString().trim();
        }else if (eName.equalsIgnoreCase("querykey"))
        	inQueryKey = false;
        else if (inCount && eName.equalsIgnoreCase("count")){
        	count = Integer.parseInt(sbCount.toString().trim());
        	ids = new int[Math.min(count,maxReturn)];
        	inCount = false;
        }
	}

	@Override
	public void characters(char[] ch,int start,int length) throws SAXException{
		// log.debug("Strings in <characters> : ");
		// log.debug("'" + new String(ch, start, length) +"'");
		if (inID){
			if (current>=ids.length)
				return;
			// ids[current++] = Integer.parseInt(new String(ch,start,length));
			sb.append(ch,start,length);

		}else if (inWebEnv)
			sb.append(ch,start,length);
        else if (inQueryKey)
        	queryKey = new String(ch,start,length);
        else if (inCount){
        	// count = Integer.parseInt(new String(ch,start,length));
        	sbCount.append(ch,start,length);
        }
	}


//	public static String forHTMLTag(String aTagFragment){
//	    final StringBuilder result = new StringBuilder();
//	    final StringCharacterIterator iterator = new StringCharacterIterator(aTagFragment);
//	    char character =  iterator.current();
//	    while (character != CharacterIterator.DONE ){
//	      if (character == '<') {
//	        result.append("&lt;");
//	      }
//	      else if (character == '>') {
//	        result.append("&gt;");
//	      }
//	      else if (character == '\"') {
//	        result.append("&quot;");
//	      }
//	      else if (character == '\'') {
//	        result.append("&#039;");
//	      }
//	      else if (character == '\\') {
//	         result.append("&#092;");
//	      }
//	      else if (character == '&') {
//	         result.append("&amp;");
//	      }
//	      else if ((character >= 'a' && character <= 'z') ||
//	    		   (character >= 'A' && character <= 'Z') ||
//	    		   (character >= '0' && character <= '9')){
//	    	  result.append(character);
//	      }
//	      else {
//	    	  result.append("&#"+Character.+";");
//	      }
//	      character = iterator.next();
//	    }
//	    return result.toString();
//	  }
}
