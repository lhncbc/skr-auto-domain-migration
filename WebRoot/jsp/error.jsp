
<%@ page language="java" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://displaytag.sf.net" prefix="display" %>
<%@ taglib uri="http://ajaxanywhere.sourceforge.net/" prefix="aa" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html locale="true">
  <head>
    <html:base />
    <title>SemMed - Error Page</title>    
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">    
    <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
    <meta http-equiv="description" content="Error Page">
    <link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet" type="text/css">
  </head>
  <body>
  <div id="container">
  <jsp:include page="/jsp/header.jsp"/>
  <div id="content">
  		<logic:present name="exceptions" scope="request">
			<span id="errorsHeader"><bean:message key="errors.app.header"/></span>  
 			<logic:messagesPresent>
				<html:messages id="emsg"><bean:write name="emsg"/> The stack trace follows:</html:messages> 
			</logic:messagesPresent> 
			<code><bean:write name="exceptions" scope="request"/></code>
	    	<span id="errorsFooter"><bean:message key="errors.app.footer"/></span>  
	    </logic:present>  		

  </div>
  <jsp:include page="/jsp/footer.jsp"/>	
  </div>
  </body>
</html:html>