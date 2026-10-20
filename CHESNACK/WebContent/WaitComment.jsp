<%@page import="java.util.Locale"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
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
    <title>MY CHESNACK</title>
    <link rel="stylesheet" href="style9.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <%@ include file="alert.jsp"%>
</head>
<style>
    .homepage{
        background-color: #66cdd4;
    }
    .homepage a{
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
            <img src="素材/个人.png" width="40px" height="40px">
            <a href="UserPage.jsp" target="_blank">Homepage</a>
        </div>
        <div class="order">
            <img src="素材/订单 (1).png" width="40px" height="40px">
            <a href="Order.jsp" target="_blank">Order</a>
        </div>
        <div class="cart">
            <img src="素材/购物车.png" width="40px" height="40px">
            <a href="ShoppingCart.jsp" target="_blank">Shopping cart</a>
        </div>
        <div class="delivery">
            <img src="素材/物流.png" width="40px" height="40px">
            <a href="Order.jsp" target="_blank">Delivery</a>
        </div>
        <div class="set">
            <img src="素材/设置.png" width="40px" height="40px">
            <a href="#" target="_blank">Set</a>
        </div>
        <div class="more">
            <img src="素材/更多 (1).png" width="40px" height="40px">
            <a href="#" target="_blank">More</a>
        </div>
    </div>

    <div class="header">
        <a href="WaitCommentList.jsp"><button>Back</button></a>
        <span>
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="20px" height="20px">
        </span>
        <span>
            <img src="素材/收藏.png" alt="collection" width="20px" height="20px">
            <img src="素材/更多3.png" alt="more" width="20px" height="20px">
        </span>
    </div>
    
    <%
    String orderID=request.getParameter("id");
//    System.out.println(orderID);
    if(orderID==null||orderID.equals("")){
   	 out.println("id cannot be empty");
   	 return;
    }
    Map<String,Object> item=UserDAO.find("select t1.*, t2.img1 img, t2.name name from `order` t1, chesnake.goods t2 where t1.good_id=t2.id and t1.id=?", orderID);
    if(item==null){
   	 out.println("not found by id "+orderID);
   	 return;
    }
    %>
    
    <div class="main">
        <div class="top">
            <p>Evaluate</p>
            <button  style='cursor:pointer;' onclick="submitComment()">Submit</button>
        </div>
        <div class="介绍">      
            <img src="images/<%=item.get("img") %>"  width="220px" height="200px">    
            <p><%=item.get("name").toString().substring(0, Math.min(50, item.get("name").toString().length()))+"..." %><br><%=item.get("price") %></p>
            <p id="gray"><%=StringUtils.fmtDate(item.get("time")) %></p>
        </div>
        <div class="评价">
            <button style="height: 60px;width: 150px;" onclick="setEval(this,5)">Great</button>
            <button style="height: 60px;width: 150px;" onclick="setEval(this,3)">Normal</button>
            <button style="height: 60px;width: 150px;" onclick="setEval(this,1)">Bad</button>
            <form>
                <p>Evaluation (between 1 and 5)</p>
                <input type="number" name="evaluation" min="1" max="5" onchange="onEvalChange(this)">
            </form>
        </div>
        <div class="gray">
            <textarea name="comment" style="width: 800px;height: 450px;"></textarea>
        </div>
    </div>
    <script type="text/javascript">
      function setEval(btn,level){
    	  $("input[name='evaluation']").val(level);
    	  $("div[class='评价']").find("button").removeClass('active');
    	  $(btn).addClass('active');
      }
      function onEvalChange(dom){
    	  var val=$(dom).val();if(val=='') return;
    	  var level=parseInt(val,10)
    	  var name=''
    	  if(level==5)
    		  name='Great';
    	  else if(level==4||level==3)
    		  name='Normal';
    	  else
    		  name='Bad';
    	  $("div[class='评价']").find("button").removeClass('active');
    	  $("div[class='评价']").find("button:contains('"+name+"')").addClass('active');
      }
      function submitComment(){
    	  var eval=$("input[name='evaluation']").val();
    	  if(eval==''){
    		  alert("evaluation required");
    		  return;
    	  }
    	  var comment=$("textarea[name='comment']").val();
    	  if(comment==''){
    		  alert('comment required');
    		  return;
    	  }
    	  $.post('comment?action=add',{eval:eval,comment:comment,orderId:"<%=item.get("id")%>"},function(res){
    		if(res=='NotLogin'){
      			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
      			location.href='login.jsp';
      		}else if(res=='Success'){
      			alert('Successfully make comment');
      			location.href='WaitCommentList.jsp'
      		}else if(res=='Exist'){
      			alert('You already have made comment');
      		}
    	  });
    	  
      }
    </script>
</body>
</html>