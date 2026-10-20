<%@page import="java.util.Locale"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="util.ChesnakeDAO"%>
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
<title>PRODUCT</title>
<link rel="stylesheet" href="style4.css">
<style>
body{
  background-color:#FBF8EF;
}
.show {
	top: auto;
}

.detail {
	height: auto;
	position: static;
	width: auto;
	margin: 100px 10px 0px 20px;
	padding: 10px 10px 10px 20px;
}

.detail-tab {
	display: flex;
	margin: 0px 0px 0px 20px;
}

.detail-tab-item {
	cursor: pointer;
	margin-right: 30px;
	padding-bottom: 10px;
}

.detail-tab-item:hover, .detail-tab-active-item {
	border-bottom: 3px solid #FDB03E;
}
.eval-count-panel{
  display:flex;
  justify-content: flex-start;
  align-items: center;
  margin-bottom:15px;
}
.eval-count-panel button{
  border:1px solid #eee;
  border-radius: 20px;
  padding:10px 20px;
  cursor: pointer;
  margin-right:15px;
  background-color:#fff;
    font-weight:bold;
}
.eval-count-panel button:hover, .eval-count-active{
  background-color:#F2866A!important;
  color:#fff;

}
.eval-msg-panel{
  border-radius: 15px;
  background-color:#fff;
  padding:10px 20px;
  width:100%;
  display:flex;
  align-items: stretch;
  margin-top:10px;
}
.eval-msg-reply-panel{
  width:auto;
  margin-left:30px;
  margin-right:-40px;
}
.eval-msg-avator{
   width:50px;
   height:50px;
   background-color:#F2866A;
   border-radius: 50%;
   margin-right:15px;
}
.eval-msg-body{
   flex:1;
   display:flex;
   flex-direction: column;
}
.eval-msg-title{
   display:flex;
   justify-content: flex-start;
   align-items: center;
   color:#888;
}
.eval-msg-title div{
   margin-right:15px;
}
.eval-msg-text{
   margin-top:20px;
   font-size:18px;
   font-weight:bold;
}
.eval-msg-suffix{
   width:150px;
   display:flex;
   flex-direction: column;
   justify-content: space-between;
}
.eval-msg-stars{
   
}
.eval-msg-reply{
  text-align:center;
}
.eval-msg-reply a{
  text-decoration: none;
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
<script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
<%@ include file="alert.jsp"%>
<script>
    function addCart(id){
    	var cur=parseInt($('#count').text(),10);
    	$.get("addCart?id="+id+"&n="+cur,function(res){
    		if(res=='NotLogin'){
    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
    			location.href='login.jsp';
    		}else if(res=='Success'){
    			alert('Successfully added to shopping cart');
    		}
    	});
    }
    function pay(id){
    	var cur=parseInt($('#count').text(),10);
    	$.get("addCart?id="+id+"&n="+cur+"&r="+cur,function(res){
    		if(res=='NotLogin'){
    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
    			location.href='login.jsp';
    		}else if(res=='Success'){
    			location.href='ShoppingPay.jsp?ids='+id;
    		}
    	});
    }
    function updateCount(n){
 	   var cur=parseInt($('#count').text(),10);
 	   if((cur+n)<=0) return;
 	   $('#count').text((cur+n)+"");
    }
    function checkform(){
        let searchText=$("input[name='searchText']").val().trim();
        if(searchText==''){
        	alert("You search nothing.");
        	return false;
        }
        return true;
    }
    $(function(){
    	$("div.detail-tab").find("div").each(function(){
    		var div=$(this);
    		div.click(function(){
    			$("div.detail-tab").find("div").each(function(){
    				$(this).removeClass("detail-tab-active-item");
    				$('#'+$(this).data("id")).hide();
    			});
    			$(this).addClass('detail-tab-active-item');
    			var id=$(this).data("id");
    			$("#"+id).show();
    		});
    	})
    })
    
    var curLevel=5;
    function loadComment(level,btn){
    	if(btn){
    		if($(btn).hasClass('eval-count-active')) return;
    		$('div.eval-count-panel >button').removeClass('eval-count-active');
    		$(btn).addClass('eval-count-active');
    	}
    	if(!level) 
    		level=curLevel;
    	else{
    		curLevel=level;
    	}
    	$.get("comment?action=list&id=${param.id}&level="+level,function(res){
    		$('#eval-output').html(res);
    	});
    }
    
    var curCommentId=undefined;
    function reply(commentId){
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
    	$.post("comment",{
    		action:'reply',
    		id:'${param.id}',
    		commentId:curCommentId,
    		content:txt
    	},function(res){
    		if(res=='NotLogin'){
    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
    			location.href='login.jsp';
    		}else if(res=="Success"){
    			curCommentId=undefined;
    			$('#commentInput').val('');
    			loadComment();
    			$("div.overlay").hide();
    	    	$("div.comment-dialog").fadeOut();
    		}
    	})
    }
    
    $(function(){
    	$.get("comment?action=count&id=${param.id}",function(res){
    		if(res=='') return;
    		var list=res.split('|');
    		var html=[];
    		for(var i=0;i<list.length;i++){
    			var segs=list[i].split(",");
    			html.push("<button onclick='loadComment("+(i+1)+",this)'>"+segs[0]+" "+segs[1]+"</button>");
    		}
    		$('div.eval-count-panel').html(html.join(''));
    		$('div.eval-count-panel >button:first').click();
    	});
    })
    
    function report(id){
    	$.get("comment?action=report&id="+id,function(res){
    		if(res=='NotLogin'){
    			alert('The login session has expired because you have not operated on the page for a long time! Please login again!');
    			location.href='login.jsp';
    		}else if(res=="Success"){
    			alert("success to report")
    		}
    	})
    }
 </script>
</head>
<body>
	<div class="head">
		<form action="HomePageSearch" method="post"
			onsubmit="return checkform()">
			<input type="text" name="searchText" id="search"> <input
				type="submit" name="search" value="SEARCH"> <input
				type="image" src="素材/搜索.png" width="30px" height="30px">
		</form>
	</div>
	<div class="banner">
	<% 
    String goodID=request.getParameter("id");
    if(goodID==null||goodID.equals("")){
   	 out.println("id cannot be empty");
   	 return;
    }
    Map<String,Object> item=ChesnakeDAO.find("SELECT * FROM goods WHERE id=?", goodID);
    if(item==null){
   	 out.println("not found by id "+goodID);
   	 return;
    }
    ChesnakeDAO.exec("update goods set score=score+1 where id=?", goodID);
    %>
		<div class="bigpicture">
			<img src="images/<%=item.get("img1") %>" width="400px" height="400px">
		</div>
		<div class="detail">
			<h1><%=item.get("name") %></h1>
			<p id="price"><%=item.get("sale_quantity") %> copies sold</p>
			<p id="销量">$<%=item.get("price") %></p>
			<p id="delivery">
				Delivery: From <b><%=item.get("origin") %></b>
			</p>
			<p id="express">
				Brand:
				<%=item.get("brand") %></p>
			<!-- <p id="variety">Variety: </p>
            <div id="500">500g</div>
            <div id="600g">600g</div>
            <div id="1000g">1000g</div>
            <div id="1100g">1100g</div>
            <div id="1200g">1200g</div>
            <div id="1500g">1500g</div> -->
			<p id="num">Number:</p>
			<div id="减" style="cursor: pointer" onclick="updateCount(-1)"
				onselectstart="return false">-</div>
			<div id="count">1</div>
			<div id="加" style="cursor: pointer" onclick="updateCount(1)"
				onselectstart="return false">+</div>
			<button onclick="addCart('<%=item.get("id") %>')">Add to
				Shopping cart</button>
			<button onclick="pay('<%=item.get("id") %>')">BUY</button>
			<img src="素材/收藏.png" width="30px" height="30px">
			<p id="收藏">Favorite</p>
		</div>
	</div>

	<div class="detail-tab">
		<div class="detail-tab-item detail-tab-active-item"
			data-id="introduction">Detailed Introduction</div>
		<div class="detail-tab-item" data-id="evaluation">Evaluation</div>
	</div>

	<div id="introduction" style="padding: 20px;">
		<div style="display: flex; margin-bottom: 10px;">
			<div id="left">
				Expiration Date: 2023/10/11-2024/12/11<br> Production Address:
				<%=item.get("origin") %><br> Specialty: French fries<br>
				Packing size: 1 bag<br> Storage: Store in a cool and dry place
			</div>
			<div id="right">
				Packing type: Bag<br> Orangic food: No<br> Net content per
				piece: 100g<br> Ingredients list: Please refer to instructions<br>
				Food additives: Please refer to instructions
			</div>
		</div>

		<div
			style="display: grid; grid-template-columns: repeat(auto-fill, 400px); row-gap: 20px; column-gap: 20px;">
			<img src="images/<%=item.get("img1") %>" style="max-width: 400px;">
			<img src="images/<%=item.get("img2") %>" style="max-width: 400px;">
		</div>
		<div id="description" style="color: #888;">
			Price description<br> Scribing price:<br> The counter
			price, tag price, geniune retail price, manufacturer's guide price or
			the previously displayed sales price of the product are not the
			original price and are for reference only.<br> Uncrossed price:<br>
			The real time price of a commodity does not change its nature due to
			differences in presentation. The specific transaction price changes
			according to the product participation in the activity, or the member
			uses coupons, points, etc., and the final price is subject to the
			order settlement page.<br> The price, promotion price, special
			price and other prices marked in the form of pictures or text on the
			merchant details page(including the main picture) may be the price
			under the circumstances of using coupons, full reduction or specific
			promotional activities and periods, etc. Please refer to the price,
			preferential conditions or event rules on the settlement page.<br>
			This description is only valid when there is a price comparison,
			please refer to the "Chesnack Price Publication Specification" for
			details. If the merchant explains the crossed price separately, the
			merchant's statement shall prevail.
		</div>
	</div>
	<div id="evaluation" style="padding:20px;display:block;margin-right:30px;">
	   <div class="eval-count-panel">
	   </div>
	   <div id="eval-output" style="width:100%;"></div>
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
	<div class="SHOW1">
    <%
    List<Map<String,Object>> list=ChesnakeDAO.list("select * from goods order by score desc");  
    for(int i=0;i<list.size();i++){
    	Map<String,Object> item1=list.get(i);
//    	String goodsID=item.get("id").toString();
    %>
        <a href="ShowProduct.jsp?id=<%= item1.get("id") %>">
        <div class="show">
            <img src="images/<%=item1.get("img1") %>" alt="chips" width="210px" height="210px">
            <p><b><%=item1.get("name").toString().substring(0, Math.min(20, item1.get("name").toString().length()))+"..." %></b></p>
            <p>$<%=item1.get("price") %></p>
            <p><%=item1.get("sale_quantity") %> copies sold</p>
            <p><b>...</b></p>
        </div>
        </a>
    <%} %>
    </div>
</body>
</html>