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
    <title>MY CART</title>
    <link rel="stylesheet" href="style6.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <script src="<%=request.getContextPath() %>/js/toastr.min.js"></script>
    <%@ include file="alert.jsp" %>
    <script>
	    function del(id){
	    	$.get("deleteCart?id="+id,function(res){
	    		if(res=='Success'){
	    			$('#order'+id).detach();
	    			calculateTotalPrice();
	    		}else if(res=='NotLogin'){
	    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
	    			location.href='login.jsp';
	    		}else if(res=='Failure'){
	    			toastr.error('fail to refund, the order does not exist!');
	    		}
	    	});
	    }
	    function updateCount(n,id,unitPrice){
	    	var cur=parseInt($('#quantity'+id).text(),10);
	    	if((cur+n)<=0) return;
	    	$.get('addCart?id='+id+'&n='+n,function(res){
	    		console.log(res);
	    		if(res=='NotLogin'){
	    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
	    			location.href='login.jsp';
	    			return;
	    		}
	    		if(res=='Success'){
	    			$('#quantity'+id).text(cur+n);
	    			$('#total'+id).text(parseFloat((cur+n)*unitPrice).toFixed(2));
	    			calculateTotalPrice();
	    		}
	    	});
	    	
	    }
	    function triggerTotalSelect(t){
	    	var items=document.querySelectorAll("input[name='orderSelect']");
	    	for(var i=0;i<items.length;i++)
	    		items[i].checked=t.checked;
	    	calculateTotalPrice();
	    }
	    function triggerOrderSelect(t){
	    	var items=document.querySelectorAll("input[name='orderSelect']");
	    	var selected=0;
	    	for(var i=0;i<items.length;i++)
	    		if(items[i].checked) selected+=1;
	    	$('#totalSelect').prop('checked',items.length==selected);
	    	calculateTotalPrice();
	    }
	    
	    function calculateTotalPrice(){
	    	var items=document.querySelectorAll("input[name='orderSelect']");
	    	var total=0.0;
	    	for(var i=0;i<items.length;i++)
	    		if(items[i].checked) total+=parseFloat($('#total'+items[i].value).text());
	    	$('#totalPrice').text(total.toFixed(2));
	    }
	    function pay(){
	    	var items=document.querySelectorAll("input[name='orderSelect']");
	    	var ids=[];
	    	for(var i=0;i<items.length;i++)
	    		if(items[i].checked) ids.push(items[i].value);
	    	if(ids.length==0) return;
	    	location.href='ShoppingPay.jsp?ids='+ids.join(',');
	    }
	    $(function(){
	    	calculateTotalPrice();
	    });
    </script>
</head>
<style>
    .cart{
        background-color: #66cdd4;
    }
    .cart a{
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
            <img src="素材/订单 (1).png" width="40px" height="40px">
            <a href="Order.jsp">Order</a>
        </div>
        <div class="cart">
            <img src="素材/购物车 (1).png" width="40px" height="40px">
            <a href="ShoppingCart.jsp">Shopping cart</a>
        </div>
        <div class="community">
            <img src="素材/社区/社区 (1).png" width="40px" height="40px">
            <a href="community-personal.jsp" target="_blank">Community</a>
        </div>
        <div class="set">
            <img src="素材/设置.png" width="40px" height="40px">
            <a href="#" target="_blank">Set</a>
        </div>
        <div class="more">
            <img src="素材/更多 (1).png" width="40px" height="40px">
            <a href="#">More</a>
        </div>
    </div>

    <div class="header">
        <p>Shopping Cart</p>
        <span>
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="20px" height="20px">
        </span>
        <span class="图标">
            <img src="素材/收藏.png" alt="collection" width="30px" height="30px">
            <img src="素材/更多3.png" alt="more" width="30px" height="30px">
        </span>
    </div>

    <div class="main">
    <%
    List<Map<String,Object>> list=UserDAO.list("SELECT t2.name,t2.price AS unitPrice, t2.img1, t1.* FROM `cart` t1, chesnake.goods t2 WHERE t1.good_id = t2.id AND t1.email=? ORDER BY t1.`time` DESC",account);
    %>
        <div class="firstline">
            <p>Product(<%=list.size() %>)</p>
            <p>Selected(Excluding freight)</p>
            <p style="color: #f3a401;"><b>$</b><b id="totalPrice"></b></p>
            <button style="width: 120px; height: 40px; background-color: #f2866a; color: white;" onclick="pay()">BUY</button>
        </div>
        <hr style="width: 100%; color: gray;">
        <span class="全选">
            <input type="checkbox" id="totalSelect" onclick="triggerTotalSelect(this)"><p>Select All  Information  Unit Price  Quantity  Price  Operation</p>
        </span><br>
        <%
	    for(int i=0;i<list.size();i++){
	    	Map<String,Object> item=list.get(i);
//    	String goodsID=item.get("id").toString();
	    %>
        <div class="gray" id="order<%=item.get("id") %>">
            <input type="checkbox" name="orderSelect" value="<%=item.get("good_id") %>" onclick="triggerOrderSelect(this)">
            <img src="images/<%=item.get("img1") %>" width="40px" height="40px">
            <p class="名称"><%=item.get("name").toString().substring(0, Math.min(25, item.get("name").toString().length()))+"..." %></p>
            <p class="单价">$<%=item.get("unitPrice") %></p>
            <span>
                <div class="减" style="cursor:pointer;" onselectstart="return false" onclick="updateCount(-1,'<%=item.get("good_id")%>',<%=item.get("unitPrice") %>)">-</div>
                <div class="数" id="quantity<%=item.get("good_id")%>"><%=item.get("amount") %></div>
                <div class="加"  style="cursor:pointer;" onselectstart="return false"  onclick="updateCount(1,'<%=item.get("good_id")%>',<%=item.get("unitPrice") %>)" >+</div>
            </span>
            <p id="total<%=item.get("good_id")%>"><%=item.get("price") %></p>
            <button onclick="del('<%=item.get("id") %>')">Delete</button>
        </div>
        <%} %>
    </div>
</body>
</html>