<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="util.StringUtils"%>
<%@page import="util.UserDAO"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="session.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VIOLATION INFORMATION</title>
    <link rel="stylesheet" href="style12.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <script type="text/javascript">
      function deleteReport(id){
    	    if(!confirm('confirm if delete comment '+id+'?')) return;
	    	$.get("comment?action=del&id="+id,function(res){
	    		if(res=='NotLogin'){
	    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
	    			location.href='login.jsp';
	    		}else if(res=='Success'){
	    			alert('Successfully delete');
	    			location.reload();
	    		}
	    	});
      }
    </script>
</head>
<style>
    .report{
        background-color: #66cdd4;
    }
    .report a{
        color: #f2866a;
        text-decoration: none;
        position: relative;
        left:60px;
        top:8px;
        font-size: 20px;
        font-weight: bold;
    }
</style>
<body>
    <div class="subnav">
        <div class="myname">
            <img src="素材/白底logo.png" alt="头像" width="80px" height="75px">
            <p>Administrator</p>
        </div>
        <div class="dashboard">
            <img src="素材/dashboard(1).png" width="40px" height="40px">
            <a href="administrator.jsp" target="_blank">Dashboard</a>
        </div>
        <div class="order">
            <img src="素材/订单 (1).png" width="40px" height="40px">
            <a href="administrator.jsp" target="_blank">Order</a>
        </div>
        <div class="delivery">
            <img src="素材/物流.png" width="40px" height="40px">
            <a href="administrator.jsp" target="_blank">Delivery</a>
        </div>
        <div class="warehouse">
            <img src="素材/社区/商品.png" width="40px" height="40px">
            <a href="administrator-warehouse.jsp">Warehouse</a>
        </div>
        <div class="report">
            <img src="素材/设置 (1).png" width="40px" height="40px">
            <a href="administrator-report.jsp">Report</a>
        </div>
    </div>

    <div class="header">
        <p>Deal information</p>
        <span id="search">
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="30px" height="30px">
        </span>
        <span id="user">
            <img src="素材/purple-M.jpg" alt="touxiang" width="50px" height="45px">
            <p id="name"><%=uname %></p>
        </span>
    </div>
    <%
       List<Map<String,Object>> reportlist=UserDAO.list("select * from `comment` where reported=1 order by send_time desc");
    %>
    <div class="main">
        <p id="title">Violation information(<%=reportlist.size() %>)</p>
        <% if(reportlist.size()>0) {%>
        <hr style="width: 100%; color: gray;">
        <%} %>
        <%
        DateFormat format=new SimpleDateFormat("yyyy/MM/dd");
          for(Map<String,Object> report : reportlist){
        %>
        <div class="gray">
            <div class="gray-inner">
              <div class="circle2"></div>
              <p id="grey"><%=StringUtils.fmtDate(report.get("send_time"))%></p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
             
            <%
              int level=0;
             if(report.get("level")!=null) 
            	level=Integer.parseInt(report.get("level").toString());
             for(int i=0;i<level;i++){
            %>
            <img src="素材/黄色五角星.png" width="30px" height="40px">
            <%} %>
              <div style="flex:1">
                 <button id="delete" onclick="deleteReport('<%=report.get("id")%>')">Delete</button>
              </div>
            </div>

            <p id="black"><%=report.get("content") %></p>

            
        </div>
        <%} %>
    </div>
    
</body>
</html>