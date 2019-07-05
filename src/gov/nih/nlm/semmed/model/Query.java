/**
 *
 */
package gov.nih.nlm.semmed.model;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Element;

/**
 * @author shindongwoo
 *
 */
public class Query {
	private String term;
	private String source;
	private Map<String,Object> options;

	public Query(String term, String source) {
		this();
		// TODO Auto-generated constructor stub
		this.term = term;
		this.source = source;
		options = new HashMap<String,Object>();
		/* this.mostRecent = mostRecent;
		this.startDate = startDate;
		this.endDate = endDate; */
	}

	public Query(Element e) {
		this.term = e.getChildTextTrim("Term");
		this.source = e.getChildTextTrim("Source");
//		if ("ctrials".equals(source))
//			this.options = new EssieQueryOptions(e.getChild("QueryOptions"));
//		else if ("medline".equals(source))
//			this.options = new PubmedQueryOptions(e.getChild("QueryOptions"));
//		else if ("both".equals(source))
//			this.options = new CompositeQueryOptions(e.getChild("QueryOptions"));
/*		this.mostRecent = e.getChildTextTrim("MostRecent");
		this.startDate = e.getChildTextTrim("StartDate");
		this.endDate = e.getChildTextTrim("EndDate");	*/
	}

	public Query() {
	}

//    public Element toXml(String nodeName) {
//    	Element queryNode = new Element(nodeName);
//    	Element termNode = new Element("Term");
//    	termNode.setText(getTerm());
//    	Element sourceNode = new Element("Source");
//    	sourceNode.setText(getSource());
//    	Element optionsNode = options.toXml("QueryOptions");
///*    	Element mostRecentNode = new Element("MostRecent");
//    	mostRecentNode.setText(getMostRecent());
//    	Element startDateNode = new Element("StartDate");
//    	startDateNode.setText(getStartDate());
//    	Element endDateNode = new Element("EndDate");
//    	endDateNode.setText(getEndDate()); */
//    	queryNode.addContent(termNode);
//    	queryNode.addContent(sourceNode);
//    	queryNode.addContent(optionsNode);
///*    	queryNode.addContent(mostRecentNode);
//    	queryNode.addContent(startDateNode);
//    	queryNode.addContent(endDateNode);  */
//    	return queryNode;
//    }

    public Element toXml(String nodeName) {
    	Element queryNode = new Element(nodeName);
    	Element termNode = new Element("Term");
    	termNode.setText(getTerm());
    	Element sourceNode = new Element("Source");
    	sourceNode.setText(getSource());
    	Element optionsNode = new Element("QueryOptions");
    	optionsNode.setText(options.toString());
///*    	Element mostRecentNode = new Element("MostRecent");
//    	mostRecentNode.setText(getMostRecent());
    	// Element startDateNode = new Element("StartDate");
    	// startDateNode.setText(getStartDate());
//    	Element endDateNode = new Element("EndDate");
//    	endDateNode.setText(getEndDate()); */
    	queryNode.addContent(termNode);
    	queryNode.addContent(sourceNode);
    	queryNode.addContent(optionsNode);
///*    	queryNode.addContent(mostRecentNode);
//    	queryNode.addContent(startDateNode);
//    	queryNode.addContent(endDateNode);  */
    	return queryNode;
    }

    public String toFlatFile() {
    	StringBuffer sb = new StringBuffer();
    	sb.append("Term: \n\t");
    	sb.append(getTerm());
    	sb.append("Source: \n\t");
    	sb.append(getSource());
    	sb.append("QueryOptions: \n\t");
    	sb.append(options.toString() + "\n\n");
    	return sb.toString();
    }

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String toString() {
		return getTerm() + "|" +
			getSource() + "|" + options.toString();
/*			getMostRecent() + "|" +
			getStartDate() + "|" +
			getEndDate();*/
	}

	public Map<String,Object> getOptions() {
		return options;
	}

	public void setOptions(Map<String,Object> options) {
		this.options = options;
	}

	public void setOption(String key,Object value){
		options.put(key, value);
	}

	public Object getOption(String key){
		return options.get(key);
	}

	public void setDefaultValues(){
		setOption("pubmedMax", "500");
	    setOption("clinicalTrialMax", "500");
	    setOption("pubmedStartDate","01/01/1900");
	    setOption("pubmedEndDate","08/31/2012");
	    // setOption("hasAbstract", Boolean.TRUE); // Check abstract by default
	}

}
