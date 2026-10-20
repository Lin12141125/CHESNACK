<%@page import="java.util.Locale"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="util.ChesnakeDAO"%>
<%@page import="util.UserDAO"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="util.HttpUtils"%>
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
    
    .comment-dialog{
       width:500px;
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
    .comment-dialog-input{
        margin-bottom:20px; 
        width:100%;
    }
    .comment-dialog-input textarea{
        height:150px;
        border:0px solid #fff;
        border-radius: 13px;
        width:460px;
        padding:20px;
    }
    .comment-dialog-btn{
       display:flex;
       justify-content: flex-end;
    }
    .comment-dialog-btn-yes{
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
    .comment-dialog-btn-no{
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
<script>

let curCommentId=-1;
function showCommentDialog(commentId){
	if(commentId!=undefined)
		curCommentId=commentId;
	$("div.overlay").show();
	$("div.comment-dialog").fadeIn();
}

function confirmReply(){
	var txt=$('#commentInput').val();
	if(txt==''){
		alert("please enter your comment");
		return;
	}
	$.post("community_comment",{
		action:'reply',
		id:'${param.id}',
		commentId:curCommentId,
		content:txt
	},function(res){
		if(res=='NotLogin'){
			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
			location.href='login.jsp';
		}else if(res=="Success"){
			$('#commentInput').val('');
			$("div.comment-dialog").fadeOut();
			$("div.overlay").hide();
			loadComment();
		}
	})
}

function loadComment(){
	$.get("community_comment?action=list&id=${param.id}",function(res){
		$('#comment-container').html(res);
	});
}

$(function(){
	loadComment();
})
</script>
<body>
    <div class="subnav">
        <div class="myname">
            <img src="素材/avatar.jpg" alt="头像" width="60px" height="60px">
            <p><%=uname %></p>
        </div>
        <div class="homepage">
            <img src="素材/个人 (1).png" width="40px" height="40px">
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
        <div class="community">
            <img src="素材/社区/社区.png" width="40px" height="40px">
   <a href="community-personal.jsp">Community</a>
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
        <p>Community</p>
        <span>
            <input type="text" name="search" placeholder="Searching anything">
            <img src="素材/搜索22.png" alt="search" width="30px" height="30px">
        </span>
        <div id="recommend"><a href="community-recommend.jsp">Recommend</a></div>
        <div id="follow">Follow</div><a href="community-personal.jsp"><div id="M"><%=uname.length()>=2?uname.substring(0,2):uname %></div></a>
    </div>

    <div class="main">
    <% 
    	String ComID=request.getParameter("id");
    	if(ComID==null||ComID.equals("")){
   	 		out.println("id cannot be empty");
   	 	return;
    	}
    	Map<String,Object> item=UserDAO.find("SELECT * FROM community WHERE id=?", ComID);
    if(item==null){
   	 	out.println("not found by id "+ComID);
   	return;
    }
    %>
        <a href="community-follow.jsp"><img src="素材/返回.png" alt="back" width="40px" height="40px" id="back"></a>
        <a href="community-follow.jsp"><p id="Back">Back</p></a>
        <div class="username">
            <img src="素材/purple-user.png" alt="purple-user" width="80px" height="70px">
            <p onclick="location.href='community-blogger.jsp?account=<%=item.get("account") %>'" style="cursor:pointer;"><%=item.get("username") %></p>
        </div>
        
        <div class="container">
            <!-- 先把第一张图片显示出来 -->
            <% if(item.get("img1")!=null){ %>
            <img class="on" src="images/<%=item.get("img1")%>">
            <% }else{ %>
            <img class="on" src="素材/limon chips.jpg">
            <% } %>
            <!-- 左右切换 -->
            <div class="left"><</div>
            <div class="right">></div>
        </div>

        <p id="title"><%=item.get("title") %></p>
        <p id="content"><%=item.get("content") %></p>
        <div class="tubiao">
            <img src="素材/社区/点赞 .png" width="30px" height="30px">
            <img src="素材/社区/评论.png"width="30px" height="30px" style="cursor:pointer" onclick="showCommentDialog()">
            <img src="素材/社区/收藏.png"width="30px" height="30px">
            <img src="素材/社区/转发 (1).png" width="30px" height="30px">
        </div>
        <hr>
			
        <p id="comment">Comment</p>
        <div id="comment-container" style="padding:10px;">
        </div>


    </div> 

		<div class="overlay" onclick="$('div.comment-dialog').fadeOut();$('div.overlay').hide();"></div>
    <div class="comment-dialog">
        <div class="comment-dialog-input">
          <textarea id="commentInput" placeholder="Enter your comment..."></textarea>
        </div>
        <div class="comment-dialog-btn">
            <button class="comment-dialog-btn-yes" onclick="confirmReply();">Submit</button>
            <button class="comment-dialog-btn-no" onclick="$('div.comment-dialog').fadeOut();$('div.overlay').hide();">Cancel</button>
        </div>
    </div>
</body>
</html> 