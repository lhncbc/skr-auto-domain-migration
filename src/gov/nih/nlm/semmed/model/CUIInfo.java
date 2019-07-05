package gov.nih.nlm.semmed.model;
import java.util.List;

public class CUIInfo {
	public List<String> stype = null;
	public String pname = null;

	public CUIInfo() {
		super();
	}
	public CUIInfo(List<String> stype, String pname) {
		this.stype = stype;
		this.pname = pname;
	}

	public List<String> getStype() {
		return stype;
	}

	public void setStype(List<String> stype) {
		this.stype = stype;
	}

	public String getPname() {
		return pname;
	}

	public void setPname(String pname) {
		this.pname = pname;
	}
}
