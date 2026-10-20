<%@page import="util.ChesnakeDAO"%>
<%@page import="util.UserDAO"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="util.HttpUtils"%>
<%@page import="util.StringUtils"%>
<%@page import="java.time.LocalDateTime" %>
<%@page import="java.time.format.DateTimeFormatter" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="session.jsp" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>MY ORDER</title>
    <link rel="stylesheet" href="style3.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <script src="<%=request.getContextPath() %>/js/toastr.min.js"></script>
    <%@ include file="alert.jsp" %>
</head>
<style>
    .order{
        background-color: #66cdd4;
    }
    .order a{
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
            <img src="素材/avatar.jpg" alt="头像" width="60px" height="60px">
            <p><%=uname %></p>
        </div>
        <div class="homepage">
            <img src="素材/个人 (1).png" width="40px" height="40px">
            <a href="UserPage.jsp">Homepage</a>
        </div>
        <div class="order">
            <img src="素材/订单.png" width="40px" height="40px">
            <a href="Order.jsp">Order</a>
        </div>
        <div class="cart">
            <img src="素材/购物车.png" width="40px" height="40px">
            <a href="ShoppingCart.jsp">Shopping cart</a>
        </div>
        <div class="homepage">
            <img src="素材/社区/社区 (1).png" width="40px" height="40px">
            <a href="community-personal.jsp">Community</a>
        </div>
        <div class="set">
            <img src="素材/设置.png" width="40px" height="40px">
            <a href="#">Set</a>
        </div>
        <div class="more">
            <img src="素材/更多 (1).png" width="40px" height="40px">
            <a href="#" target="_blank">More</a>
        </div>
    </div>

    <div class="header">
        <p>My Order</p>
        <span>
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="20px" height="20px">
        </span>
        <span>
            <img src="素材/收藏.png" alt="collection" width="20px" height="20px">
            <img src="素材/更多3.png" alt="more" width="20px" height="20px">
        </span>
    </div>

    <div class="myorder">
        <div class="表头">
            <p>Product</p><p>Date</p><p>Price</p>
        </div>
        <%
	    List<Map<String,Object>> list=UserDAO.list("select t1.*, t2.img1 img, t2.name name from `order` t1, chesnake.goods t2 where t1.good_id=t2.id and t1.email=? order by t1.`time` desc",account);
	    for(int i=0;i<list.size();i++){
	    	Map<String,Object> item=list.get(i);
    	String goodsID=item.get("id").toString();
	    %>
        <div class="details">
            <span class="goods">
                <img src="images/<%=item.get("img") %>" width="40px" height="40px">
                <p><%=item.get("name").toString().substring(0, Math.min(25, item.get("name").toString().length()))+"..." %></p><p><%=StringUtils.fmtTime(item.get("time")) %></p>
            </span>
            <p><%=StringUtils.fmtDate(item.get("time")) %></p><p><%=item.get("price") %></p>
        </div>
        <%} %>

    </div>
</body>
</html>