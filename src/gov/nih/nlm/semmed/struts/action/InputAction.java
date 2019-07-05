//Created by MyEclipse Struts
// XSL source (default): platform:/plugin/com.genuitec.eclipse.cross.easystruts.eclipse_4.0.1/xslt/JavaClass.xsl

package gov.nih.nlm.semmed.struts.action;

import gov.nih.nlm.semmed.exception.EssieException;
import gov.nih.nlm.semmed.exception.PubmedException;
import gov.nih.nlm.semmed.exception.SemMedException;
import gov.nih.nlm.semmed.exception.XMLException;
import gov.nih.nlm.semmed.model.SemMedDB;
import gov.nih.nlm.semmed.model.CUIInfo;
import gov.nih.nlm.semmed.struts.form.InputForm;
import gov.nih.nlm.semmed.util.HibernateSessionFactory;
// import gov.nih.nlm.semmed.util.UtsAuthentication;

import java.io.*;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Properties;
import java.util.Date;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream; 
import java.util.Arrays;
import java.util.Collections;

import javax.activation.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.LookupDispatchAction;
import org.apache.struts.upload.FormFile;
import org.apache.struts.util.LabelValueBean;
import org.hibernate.Session;
import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;



/**
 * MyEclipse Struts Creation date: 12-02-2005
 *
 * XDoclet definition:
 *
 * @struts.action path="/Input" name="InputForm" input="/jsp/welcome.jsp"
 *                scope="request" validate="true"
 * @struts.action-forward name="success" path="/jsp/Input.jsp"
 *                        contextRelative="true"
 * @struts.action-forward name="failure" path="/jsp/welcome.jsp"
 *                        contextRelative="true"
 */
public class InputAction extends LookupDispatchAction {

	// private static int uploadMax = 15000;
	// private static int BUFSIZE = 4096;
	private static Log log = LogFactory.getLog(InputAction.class);
	
	private static String relationMap[][] = new String[200][2];
	// private int relationMapCtr = 0;
	protected Map<String,String> getKeyMethodMap() {
	      Map<String,String> map = new HashMap<String,String>();
	      map.put("search.button.uploadDOMExcel", "uploadDOMExcel");
	      map.put("search.button.uploadGENExcel", "uploadGENExcel");
	      return map;
	  }

	@Override
	public ActionForward unspecified(ActionMapping mapping,
            ActionForm form,
            javax.servlet.http.HttpServletRequest request,
            javax.servlet.http.HttpServletResponse response)
	throws PubmedException, EssieException, SemMedException, Exception {
		if ("uploadDOMExcel".equals(request.getParameter("method")))
			return uploadDOMExcel(mapping,form,request,response);
		else if ("uploadGENExcel".equals(request.getParameter("method")))
			return uploadGENExcel(mapping,form,request,response);
		else
			return super.unspecified(mapping, form, request, response);

	}


	/**
	 * Method search
	 *
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 *
	 *
	 * Sets the session attribute 'citlist' to a list of PubmedArticle
	 *
	 *
	 */
	public ActionForward uploadDOMExcel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws PubmedException, EssieException, SemMedException {
		HttpSession session = request.getSession();
		ServletContext ctx  = session.getServletContext();
		InputForm inputForm = (InputForm) form;
		String domainName = request.getParameter("domain");
		FormFile domainFile = inputForm.getUploadExcelDomainFile();
		FormFile locsemnetFile = inputForm.getUploadExcelLocsemnetFile();
		FormFile semrulesFile = inputForm.getUploadExcelSemrulesFile();
		FormFile exceptionFile = inputForm.getUploadExcelExceptionFile();
		log.debug("Input Domain file = " + domainFile.getFileName());
		log.debug("Input Locsemnet file = " + locsemnetFile.getFileName());
		log.debug("Input Semrules file = " + semrulesFile.getFileName());
		log.debug("Input exception file = " + exceptionFile.getFileName());
		// log.debug("Input Exception Relation file = " + exceptionFile.getFileName());
		StringBuffer domainFileStmt = new StringBuffer();
		StringBuffer locsemnetFileStmt = new StringBuffer();
		StringBuffer semrulesFileStmt = new StringBuffer();
		StringBuffer exceptionFileStmt = new StringBuffer();
		StringBuffer errorFileStmt = new StringBuffer();
		// String ticket = UtsAuthentication.authenticate("dongwookshin","Wooyong1!");
		String umlsRelease = new String("2006AA");

		try {
			// PrintWriter 
			//   = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\PMIDListEcho.")));
			// InputStream is   = file.getInputStream();
			// BufferedReader br = new BufferedReader(new InputStreamReader(is));
			// Context context = new InitialContext();
	        // DataSource ds =
	        //    (DataSource)context.lookup("java:comp/env/jdbc/SemMedDB");
			Connection con = SemMedDB.getConnection(); 
			Statement stmt = con.createStatement();
			StringBuffer queryBuf = new StringBuffer("SELECT  c.PREFERRED_NAME, c.CUI, cs.SEMTYPE FROM CONCEPT as c, CONCEPT_SEMTYPE as cs WHERE c.CONCEPT_ID = cs.CONCEPT_ID and c.CUI = \"");
			HashSet conceptCUIHash = new HashSet();
			HashSet preferredConceptHash = new HashSet();
			// Hashtable cuiPreferredTable = new Hashtable();
			HashSet conceptCUIWithPREHash = new HashSet();
			HashSet newTripleHash = new HashSet();
			HashSet LocsemnetTripleHash = new HashSet();
			HashSet ExceptionTripleHash = new HashSet();
			if(domainFile != null) {
				InputStream domainInput = domainFile.getInputStream();
				POIFSFileSystem semtypeFileSystem = new POIFSFileSystem(domainInput);
				HSSFWorkbook semtypeWorkBook = new HSSFWorkbook(semtypeFileSystem);
				// log.debug("Check point 3");
				HSSFSheet replaceSheet = semtypeWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				log.debug("Read domain file");
				int it = 0;
				List<String> cellRowListSemtype = new ArrayList<String>();
				List<String> cellRowListConcept = new ArrayList<String>();
				List<String> cellRowListReplace = new ArrayList<String>();
				// List<String> cellRowListComment1 = new ArrayList<String>();
				int stage = 0;

				domainFileStmt.append("%      Do Not Modify This File    %\n" +
									"%     It is machine generated.    %\n" +
										"% file:	    " + domainName + "_domain.pl\n" +
                                       "% module:   " + domainName + "_domain.pl\n" +
										":- module(" + domainName + "_domain, [\n" +
										"\tdomain_name/1,\n" +
										"\tdomain_concept/2,\n" +
										"\tdomain_replace/2,\n" +
	      								"\tdomain_semtype/3\n" +
	      								"]).\n" +
	      								"domain_name(" + domainName + ").\n");

				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					// log.debug("Read row : " + it);
					if(stage == 1)
						cellRowListConcept.add("||");
					else if(stage == 2)
						cellRowListReplace.add("||");
					else if(stage == 3)
						cellRowListSemtype.add("||");
					int columnI = 0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_Concept")) {
							stage = 1;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_Replace")) {
							stage = 2;
							// log.debug("Processing Replace");
						} else if(myCell.toString().equals("_Semtype")) {
							stage = 3;
							// log.debug("Processing Semtype");
						}  else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Concept list");
									cellRowListConcept.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListReplace.add(myCell.toString());
								} else if(stage == 3) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListSemtype.add(myCell.toString());
								}
							} else if(columnI <  myCell.getColumnIndex()) { // If there is nothing in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Concept list");
									cellRowListConcept.add("");
									cellRowListConcept.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListReplace.add("");
									cellRowListReplace.add(myCell.toString());
								} else if(stage == 3) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListSemtype.add("");
									cellRowListSemtype.add(myCell.toString());
								}
								columnI++;
							}
						}
						columnI++;
					}

				}

						String comment = null;
						String stype = null;
						String typeName = null;
						String superType = null;
						int k = 0;
						int j=0;
						StringBuffer thisStmt = new StringBuffer();
						while (j < cellRowListSemtype.size()) {
							String stringCellValue =   cellRowListSemtype.get(j);
							if(stringCellValue.equals("||")) {
								if(k <=2 && comment != null)
									thisStmt.append(comment);
								k=0;
								if(thisStmt.length() > 0)
									domainFileStmt.append(thisStmt + "\n");
								thisStmt = null;
								thisStmt = new StringBuffer();
							} else {
								if(k==1)
									comment = stringCellValue;
								else if(k==2)
									stype = stringCellValue;
								else if (k==3)
									typeName = stringCellValue;
								else if (k==4) {
									superType = stringCellValue;
									if(comment != null && comment.equals("%"))
										thisStmt.append("% domain_semtype(" +
											stype + ",'" + typeName + "'," + superType +  ").");
									else
										domainFileStmt.append("domain_semtype(" +
										 	stype + ",'" + typeName + "'," + superType +  ").");
								} else if (k==5) { // process the comment at the end
									thisStmt.append(" " + stringCellValue);
									// log.debug(thisStmt.toString());
								}
							}
							j++;
							k++;
						}
						log.debug("End of Semtype stmt added");



					// } 	 else if(i== 2) {
						String fromName = null;
						String fromCUI = null;
						String fromType = null;
						String toName = null;
						String toCUI = null;
						String toType = null;
						j=0;
						k=0;
						log.debug("Size of cellRowListReplace = " + cellRowListReplace.size());
						thisStmt = new StringBuffer();
						while(j < cellRowListReplace.size()) {
							String stringCellValue = cellRowListReplace.get(j);
							// log.debug("j = " + j + ", cellValue = " + stringCellValue);
							if(stringCellValue.equals("||")) {
								if(k <=2 && comment != null)
									thisStmt.append(comment);
								k=0;
								if(thisStmt.length() > 0)
									domainFileStmt.append(thisStmt + "\n");
								thisStmt = null;
								thisStmt = new StringBuffer();
							} else {
								if(k==1)
									comment = stringCellValue;
								else if(k==2)
									fromName = stringCellValue;
								else if (k==3)
									fromCUI = stringCellValue;
								else if (k==4)
									fromType = stringCellValue;
								else if (k==5)
									toName = stringCellValue;
								else if (k==6)
									toCUI = stringCellValue;
								else if (k==7) {
									toType = stringCellValue;
									if(comment.startsWith("%")) {
										thisStmt.append("% domain_replace('" +
												escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
												escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
									}
									/* Processing Prolog variables type and name like $CUI */
									if(fromName.startsWith("$") && fromCUI.startsWith("$") && toName.startsWith("$") && toCUI.startsWith("$")) {
										// log.debug("Processing Prolog variables type and name like $CUI ");
										String realFromName = fromName.substring(1);
										String realFromCUI = fromCUI.substring(1);
										String realToName = toName.substring(1);
										String realToCUI = toCUI.substring(1);
										thisStmt.append("domain_replace(" +
												escapeQuote(realFromName) + ":" + realFromCUI + ":[" + fromType +  "]," +
												escapeQuote(realToName) + ":" + realToCUI + ":[" + toType + "]).");
										/* log.debug(domainName + "_domain_replace(" +
												escapeQuote(realFromName) + ":" + realFromCUI + ":[" + fromType +  "]," +
												escapeQuote(realToName) + ":" + realToCUI + ":[" + toType + "])."); */
									} else {
										// log.debug("Get the CUI info from SemDB");
										CUIInfo cinfo = SemMedDB.getConceptSemtypeInfo(fromCUI);
										boolean sameType = compareSemtype(cinfo.getStype(), fromType);
										if(fromName.equals(cinfo.getPname()) && sameType) {
											thisStmt.append("domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
											String newToType = sortTypes(toType);
											String newTriple = new String(toName + "|" + toCUI + "|" + newToType);
											// log.debug("Newly replaced concept |" + newTriple);
											newTripleHash.add(newTriple);
										} else if(!fromName.equals(cinfo.getPname())) {
											thisStmt.append("% ---Wrong Concept name for \"" + fromCUI + "\" in Replace---------\n" +
													"% ---Correct Concept name is \"" + cinfo.getPname() + "\"---------\n" +
													"% domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
											errorFileStmt.append("% ---Wrong Concept name for \"" + fromCUI + "\" in Replace---------\n" +
													"% ---Correct Concept name is \"" + cinfo.getPname() + "\"---------\n" +
													"%domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).\n\n");
										}  else if(!sameType) {
											thisStmt.append("% ---Wrong type name for \"" + fromCUI + "\" used in Replace---------\n" +
													"% ---Correct type is \"" + makeString(cinfo.getStype()) + "\"---------\n" +
													"% domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
											errorFileStmt.append("% ---Wrong type name for \"" + fromCUI + "\" used in Replace---------\n" +
													"% ---Correct type is \"" + makeString(cinfo.getStype()) + "\"---------\n" +
													"% domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).\n\n");
										}
									}
								} else if (k==8) { // process the comment at the end
									thisStmt.append(" " + stringCellValue);
									// log.debug(thisStmt.toString());
								}
							}
							j++;
							k++;
						}
						if(thisStmt.length() > 0) {
							domainFileStmt.append(thisStmt);
							thisStmt = null;
							thisStmt = new StringBuffer();
						}
						// log.debug("end of replace");

						String Name = null;
						String preName = null;
						String cui = null;
						j=0;
						k=0;
						while (j < cellRowListConcept.size()) {
							String stringCellValue =  cellRowListConcept.get(j);
							// log.debug("j = " + j + ", cellValue = " + stringCellValue);
							if(stringCellValue.equals("||")) {
								if(k <=2 && comment != null)
									thisStmt.append(comment);
								k=0;
								if(thisStmt.length() > 0)
									domainFileStmt.append(thisStmt +"\n");
								thisStmt = null;
								thisStmt = new StringBuffer();
							} else {
								if(k==1)
									comment = stringCellValue;
								else if(k==2)
									Name = stringCellValue.trim();
								else if (k==3)
									preName = stringCellValue.trim();
								else if (k==4)
									cui = stringCellValue.trim();
								else if (k==5) {
									stype = stringCellValue.trim();
									if(comment.startsWith("%")) {
										thisStmt.append("% domain_concept('" +
												escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
									}
									CUIInfo cinfo = SemMedDB.getConceptSemtypeInfo(cui);
									boolean sameType = compareSemtype(cinfo.getStype(), stype);
									String CUIWithPRE = new String(cui.trim() + "||" + preName.trim() + "||" + stype.trim());
									// log.debug(CUIWithPRE);
									// if(conceptCUIHash.contains(cui) && conceptCUIWithPREHash.add(CUIWithPRE)) {
									if(cui.startsWith("C")) {
										String triple = new String(preName + "|" + cui + "|" + sortTypes(stype));
										// log.debug("Checking concept starting with C |" + triple);
										if(newTripleHash.contains(triple)) { // The triple was declared in the second part of concept_replace
											thisStmt.append("domain_concept('" +
													escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
										} else {
											// log.debug("Triple is not defined bu concept_replace, so checking DB with cui :" + cui);
											ResultSet rs = stmt.executeQuery(queryBuf + cui + "\"");
											if(rs.first()) {
												String preferredDB = rs.getString(1);
												String cuiDB = rs.getString(2);
												StringBuffer typeDBBuf = new StringBuffer(rs.getString(3));
												while(rs.next()) {
													typeDBBuf.append("," + rs.getString(3));
												}
												rs.close();
												String typeDB = typeDBBuf.toString();
												// log.debug("In DB : " + preferredDB + "|" + cuiDB + "|" + typeDB);
												// log.debug("In DB: " + preferredDB + " | " + cuiDB + " | " + typeDB);
												// log.debug("From domain_concept: " + preName + "|" + cui + "|" + stype);
												String sortedSType = sortTypes(stype);
												if(preName.trim().equals(preferredDB) && cui.trim().equals(cuiDB) && sortedSType.trim().equals(typeDB)) {
													thisStmt.append("domain_concept('" +
															escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
												} else {
													 thisStmt.append("% --- the concept is defined differently in UMLS 2006AA. Either preferred name or semantic type is incorrectly used ---------\n" +
																"% domain_concept('" +
																escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).");
													 errorFileStmt.append("% --- the concept is defined differently in UMLS 2006AA. Either preferred name or semantic type is incorrectly used ---------\n" +
																"% domain_concept('" +
																escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
												}
											} else { // there is no Database definitiosn for the CUI
												thisStmt.append("% --- the CUI is not defined in the UMLS 2006AA ---------\n" +
														"% domain_concept('" +
														escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
												errorFileStmt.append("% --- the CUI is not defined in the UMLS 2006AA ---------\n" +
														"% domain_concept('" +
														escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
											}
										}
									} else if(!cui.startsWith("C") && !conceptCUIHash.contains(cui) && !preferredConceptHash.contains(preName.trim()) ) {
										thisStmt.append("domain_concept('" +
												escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
										conceptCUIHash.add(cui);
										conceptCUIWithPREHash.add(CUIWithPRE);
										preferredConceptHash.add(preName.trim());
									} else if(!cui.startsWith("C") &&
											( conceptCUIHash.contains(cui) && conceptCUIWithPREHash.contains(CUIWithPRE))) {
										thisStmt.append("domain_concept('" +
												escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
									} else {
										 thisStmt.append("% --- Redefinition of the same CUI with different preferred name or semantic type ---------\n" +
													"% domain_concept('" +
													escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).");
										 errorFileStmt.append("% --- Redefinition of the same CUI with different preferred name or semantic type ---------\n" +
													"% domain_concept('" +
													escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
									}
								} else if(k==6) {
									thisStmt.append(" " + stringCellValue);
									// log.debug(thisStmt.toString());
								}
							}
							j++;
							k++;
						}
						if(thisStmt.length() > 0) {
							domainFileStmt.append(thisStmt);
							thisStmt = null;
							thisStmt = new StringBuffer();
						}
				domainInput.close();
				log.debug("write domain Prolog file");


			String filename = "/download/" + domainName + "_domain.pl";
			String xmlrealpath = ctx.getRealPath(filename);
			// log("XML context path " + xmlcontextpath);
			log.debug("XML real path " + xmlrealpath);
			PrintWriter domainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(xmlrealpath)));
			domainfile.println(domainFileStmt.toString());
			domainFileStmt = null;
			domainfile.close();
		}

			if(locsemnetFile != null) {
				InputStream locsemnetInput = locsemnetFile.getInputStream();
				POIFSFileSystem replaceFileSystem = new POIFSFileSystem(locsemnetInput);
				HSSFWorkbook replaceWorkBook = new HSSFWorkbook(replaceFileSystem);
				HSSFSheet replaceSheet = replaceWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				log.debug("Read locsemnetFile file");
				int it = 0;
				List<String> cellRowListPreferred = new ArrayList<String>();
				List<String> cellRowListInverse = new ArrayList<String>();
				List<String> cellRowListSemnet = new ArrayList<String>();
				int stage = 0;

				locsemnetFileStmt.append("%      Do Not Modify This File    %\n" +
						"%     It is machine generated.    %\n" +
						":- module(locsemnet_DOM, [\n" +
						"\t\tlocal_preferred_relation_DOM/1,\n" +
						"\t\tlocal_relation_inverse_DOM/2,\n" +
						"\t\tlocal_semnet_DOM/3\n" +
						"%\tlocal_semnet_1/3\n\t]).\n\n" +
						":- load_files( usemrep_lib(module_version), [\n" +
						"\t\twhen(compile_time)\n\t]).\n\n" +
						":- use_module( usemrep_lib( semnet_access ),[\n" +
						"\t\tpreferred_relation/2,\n" +
						"\t\trelation_inverse/3\n\t]).\n\n" +
						":- use_module( usemrep_lib(module_version), [\n" +
						"\t\tglobal_module_version/1\n\t]).\n\n" +
						"local_semnet_DOM(Type1, Relation, Type2) :-\n" +
						"\t( Relation == 'ISA' ->\n" +
						"\t  true \n" +
						"\t; local_semnet_1_DOM(Type1, Relation, Type2) ->\n" +
						"\t  true\n" +
						"\t; local_relation_inverse_DOM(Relation, Inverse) ->\n" +
						"\t  local_semnet_1_DOM(Type2, Inverse, Type1)\n" +
						"\t; Relation \\== unspecified_relation ->\n" +
						"%\t  format('~n~n### ERROR in locsemnet: ~q is neither preferred nor inverse relation.~n~n',\n" +
						"%\t	 [Relation]),\n" +
						"\t  fail\n\t\t).\n\n" +
					"local_relation_inverse_DOM(Relation, Inverse) :-\n" +
					"\tglobal_module_version(Version),\n" +
					"\t	( relation_inverse(Version, Relation, Inverse) ->\n" +
					"\t	  true\n" +
					"\t      ; local_relation_inverse_1_DOM(Relation, Inverse)\n\t).\n\n" +
					"local_preferred_relation_DOM(Relation) :-\n" +
					"\tglobal_module_version(Version),\n" +
					"\t	( preferred_relation(Version, Relation) ->\n" +
					"\t	  true\n" +
					"\t       ; local_preferred_relation_1_DOM(Relation)\n).\n\n");

				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					// log.debug("Read row " + it);
					if(stage ==1)
						cellRowListPreferred.add("||");
					else if(stage == 2)
						cellRowListInverse.add("||");
					else if(stage ==3)
						cellRowListSemnet.add("||");
					int columnI = 0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_local_preferred_relation_1")) {
							stage = 1;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_local_relation_inverse_1")) {
							stage = 2;
							// log.debug("Processing Replace");
						} else if(myCell.toString().equals("_local_semnet_1")) {
							stage = 3;
							// log.debug("Processing Semtype");
						} else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListPreferred.add(myCell.toString());
								} else if(stage == 2) {
								// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListInverse.add(myCell.toString());
								} else if(stage == 3) {
								// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListSemnet.add(myCell.toString());
								} else {
									locsemnetFileStmt.append(myCell.toString() +"\n");
								}
							} else if(columnI <  myCell.getColumnIndex()) { // If there is nothing in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListPreferred.add("");
									cellRowListPreferred.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListInverse.add("");
									cellRowListInverse.add(myCell.toString());
								} else if(stage == 3){
									// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListSemnet.add("");
									cellRowListSemnet.add(myCell.toString());
								} else {
									locsemnetFileStmt.append(myCell.toString() +"\n");
								}
								columnI++;
							}
						}
						columnI++;
					}
				}

				int j=0;
				int k=0;
				String comment = null;
				StringBuffer thisStmt = new StringBuffer();
				while(j < cellRowListPreferred.size()) {
					String stringCellValue =  cellRowListPreferred.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							locsemnetFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2) {
							if(comment.startsWith("%"))
								thisStmt.append("%local_preferred_relation_1_DOM(" +
										stringCellValue +  ").");
							else
								thisStmt.append("local_preferred_relation_1_DOM(" +
										stringCellValue +  ").");
						} else if(k==3) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					locsemnetFileStmt.append(thisStmt +"\n");

				String Name = null;
				String invName = null;
				j=0;
				k=0;
				while(j < cellRowListInverse.size()) {
					String stringCellValue =  cellRowListInverse.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							locsemnetFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						if(k==2)
							Name = stringCellValue;
						else if (k==3) {
							invName = stringCellValue;;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%local_relation_inverse_1_DOM(" +
									Name + "," + invName + ").");
							else
								thisStmt.append("local_relation_inverse_1_DOM(" +
									Name + "," + invName + ").");
						} else if(k==4) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					locsemnetFileStmt.append(thisStmt +"\n");

				String subj = null;
				String predicate = null;
				String obj = null;
				j=0;
				k=0;
				while(j < cellRowListSemnet.size()) {
					String stringCellValue = cellRowListSemnet.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							locsemnetFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							subj = stringCellValue;
						else if (k==3)
							predicate = stringCellValue;
						else if (k==4) {
							obj = stringCellValue;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%local_semnet_1_DOM(" +
									subj + "," + predicate + "," + obj + ").");
							else {
								String tripleStr = new String(subj + "-" + predicate + "-" + obj);

								if(LocsemnetTripleHash.contains(tripleStr)) {
									errorFileStmt.append("ERROR: " + tripleStr + "  is duplicated in locsemnet file.\n");
								} else {
									thisStmt.append("local_semnet_1_DOM(" +
											subj + "," + predicate + "," + obj + ").");
									LocsemnetTripleHash.add(tripleStr);
								}
							}			
						} else if(k==5) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					locsemnetFileStmt.append(thisStmt +"\n");
				locsemnetInput.close();

			String locsemnetFilename = "/download/" + domainName + "_locsemnet.pl";
			String locsemnetxmlrealpath = ctx.getRealPath(locsemnetFilename);
			// log("XML context path " + xmlcontextpath);
			log.debug("XML real path " +locsemnetxmlrealpath);
			PrintWriter locsemnetdomainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(locsemnetxmlrealpath)));
			locsemnetdomainfile.println(locsemnetFileStmt.toString());
			locsemnetFileStmt = null;
			locsemnetdomainfile.close();
			}

			// processing semrules
			if(semrulesFile != null) {
				InputStream semrulesInput = semrulesFile.getInputStream();
				POIFSFileSystem replaceFileSystem = new POIFSFileSystem(semrulesInput);
				HSSFWorkbook replaceWorkBook = new HSSFWorkbook(replaceFileSystem);
				HSSFSheet replaceSheet = replaceWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				log.debug("Read semruleFile file");
				int it = 0;
				// List<String> cellRowListCorrespond = new ArrayList<String>();

				int stage = 0;
				/* while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					cellRowListCorrespond.add("||");
					// log.debug("Read row " + it);
					int columnI=0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_word_corresponds_to_semnet_relation_DOM")) {
							stage = 1;
							// log.debug("Processing Concepts");
						} else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListCorrespond.add(myCell.toString());
								}
							} else if(columnI <  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListCorrespond.add("");
									cellRowListCorrespond.add(myCell.toString());
								}
								columnI++;
							}
						}
						columnI++;
					}

				}
				int k=0;
				int j=0;
				String predicate = null;
				String first = null;
				String second = null;
				String third = null;
				String comment = null;
				StringBuffer thisStmt = new StringBuffer();
				semrulesFileStmt.append("%      Do Not Modify This File    %\n" +
						"%     It is machine generated.    %\n" +
						":- module(semrules_DOM,	[\n" +
						"\tword_corresponds_to_semnet_relation_DOM/4," +
						"\tmultiphrase_corresponds_to_semnet_relation_DOM/6," +
						"\tphrase_corresponds_to_semnet_relation_DOM/6" +
						"\n]).\n\n" +
						"multiphrase_corresponds_to_semnet_relation_DOM(_, _, _, _, _, _) :- !, fail.\n\n" +
						"phrase_corresponds_to_semnet_relation_DOM(_, _, _, _, _, _) :- !, fail.\n\n" +
						"% ----- Source Code Control System\n" +
						"%\n" +
						"%  Set of current semantic types, many not in UMLS Semantic Network\n" +
						"%  Needs to be manually updated\n" +
						"%  word_corresponds_to_semnet_relation_DOM( ?Word, ?POS, ?Cue, ?Relation )\n%\n\n");

				while(j < cellRowListCorrespond.size()) {
					String stringCellValue =  cellRowListCorrespond.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							semrulesFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2) {

							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								predicate = new String("'" + stringCellValue + "'");
							else
								predicate = stringCellValue;
						} else if (k==3) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								first = new String("'" + stringCellValue + "'");
							else
								first = stringCellValue;
						} else if (k==4) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								second = new String("'" + stringCellValue + "'");
							else
								second = stringCellValue;
						} else if (k==5) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								third = new String("'" + stringCellValue + "'");
							else
								third = stringCellValue;
							if(comment != null && comment.startsWith("%")) {
								thisStmt.append("%word_corresponds_to_semnet_relation_DOM(" +
									predicate + "," + first + "," + second + "," + third + ").");
							} else
								thisStmt.append("word_corresponds_to_semnet_relation_DOM(" +
										predicate + "," + first + "," + second + "," + third + ").");
						} else if(k==6) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					j++;
					k++;
				} */
				List<String> cellRowListMultiphrase = new ArrayList<String>();
				List<String> cellRowListPhrase = new ArrayList<String>();
				List<String> cellRowListWord = new ArrayList<String>();
				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					if(stage ==1)
						cellRowListMultiphrase.add("||");
					else if(stage == 2)
						cellRowListPhrase.add("||");
					else if(stage ==3)
						cellRowListWord.add("||"); 
					int columnI = 0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						// log.debug(myCell.toString());
						if(myCell.toString().equals("_multiphrase_corresponds_to_semnet_relation_DOM")) {
							stage = 1;
							// log.debug("Processing _multiphrase_corresponds_to_semnet_relation_DOM");
						} else if(myCell.toString().equals("_phrase_corresponds_to_semnet_relation_DOM")) {
							stage = 2;
							// log.debug("_phrase_corresponds_to_semnet_relation_DOM");
						} else if(myCell.toString().equals("_word_corresponds_to_semnet_relation_DOM")) {
							stage = 3;
							// log.debug("_word_corresponds_to_semnet_relation_DOM");
							// log.debug("Processing Concepts");
						} else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListMultiphrase.add(myCell.toString());
								} else if(stage == 2) {
								// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListPhrase.add(myCell.toString());
								} else if(stage == 3){
								// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListWord.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										semrulesFileStmt.append(myCell.toString() +"\n");
								}
							} else if(columnI <  myCell.getColumnIndex()) { // If there is nothing in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListMultiphrase.add("");
									cellRowListMultiphrase.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListPhrase.add("");
									cellRowListPhrase.add(myCell.toString());
								} else if(stage == 3){
									// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListWord.add("");
									cellRowListWord.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										semrulesFileStmt.append(myCell.toString() +"\n");
								}
								columnI++;
							}
						}							
						columnI++;
					}

				}
				int k=0;
				int j=0;
				String first = null;
				String second = null;
				String third = null;
				String fourth = null;
				String fifth = null;
				String sixth = null;
				String comment = null;
				System.out.println("Read completed of Semrule Excel files");
				StringBuffer thisStmt = new StringBuffer();
				semrulesFileStmt.append("%      Do Not Modify This File    %\n" +
						"%     It is machine generated.    %\n" +
						":- module(semrules_DOM,	[\n" +
						"\tword_corresponds_to_semnet_relation_DOM/4,\n" +
						"\tmultiphrase_corresponds_to_semnet_relation_DOM/6,\n" +
						"\tphrase_corresponds_to_semnet_relation_DOM/6" +
						"\n]).\n\n");

				while(j < cellRowListMultiphrase.size()) {
					String stringCellValue =  cellRowListMultiphrase.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							semrulesFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							first = stringCellValue;
						else if (k==3)
							second = stringCellValue;
						else if (k==4)
							third = stringCellValue;
						else if (k==5) 
							fourth = stringCellValue;
						else if (k == 6)
							fifth = stringCellValue;
						else if(k == 7) {
							sixth = stringCellValue;
							if(comment != null && comment.startsWith("%")) {
								thisStmt.append("%multiphrase_corresponds_to_semnet_relation_DOM(" +
										first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
							} else
								thisStmt.append("multiphrase_corresponds_to_semnet_relation_DOM(" +
										first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
						} else if(k==8) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					k++;
					j++;
				}
				thisStmt.append("multiphrase_corresponds_to_semnet_relation_DOM(_, _, _, _, _, _) :- fail.\n\n");
				System.out.println("Done with multiphrase_corresponds_to_semnet_relation_DOM");
				j=0;
				k=0;
				while(j < cellRowListPhrase.size()) {
					String stringCellValue =  cellRowListPhrase.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							semrulesFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							first = stringCellValue;
						else if (k==3)
							second = stringCellValue;
						else if (k==4)
							third = stringCellValue;
						else if (k==5) 
							fourth = stringCellValue;
						else if (k == 6)
							fifth = stringCellValue;
						else if(k == 7) {
							sixth = stringCellValue;
							if(comment != null && comment.startsWith("%")) {
								thisStmt.append("%phrase_corresponds_to_semnet_relation_DOM(" +
									first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
							} else
								thisStmt.append("phrase_corresponds_to_semnet_relation_DOM(" +
										first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
						} else if(k==8) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					k++;
					j++;
				}
				thisStmt.append("phrase_corresponds_to_semnet_relation_DOM(_, _, _, _, _, _) :- fail.\n\n");
				j=0;
				k=0;
				while(j < cellRowListWord.size()) {
					String stringCellValue =  cellRowListWord.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							semrulesFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2) {
							/** Error fix: May 23 2016
							 * If cell value has space in it and it is not surrounded by ' nor "",
							 * then it has to be enclosed by "'" since Prolog does not allow a name that has space 
							 */
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								first = new String("'" + stringCellValue + "'");
							else
								first = stringCellValue;
						} else if (k==3) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								second = new String("'" + stringCellValue + "'");
							else
								second = stringCellValue;
						} else if (k==4) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								third = new String("'" + stringCellValue + "'");
							else
								third = stringCellValue;
						} else if (k==5) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								fourth = new String("'" + stringCellValue + "'");
							else
								fourth = stringCellValue;
							if(comment != null && comment.startsWith("%")) {
								thisStmt.append("%word_corresponds_to_semnet_relation_DOM(" +
										first + "," + second + "," + third + "," + fourth +  ").");
							} else
								thisStmt.append("word_corresponds_to_semnet_relation_DOM(" +
										first + "," + second + "," + third + "," + fourth +  ").");
						} else if(k==6) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					k++;
					j++;
				}
				if(thisStmt.length() > 0)
					semrulesFileStmt.append(thisStmt +"\n\n");
				
				/* semrulesFileStmt.append("% stubs\n\n" +
				"multiphrase_corresponds_to_semnet_relation_DOM(_, _, _, _, _, _) :- fail.\n\n" +
				"phrase_corresponds_to_semnet_relation_DOM(_, _, _, _, _, _) :- fail.\n"); */
				semrulesInput.close();

				String semrulesFilename = "/download/" + domainName + "_semrules.pl";
				String locsemnetxmlrealpath = ctx.getRealPath(semrulesFilename);
				PrintWriter semrulesfile
					= new PrintWriter(new BufferedWriter(new FileWriter(locsemnetxmlrealpath)));
				semrulesfile.println(semrulesFileStmt.toString());
				semrulesFileStmt = null;
				semrulesfile.close();
			}

			if(exceptionFile != null) {
				InputStream exceptionInput = exceptionFile.getInputStream();
				POIFSFileSystem exceptionFileSystem = new POIFSFileSystem(exceptionInput);
				HSSFWorkbook replaceWorkBook = new HSSFWorkbook(exceptionFileSystem);
				HSSFSheet replaceSheet = replaceWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				int it = 0;
				List<String> cellRowListEmptyHead = new ArrayList<String>();
				List<String> cellRowListEmptyHead2N = new ArrayList<String>();
				List<String> cellRowListIgnore1 = new ArrayList<String>();
				List<String> cellRowListIgnore2N = new ArrayList<String>();
				List<String> cellRowListIgnoreType = new ArrayList<String>();
				exceptionFileStmt.append("%      Do Not Modify This File    %\n" +
						"%     It is machine generated.    %\n" +
						":- module( exceptions_DOM, [\n" +
						"\t\tconcept_to_ignore_DOM/2,\n" +
		                "\t\tconditional_empty_head_base_1_DOM/2,\n" +
		                "\t\tconditional_empty_head_base_2N_DOM/3,\n" +
						"\t\tempty_head_base_1_DOM/1,\n" +
						"\t\tempty_head_base_2N_DOM/2,\n" +
						"\t\tignore_semnet_access_term_DOM/1,\n" +
						"\t\tignore_type_relation_type_DOM/3,\n" +
		                "\t\tnon_prepositionally_cued_object_DOM/1,\n" +
		                "\t\tnon_prepositionally_cued_subject_DOM/1,\n" +
						"\t transform_semnet_access_term_DOM/2,\n" +
						"\t transform_type_relation_type_DOM/6\n]).\n\n" +
						":- use_module( skr_lib( sicstus_utils ), [\n" +
						"\t lower/2\n]).\n\n" +
						"conditional_empty_head_base_1_DOM(_, _) :- !, fail.\n\n" +
						"conditional_empty_head_base_2N_DOM(_, _, _) :- !, fail.\n\n" +
						"non_prepositionally_cued_object_DOM(_) :- !, fail.\n\n" +
						"non_prepositionally_cued_subject_DOM(_) :- !, fail.\n\n" 
						);
				int stage = 0;
				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					if(stage ==1)
						cellRowListEmptyHead.add("||");
					else if(stage ==2)
						cellRowListEmptyHead2N.add("||");
					else if(stage ==3)
						cellRowListIgnore1.add("||");
					else if(stage ==4)
						cellRowListIgnore2N.add("||");
					else if(stage ==5)
						cellRowListIgnoreType.add("||");
					// log.debug("Read row " + it);
					int columnI=0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_empty_head_base_1_DOM")) {
							stage = 1;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_empty_head_base_2N_DOM")) {
							stage = 2;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_concept_to_ignore_1")) {
							stage = 3;
							// log.debug("Processing Replace");
						} else if(myCell.toString().equals("_concept_to_ignore_2N")) {
							stage = 4;
							// log.debug("Processing Semtype");
						} else if(myCell.toString().equals("_ignore_type_relation_type_1")) {
							stage = 5;
							// log.debug("Processing Semtype");
						} else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListEmptyHead.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListEmptyHead2N.add(myCell.toString());
								}else if(stage == 3) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListIgnore1.add(myCell.toString());
								} else if(stage == 4) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListIgnore2N.add(myCell.toString());
								}else if(stage == 5 ){
									// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListIgnoreType.add(myCell.toString());
								} else {
									exceptionFileStmt.append(myCell.toString() +"\n");
								}
							} else if(columnI <  myCell.getColumnIndex()) {
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
										cellRowListEmptyHead.add("");
										cellRowListEmptyHead.add(myCell.toString());
								} else if(stage == 2) {
										// log.debug("Add " + myCell.toString() + " to Preferred list");
										cellRowListEmptyHead2N.add("");
										cellRowListEmptyHead2N.add(myCell.toString());
								}else if(stage == 3) {
										// log.debug("Add " + myCell.toString() + " to Replace list");
										cellRowListIgnore1.add("");
										cellRowListIgnore1.add(myCell.toString());
								} else if(stage == 4) {
										// log.debug("Add " + myCell.toString() + " to Replace list");
										cellRowListIgnore2N.add("");
										cellRowListIgnore2N.add(myCell.toString());
								}else if(stage == 5) {
										// log.debug("Add " + myCell.toString() + " to Semtype list");
										cellRowListIgnoreType.add("");
										cellRowListIgnoreType.add(myCell.toString());
								} else {
									exceptionFileStmt.append(myCell.toString() +"\n");
								}
								columnI++;
							}
						}
						columnI++; 
					}
				}
				int k=0;
				int j=0;
				String comment = null;
				StringBuffer thisStmt = new StringBuffer();
				while(j<cellRowListEmptyHead.size()) {
					String stringCellValue =  cellRowListEmptyHead.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k == 2) {
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%empty_head_base_1_DOM(" +
										nameQuote(escapeQuote(stringCellValue)) +  ").");
							else
								thisStmt.append("empty_head_base_1_DOM(" +
										nameQuote(escapeQuote(stringCellValue)) +  ").");
						} else if(k==3)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of empty_head_base_1");

				String Name1 = null;
				String Name2 = null;
				j=0;
				k=0;
				while(j<cellRowListEmptyHead2N.size()) {
					String stringCellValue  =  cellRowListEmptyHead2N.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							Name1 = stringCellValue;
						else if(k==2)
							Name1 = stringCellValue;
						else if (k==3){
							Name2 = stringCellValue;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%empty_head_base_2N_DOM(" +
										nameQuote(Name1) + "," + nameQuote(Name2) + ").");
							else
								thisStmt.append("empty_head_base_2N_DOM(" +
										nameQuote(Name1) + "," + nameQuote(Name2) + ").");
						} else if(k==4)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of empty_head_base_2N");

				exceptionFileStmt.append("concept_to_ignore_DOM([Base|RestBases], MetaConc) :-\n" +
						"\tlower(Base, LCBase),\n" +
						"\tconcept_to_ignore_aux(RestBases, LCBase, MetaConc).\n\n" +
						"concept_to_ignore_aux([], Base, MetaConc) :-\n" +
						"\tconcept_to_ignore_1(Base, MetaConc).\n" +
						"concept_to_ignore_aux([Base2|RestBases], Base1, MetaConc) :-\n" +
						"\tconcept_to_ignore_2N(Base1, [Base2|RestBases], MetaConc).\n\n"); 

				k=0;
				j=0;
				while(j<cellRowListIgnore1.size()) {
					String stringCellValue =  cellRowListIgnore1.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							Name1 = stringCellValue;
						else if (k==3) {
							Name2 = stringCellValue;
							StringBuffer queryBuf2 = new StringBuffer("select CUI from CONCEPT where PREFERRED_NAME = BINARY \"");
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%concept_to_ignore_1(" +
										nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2))+ ").");
							else {
								ResultSet rs = stmt.executeQuery(queryBuf2 + Name2 + "\"");
								if(rs.first()) {
										thisStmt.append("concept_to_ignore_1(" +
												nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2))+ ").");
								} else {
									thisStmt.append("% Error: Preferred name: " + Name2 + " is not in UMLS 2006AA\n");
									thisStmt.append("%concept_to_ignore_1(" +
											nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2)) + ").\n\n");
									errorFileStmt.append("% Error: Preferred name: " + Name2 + " is not in UMLS 2006AA\n");
									errorFileStmt.append("%concept_to_ignore_1(" +
											nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2)) + ").\n\n");
								}
							}
						} else if(k==4)
									thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of concept_to_ignore_1");

				String name1 = null;
				String name2 = null;
				String name3 = null;
				j=0;
				k=0;
				if(cellRowListIgnore2N.size() <= 0) {
					exceptionFileStmt.append("concept_to_ignore_2N(_Base1,_Base2,_MetaConc).\n\n");
				} else {
				  while(j < cellRowListIgnore2N.size()) {
					String stringCellValue =  cellRowListIgnore2N.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							name1 = stringCellValue;
						else if (k==3)
							name2 = stringCellValue;
						else if (k==4) {
							name3 = stringCellValue;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%concept_to_ignore_2N(" +
									nameQuote(name1) + "," + name2 + "," + nameQuote(name3) + ").");
							else
								thisStmt.append("concept_to_ignore_2N(" +
										nameQuote(name1) + "," + name2 + "," + nameQuote(name3) + ").");
						} else if(k==5)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				  } // while
				} // else
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of concept_to_ignore_2N");

				String subj = null;
				String predicate = null;
				String obj = null;
				exceptionFileStmt.append(
						"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +
						"%%%% These next predicates are for transforming\n" +
						"%%%% terms of the form\n" +
						"%%%%    * preferred_relation/1,\n" +
						"%%%%    * relation_inverse/2, and\n" +
						"%%%%    * type_relation_type/3,\n" +
						"%%%% all of which are defined in semnet_accessXX.pl.\n" +
						"%%%% These predicates are called from pre_compilation.pl.\n" +
						"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n" +
						"\ntransform_semnet_access_term_DOM(preferred_relation(Rel),\n" +
					     "\t\tpreferred_relation(NewRel)) :-\n" +
					     "\t( transform_preferred_relation_DOM(Rel, NewRel) ->\n" +
					     "\t  true\n" +
					     "\t; NewRel = Rel\n\t).\n\n" +
					     "transform_semnet_access_term_DOM(relation_inverse(Rel,    InverseRel),\n" +
					     "\t\trelation_inverse(NewRel, NewInverseRel)) :-\n" +
					     "\t( transform_relation_inverse_DOM(Rel, InverseRel, NewRel, NewInverseRel) ->\n" +
					     "\t  true\n" +
					     "\t; NewRel = Rel,\n" +
					     "\t  NewInverseRel = InverseRel\n\t).\n\n" +
					     "transform_semnet_access_term_DOM(type_relation_type(Type1,    Rel,    Type2),\n" +
					     "\t\ttype_relation_type(NewType1, NewRel, NewType2)) :-\n" +
			 			"\t(transform_type_relation_type_DOM(Type1, Rel, Type2, NewType1, NewRel, NewType2) ->\n" +
			 			"\t  true\n" +
			 			"\t; NewType1 = Type1,\n" +
			 			"\t  NewRel = Rel,\n" +
			 			"\t  NewType2 = Type2\n\t).\n\n" +
			 			"ignore_semnet_access_term_DOM(preferred_relation(Rel)) :-\n" +
			 			"\tignore_preferred_relation_DOM(Rel).\n\n" +
			 			"ignore_semnet_access_term_DOM(relation_inverse(Rel, InverseRel)) :-\n" +
			 			"\tignore_relation_inverse_DOM(Rel, InverseRel).\n\n" +
			 			"ignore_semnet_access_term_DOM(type_relation_type(Type1, Rel, Type2)) :-\n" +
			 			"\tignore_type_relation_type_DOM(Type1, Rel, Type2).\n\n" +
			 			"transform_preferred_relation_DOM('co-occurs_with', coexists_with).\n\n" +
			 			"ignore_preferred_relation_DOM(associated_with).\n\n" +
			 			"transform_relation_inverse_DOM('co-occurs_with', 'co-occurs_with', coexists_with, coexists_with).\n\n" +
			 			"% associated_with becomes ROOT_RELATION, which we ignore\n\n" +
			 			"ignore_relation_inverse_DOM(associated_with,  associated_with).\n\n" +
			 			"transform_type_relation_type_DOM(Type1,    Rel,    Type2,\n" +
					    "\t\tNewType1, NewRel, NewType2) :-\n" +
					    "\ttransform_preferred_relation_if_possible(Type1, NewType1),\n" +
					    "\ttransform_preferred_relation_if_possible(Rel,   NewRel),\n" +
					    "\ttransform_preferred_relation_if_possible(Type2, NewType2).\n\n" +
					    "transform_preferred_relation_if_possible(Rel, TransformedRel) :-\n" +
					    "\t( transform_preferred_relation_DOM(Rel, TransformedRel) ->\n" +
					    "\t  true\n" +
					    "\t; TransformedRel = Rel\n).\n\n" +
					    "ignore_type_relation_type_DOM(Type1, Relation, Type2) :-\n" +
					    "\t( ignore_preferred_relation_DOM(Type1)    ->\n" +
					    "\t  true\n" +
					    "\t; ignore_preferred_relation_DOM(Relation) ->\n" +
					    "\t  true\n" +
					    "\t; ignore_preferred_relation_DOM(Type2)    ->\n" +
					    "\t  true\n" +
					    "\t; ignore_type_relation_type_1_DOM(Type1, Relation, Type2)\n).\n\n");
				j=0;
				k=0;
				while(j<cellRowListIgnoreType.size()) {
					String stringCellValue =  cellRowListIgnoreType.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							subj = stringCellValue;
						else if (k==3)
							predicate = stringCellValue;
						else if (k==4) {
							obj = stringCellValue;;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%ignore_type_relation_type_1_DOM(" + 
										subj + "," + predicate + "," + obj + ").");
							else {
								String exceptionTriple = new String(subj + "-" + predicate + "-" + obj);	

								if (ExceptionTripleHash.contains(exceptionTriple)) {
									errorFileStmt.append("ERROR: The triple " + exceptionTriple + " is duplicated in Exceptions files.\n");
								} else {
									thisStmt.append("ignore_type_relation_type_1_DOM(" +
											subj + "," + predicate + "," + obj + ").");
									ExceptionTripleHash.add(exceptionTriple);
								}								
							}
						} else if(k==5)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				exceptionInput.close();
				log.debug("ignore_type_relation_type_1");

			String exceptionFilename = "/download/" + domainName + "_exceptions.pl";
			String exceptionxmlrealpath = ctx.getRealPath(exceptionFilename);
			// log("XML context path " + xmlcontextpath);
			log.debug("XML real path " + exceptionxmlrealpath);
			PrintWriter exceptiondomainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(exceptionxmlrealpath)));
			exceptiondomainfile.println(exceptionFileStmt.toString());
			exceptionFileStmt = null;
			exceptiondomainfile.close();
			}

			session.setAttribute("domain", domainName);
			String errorFilename = "/download/error_" + domainName + ".txt";
			String errorxmlrealpath = ctx.getRealPath(errorFilename);
			PrintWriter errordomainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(errorxmlrealpath)));
			errordomainfile.println(errorFileStmt.toString());
			errordomainfile.close();
			stmt.close();
			// con.close();

			String path = ctx.getRealPath("/download");
			File directory = new File(path);
			String[] files = directory.list();             //
			// Checks to see if the directory contains some files.            //            if (files != null && files.length > 0) {                 //                // Call the zipFiles method for creating a zip stream.                //
			byte[] zip = zipFiles(directory, files);                 //                // Sends the response back to the user / browser. The                // content for zip file type is "application/zip". We                // also set the content disposition as attachment for                // the browser to show a dialog that will let user                 // choose what action will he do to the sent content.                //
			ServletOutputStream sos = response.getOutputStream();
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\"prolog.zip\"");
			sos.write(zip);
			sos.flush();
			sos.close();

		} catch(Exception e) {  }
		// return mapping.findForward("success");
		return null;
	}
	
	public ActionForward uploadGENExcel(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
		throws PubmedException, EssieException, SemMedException {
		HttpSession session = request.getSession();
		String UMLSyear = request.getParameter("year");
		ServletContext ctx  = session.getServletContext();
		InputForm inputForm = (InputForm) form;
		FormFile domainFile = inputForm.getUploadExcelDomainFile();
		FormFile exceptionFile = inputForm.getUploadExcelExceptionFile();
		FormFile locsemnetFile = inputForm.getUploadExcelLocsemnetFile();
		FormFile semrulesFile = inputForm.getUploadExcelSemrulesFile(); 
		FormFile semnetaccessFile = inputForm.getUploadSemnetaccessFile(); 
		log.debug("Input Domain file = " + domainFile.getFileName());
		log.debug("Input Locsemnet file = " + locsemnetFile.getFileName());
		log.debug("Input Semrules file = " + semrulesFile.getFileName());
		log.debug("Input exception file = " + exceptionFile.getFileName());
		log.debug("Input semnetaccess.pl file = " + semnetaccessFile.getFileName());
		log.debug("UMLS year = " + UMLSyear);
		// log.debug("Input Exception Relation file = " + exceptionFile.getFileName());
		StringBuffer domainFileStmt = new StringBuffer();
		StringBuffer locsemnetFileStmt = new StringBuffer();
		StringBuffer semrulesFileStmt = new StringBuffer();
		StringBuffer exceptionFileStmt = new StringBuffer();
		StringBuffer errorFileStmt = new StringBuffer();
		// String ticket = UtsAuthentication.authenticate("dongwookshin","Wooyong1!");
		HashSet newTripleHash = new HashSet();
		String umlsRelease = new String("2006AA");
		String domainName = new String("generic");
		Hashtable inverseRel = new Hashtable(); 
		int relationMapCtr = 0;

		try {
			// PrintWriter 
			//   = new PrintWriter(new BufferedWriter(new FileWriter("C:\\Projects\\SemMedDebug\\PMIDListEcho.")));
			// InputStream is   = file.getInputStream();
			// BufferedReader br = new BufferedReader(new InputStreamReader(is));
			// Context context = new InitialContext();
	        // DataSource ds =
	        //    (DataSource)context.lookup("java:comp/env/jdbc/SemMedDB");
			Connection con = SemMedDB.getConnection();
			Statement stmt = con.createStatement();
			StringBuffer queryBuf = new StringBuffer("SELECT  c.PREFERRED_NAME, c.CUI, cs.SEMTYPE FROM CONCEPT as c, CONCEPT_SEMTYPE as cs WHERE c.CONCEPT_ID = cs.CONCEPT_ID and c.CUI = \"");

			HashSet conceptCUIHash = new HashSet();
			HashSet preferredConceptHash = new HashSet();
			// Hashtable cuiPreferredTable = new Hashtable();
			HashSet conceptCUIWithPREHash = new HashSet();
			// HashSet newTripleHash = new HashSet();
			HashSet ExceptionTripleHash = new HashSet();
			HashSet LocsemnetTripleHash = new HashSet();
			// HashSet ExceptionTripleHash = new HashSet();
			HashSet UMLSHash = new HashSet();
			
			if(domainFile != null) {
				InputStream domainInput = domainFile.getInputStream();
				POIFSFileSystem semtypeFileSystem = new POIFSFileSystem(domainInput);
				HSSFWorkbook semtypeWorkBook = new HSSFWorkbook(semtypeFileSystem);
				// log.debug("Check point 3");
				HSSFSheet replaceSheet = semtypeWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				log.debug("Read domain file");
				int it = 0;
				List<String> cellRowListSemtype = new ArrayList<String>();
				List<String> cellRowListConcept = new ArrayList<String>();
				List<String> cellRowListReplace = new ArrayList<String>();
				// List<String> cellRowListComment1 = new ArrayList<String>();
				int stage = 0;

				domainFileStmt.append("%      Do Not Modify This File    %\n" +
									"%     It is machine generated.    %\n" +
										"% file:	    " + domainName + "_domain.pl\n" +
                                       "% module:   " + domainName + "_domain.pl\n" +
										":- module(" + domainName + "_domain, [\n" +
										"\tdomain_name/1,\n" +
										"\tdomain_concept/2,\n" +
										"\tdomain_replace/2,\n" +
	      								"\tdomain_semtype/3\n" +
	      								"]).\n" +
	      								"domain_name(generic).\n");

				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					// log.debug("Read row : " + it);
					if(stage == 1)
						cellRowListConcept.add("||");
					else if(stage == 2)
						cellRowListReplace.add("||");
					else if(stage == 3)
						cellRowListSemtype.add("||");
					int columnI = 0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_Concept")) {
							stage = 1;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_Replace")) {
							stage = 2;
							// log.debug("Processing Replace");
						} else if(myCell.toString().equals("_Semtype")) {
							stage = 3;
							// log.debug("Processing Semtype");
						}  else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Concept list");
									cellRowListConcept.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListReplace.add(myCell.toString());
								} else if(stage == 3) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListSemtype.add(myCell.toString());
								}
							} else if(columnI <  myCell.getColumnIndex()) { // If there is nothing in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Concept list");
									cellRowListConcept.add("");
									cellRowListConcept.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListReplace.add("");
									cellRowListReplace.add(myCell.toString());
								} else if(stage == 3) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListSemtype.add("");
									cellRowListSemtype.add(myCell.toString());
								}
								columnI++;
							}
						}
						columnI++;
					}

				}

						String comment = null;
						String stype = null;
						String typeName = null;
						String superType = null;
						int k = 0;
						int j=0;
						StringBuffer thisStmt = new StringBuffer();
						while (j < cellRowListSemtype.size()) {
							String stringCellValue =   cellRowListSemtype.get(j);
							if(stringCellValue.equals("||")) {
								if(k <=2 && comment != null)
									thisStmt.append(comment);
								k=0;
								if(thisStmt.length() > 0)
									domainFileStmt.append(thisStmt + "\n");
								thisStmt = null;
								thisStmt = new StringBuffer();
							} else {
								if(k==1)
									comment = stringCellValue;
								else if(k==2)
									stype = stringCellValue;
								else if (k==3)
									typeName = stringCellValue;
								else if (k==4) {
									superType = stringCellValue;
									if(comment != null && comment.equals("%"))
										thisStmt.append("% domain_semtype(" +
											stype + ",'" + typeName + "'," + superType +  ").");
									else
										domainFileStmt.append("domain_semtype(" +
										 	stype + ",'" + typeName + "'," + superType +  ").");
								} else if (k==5) { // process the comment at the end
									thisStmt.append(" " + stringCellValue);
									// log.debug(thisStmt.toString());
								}
							}
							j++;
							k++;
						}
						log.debug("End of Semtype stmt added");



					// } 	 else if(i== 2) {
						String fromName = null;
						String fromCUI = null;
						String fromType = null;
						String toName = null;
						String toCUI = null;
						String toType = null;
						j=0;
						k=0;
						log.debug("Size of cellRowListReplace = " + cellRowListReplace.size());
						thisStmt = new StringBuffer();
						while(j < cellRowListReplace.size()) {
							String stringCellValue = cellRowListReplace.get(j);
							// log.debug("j = " + j + ", cellValue = " + stringCellValue);
							if(stringCellValue.equals("||")) {
								if(k <=2 && comment != null)
									thisStmt.append(comment);
								k=0;
								if(thisStmt.length() > 0)
									domainFileStmt.append(thisStmt + "\n");
								thisStmt = null;
								thisStmt = new StringBuffer();
							} else {
								if(k==1)
									comment = stringCellValue;
								else if(k==2)
									fromName = stringCellValue;
								else if (k==3)
									fromCUI = stringCellValue;
								else if (k==4)
									fromType = stringCellValue;
								else if (k==5)
									toName = stringCellValue;
								else if (k==6)
									toCUI = stringCellValue;
								else if (k==7) {
									toType = stringCellValue;
									if(comment.startsWith("%")) {
										thisStmt.append("% domain_replace('" +
												escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
												escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
									}
									/* Processing Prolog variables type and name like $CUI */
									if(fromName.startsWith("$") && fromCUI.startsWith("$") && toName.startsWith("$") && toCUI.startsWith("$")) {
										// log.debug("Processing Prolog variables type and name like $CUI ");
										String realFromName = fromName.substring(1);
										String realFromCUI = fromCUI.substring(1);
										String realToName = toName.substring(1);
										String realToCUI = toCUI.substring(1);
										thisStmt.append("domain_replace(" +
												escapeQuote(realFromName) + ":" + realFromCUI + ":[" + fromType +  "]," +
												escapeQuote(realToName) + ":" + realToCUI + ":[" + toType + "]).");
										/* log.debug(domainName + "_domain_replace(" +
												escapeQuote(realFromName) + ":" + realFromCUI + ":[" + fromType +  "]," +
												escapeQuote(realToName) + ":" + realToCUI + ":[" + toType + "])."); */
									} else {
										// log.debug("Get the CUI info from SemDB");
										CUIInfo cinfo = SemMedDB.getConceptSemtypeInfo(fromCUI);
										boolean sameType = compareSemtype(cinfo.getStype(), fromType);
										if(fromName.equals(cinfo.getPname()) && sameType) {
											thisStmt.append("domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
											String newToType = sortTypes(toType);
											String newTriple = new String(toName + "|" + toCUI + "|" + newToType);
											// log.debug("Newly replaced concept |" + newTriple);
											newTripleHash.add(newTriple);
										} else if(!fromName.equals(cinfo.getPname())) {
											thisStmt.append("% ---Wrong Concept name for \"" + fromCUI + "\" in Replace---------\n" +
													"% ---Correct Concept name is \"" + cinfo.getPname() + "\"---------\n" +
													"% domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
											errorFileStmt.append("% ---Wrong Concept name for \"" + fromCUI + "\" in Replace---------\n" +
													"% ---Correct Concept name is \"" + cinfo.getPname() + "\"---------\n" +
													"%domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).\n\n");
										}  else if(!sameType) {
											thisStmt.append("% ---Wrong type name for \"" + fromCUI + "\" used in Replace---------\n" +
													"% ---Correct type is \"" + makeString(cinfo.getStype()) + "\"---------\n" +
													"% domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).");
											errorFileStmt.append("% ---Wrong type name for \"" + fromCUI + "\" used in Replace---------\n" +
													"% ---Correct type is \"" + makeString(cinfo.getStype()) + "\"---------\n" +
													"% domain_replace('" +
													escapeQuote(fromName) + "\':'" + fromCUI + "\':[" + fromType +  "],'" +
													escapeQuote(toName) + "\':'" + toCUI + "\':[" + toType + "]).\n\n");
										}
									}
								} else if (k==8) { // process the comment at the end
									thisStmt.append(" " + stringCellValue);
									// log.debug(thisStmt.toString());
								}
							}
							j++;
							k++;
						}
						if(thisStmt.length() > 0) {
							domainFileStmt.append(thisStmt);
							thisStmt = null;
							thisStmt = new StringBuffer();
						}
						// log.debug("end of replace");

						String Name = null;
						String preName = null;
						String cui = null;
						j=0;
						k=0;
						while (j < cellRowListConcept.size()) {
							String stringCellValue =  cellRowListConcept.get(j);
							// log.debug("j = " + j + ", cellValue = " + stringCellValue);
							if(stringCellValue.equals("||")) {
								if(k <=2 && comment != null)
									thisStmt.append(comment);
								k=0;
								if(thisStmt.length() > 0)
									domainFileStmt.append(thisStmt +"\n");
								thisStmt = null;
								thisStmt = new StringBuffer();
							} else {
								if(k==1)
									comment = stringCellValue;
								else if(k==2)
									Name = stringCellValue.trim();
								else if (k==3)
									preName = stringCellValue.trim();
								else if (k==4)
									cui = stringCellValue.trim();
								else if (k==5) {
									stype = stringCellValue.trim();
									if(comment.startsWith("%")) {
										thisStmt.append("% domain_concept('" +
												escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
									}
									CUIInfo cinfo = SemMedDB.getConceptSemtypeInfo(cui);
									boolean sameType = compareSemtype(cinfo.getStype(), stype);
									String CUIWithPRE = new String(cui.trim() + "||" + preName.trim() + "||" + stype.trim());
									// log.debug(CUIWithPRE);
									// if(conceptCUIHash.contains(cui) && conceptCUIWithPREHash.add(CUIWithPRE)) {
									if(cui.startsWith("C")) {
										String triple = new String(preName + "|" + cui + "|" + sortTypes(stype));
										// log.debug("Checking concept starting with C |" + triple);
										if(newTripleHash.contains(triple)) { // The triple was declared in the second part of concept_replace
											thisStmt.append("domain_concept('" +
													escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
										} else {
											// log.debug("Triple is not defined bu concept_replace, so checking DB with cui :" + cui);
											ResultSet rs = stmt.executeQuery(queryBuf + cui + "\"");

											if(rs.first()) {
												String preferredDB = rs.getString(1);
												String cuiDB = rs.getString(2);
												StringBuffer typeDBBuf = new StringBuffer(rs.getString(3));
												while(rs.next()) {
													typeDBBuf.append("," + rs.getString(3));
												}
												rs.close();
												String typeDB = typeDBBuf.toString();
												// log.debug("In DB : " + preferredDB + "|" + cuiDB + "|" + typeDB);
												// log.debug("In DB: " + preferredDB + " | " + cuiDB + " | " + typeDB);
												// log.debug("From domain_concept: " + preName + "|" + cui + "|" + stype);
												String sortedSType = sortTypes(stype);
												if(preName.trim().equals(preferredDB) && cui.trim().equals(cuiDB) && sortedSType.trim().equals(typeDB)) {
													thisStmt.append("domain_concept('" +
															escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
												} else {
													 thisStmt.append("% --- the concept is defined differently in UMLS 2006AA. Either preferred name or semantic type is incorrectly used ---------\n" +
																"% domain_concept('" +
																escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).");
													 errorFileStmt.append("% --- the concept is defined differently in UMLS 2006AA. Either preferred name or semantic type is incorrectly used ---------\n" +
																"% domain_concept('" +
																escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
												}
											} else { // there is no Database definitiosn for the CUI
												log.debug(queryBuf + cui + "\"");
												thisStmt.append("% --- the CUI is not defined in the UMLS 2006AA ---------\n" +
														"% domain_concept('" +
														escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
												errorFileStmt.append("% --- the CUI is not defined in the UMLS 2006AA ---------\n" +
														"% domain_concept('" +
														escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
											}
										}
									} else if(!cui.startsWith("C") && !conceptCUIHash.contains(cui) && !preferredConceptHash.contains(preName.trim()) ) {
										thisStmt.append("domain_concept('" +
												escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
										conceptCUIHash.add(cui);
										conceptCUIWithPREHash.add(CUIWithPRE);
										preferredConceptHash.add(preName.trim());
									} else if(!cui.startsWith("C") &&
											( conceptCUIHash.contains(cui) && conceptCUIWithPREHash.contains(CUIWithPRE))) {
										thisStmt.append("domain_concept('" +
												escapeQuote(Name) + "\','" + escapeQuote(preName) + "\':'" + cui + "\':[" + stype + "]).");
									} else {
										 thisStmt.append("% --- Redefinition of the same CUI with different preferred name or semantic type ---------\n" +
													"% domain_concept('" +
													escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).");
										 errorFileStmt.append("% --- Redefinition of the same CUI with different preferred name or semantic type ---------\n" +
													"% domain_concept('" +
													escapeQuote(Name) + "','" + escapeQuote(preName) + "':'" + cui + "':[" + stype + "]).\n\n");
									}
								} else if(k==6) {
									thisStmt.append(" " + stringCellValue);
									// log.debug(thisStmt.toString());
								}
							}
							j++;
							k++;
						}
						if(thisStmt.length() > 0) {
							domainFileStmt.append(thisStmt);
							thisStmt = null;
							thisStmt = new StringBuffer();
						}
				domainInput.close();
				log.debug("write domain Prolog file");


			String filename = "/download/domain_GEN.pl";
			String xmlrealpath = ctx.getRealPath(filename);
			// log("XML context path " + xmlcontextpath);
			log.debug("XML real path " + xmlrealpath);
			PrintWriter domainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(xmlrealpath)));
			domainfile.println(domainFileStmt.toString());
			domainFileStmt = null;
			domainfile.close();
		}
			queryBuf = new StringBuffer("SELECT  c.PREFERRED_NAME FROM CONCEPT as c WHERE c.PREFERRED_NAME = BINARY \"");
			
			if(semnetaccessFile != null) {
				InputStream semnetaccessInput = semnetaccessFile.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(semnetaccessInput));
				String aLine = null;
				while((aLine = in.readLine()) != null) {
					// log.debug(aLine); 
					if(aLine.startsWith("type_relation_type_") && aLine.length() > 25 && !aLine.contains(":-")) { // Extract ontology from UMLS Semantic network
						String subj = aLine.substring(19, 23);
						int sparenIndex = aLine.indexOf("(");
						int eparenIndex = aLine.indexOf(")");
						String predicate = aLine.substring(24,sparenIndex);
						String obj = aLine.substring(eparenIndex-4, eparenIndex);
						int firstQuoteIndex = aLine.indexOf("'", sparenIndex);
						String year = aLine.substring(firstQuoteIndex+1, firstQuoteIndex+3);
						// log.debug("UMLS Semantic Network : " + subj + "-" + predicate + "-" + obj + ":" + year);
						if(UMLSyear.equals(year)) {
							String UMLStriple = new String(subj + "-" + predicate + "-" + obj);
							UMLSHash.add(UMLStriple);
						}
					}
				}
			}
			if(locsemnetFile != null) {
				InputStream locsemnetInput = locsemnetFile.getInputStream();
				POIFSFileSystem replaceFileSystem = new POIFSFileSystem(locsemnetInput);
				HSSFWorkbook replaceWorkBook = new HSSFWorkbook(replaceFileSystem);
				HSSFSheet replaceSheet = replaceWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				log.debug("Read locsemnetFile file");
				int it = 0;
				List<String> cellRowListPreferred = new ArrayList<String>();
				List<String> cellRowListInverse = new ArrayList<String>();
				List<String> cellRowListSemnet = new ArrayList<String>();
				int stage = 0;

				locsemnetFileStmt.append("%      Do Not Modify This File    %\n" +
						"%     It is machine generated.    %\n" +
						":- module(locsemnet_GEN, [\n" +
						"\t\tlocal_preferred_relation_GEN/1,\n" +
						"\t\tlocal_relation_inverse_GEN/2,\n" +
						"\t\tlocal_semnet_GEN/3\n" +
						"%\tlocal_semnet_1_GEN/3\n\t]).\n\n" +
						":- load_files( usemrep_lib(module_version), [\n" +
						"\t\twhen(compile_time)\n\t]).\n\n" +
						":- use_module( usemrep_lib(module_version), [\n" +
						"\t\tglobal_module_version/1\n\t]).\n\n" +
						":- use_module( usemrep_lib( semnet_access ),[\n" +
						"\t\tpreferred_relation/2,\n" +
						"\t\trelation_inverse/3\n\t]).\n\n" +
						"local_semnet_GEN(Type1, Relation, Type2) :-\n" +
						"\t( Relation == 'ISA' ->\n" +
						"\t  true \n" +
						"\t; local_semnet_1_GEN(Type1, Relation, Type2) ->\n" +
						"\t  true\n" +
						"\t; local_relation_inverse_GEN(Relation, Inverse) ->\n" +
						"\t  local_semnet_1_GEN(Type2, Inverse, Type1)\n" +
						"\t; Relation \\== unspecified_relation ->\n" +
						"%\t  format(user_put, '~n~n### ERROR in locsemnet: ~q is neither preferred nor inverse relation.~n~n',\n" +
						"%\t	 [Relation]),\n" +
						"\t  fail\n\t\t).\n\n" +
					"local_relation_inverse_GEN(Relation, Inverse) :-\n" +
					"\tglobal_module_version(Version),\n" +
					"\t	( relation_inverse(Version, Relation, Inverse) ->\n" +
					"\t	  true\n" +
					"\t      ; local_relation_inverse_1_GEN(Relation, Inverse)\n\t).\n\n" +
					"local_preferred_relation_GEN(Relation) :-\n" +
					"\tglobal_module_version(Version),\n" +
					"\t	( preferred_relation(Version, Relation) ->\n" +
					"\t	  true\n" +
					"\t       ; local_preferred_relation_1_GEN(Relation)\n).\n\n");

				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					// log.debug("Read row " + it);
					if(stage ==1)
						cellRowListPreferred.add("||");
					else if(stage == 2)
						cellRowListInverse.add("||");
					else if(stage ==3)
						cellRowListSemnet.add("||");
					int columnI = 0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_local_preferred_relation_1_GEN")) {
							stage = 1;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_local_relation_inverse_1_GEN")) {
							stage = 2;
							// log.debug("Processing Replace");
						} else if(myCell.toString().equals("_local_semnet_1_GEN")) {
							stage = 3;
							// log.debug("Processing Semtype");
						} else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListPreferred.add(myCell.toString());
								} else if(stage == 2) {
								// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListInverse.add(myCell.toString());
								} else if(stage == 3) {
								// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListSemnet.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										locsemnetFileStmt.append(myCell.toString() +"\n");
								}
							} else if(columnI <  myCell.getColumnIndex()) { // If there is nothing in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListPreferred.add("");
									cellRowListPreferred.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListInverse.add("");
									cellRowListInverse.add(myCell.toString());
								} else if (stage == 3) {
									// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListSemnet.add("");
									cellRowListSemnet.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										locsemnetFileStmt.append(myCell.toString() +"\n");
								}
								columnI++;
							}
						}
						columnI++;
					}
				}
				String Name = null;
				String invName = null;
				int j=0;
				int k=0;
				String comment = null;
				StringBuffer thisStmt = new StringBuffer();
				
				while(j < cellRowListInverse.size()) {
					String stringCellValue =  cellRowListInverse.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							locsemnetFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						if(k==2)
							Name = stringCellValue;
						else if (k==3) {
							invName = stringCellValue;;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%local_relation_inverse_1_GEN(" +
									Name + "," + invName + ").");
							else {
								thisStmt.append("local_relation_inverse_1_GEN(" +
									Name + "," + invName + ").");
								relationMap[relationMapCtr][0] = new String(Name);
								relationMap[relationMapCtr][1] = new String(invName);
								// log.debug("relationMap :" + relationMap[relationMapCtr][0] + " : " + relationMap[relationMapCtr][1]);
								relationMapCtr++;
							}
						} else if(k==4) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					locsemnetFileStmt.append(thisStmt +"\n");
				thisStmt = null;
				thisStmt = new StringBuffer();
				j=0;
				k=0;

				while(j < cellRowListPreferred.size()) {
					String stringCellValue =  cellRowListPreferred.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							locsemnetFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2) {
							if(comment.startsWith("%"))
								thisStmt.append("%local_preferred_relation_1_GEN(" +
										stringCellValue +  ").");
							else
								thisStmt.append("local_preferred_relation_1_GEN(" +
										stringCellValue +  ").");
						} else if(k==3) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					locsemnetFileStmt.append(thisStmt +"\n");
				thisStmt = null;
				thisStmt = new StringBuffer();
				
				
				String subj = null;
				String predicate = null;
				String obj = null;
				j=0;
				k=0;
				while(j < cellRowListSemnet.size()) {
					String stringCellValue = cellRowListSemnet.get(j);
					if(stringCellValue.equals("||")) { 
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							locsemnetFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							subj = stringCellValue;
						else if (k==3)
							predicate = stringCellValue;
						else if (k==4) {
							obj = stringCellValue;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%local_semnet_1_GEN(" +
									subj + "," + predicate + "," + obj + ")."); 
							else {
								String tripleStr = new String(subj + "-" + predicate + "-" + obj);

								if(LocsemnetTripleHash.contains(tripleStr)) {
									errorFileStmt.append("ERROR: " + tripleStr + "  is duplicated in Locsemnet file.\n");
								} else {
									thisStmt.append("local_semnet_1_GEN(" +
											subj + "," + predicate + "," + obj + ").");
									LocsemnetTripleHash.add(tripleStr);
								}
								if(UMLSHash.contains(tripleStr)) {
									errorFileStmt.append("ERROR: " + tripleStr + " in Locsemnet file is also declared in the year " + UMLSyear + " of semnet_access.pl\n");
								} 
								
								/* if(!inverseRel.containsKey(predicate)) {
									HashSet pairlist = new HashSet();
									String pair = new String(subj + "-" + obj);
									pairlist.add(pair);
									inverseRel.put(predicate, pairlist);
								} else {
									HashSet pairList = (HashSet) inverseRel.remove(predicate);
									pairList.add(new String(subj + "-" + obj));
									inverseRel.put(predicate, pairList);
								} */
							}
						} else if(k==5) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					j++;
					k++;
				}
		

				if(thisStmt.length() > 0)
					locsemnetFileStmt.append(thisStmt +"\n");
				locsemnetInput.close();

			String locsemnetFilename = "/download/locsemnet_GEN.pl";
			String locsemnetxmlrealpath = ctx.getRealPath(locsemnetFilename);
			// log("XML context path " + xmlcontextpath);

			PrintWriter locsemnetdomainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(locsemnetxmlrealpath)));
			locsemnetdomainfile.println(locsemnetFileStmt.toString());
			locsemnetFileStmt = null;
			locsemnetdomainfile.close();
			}

			// processing semrules
			if(semrulesFile != null) {
				InputStream semrulesInput = semrulesFile.getInputStream();
				POIFSFileSystem replaceFileSystem = new POIFSFileSystem(semrulesInput);
				HSSFWorkbook replaceWorkBook = new HSSFWorkbook(replaceFileSystem);
				HSSFSheet replaceSheet = replaceWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				log.debug("Read semruleFile file");
				int it = 0;
				List<String> cellRowListMultiphrase = new ArrayList<String>();
				List<String> cellRowListPhrase = new ArrayList<String>();
				List<String> cellRowListWord = new ArrayList<String>();
				int stage = 0;
				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					if(stage ==1)
						cellRowListMultiphrase.add("||");
					else if(stage == 2)
						cellRowListPhrase.add("||");
					else if(stage ==3)
						cellRowListWord.add("||"); 
					int columnI = 0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_multiphrase_corresponds_to_semnet_relation_GEN")) {
							stage = 1;
						} else if(myCell.toString().equals("_phrase_corresponds_to_semnet_relation_GEN")) {
							stage = 2;
						} else if(myCell.toString().equals("_word_corresponds_to_semnet_relation_GEN")) {
							stage = 3;
							// log.debug("Processing Concepts");
						} else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListMultiphrase.add(myCell.toString());
								} else if(stage == 2) {
								// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListPhrase.add(myCell.toString());
								} else if(stage == 3){
								// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListWord.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										semrulesFileStmt.append(myCell.toString() +"\n");
								}
							} else if(columnI <  myCell.getColumnIndex()) { // If there is nothing in the column
								if(stage == 1) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListMultiphrase.add("");
									cellRowListMultiphrase.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListPhrase.add("");
									cellRowListPhrase.add(myCell.toString());
								} else if(stage == 3){
									// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListWord.add("");
									cellRowListWord.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										semrulesFileStmt.append(myCell.toString() +"\n");
								}
								columnI++;
							}
						}							
						columnI++;
					}

				}
				int k=0;
				int j=0;
				String first = null;
				String second = null;
				String third = null;
				String fourth = null;
				String fifth = null;
				String sixth = null;
				String comment = null;
				System.out.println("Read completed of Semrule Excel files");
				StringBuffer thisStmt = new StringBuffer();
				semrulesFileStmt.append("%      Do Not Modify This File    %\n" +
						"%     It is machine generated.    %\n" +
						":- module(semrules_GEN,	[\n" +
						"\tword_corresponds_to_semnet_relation_GEN/4,\n" +
						"\tmultiphrase_corresponds_to_semnet_relation_GEN/6,\n" +
						"\tphrase_corresponds_to_semnet_relation_GEN/6" +
						"\n]).\n\n");

				while(j < cellRowListMultiphrase.size()) {
					String stringCellValue =  cellRowListMultiphrase.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							semrulesFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							first = stringCellValue;
						else if (k==3)
							second = stringCellValue;
						else if (k==4)
							third = stringCellValue;
						else if (k==5) 
							fourth = stringCellValue;
						else if (k == 6)
							fifth = stringCellValue;
						else if(k == 7) {
							sixth = stringCellValue;
							if(comment != null && comment.startsWith("%")) {
								thisStmt.append("%multiphrase_corresponds_to_semnet_relation_GEN(" +
										first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
							} else
								thisStmt.append("multiphrase_corresponds_to_semnet_relation_GEN(" +
										first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
						} else if(k==8) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					k++;
					j++;
				}
				System.out.println("Done with multiphrase_corresponds_to_semnet_relation_GEN");
				j=0;
				k=0;
				while(j < cellRowListPhrase.size()) {
					String stringCellValue =  cellRowListPhrase.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							semrulesFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							first = stringCellValue;
						else if (k==3)
							second = stringCellValue;
						else if (k==4)
							third = stringCellValue;
						else if (k==5) 
							fourth = stringCellValue;
						else if (k == 6)
							fifth = stringCellValue;
						else if(k == 7) {
							sixth = stringCellValue;
							if(comment != null && comment.startsWith("%")) {
								thisStmt.append("%phrase_corresponds_to_semnet_relation_GEN(" +
									first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
							} else
								thisStmt.append("phrase_corresponds_to_semnet_relation_GEN(" +
										first + "," + second + "," + third + "," + fourth + "," + fifth + "," + sixth + ").");
						} else if(k==8) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					k++;
					j++;
				}
				j=0;
				k=0;
				while(j < cellRowListWord.size()) {
					String stringCellValue =  cellRowListWord.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							semrulesFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2) {
							/** Error fix: May 23 2016
							 * If cell value has space in it and it is not surrounded by ' nor "",
							 * then it has to be enclosed by "'" since Prolog does not allow a name that has space 
							 */
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								first = new String("'" + stringCellValue + "'");
							else
								first = stringCellValue;
						} else if (k==3) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								second = new String("'" + stringCellValue + "'");
							else
								second = stringCellValue;
						} else if (k==4) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								third = new String("'" + stringCellValue + "'");
							else
								third = stringCellValue;
						} else if (k==5) {
							if(stringCellValue.contains(" ") && !stringCellValue.startsWith("'") && !stringCellValue.startsWith("\""))
								fourth = new String("'" + stringCellValue + "'");
							else
								fourth = stringCellValue;
							if(comment != null && comment.startsWith("%")) {
								thisStmt.append("%word_corresponds_to_semnet_relation_GEN(" +
										first + "," + second + "," + third + "," + fourth +  ").");
							} else
								thisStmt.append("word_corresponds_to_semnet_relation_GEN(" +
										first + "," + second + "," + third + "," + fourth +  ").");
						} else if(k==6) {
							thisStmt.append(" " + stringCellValue);
						}
					}
					k++;
					j++;
				}
				if(thisStmt.length() > 0)
					semrulesFileStmt.append(thisStmt +"\n\n");
				semrulesInput.close();

				String semrulesFilename = "/download/semrules_GEN.pl";
				String locsemnetxmlrealpath = ctx.getRealPath(semrulesFilename);
				PrintWriter semrulesfile
					= new PrintWriter(new BufferedWriter(new FileWriter(locsemnetxmlrealpath)));
				semrulesfile.println(semrulesFileStmt.toString());
				semrulesFileStmt = null;
				semrulesfile.close();
			}

			if(exceptionFile != null) {
				InputStream exceptionInput = exceptionFile.getInputStream();
				POIFSFileSystem exceptionFileSystem = new POIFSFileSystem(exceptionInput);
				HSSFWorkbook replaceWorkBook = new HSSFWorkbook(exceptionFileSystem);
				HSSFSheet replaceSheet = replaceWorkBook.getSheetAt(0);
				Iterator rowIter = replaceSheet.rowIterator();
				int it = 0;
				List<String> cellRowListEmptyHead = new ArrayList<String>();
				List<String> cellRowListEmptyHead2N = new ArrayList<String>();
				List<String> cellRowListIgnore1 = new ArrayList<String>();
				List<String> cellRowListIgnore2N = new ArrayList<String>();
				List<String> cellRowListIgnoreType = new ArrayList<String>();
				List<String> cellRowListNonCuedSubject = new ArrayList<String>();
				List<String> cellRowListNonCuedObject = new ArrayList<String>();
				exceptionFileStmt.append("%      Do Not Modify This File    %\n" +
						"%     It is machine generated.    %\n" +
						":- module( exceptions_GEN, [\n" +
						"\t\tconcept_to_ignore_GEN/2,\n" +
		                "\t\tconditional_empty_head_base_1_GEN/2,\n" +
		                "\t\tconditional_empty_head_base_2N_GEN/3,\n" +
						"\t\tempty_head_base_1_GEN/1,\n" +
						"\t\tempty_head_base_2N_GEN/2,\n" +
						"\t\tempty_macro_np_head_list/1,\n" +
						"\t\tignore_semnet_access_term_GEN/1,\n" +
						"%\t\tignore_type_relation_type_GEN/3,\n" +
						"\t\tnon_compositional_phrase_list/1,\n" + 
		                "\t\tnon_prepositionally_cued_object_GEN/1,\n" + 
		                "\t\tnon_prepositionally_cued_subject_GEN/1,\n" +
						"\t\ttransform_semnet_access_term_GEN/2\n" + 
						"%\t\ttransform_type_relation_type_GEN/6\n]).\n\n" +
						":- use_module( skr_lib( sicstus_utils ), [\n" +
						"\t lower/2\n]).\n\n" 
						);
				int stage = 0;
				while (rowIter.hasNext()) {
					HSSFRow myRow = (HSSFRow) rowIter.next();
					Iterator cellIter = myRow.cellIterator();
					it++;
					if(stage ==1)
						cellRowListEmptyHead.add("||");
					else if(stage ==2)
						cellRowListEmptyHead2N.add("||");
					else if(stage ==3)
						cellRowListIgnore1.add("||");
					else if(stage ==4)
						cellRowListIgnore2N.add("||");
					else if(stage ==5)
						cellRowListIgnoreType.add("||");
					else if(stage ==6)
						cellRowListNonCuedSubject.add("||");
					else if(stage ==7)
						cellRowListNonCuedObject.add("||");
					// log.debug("Read row " + it);
					int columnI=0;
					while (cellIter.hasNext()) {
						HSSFCell myCell = (HSSFCell) cellIter.next();
						if(myCell.toString().equals("_empty_head_base_1_GEN")) {
							stage = 1;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_empty_head_base_2N_GEN")) {
							stage = 2;
							// log.debug("Processing Concepts");
						} else if(myCell.toString().equals("_concept_to_ignore_1")) {
							stage = 3;
							// log.debug("Processing Replace");
						} else if(myCell.toString().equals("_concept_to_ignore_2N")) {
							stage = 4;
							// log.debug("Processing Semtype");
						} else if(myCell.toString().equals("_ignore_type_relation_type_1_GEN")) {
							stage = 5;
							// log.debug("Processing Semtype");
						} else if(myCell.toString().equals("_non_prepositionally_cued_subject_GEN")) {
							stage = 6;
							// log.debug("Processing Semtype");
						} else if(myCell.toString().equals("_non_prepositionally_cued_object_GEN")) {
							stage = 7;
							// log.debug("Add " + myCell.toString() + " cellRowListNonCuedObject");
						} else {
							// log.debug("value of stage = " + stage);
							if(columnI ==  myCell.getColumnIndex()) { // If there is someting in the column
								if(stage == 1) {
								// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListEmptyHead.add(myCell.toString());
								} else if(stage == 2) {
									// log.debug("Add " + myCell.toString() + " to Preferred list");
									cellRowListEmptyHead2N.add(myCell.toString());
								}else if(stage == 3) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListIgnore1.add(myCell.toString());
								} else if(stage == 4) {
									// log.debug("Add " + myCell.toString() + " to Replace list");
									cellRowListIgnore2N.add(myCell.toString());
								}else if(stage == 5) {
									// log.debug("Add " + myCell.toString() + " to Semtype list");
									cellRowListIgnoreType.add(myCell.toString());
								} else if(stage == 6) {
									// log.debug("Add " + myCell.toString() + " cellRowListNonCuedSubject");
									cellRowListNonCuedSubject.add(myCell.toString());
								}  else if(stage == 7) {
									// log.debug("Add " + myCell.toString() + " cellRowListNonCuedObject");
									cellRowListNonCuedObject.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										exceptionFileStmt.append(myCell.toString() +"\n");
								}
							}  else if(columnI <  myCell.getColumnIndex()) {
								if(stage == 1) {
											// log.debug("Add " + myCell.toString() + " to Preferred list");
												cellRowListEmptyHead.add("");
												cellRowListEmptyHead.add(myCell.toString());
								} else if(stage == 2) {
												// log.debug("Add " + myCell.toString() + " to Preferred list");
												cellRowListEmptyHead2N.add("");
												cellRowListEmptyHead2N.add(myCell.toString());
								}else if(stage == 3) {
												// log.debug("Add " + myCell.toString() + " to Replace list");
												cellRowListIgnore1.add("");
												cellRowListIgnore1.add(myCell.toString());
								} else if(stage == 4) {
												// log.debug("Add " + myCell.toString() + " to Replace list");
												cellRowListIgnore2N.add("");
												cellRowListIgnore2N.add(myCell.toString());
								}else if(stage == 5){
												// log.debug("Add " + myCell.toString() + " to Semtype list");
												cellRowListIgnoreType.add("");
												cellRowListIgnoreType.add(myCell.toString());
								} else if(stage == 6){
									// log.debug("Add " + myCell.toString() + " cellRowListNonCuedSubject");
									cellRowListNonCuedSubject.add("");
									cellRowListNonCuedSubject.add(myCell.toString());
								} else  if(stage == 7) {
									// log.debug("Add " + myCell.toString() + " cellRowListNonCuedObject");
									cellRowListNonCuedObject.add("");
									cellRowListNonCuedObject.add(myCell.toString());
								} else {
									if(myCell.toString().startsWith("%"))
										exceptionFileStmt.append(myCell.toString() +"\n");
								}
								columnI++;
							}
						}																																																											
						columnI++; 
					}
				}
				int k=0;
				int j=0;
				String comment = null;
				StringBuffer thisStmt = new StringBuffer();
				while(j<cellRowListEmptyHead.size()) {
					String stringCellValue =  cellRowListEmptyHead.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k == 2) {
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%empty_head_base_1_GEN(" +
										nameQuote(escapeQuote(stringCellValue)) +  ").");
							else
								thisStmt.append("empty_head_base_1_GEN(" +
										nameQuote(escapeQuote(stringCellValue)) +  ").");
						} else if(k==3)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of empty_head_base_1_GEN");

				String Name1 = null;
				String Name2 = null;
				j=0;
				k=0;
				while(j<cellRowListEmptyHead2N.size()) {
					String stringCellValue  =  cellRowListEmptyHead2N.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							Name1 = stringCellValue;
						else if(k==2)
							Name1 = stringCellValue;
						else if (k==3){
							Name2 = stringCellValue;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%empty_head_base_2N_GEN(" +
										nameQuote(Name1) + "," + nameQuote(Name2) + ").");
							else
								thisStmt.append("empty_head_base_2N_GEN(" +
										nameQuote(Name1) + "," + nameQuote(Name2) + ").");
						} else if(k==4)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of empty_head_base_2N");

				exceptionFileStmt.append(
						"% Those that qualify as empty heads when preceded by\n" +
						"% concepts of certain semantic types.\n" +
						"conditional_empty_head_base_1_GEN(family, [gene]).\n" +
						"conditional_empty_head_base_2N_GEN(family, [members], [gene]).\n\n" +
						"concept_to_ignore_GEN([Base|RestBases], MetaConc) :-\n" +
						"\tlower(Base, LCBase),\n" +
						"\tconcept_to_ignore_aux(RestBases, LCBase, MetaConc).\n\n" +
						"concept_to_ignore_aux([], Base, MetaConc) :-\n" +
						"\tconcept_to_ignore_1(Base, MetaConc).\n" +
						"concept_to_ignore_aux([Base2|RestBases], Base1, MetaConc) :-\n" +
						"\tconcept_to_ignore_2N(Base1, [Base2|RestBases], MetaConc).\n\n");

				k=0;
				j=0;
				while(j<cellRowListIgnore1.size()) {
					String stringCellValue =  cellRowListIgnore1.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							Name1 = stringCellValue;
						else if (k==3) {
							Name2 = stringCellValue;
							StringBuffer queryBuf2 = new StringBuffer("select CUI from CONCEPT where PREFERRED_NAME = BINARY \"");
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%concept_to_ignore_1(" +
										nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2))+ ").");
							else {
								ResultSet rs = stmt.executeQuery(queryBuf2 + Name2 + "\"");
								if(rs.first()) {
										thisStmt.append("concept_to_ignore_1(" +
												nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2))+ ").");
								} else {
									thisStmt.append("% Error: Preferred name: " + Name2 + " is not in UMLS 2006AA\n");
									thisStmt.append("%concept_to_ignore_1(" +
											nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2)) + ").\n\n");
									errorFileStmt.append("% Error: Preferred name: " + Name2 + " is not in UMLS 2006AA\n");
									errorFileStmt.append("%concept_to_ignore_1(" +
											nameQuote(escapeQuote(Name1)) + "," + nameQuote(escapeQuote(Name2)) + ").\n\n");
								}
							}
						} else if(k==4)
									thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of concept_to_ignore_1");

				String name1 = null;
				String name2 = null;
				String name3 = null;
				j=0;
				k=0;
				if(cellRowListIgnore2N.size() <= 0) {
					exceptionFileStmt.append("concept_to_ignore_2N(_Base1,_Base2,_MetaConc).\n\n");
				} else {
				  while(j < cellRowListIgnore2N.size()) {
					String stringCellValue =  cellRowListIgnore2N.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							name1 = stringCellValue;
						else if (k==3)
							name2 = stringCellValue;
						else if (k==4) {
							name3 = stringCellValue;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%concept_to_ignore_2N(" +
									nameQuote(name1) + "," + name2 + "," + nameQuote(name3) + ").");
							else
								thisStmt.append("concept_to_ignore_2N(" +
										nameQuote(name1) + "," + name2 + "," + nameQuote(name3) + ").");
						} else if(k==5)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				  } // while
				} // else
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				log.debug("End of concept_to_ignore_2N");

				String subj = null;
				String predicate = null;
				String obj = null;
				exceptionFileStmt.append(
						"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n" +
						"%%%% These next predicates are for transforming\n" +
						"%%%% terms of the form\n" +
						"%%%%    * preferred_relation/1,\n" +
						"%%%%    * relation_inverse/2, and\n" +
						"%%%%    * type_relation_type/3,\n" +
						"%%%% all of which are defined in semnet_accessXX.pl.\n" +
						"%%%% These predicates are called from pre_compilation.pl.\n" +
						"%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n\n" +
						"\ntransform_semnet_access_term_GEN(preferred_relation(Rel),\n" +
					     "\t\tpreferred_relation(NewRel)) :-\n" +
					     "\t( transform_preferred_relation_GEN(Rel, NewRel) ->\n" +
					     "\t  true\n" +
					     "\t; NewRel = Rel\n\t).\n\n" +
					     "transform_semnet_access_term_GEN(relation_inverse(Rel,    InverseRel),\n" +
					     "\t\trelation_inverse(NewRel, NewInverseRel)) :-\n" +
					     "\t( transform_relation_inverse_GEN(Rel, InverseRel, NewRel, NewInverseRel) ->\n" +
					     "\t  true\n" +
					     "\t; NewRel = Rel,\n" +
					     "\t  NewInverseRel = InverseRel\n\t).\n\n" +
					     "transform_semnet_access_term_GEN(type_relation_type(Type1,    Rel,    Type2),\n" +
					     "\t\ttype_relation_type(NewType1, NewRel, NewType2)) :-\n" +
			 			"\t(transform_type_relation_type_GEN(Type1, Rel, Type2, NewType1, NewRel, NewType2) ->\n" +
			 			"\t  true\n" +
			 			"\t; NewType1 = Type1,\n" +
			 			"\t  NewRel = Rel,\n" +
			 			"\t  NewType2 = Type2\n\t).\n\n" +
			 			"ignore_semnet_access_term_GEN(preferred_relation(Rel)) :-\n" +
			 			"\tignore_preferred_relation_GEN(Rel).\n\n" +
			 			"ignore_semnet_access_term_GEN(relation_inverse(Rel, InverseRel)) :-\n" +
			 			"\tignore_relation_inverse_GEN(Rel, InverseRel).\n\n" +
			 			"ignore_semnet_access_term_GEN(type_relation_type(Type1, Rel, Type2)) :-\n" +
			 			"\tignore_type_relation_type_GEN(Type1, Rel, Type2).\n\n" +
			 			"transform_preferred_relation_GEN('co-occurs_with', coexists_with).\n\n" +
			 			"ignore_preferred_relation_GEN(associated_with).\n\n" +
			 			"transform_relation_inverse_GEN('co-occurs_with', 'co-occurs_with', coexists_with, coexists_with).\n\n" +
			 			"% associated_with becomes ROOT_RELATION, which we ignore\n\n" +
			 			"ignore_relation_inverse_GEN(associated_with,  associated_with).\n\n" +
			 			"transform_type_relation_type_GEN(Type1,    Rel,    Type2,\n" +
					    "\t\tNewType1, NewRel, NewType2) :-\n" +
					    "\ttransform_preferred_relation_if_possible(Type1, NewType1),\n" +
					    "\ttransform_preferred_relation_if_possible(Rel,   NewRel),\n" +
					    "\ttransform_preferred_relation_if_possible(Type2, NewType2).\n\n" +
					    "transform_preferred_relation_if_possible(Rel, TransformedRel) :-\n" +
					    "\t( transform_preferred_relation_GEN(Rel, TransformedRel) ->\n" +
					    "\t  true\n" +
					    "\t; TransformedRel = Rel\n).\n\n" +
					    "ignore_type_relation_type_GEN(Type1, Relation, Type2) :-\n" +
					    "\t( ignore_preferred_relation_GEN(Type1)    ->\n" +
					    "\t  true\n" +
					    "\t; ignore_preferred_relation_GEN(Relation) ->\n" +
					    "\t  true\n" +
					    "\t; ignore_preferred_relation_GEN(Type2)    ->\n" +
					    "\t  true\n" +
					    "\t; ignore_type_relation_type_1_GEN(Type1, Relation, Type2)\n).\n\n");
				j=0;
				k=0;
				while(j<cellRowListIgnoreType.size()) {
					String stringCellValue =  cellRowListIgnoreType.get(j);
					if(stringCellValue.equals("||")) {
						if(k <=2 && comment != null)
							thisStmt.append(comment);
						k=0;
						if(thisStmt.length() > 0)
							exceptionFileStmt.append(thisStmt +"\n");
						thisStmt = null;
						thisStmt = new StringBuffer();
					} else {
						if(k==1)
							comment = stringCellValue;
						else if(k==2)
							subj = stringCellValue;
						else if (k==3)
							predicate = stringCellValue;
						else if (k==4) {
							obj = stringCellValue;;
							if(comment != null && comment.startsWith("%"))
								thisStmt.append("%ignore_type_relation_type_1_GEN(" + 
										subj + "," + predicate + "," + obj + ").");
							else {
								String exceptionTriple = new String(subj + "-" + predicate + "-" + obj);	

								if (ExceptionTripleHash.contains(exceptionTriple)) {
									errorFileStmt.append("ERROR: The triple " + exceptionTriple + " is duplicated in Exceptions file.\n");
								} else {
									thisStmt.append("ignore_type_relation_type_1_GEN(" +
											subj + "," + predicate + "," + obj + ").");
									ExceptionTripleHash.add(exceptionTriple);
								}
								// Check the type 1 error
								if(LocsemnetTripleHash.contains(exceptionTriple)) {
									errorFileStmt.append("ERROR: The triple " + exceptionTriple + " is defined both in Locsemnet and Exceptions files.\n");
								} 
								// Check if subj-pred-obj is in the relation, obj-prev_inverse-subj is also defined
								if(!inverseRel.containsKey(predicate)) {
									HashSet pairlist = new HashSet();
									String pair = new String(subj + "-" + obj);
									pairlist.add(pair);
									inverseRel.put(predicate, pairlist);
								} else {
									HashSet pairList = (HashSet) inverseRel.remove(predicate);
									pairList.add(new String(subj + "-" + obj));
									inverseRel.put(predicate, pairList);
								} 
								
							}
						} else if(k==5)
							thisStmt.append(" " + stringCellValue);
					}
					j++;
					k++;
				}
				if(thisStmt.length() > 0)
					exceptionFileStmt.append(thisStmt +"\n");
				
					j=0;
					k=0;
					String value = null;
					while(j<cellRowListNonCuedSubject.size()) {
						String stringCellValue =  cellRowListNonCuedSubject.get(j);
						// log.debug(stringCellValue + " : cellRowListNonCuedSubject" + " : k = " + k);
						if(stringCellValue.equals("||")) {
							if(k <=2 && comment != null)
								thisStmt.append(comment);
							k=0;
							if(thisStmt.length() > 0)
								exceptionFileStmt.append(thisStmt +"\n");
							thisStmt = null;
							thisStmt = new StringBuffer();
						} else {
							if(k==1)
								comment = stringCellValue;
							else if(k==2) {
								value = stringCellValue;
								if(comment != null && comment.startsWith("%")) {
									thisStmt.append("%non_prepositionally_cued_subject_GEN(" + 
											value.trim() + ").");
									// log.debug(thisStmt);
								} else {
									thisStmt.append("non_prepositionally_cued_subject_GEN(" +
											value.trim() + ").");
									// log.debug(thisStmt);
								}
							}  else if(k==3)
								thisStmt.append(" " + stringCellValue);
						}
						j++;
						k++;
					} // while
					if(thisStmt.length() > 0)
						exceptionFileStmt.append(thisStmt +"\n");
					
					log.debug("end of non_prepositionally_cued_subject_GEN");
					
					j=0;
					k=0;
					while(j<cellRowListNonCuedObject.size()) {
						String stringCellValue =  cellRowListNonCuedObject.get(j);
						// log.debug(stringCellValue + " : cellRowListNonCuedObject" + " : k = " + k);
						if(stringCellValue.equals("||")) {
							if(k <=2 && comment != null)
								thisStmt.append(comment);
							k=0;
							if(thisStmt.length() > 0)
								exceptionFileStmt.append(thisStmt +"\n");
							thisStmt = null;
							thisStmt = new StringBuffer();
						} else {
							if(k==1)
								comment = stringCellValue;
							else if(k==2) {
								value = stringCellValue;
								if(comment != null && comment.startsWith("%")) {
									thisStmt.append("%non_prepositionally_cued_object_GEN(" + 
											value.trim() + ").");
									// log.debug(thisStmt);
								} else {
									thisStmt.append("non_prepositionally_cued_object_GEN(" +
											value.trim() + ").");
									// log.debug(thisStmt);
								}
							}  else if(k==3)
								thisStmt.append(" " + stringCellValue);
						}
						j++;
						k++;
					}
					
					thisStmt.append("% Graciela's non-compositional phrase list.\n" +
"non_compositional_phrase_list([[in,turn], [on,the,other,hand], [on,the,one,hand], [in,the,face,of],\n" + 
	     "\t\t['double-',blind], [double,blind], [double,'-',blind], [slow,down],\n" +
	     "\t\t[single,blind], ['single-',blind], [single,'-',blind], [in,view,of], [mouse,task],\n" +
	     "\t\t[pro,re,nata], [per,se], [take,into,consideration], [with,a,view,to],\n" +
	     "\t\t[body,of,evidence], [body,of,water], [body,of,literature], ['first-',line],\n" +
	     "\t\t[body,of,opinion], [body,of,law], [battle,with,the,bulge], [gold,standard],\n" +
	     "\t\t[battle,of,the,bulge], [battle,for,the,bulge], ['i.e.'], [first,'-',line],\n" + 
	     "\t\t[in,term,of], [state,of,the,art], [state,of,art], [in,detail], [stage,iva],\n" + 
	     "\t\t['state-of-',the,'-',art], ['e.g.'], [in,the,hand,of], [very,low], [under,investigation],\n" +
	     "\t\t% the rest are discourse connectives from PDTB and BioDRB\n" +
	     "\t\t[as,a,matter,of,fact], [as,a,consequence], [as,a,result],\n" + 
	     "\t\t[as,it,turn,out], [at,the,same,time], [by,comparison], [by,contrast],\n" +
	     "\t\t[even,though], [for,example], [for,instance], [for,one,thing],\n" + 
	     "\t\t[in,addition], [in,comparison], [in,contrast], [in,fact],\n" + 
	     "\t\t[in,other,word], [in,particular], [in,return], [in,short], [in,sum],\n" +
	     "\t\t[in,summary], [in,the,end], [in,the,meantime],\n" + 
	     "\t\t[inasmuch,as], [in,as,much,as], [insofar,as], [in,so,far,as],\n" +
	     "\t\t[on,the,contrary], [so,far], [to,this,end], [in,response,to],\n" +
	     "\t\t[in,part,by]]).\n\n" +

	"% This is a list of strings that essentially act like the string in the list\n" +
	"% above, but qualitatively they are different, so I am keeping them in a\n" +
	"% separate list for now. We may find a better solution for these within\n" +
	"% macro-NP processing.\n" +
	"empty_macro_np_head_list([[a,host,of], [a,number,of], [a,series,of]]).\n");
		
					if(thisStmt.length() > 0)
						exceptionFileStmt.append(thisStmt +"\n");
					
					exceptionInput.close();
				log.debug("end of non_prepositionally_cued_object_GEN");
				 errorFileStmt.append("\n");
				Enumeration keys = inverseRel.keys();
				 while (keys.hasMoreElements()) {
					 String pred = (String) keys.nextElement();
					 // log.debug("predicate declared in Exception file : " + pred);
					 HashSet pairSet = (HashSet) inverseRel.get(pred);
					 HashSet invpairSet = null;
					 String predInv = null;
					 for(int ii=0; ii < relationMap.length; ii++) {
						 if(pred.equals(relationMap[ii][0])) {
							 predInv = relationMap[ii][1];
							 break;
						 } else if(pred.equals(relationMap[ii][1])) {
							 predInv = relationMap[ii][0];
							 break;
						 }
					 }
					 // log.debug("predicate declared in Exception file : " + pred + " - " + predInv);
					 
					 if(predInv != null) {
						 invpairSet = (HashSet) inverseRel.get(predInv);
						 Iterator iter = pairSet.iterator();
						 while ( iter.hasNext()) {
							 String pair = (String) iter.next();
							 String compo[] = pair.split("\\-");
							 String pairRev = new String(compo[1] + "-" + compo[0]);
							 // log.debug("looking at inverse pair: " + pair + " : " + pairRev);
							 if(invpairSet != null && !invpairSet.contains(pairRev)) {
								 errorFileStmt.append("ERROR: " + compo[0] + "-" + pred + "-" + compo[1] + " is defined, but not " +compo[1] + "-" + predInv + "-" + compo[0]  + " in Exceptions file\n");
								 // log.debug(compo[0] + "-" + pred + "-" + compo[1] + " is defined, but not " +compo[1] + "-" + predInv + "-" + compo[0]  + " in Exceptions file\n");
							 }
							}  
						      
					 } 
					 
				 } 

			String exceptionFilename = "/download/exceptions_GEN.pl";
			String exceptionxmlrealpath = ctx.getRealPath(exceptionFilename);
			// log("XML context path " + xmlcontextpath);
			log.debug("XML real path " + exceptionxmlrealpath);
			PrintWriter exceptiondomainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(exceptionxmlrealpath)));
			exceptiondomainfile.println(exceptionFileStmt.toString());
			exceptionFileStmt = null;
			exceptiondomainfile.close();
			}
			
			stmt.close();
			// con.close();

			String errorFilename = "/download/error.txt";
			String errorxmlrealpath = ctx.getRealPath(errorFilename);
			PrintWriter errordomainfile
			= new PrintWriter(new BufferedWriter(new FileWriter(errorxmlrealpath)));
			errordomainfile.println(errorFileStmt.toString());
			errordomainfile.close();

			String path = ctx.getRealPath("/download");
			File directory = new File(path);
			String[] files = directory.list();             //
			// Checks to see if the directory contains some files.            //            if (files != null && files.length > 0) {                 //                // Call the zipFiles method for creating a zip stream.                //
			byte[] zip = zipFiles(directory, files);                 //                // Sends the response back to the user / browser. The                // content for zip file type is "application/zip". We                // also set the content disposition as attachment for                // the browser to show a dialog that will let user                 // choose what action will he do to the sent content.                //
			ServletOutputStream sos = response.getOutputStream();
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=\"prolog.zip\""); 
			sos.write(zip);
			sos.flush();
			sos.close();

		} catch(Exception e) {  }
		// return mapping.findForward("success");
		return null; 
	}

    /**     * Compress the given directory with all its files.     */
	private byte[] zipFiles(File directory, String[] files) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(baos);
		byte bytes[] = new byte[2048];
		for (String fileName : files) {
			FileInputStream fis = new FileInputStream(directory.getPath() + "/" + fileName);
			BufferedInputStream bis = new BufferedInputStream(fis);
			zos.putNextEntry(new ZipEntry(fileName));
			int bytesRead;
			while ((bytesRead = bis.read(bytes)) != -1) {
				zos.write(bytes, 0, bytesRead);
			}
			zos.closeEntry();
			bis.close();
			fis.close();
			File thisfile = new File(directory.getPath() + "/" + fileName);
			thisfile.delete();
		}
		zos.flush();
		baos.flush();
		zos.close();
		baos.close();
		return baos.toByteArray();
	}

	private boolean compareSemtype(List<String> ltypes, String stypes) {
		if(ltypes == null)
			return false;
		String[] dmtypes = stypes.split(",");
		if(ltypes.size() != dmtypes.length)
			return false;
		for(int i = 0; i < dmtypes.length; i++) {
			String compared = dmtypes[i];
			if(!ltypes.contains(compared))
				return false;
		}
		return true;

	}

	private String makeString(List<String> ltypes) {
		StringBuffer sb = new StringBuffer();
		// log.debug("String length = " + ltypes.size());
		for(int i=0; i <ltypes.size(); i++) {
			// log.debug("Type component : " + ltypes.get(i));
			sb.append(ltypes.get(i));
			if(i < ltypes.size() -1)
				sb.append(",");
		}
		// log.debug("Type String : " + sb.toString());
		return sb.toString();
	}

	private String nameQuote(String name) {
		String nameTrim = name.trim();
		// log.debug("original name = " + name);
		if( Character.isUpperCase(name.charAt(0)) || (name.charAt(0) == '*') || nameTrim.contains(" ")) { // If name starts with a capital letter or has space
			// log.debug("converted name = " +  "'" + nameTrim + "'");
			return "'" + nameTrim + "'";
		} else {
			// log.debug("converted name = " +  nameTrim);
			return nameTrim;
		}
	}

	private String escapeQuote(String name) {
		String nameTrim = name.trim();
		// log.debug("original name = " + name);
		StringBuffer newStrBuf = new StringBuffer();
		for(int i=0; i < nameTrim.length(); i++) {
			if(nameTrim.charAt(i) == '\'')
				newStrBuf.append("\\\'");
			else
				newStrBuf.append(nameTrim.charAt(i));
		}
		return newStrBuf.toString();
	}

	private String sortTypes(String inTypes) { // Sort list of types in alphabetical orders like "phsu,aapp" -> "aapp,phsu"
		StringBuffer newToType = new StringBuffer();
		if(inTypes.contains(",")) {
			String[] Types = inTypes.split(","); 
			List<String> TypeList= Arrays.asList(Types);
			Collections.sort(TypeList);
			for(String t: TypeList) {
				newToType.append(t + ",");
			}
			newToType.deleteCharAt(newToType.length()-1);
			// log.debug("New type : " + newToType.toString());
			return newToType.toString();
		} else
			return inTypes;
	}
}



