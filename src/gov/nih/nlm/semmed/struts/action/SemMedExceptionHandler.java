/**
 * 
 */
package gov.nih.nlm.semmed.struts.action;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;

/**
 * @author hkilicoglu
 *
 */
public class SemMedExceptionHandler extends ExceptionHandler {
	
//	private static Log log = LogFactory.getLog(SemMedExceptionHandler.class);		
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		Exception ex,
		ExceptionConfig ec,
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws ServletException {
		
		ActionForward forward = super.execute(ex, ec, mapping, form, request, response);
		
		ExceptionConfig config = mapping.findException(ex.getClass());
		String property = config.getKey();
		ActionError error = new ActionError(property);
		ActionErrors errors = new ActionErrors();
		errors.add(property, error);

		String trace = new String();
		if (ex != null) {
		    Throwable t = ex;
		    StringWriter sw = new StringWriter();
		    PrintWriter  pw = new PrintWriter(sw);
		    t.printStackTrace(pw);
		    pw.flush();
		    trace = sw.toString();
		}
		request.setAttribute("exceptions", trace);
	
		return forward;
	}
	

}
