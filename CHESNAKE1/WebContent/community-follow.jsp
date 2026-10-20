<%@page import="util.ChesnakeDAO"%>
<%@page import="util.UserDAO"%>
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
    <title>COMMUNITY</title>
    <link rel="stylesheet" href="style11.css">
</head>
<style>
    .community{
        background-color: #66cdd4;
    }
    .community a{
        color: #f2866a;
        text-decoration: none;
        position: relative;
        left:60px;
        top:8px;
        font-size: 20px;
        font-weight: bold;
    }
    .SHOW1{
        position: absolute;
        top: 220px;
        left: 20%;
    }
    #recommend:hover,#follow{
        width: 140px;
        height: 40px;
        color: white;
        text-align: center;
        font-size: 20px;
        font-weight: bold;
        background-color: #66cdd4;
        line-height: 40px;
        border: 1px solid #66cdd4;
        border-radius: 20px;
        position: relative;
        bottom: 10px;
    }
    #recommend a{
        font-size: 20px;
        font-weight: bold;
        color: #f2866a;
        text-decoration: none;
    }
    #recommend a:hover{
        color: white;
    }
    .round{
        margin-top:20px;
        margin-left:10px;
       display:flex;
       justify-content: center;
       align-items: center;
              border-radius: 50%;
       background-color:#9F91C2;
       color:#fff;
       width:50px;
       height:50px
    }
    .SHOW1{
       top:280px;
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
            <img src="素材/订单 (1).png" width="40px" height="40px">
            <a href="Order.jsp">Order</a>
        </div>
        <div class="cart">
            <img src="素材/购物车.png" width="40px" height="40px">
            <a href="ShoppingCart.jsp">Shopping cart</a>
        </div>
        <div class="community">
            <img src="素材/社区/社区.png" width="40px" height="40px">
            <a href="community-personal.jsp">Community</a>
        </div>
        <div class="set">
            <img src="素材/设置.png" width="40px" height="40px">
            <a href="#">Set</a>
        </div>
        <div class="more">
            <img src="素材/更多 (1).png" width="40px" height="40px">
            <a href="#">More</a>
        </div>
    </div>

    <div class="header">
        <p>Community</p>
        <span>
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="30px" height="30px">
        </span>
        <div id="recommend"><a href="community-recommend.jsp">Recommend</a></div>
        <div id="follow">Follow</div>
        <a href="community-personal.jsp"><div id="M"><%=uname.length()>=2?uname.substring(0,2):uname %></div></a>
    </div>

    <%
      List<Map<String,Object>> followList=UserDAO.list("select * from login where email in (select account from follow where followee=?) ",uname);
    %>
    <div class="follower" style="display:flex;">
        <%
          for(Map<String,Object> follow :followList){
            String fname=follow.get("username")==null?"":follow.get("username").toString();
            if(fname.length()>3) fname=fname.substring(0,3);
        %>
          <div class="round"><%=fname %></div>
        <%} %>
        <img src="素材/更多.png" alt="more" width="80px" height="70px" id="more">
    </div>
    <div class="SHOW1">
    <%
    List<Map<String,Object>> list=UserDAO.list("select * from community where account in (select account from follow where followee=?)",uname);
    for(int i=0;i<list.size();i++){
    	Map<String,Object> item=list.get(i);
    %>
        
        
        <a href="ShowCommunity.jsp?id=<%= item.get("id") %>">
        <div class="show">
            <% if(item.get("img1")!=null){ %>
            <img src="images/<%=item.get("img1") %>" alt="chips" width="230px" height="230px">
            <% } %>
            <%
            String title=item.get("title")==null?"":item.get("title").toString();
            if(title.length()>=20) title=title.substring(0,20)+"...";
            %>
            <p><%=title %></p>
            <% String pname=item.get("username")==null?"":item.get("username").toString(); %>
            <div id="touxiang"><%=pname.length()>=2?pname.substring(0,2):pname %></div>
            <p id="user"><%=item.get("username") %></p>
            <img src="素材/社区/点赞 .png" alt="like" width="35px" height="30px" id="like">
        </div>
        </a>
        
        <%} %>
    </div>

    <div class="upload"><a href="community-upload.jsp">+</a></div>
</body>
</html>