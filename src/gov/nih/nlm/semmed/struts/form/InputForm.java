//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

/**
 * MyEclipse Struts
 * Creation date: 12-02-2005
 *
 * XDoclet definition:
 * @struts.form name="SearchForm"
 */
public class InputForm extends ActionForm {

	private static SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	// --------------------------------------------------------- Instance Variables
	private static Log log = LogFactory.getLog(InputForm.class);

	/** uploadFilename property */
	private String domain;
	private String year;  
	/** uploading citation File */
	private FormFile uploadExcelDomainFile;
	private FormFile uploadExcelLocsemnetFile;
	private FormFile uploadExcelSemrulesFile;
	private FormFile uploadExcelExceptionFile;
	private FormFile uploadSemnetaccessFile;
	/** uploading citation File */


	// --------------------------------------------------------- Methods

	/**
	 * Method validate
	 * @param mapping
	 * @param request
	 * @return ActionErrors
	 */
	public ActionErrors validate(
		ActionMapping mapping,
		HttpServletRequest request) {
		// TODO Auto-generated method stub
		ActionErrors errors = new ActionErrors();
		String method = request.getParameter("method");
		/* Enumeration en = request.getParameterNames();
		    while (en.hasMoreElements()) {
			String paramName = (String)en.nextElement();
			log.debug(paramName +"|" + request.getParameter(paramName));
		} */
		if (method != null) {

			if(method.equals("Upload DOM Excel File") && (uploadExcelDomainFile == null || uploadExcelDomainFile.getFileName().trim().length() == 0) &&
					method.equals("Upload Domain Concept File")) {
					errors.add("uploadFilename", new ActionError("error.uploadfile.required"));
				}
		}
		return errors;
	}

	/**
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(
			ActionMapping mapping,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
	}


	/**
	 * @return Returns the searchTypes.
	 */
/*	public ArrayList getSearchTypes() {
		return searchTypes;
	}*/

	/**
	 * @param searchTypes The searchTypes to set.
	 */


	/**
	 * @return Returns the selectedSearchType.
	 */
/*	public String getSelectedSearchType() {
		return selectedSearchType;
	}*/
	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	/**
	 * @return Returns the uploadFile.
	 */
	public FormFile getUploadExcelDomainFile() {
		return uploadExcelDomainFile;
	}

	/**
	 * @param uploadFile The uploadFile to set.
	 */
	public void setUploadExcelDomainFile(FormFile uploadExcelDomainFile) {
		this.uploadExcelDomainFile = uploadExcelDomainFile;
	}

	public FormFile getUploadExcelLocsemnetFile() {
		return uploadExcelLocsemnetFile;
	}

	public void setUploadExcelLocsemnetFile(FormFile uploadExcelLocsemnetFile) {
		this.uploadExcelLocsemnetFile = uploadExcelLocsemnetFile;
	}

	public FormFile getUploadExcelSemrulesFile() {
		return uploadExcelSemrulesFile;
	}

	public void setUploadExcelSemrulesFile(FormFile uploadExcelSemrulesFile) {
		this.uploadExcelSemrulesFile = uploadExcelSemrulesFile;
	}

	public FormFile getUploadExcelExceptionFile() {
		return uploadExcelExceptionFile;
	}

	public void setUploadExcelExceptionFile(FormFile uploadExcelExceptionFile) {
		this.uploadExcelExceptionFile = uploadExcelExceptionFile;
	}

	public FormFile getUploadSemnetaccessFile() {
		return uploadSemnetaccessFile;
	}

	public void setUploadSemnetaccessFile(FormFile  uploadSemnetaccessFile) {
		this. uploadSemnetaccessFile =  uploadSemnetaccessFile;
	}
	
	/*

	public FormFile getUploadExcelLocalRelInverseFile() {
		return uploadExcelLocalRelInverseFile;
	}

	public void setUploadExcelLocalRelInverseFile(FormFile uploadExcelLocalRelInverseFile) {
		this.uploadExcelLocalRelInverseFile = uploadExcelLocalRelInverseFile;
	}

	public FormFile getuploadExcelLocalSemnetFile() {
		return uploadExcelLocalSemnetFile;
	}

	public void setUploadExcelLocalSemnetFile(FormFile uploadExcelLocalSemnetFile) {
		this.uploadExcelLocalSemnetFile = uploadExcelLocalSemnetFile;
	} */
}

