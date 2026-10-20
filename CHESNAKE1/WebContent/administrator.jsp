<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="session.jsp" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ADMINISTRATOR</title>
    <link rel="stylesheet" href="style10.css">
</head>
<style>
    .dashboard{
        background-color: #66cdd4;
    }
    .dashboard a{
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
            <img src="素材/dashboard.png" width="40px" height="40px">
            <a href="administrator.jsp">Dashboard</a>
        </div>
        <div class="order">
            <img src="素材/订单 (1).png" width="40px" height="40px">
            <a href="#" target="_blank">Order</a>
        </div>
        <div class="delivery">
            <img src="素材/物流.png" width="40px" height="40px">
            <a href="#" target="_blank">Delivery</a>
        </div>
        <div class="warehouse">
            <img src="素材/社区/商品.png" width="40px" height="40px">
            <a href="administrator-warehouse.jsp">Warehouse</a>
        </div>
        <div class="report">
            <img src="素材/设置.png" width="40px" height="40px">
            <a href="administrator-report.jsp">Report</a>
        </div>
    </div>

    <div class="header">
        <p>Dashboard</p>
        <span id="search">
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="30px" height="30px">
        </span>
        <span id="user">
            <img src="素材/purple-M.jpg" alt="touxiang" width="50px" height="45px">
            <p id="name"><%=uname %></p>
        </span>
    </div>

    <div class="status">
        <p>Status</p>
        <img src="素材/管理员主界面.png" alt="more" width="460px" height="230px">
    </div>

    <div class="traffic">
        <p>Traffic</p>
        <img src="素材/traffic.png" alt="traffic" width="460px" height="230px">
    </div>

    <div class="shipment">
        <p>Shipment</p>
        <table>
            <tr>
                <th>Product</th>
                <th>Shipper</th>
                <th>Status</th>
                <th>Customer</th>
                <th>Number</th>
            </tr>
            <tr>
                <td>#123789</td>
                <td>Amazom</td>
                <td>Pickup</td>
                <td>Name</td>
                <td>QA123789</td>
            </tr>
            <tr>
                <td>#123789</td>
                <td>Amazom</td>
                <td>Shipping</td>
                <td>Name</td>
                <td>QA123789</td>
            </tr>
            <tr>
                <td>#123789</td>
                <td>Amazom</td>
                <td>Pickup</td>
                <td>Name</td>
                <td>QA123789</td>
            </tr>
            <tr>
                <td>#123789</td>
                <td>Amazom</td>
                <td>Pending</td>
                <td>Name</td>
                <td>QA123789</td>
            </tr>
            <tr>
                <td>#123789</td>
                <td>Amazom</td>
                <td>Shipping</td>
                <td>Name</td>
                <td>QA123789</td>
            </tr>
        </table>                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
    </div>

    <div class="tracking">
        <p>Tracking</p>
        <img src="素材/管理员主界面2.png" alt="table" width="460px" height="230px">
    </div>
</body>
</html>