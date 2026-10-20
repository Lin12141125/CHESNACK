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
    <title>Main Page</title>
    <link rel="stylesheet" href="style2.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
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
    </script>
</head>

<body>
    <!-- 导航栏 -->
    <div class="navbar">
        <div class="search">
            <div class="logo">
                <img src="素材/白底logo.png" width="100px" height="80px">
            </div>
            <form action="HomePageSearch" method="post" onsubmit="return checkform()">
                <input type="text" name="searchText" id="search">
                <input type="submit" name="search" value="SEARCH" src="素材/搜索.png" width="30px" height="30px">
            </form>
            
        </div>
        <div class="menu">
            <ul>
                <li><a href="#">HomePage</a></li>
                <li><a href="#">Connect</a></li>
                <li><a href="UserPage.jsp" target="_blank">Privacy</a></li>
                <li><a href="ShoppingCart.jsp" id="cart" target="_blank"><img src="素材/购物车空.png" width="30px" height="30px"></a></li>
            </ul>
        </div>
        <div class="user-info">
            <a href="UserPage.jsp" target="_blank"><img src="素材/avatar.jpg" alt="touxiang" width="40px" height="40px"></a>
            <a href="UserPage.jsp" target="_blank">
                <p id="username"><%=uname %></p>
            </a>
        </div>
    </div>

    <!-- 首页展示 -->
    <div class="container">
        <!-- 先把第一张图片显示出来 -->
        <img class="on" src="素材/snack1.jpg">
        <img src="素材/snack2.jpg">
        <img src="素材/snack3.jpg">
        <img src="素材/snack4.jpg">
        <!-- 左右切换 -->
        <div class="left"><</div>
        <div class="right">></div>
        <!-- 焦点 -->
        <ul>
            <li class="active"></li>
            <li></li>
            <li></li>
            <li></li>
        </ul>
    </div>

    <div class="component">
        <span>
            <div class="like">
                <img src="素材/snack5.jpg" width="150px" height="150px">
            </div>
            <p>Like</p>
        </span>
        <span>
            <div class="discount">
                <img src="素材/snack6.jpg" width="150px" height="150px">
            </div>
            <p>Discount</p>
        </span>
        <span>
            <div class="recommend">
                <img src="素材/snack7.jpg" width="150px" height="150px">
            </div>
            <p>Recommend</p>
        </span>
        <span>
            <div class="sales">
                <img src="素材/limon chips.jpg" width="150px" height="150px">
            </div>
            <p>Sales</p>
        </span>
        
    </div>
    
    <!-- 分界线 -->
    <img src="素材/资源 2.png" alt="资源2" width="180px" height="90px" class="leftpic">
    <img src="素材/资源 11.png" alt="资源11" width="200px" height="210px" class="rightpic">
    <div class="pline"></div>
    
    <!-- 猜你喜欢 -->
    <div class="gwyl">
        <p><b>Guess What You Like</b></p>
    </div>

    <div class="SHOW1">
    <%
    List<Map<String,Object>> list=ChesnakeDAO.list("select * from goods order by score desc");  
    for(int i=0;i<list.size();i++){
    	Map<String,Object> item=list.get(i);
//    	String goodsID=item.get("id").toString();
    %>
        <a href="ShowProduct.jsp?id=<%= item.get("id") %>">
        <div class="show">
            <img src="images/<%=item.get("img1") %>" alt="chips" width="210px" height="210px">
            <p><b><%=item.get("name").toString().substring(0, Math.min(20, item.get("name").toString().length()))+"..." %></b></p>
            <p>$<%=item.get("price") %></p>
            <p><%=item.get("sale_quantity") %> copies sold</p>
            <p><b>...</b></p>
        </div>
        </a>
    <%} %>
    </div>
    
    <script src="js.js"></script>
</body>
</html>