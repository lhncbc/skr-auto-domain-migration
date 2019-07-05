/**
 * 
 */
package gov.nih.nlm.semmed.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * @author hkilicoglu
 *
 *
 * This class doesn't do anything at the time [Alejandro]
 *
 *
 */
public class InitializeVisualizationAction extends Action {
	

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) {
	
		request.getSession().removeAttribute("executionTime");
		return mapping.findForward("success");
	}

}

