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
    <title>MY CHESNACK</title>
    <link rel="stylesheet" href="style3.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <%@ include file="alert.jsp" %>
</head>
<style>
    .homepage{
        background-color: #66cdd4;
    }
    .homepage a{
        color: #f2866a;
        text-decoration: none;
        position: relative;
        left:50px;
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
            <img src="素材/订单 (1).png" width="40px" height="40px">
            <a href="Order.jsp">Order</a>
        </div>
        <div class="cart">
            <img src="素材/购物车.png" width="40px" height="40px">
            <a href="ShoppingCart.jsp">Shopping cart</a>
        </div>
        <div class="order">
            <img src="素材/社区/社区 (1).png" width="40px" height="40px">
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
        <p>My Order</p>
        <span>
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="20px" height="20px">
        </span>
        <span>
            <img src="素材/收藏.png" alt="collection" width="40px" height="40px">
            <img src="素材/更多3.png" alt="more" width="40px" height="40px">
        </span>
    </div>

    <div class="info">
        <img src="素材/purple-user.png" width="80px" height="70px" id="touxiang">
        <div class="快递情况">
            <span class="Delivery">
                <img src="素材/3.1待发货.png" width="40px" height="40px">
                <p>Delivery</p>
            </span>
            <span class="Receive">
                <img src="素材/待收货.png" width="40px" height="40px">
                <p>Receive</p>
            </span>
            <span class="Evaluate">
                <a href="WaitCommentList.jsp"><img src="素材/3.1待评价.png" width="40px" height="40px"></a>
                <p>Evaluate</p>
            </span>
        </div>
    </div>

    <div class="vip">
        <div class="text">
            <p id="vip">VIP</p>
            <p id="enjoy">Enjoy more benefits!</p>
            <button>JOIN</button>
        </div>
        <img src="素材/购物车空空.png" width="210px" height="210px">
    </div>

    <div class="record">
        <h1>Record</h1>
        <a href="#" target="_blank">All</a>
        <a href="#" target="_blank">Deliver</a>
        <a href="#" target="_blank">Receive</a>

        <table>
            <tr class="date">
                <td>2023/11/3</td>
            </tr>
            <tr>
                <td class="goods">
                    <img src="素材/limon chips.jpg" width="40px" height="40px">
                    <p>Crisps,Lime flavor</p><p>12:09</p>
                </td>
                <td class="price">-76.00</td>
            </tr>
            <tr>
                <td class="goods">
                    <img src="素材/limon chips.jpg" width="40px" height="40px">
                    <p>Crisps,Lime flavor</p><p>12:09</p>
                </td>
                <td class="price">-96.00</td>
            </tr>
            <tr>
                <td class="goods">
                    <img src="素材/snack1.jpg" width="40px" height="40px">
                    <p>Chocolate chip cookie</p><p>15:39</p>
                </td>
                <td class="price">-12.00</td>
            </tr>
        </table>
    </div>

    <div class="report">
        <h1>Report</h1>
        <div class="top">
            <div class="card">
                <p>Card</p>
                <div class="建行">China Construction Bank</div>
                <div class="农行">China Agricultural Bank</div>
                <div class="工行">China Industrial Bank</div>
            </div>
            <div class="diagram">
                <img src="素材/组 33.png" width="150px" height="150px">
                <div class="color">
                    <div class="orange"></div>
                    <p>nut</p>
                    <div class="blue"></div>
                    <p>cookie</p>
                    <div class="purple"></div>
                    <p>fruit</p>
                </div>
            </div>
        </div>
        <img src="素材/组 38.png" width="520px" height="200px">
        

    </div>


    
    
    
</body>
</html>