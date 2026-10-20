<%@ page  import="util.StringUtils" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<script defer type="text/javascript">
    <%
    String alertMes=(String)request.getSession().getAttribute("__page.alert");
    if(!StringUtils.isEmpty(alertMes)){
	   request.getSession().removeAttribute("__page.alert");
    %>
    $(function(){
    	alert("<%=alertMes%>");
    	});
    <%}%>
</script>