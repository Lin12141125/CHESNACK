<%@page import="java.util.Locale"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.util.ArrayList"%>
<%@page import="util.ChesnakeDAO"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="util.HttpUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@ include file="session.jsp" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>PRODUCT</title>
    <link rel="stylesheet" href="style7.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <script src="<%=request.getContextPath() %>/js/toastr.min.js"></script>
    <%@ include file="alert.jsp" %>
    <script>
        function checkform(){
            let searchText=$("input[name='searchText']").val().trim();
            if(searchText==''){
            	alert("You search nothing.");
            	return false;
            }
            return true;
        }
        
        function filter(name,val){
        	$("input[name='"+name+"']").val(val);
        	$("form[name='filterForm']")[0].submit();
        }
    </script>
    <style>
      .items:hover{
        background-color:rgba(255,255,255,0.1)!important;
        box-sizing: border-box;
      }
      .link1{
        font-weight: normal!important;
      }
      .active-link{
        color: #f2866a;
         background-color: #fff2ec;
      }
    </style>
</head>
<body>
    <form name="filterForm" method="post" action="HomePageSearch">
       <input type="hidden" name="searchText" value="${param.searchText}"/>
       <input type="hidden" name="brand"  value="${param.brand}"/>
       <input type="hidden" name="type" value="${param.type}"/>
       <input type="hidden" name="origin" value="${param.origin}"/>
       <input type="hidden" name="field" value="${param.field}"/>
       <input type="hidden" name="direction" value="${param.direction}"/>
    </form>
    <div class="navbar">
    	<div class="logo">
    		<img src="素材/白底logo.png" width="60px" height="50px">
        </div>
        <div class="search">
            <form action="HomePageSearch" method="post" onsubmit="return checkform()">
                <input type="text" name="searchText" id="search">
                <input type="submit" name="search" value="SEARCH">
                <input type="image" src="素材/搜索.png" width="30px" height="30px">
            </form>  
        </div>
        <div class="menu">
            <ul>
                <li><a href="HomePage.jsp">HomePage</a></li>
                <li><a href="#">Connect</a></li>
                <li><a href="UserPage.jsp" target="_blank">Privacy</a></li>
                <li><a href="#" id="cart" target="_blank"><img src="素材/购物车空.png" width="30px" height="30px"></a></li>
            </ul>
        </div>
        <div class="user-info">
            <div class="user-avatar"></div>
            <span class="username"><%=uname %></span>
            <!-- <a href="#"><img src="素材/avatar.jpg" width="30px" height="30px"></a>
            <p>myname</p> -->
        </div>
    </div>

    <!--筛选部分-->
    <div class="filter">
        <p class="Screen">Screen</p>
        
        <%
           List<Map<String,Object>> brandlist=ChesnakeDAO.list("select distinct brand from goods");
        %>
        <div class="Brand">
            <p style="bottom:auto;">Brand :</p>
            <div class="items" style="height:auto;text-align:left;">
	            <% for(int i=0;i<brandlist.size();i++){ %>
	            <div <%=brandlist.get(i).get("brand").toString().equals(request.getParameter("brand"))?"class='active-link'":"" %>><a href="#" class="link1" onclick="filter('brand','<%=brandlist.get(i).get("brand") %>')"><%=brandlist.get(i).get("brand") %></a></div>
	            <%} %>
            </div>
        </div>
         <%
           List<Map<String,Object>> typelist=ChesnakeDAO.list("select distinct type from goods");
        %>
        <div class="Madein">
            <p style="bottom:auto;">Type :</p>
            <div class="items" style="height:auto;text-align:left;margin-left:15px;">
	            <% for(int i=0;i<typelist.size();i++){ %>
	            <div <%=typelist.get(i).get("type").toString().equals(request.getParameter("type"))?"class='active-link'":"" %>>
	            <a href="#" class="link1" onclick="filter('type',$(this).text())"><%=typelist.get(i).get("type") %></a></div>
	            <%} %>
            </div>
        </div>
        <%
           List<Map<String,Object>> originList=ChesnakeDAO.list("select distinct origin from goods");
        %>
        <div class="Madein">
            <p style="bottom:auto;">Origin :</p>
            <div class="items" style="height:auto;text-align:left;">
	            <% for(int i=0;i<originList.size();i++){ %>
	            <div <%=originList.get(i).get("origin").toString().equals(request.getParameter("origin"))?"class='active-link'":"" %>>
	            <a href="#" class="link1"  onclick="filter('origin','<%=originList.get(i).get("origin") %>')"><%=originList.get(i).get("origin").toString().replaceAll("province", "").replaceAll("Province", "").trim() %></a></div>
	            <%} %>
            </div>
        </div>
        
        <!-- 排序方式 -->
        <div class="排序方式">
            <div <%="score".equals(request.getParameter("field"))?"class='active-link'":"" %>><a href="#" onclick="filter('field','score')">Comprehensive</a></div>
            <div <%="price".equals(request.getParameter("field"))?"class='active-link'":"" %>><a href="#" onclick="filter('field','price')">Price</a></div>
            <div <%="sale_quantity".equals(request.getParameter("field"))?"class='active-link'":"" %>><a href="#" onclick="filter('field','sale_quantity')">Sale</a></div>
            <div>
                <img src="素材/升序.png" width="30px" height="30px"  onclick="filter('direction','asc')">
                <img src="素材/降序.png" width="30px" height="30px"  onclick="filter('direction','desc')">
            </div>
        </div>
    </div>
    <div class="SHOW1">
    <%
    List<Map<String,Object>> list=(List<Map<String,Object>>)request.getAttribute("list");
    if(list==null){
    	list=new ArrayList<Map<String,Object>>();
    }
    for(int i=0;i<list.size();i++){
    	Map<String,Object> item=list.get(i);
  	%>
        <a href="ShowProduct.jsp?id=<%= item.get("id") %>">
        <div class="show">
            <img src="images/<%=item.get("img1") %>" alt="chips" width="210px" height="210px">
            <p><b><%=item.get("name").toString().substring(0, Math.min(20, item.get("name").toString().length()))+"..." %></b></p>
            <p>$<%=item.get("price") %></p>
            <p><%=item.get("sale_quantity") %> copies sold</p>
            <p><b></b></p>
        </div>
        </a>
        <%} %>

    </div>
    
</body>
</html>