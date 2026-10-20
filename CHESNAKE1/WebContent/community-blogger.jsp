<%@page import="util.UserDAO"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%@ include file="session.jsp"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>COMMUNITY</title>
    <link rel="stylesheet" href="style11.css">

    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
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
    #recommend a,#follow a{
        color: #f2866a;
        text-decoration: none;
        font-size: 20px;
        font-weight: bold;
    }
    #recommend:hover,#follow:hover{
        width: 140px;
        height: 40px;
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
    #recommend a:hover,#follow a:hover{
        color: white;
    }
</style>
<body>
<%
 String userName=request.getParameter("account");
  Map<String,Object> user=UserDAO.find("select * from login where email=?", userName);
  if(user==null){
	  out.println("User not found!");
	  return;
  }
%>
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
    </div>

    <div class="personal">
        <img src="素材/purple-M.jpg" alt="头像" width="85px" height="80px" id="头像">
        <p id="name"><%=user.get("username") %></p>
        <p id="id"><%=user.get("email") %></p>
        <span class="collection">
            <img src="素材/社区/收藏.png" width="45px" height="45px">
            <p>123</p>
        </span>
        <span class="like">
            <img src="素材/社区/点赞 .png" width="45px" height="45px">
            <p>345</p>
        </span>
        <span class="evaluation">
            <img src="素材/社区/评论.png" width="45px" height="45px">
            <p>68</p>
        </span>
        <span class="follows">
            <a href="javascript:void(0);" onclick="doFollow()">
                <img src="素材/社区/我的关注.png" width="45px" height="45px">
                <p>25</p>
            </a>
            
        </span>
        <img src="素材/购物车空空.png" width="250px" height="250px" id="购物车空空">
    </div>

    <p id="post">Posts</p>

    <div class="SHOW2">
    	<%
    		List<Map<String,Object>> list=UserDAO.list("select * from community WHERE account = ?",user.get("email"));
    		for(int i=0;i<list.size();i++){
    			Map<String,Object> item2=list.get(i);
    	%>
    	<a href="javascript:void(0);">
        <div class="show">
            <% if(item2.get("img1")!=null){ %>
            <img  onclick="location.href='ShowCommunity.jsp?id=<%= item2.get("id") %>'" src="images/<%=item2.get("img1") %>" alt="chips" width="230px" height="230px">
            <% } %>
            <p onclick="location.href='ShowCommunity.jsp?id=<%= item2.get("id") %>'"><%=item2.get("title")==null?"":item2.get("title") %></p>
            <div id="touxiang">M</div>
            <p id="user"><%=item2.get("username") %></p>
            <img src="素材/社区/点赞 .png" alt="like" width="35px" height="30px" id="like">
        </div>
       </a>
        <%} %>
    </div>
    <script>
       function doFollow(){
    	   $.post("addFollow",{
    			username:"<%=user.get("username")%>"
    		},function(res){
    			if(res=='NotLogin'){
    				alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
    				location.href='login.jsp';
    			}else if(res=="Success"){
    				alert("success Follow");
    				
    			}else if(res=="Already follow"){
    				alert("success Follow");
    			}else if(res=="self follow"){
    				alert("you cannot follow yourself")
    			}
    		})
       }
    </script>
</body>
</html>