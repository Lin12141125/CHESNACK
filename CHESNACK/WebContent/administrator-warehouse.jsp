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
    <title>WAREHOUSE</title>
    <link rel="stylesheet" href="style10.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <script src="<%=request.getContextPath() %>/js/toastr.min.js"></script>
    <script>
        function del(id){
            $.get("deleteGoods","id="+id,function(res){
                if(res=='Success'){
                    $('#order'+id).detach();
                }else if(res=='NotLogin'){
                    alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
                    location.href='login.jsp';
                }else if(res=='Failure'){
                    toastr.error('fail to refund, the order does not exist!');
                }
            });
        }
        function triggerTotalSelect(t){
            var items=document.querySelectorAll("input[name='orderSelect']");
            for(var i=0;i<items.length;i++)
                items[i].checked=t.checked;
        }
        
        function triggerOrderSelect(t){
            var items=document.querySelectorAll("input[name='orderSelect']");
            var selected=0;
            for(var i=0;i<items.length;i++)
                if(items[i].checked) selected+=1;
            $('#totalSelect').prop('checked',items.length==selected);
        }
    </script>
</head>
<style>
    .warehouse{
        background-color: #66cdd4;
    }
    .warehouse a{
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
            <a href="administrator.jsp">Dashboard</a>
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
            <img src="素材/商品 (1).png" width="40px" height="40px">
            <a href="administrator-warehouse.jsp">Warehouse</a>
        </div>
        <div class="report">
            <img src="素材/设置.png" width="40px" height="40px">
            <a href="administrator-report.jsp">Report</a>
        </div>
    </div>

    <div class="header">
        <p>Warehouse</p>
        <span id="search">
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="30px" height="30px">
        </span>
        <span id="user">
            <img src="素材/purple-M.jpg" alt="touxiang" width="50px" height="45px">
            <p id="name"><%=uname %></p>
        </span>
    </div>

    <div class="main">
    <%
    	List<Map<String,Object>> list=ChesnakeDAO.list("SELECT * FROM chesnake.goods");
    %>
        <p id="title">Product(<%=list.size() %>)</p>
        <hr style="width: 100%; color: gray;">
        <span class="全选">
            <input type="checkbox" name="Selcet1">
            <p>Select All</p>
            <p>Information</p>
            <p>Unit Price</p>
            <p>Quantity</p>
            <p>Producer</p>
            <p>Operation</p>
        </span><br>
        <%
	    for(int i=0;i<list.size();i++){
	    	Map<String,Object> item=list.get(i);
	    %>
        <div class="gray" id="order<%=item.get("id") %>">
            <input type="checkbox" name="orderSelect" value="<%=item.get("id") %>" onclick="triggerOrderSelect(this)">
            
            <img src="images/<%=item.get("img1") %>" width="70px" height="70px">
            <p id="名称"><%=item.get("name").toString().substring(0, Math.min(15, item.get("name").toString().length()))+"..." %></p>
            <p id="单价"><%=item.get("price") %></p>
            <p id="数量"><%=item.get("net_content") %></p>
            <p id="厂商"><%=item.get("origin") %></p>
            <button onclick="del('<%=item.get("id") %>')">Delete</button>
        </div>
        <%} %>
<a href="administrator-add goods.jsp" target="_blank">add+</a>
        <div class="add"><a href="administrator-add goods.jsp" target="_blank">+</a></div>
    </div>
    

    
</body>
</html>