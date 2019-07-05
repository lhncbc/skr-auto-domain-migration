
<%@ page language="java"
	import="gov.nih.nlm.semmed.model.*,java.util.*,org.apache.struts.util.*"
	pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean"
	prefix="bean"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html"
	prefix="html"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic"
	prefix="logic"%>
<%@ taglib uri="pageabletable.tld" prefix="ale"%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html locale="true">
<head>
	<html:base />
	<title>ADM</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="Search Page">
	<link href="<html:rewrite page="/css/semmed.css" />" rel="stylesheet"
		type="text/css">
	<script type="text/javascript" src="../scripts/boxover.js" /></script>
	<script type="text/javascript" src="../scripts/search.js"></script>
</head>
<body>
	<!--
	<div id="container">
-->
	<jsp:include page="/jsp/header.jsp" />
	<ul class="tabs">
		<li>
			<span class="left"><strong><a href="../Welcome.do"><span
						class="center">Excel Input</span> </a> </strong> </span>
		</li>
	</ul>

	<br>

	<div id="content">
		<html:form action="Input" method="POST" focus="file" enctype="multipart/form-data">
			<logic:messagesPresent>
				<span id="errorsHeader"><bean:message key="errors.header" />
				</span>
				<html:messages id="emsg">
					<li>
						<bean:write name="emsg" />
					</li>
				</html:messages>
				<span id="errorsFooter"><bean:message key="errors.footer" />
				</span>
			</logic:messagesPresent>
	<br>
	<hr>
	<b> Domain Part Conversion </b>
	<br>
				<table>
					<tr>
						<td>
								Domain name:
						</td>
						<td colspan="2" nowrap="nowrap">
							<input type="text" size="20" name="domain" >
						</td>
					</tr>
					<tr>
						<td>
								Upload Excel Domain File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelDomainFile" />
						</td>
					</tr>
					<tr>
						<td>
							Upload Excel Exceptions File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelExceptionFile" />
						</td>
					</tr>
					<tr>
						<td>
							Upload Excel  Locsemnet File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelLocsemnetFile" />
						</td>
					</tr>
					<tr>
						<td>
							Upload Excel Semrules File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelSemrulesFile" />
						</td>
						<td>
						<html:submit property="method">
							<bean:message key="search.button.uploadDOMExcel" />
						</html:submit>
						</td>
					</tr>

					<% if(session.getAttribute("uploadDOMExcelComplete") != null){%>
					<tr>
					<td colspan="6">Upload DOM Excel file is completed! </td>
					</tr>
					<% } %>
					<tr>
					<td> &nbsp; &nbsp; </td>
					</tr>
				</table>
	</html:form>
	<br>
	<hr>
	<b> Generic Part Conversion </b> 
	<br>
	<html:form action="Input" method="POST" focus="file" enctype="multipart/form-data">			
				<table>
					<td>
							Year of UMLS Semantic Network:
						</td>
						<td colspan="2" nowrap="nowrap">
							<input type="text" size="20" name="year" >
						</td>
					</tr>
					<tr>
						<td>
							Upload GEN Domain Excel File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelDomainFile" />
						</td>
					</tr>
					<tr>
						<td>
							Upload GEN Exceptions Excel File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelExceptionFile" />
						</td>
					</tr>
					<tr>
						<td>
							Upload GEN Locsemnet Excel File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelLocsemnetFile" />
						</td>
					</tr>
					<tr>
						<td>
							Upload GEN Semrules Excel File
						</td>
						<td colspan ="3">
							<html:file property="uploadExcelSemrulesFile" />
						</td>
					</tr>
					<tr>
						<td>
							Upload Semnet_access.pl File
						</td>
						<td colspan ="3">
							<html:file property="uploadSemnetaccessFile" />
						</td>
						<td>
						<html:submit property="method">
							<bean:message key="search.button.uploadGENExcel" />
						</html:submit>
						</td>
					</tr>

					<% if(session.getAttribute("uploadGENExcelComplete") != null){%>
					<tr>
					<td colspan="6">Upload GEN Excel file is completed! </td>
					</tr>
					<% } %>
					<tr>
					<td> &nbsp; &nbsp; </td>
					</tr>
				</table>

		</html:form>
	</div>
	<jsp:include page="/jsp/footer.jsp" />

	<!-- </div>-->
</body>
<script type="text/javascript">
	toggleOptions();
</script>

</html:html>
