<%@page import="java.util.Formatter"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Arrays"%>
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
    <title>PAY</title>
    <link rel="stylesheet" href="style6.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <script src="<%=request.getContextPath() %>/js/toastr.min.js"></script>
    <%@ include file="alert.jsp" %>
    <script>
	    function showPayDialog(){
	    	$("div.overlay").show();
	    	$("div.pay-dialog").fadeIn();
	    }
	    function confirmPay(){
	    	var ids="${param.ids}";
	    	$.get("pay?ids="+ids,function(res){
	    		if(res=='Success'){
	    			alert("Payment successful!");
	    			location.href='ShoppingCart.jsp';
	    		}else if(res=='NotLogin'){
	    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
	    			location.href='login.jsp';
	    		}
	    	});
	    }
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
    .pay-dialog{
       width:300px;
       background-color:#FFB03B;  
       padding:30px;
       border-radius:20px;
       color:#fff;
       display:flex;
       flex-direction: column;
       justify-content: center;
       align-items: center;
       box-shadow: 1px 2px 4px hsl(220deg 60% 50%);
       position: absolute;
       top: 50%;  
  		left: 50%;  
  		transform: translate(-50%, -50%);
  		display:none;
  		z-index: 999999;
    }
    .pay-dialog-title{
       font-size:18px;
       font-weight:bold;
       margin-bottom:20px;  
       text-align:center;
    }
    .pay-dialog-price{
       font-size:26px;
       font-weight:bold;
        margin-bottom:20px; 
        text-align:center;
    }
    .pay-dialog-btn{
       display:flex;
       justify-content: space-between;
    }
    .pay-dialog-btn-yes{
           padding:10px 20px;
       border-width:0px;
       border-radius: 12px;
       margin-right:15px;
       background-color:#F2866A;
       color:#fff;
       font-size:16px;
        font-weight:bold;
        cursor:pointer;
    }
    .pay-dialog-btn-no{
           padding:10px 20px;
       border-width:0px;
       border-radius: 12px; 
       color:#FAB090;
       background-color:#fff;
       font-size:16px;
        font-weight:bold;
         cursor:pointer;
    }
    .overlay {  
  position: fixed; /* 固定定位，覆盖整个页面 */  
  top: 0;  
  left: 0;  
  width: 100%;  
  height: 100%;  
  background-color: rgba(0, 0, 0, 0.5); /* 设置背景颜色为半透明黑色 */  
  z-index: 999; /* 设置 z-index 使其在页面上覆盖在其他元素之上 */ 
  display:none; 
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
       <div class="homepage">
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
        <p>Paying</p>
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
    String idsParam=request.getParameter("ids");
    if(idsParam==null || idsParam.equals("")){
    	out.println("No goods selected!");
    	return;
    }
    String[] ids=idsParam.split(",");
    Map<String,String> idmap=new HashMap<String,String>();
    for(int i=0;i<ids.length;i++) idmap.put(ids[i],"1");
    List<Map<String,Object>> list2=new ArrayList<Map<String,Object>>();
    double total=0.0;
    for(Map<String,Object> item : list){
    	String id=item.get("good_id").toString();
    	if(!idmap.containsKey(id)) continue;
    	total+=Double.valueOf(item.get("price").toString());
    	list2.add(item);
    }
    list=list2;
    %>
        <div class="第一排">
            <p>Product(<%=list.size() %>)</p>
            <span>
                <p>Selected(Excluding freight)</p>
                <p style="color: #f3a401;"><b>$</b><b id="totalPrice"><%=new Formatter().format("%.2f", total).toString() %></b></p>
                <button style="width: 120px; height: 40px; background-color: #f2866a; color: white;" onclick="showPayDialog()">Yes</button>
            </span>            
            
        </div>
        <hr style="width: 100%; color: gray;">
        <p>Information    Unit Price    Quantity    Price    Operation</p>
        <%
	    for(int i=0;i<list.size();i++){
	    	Map<String,Object> item=list.get(i);
//    	String goodsID=item.get("id").toString();
	    %>
        <div class="gray" id="order<%=item.get("id") %>">
            <img src="images/<%=item.get("img1") %>" width="40px" height="40px">
            <p class="名称"><%=item.get("name").toString().substring(0, Math.min(60, item.get("name").toString().length()))+"..." %></p>
            <p class="单价"><%=item.get("unitPrice") %></p>
            <span>
                <div class="减"  onselectstart="return false">-</div>
                <div class="数" id="quantity<%=item.get("good_id")%>"><%=item.get("amount") %></div>
                <div class="加"  onselectstart="return false" >+</div>
            </span>
            <p id="total<%=item.get("good_id")%>"><%=item.get("price") %></p>
        </div>
        <%} %>
    </div>
    <div class="overlay" onclick="$('div.pay-dialog').fadeOut();$('div.overlay').hide();"></div>
    <div class="pay-dialog">
        <div class="pay-dialog-title">The amount you need to pay is</div>
        <div class="pay-dialog-price">$<%=new Formatter().format("%.2f", total).toString() %></div>
        <div class="pay-dialog-btn">
            <button class="pay-dialog-btn-yes" onclick="confirmPay();">Yes</button>
            <button class="pay-dialog-btn-no" onclick="$('div.pay-dialog').fadeOut();$('div.overlay').hide();">No</button>
        </div>
    </div>
</body>
</html>