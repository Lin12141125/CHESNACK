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
    <script type="text/javascript" src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
	<script type="text/javascript" src="https://cdn.bootcdn.net/ajax/libs/layer/3.1.1/layer.min.js"></script>
    
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
    hr{
        position: relative;
        top: 5px;
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

    <div class="main">
        <a href="community-personal.jsp">
            <img src="素材/返回.png" alt="back" width="40px" height="40px" id="back">
            <p id="Back">Back</p>
        </a>
        
        
        <form action="comUpload" method="post"  enctype="multipart/form-data">
            <input type="submit" value="upload" name="upload">
            <div id="uploadBox" class="uploadDIv" onclick="document.getElementById('inputs').click();">
                <span>
                    +
                    <p>Uploading pictures</p>
                </span>
 
            </div>
                           <input type="file" name="file" multiple id="inputs" accept="image/*" class='fileTest' style="display:none;" />
            <div class="yourpost">
                <input type="text" name="title" placeholder="Type your title here...">
                <hr>
                <textarea name="content" id="content" cols="60" rows="15" placeholder="Type your content here..."></textarea>
            </div>
        </form>
    </div>
</body>
<script type="text/javascript">

$(function() {
	var img = []; //创建一个空对象用来保存传入的图片
	var AllowImgFileSize = '101376'; //1兆
	$("#inputs").change(function() {
	var fil = this.files;
	for(var i = 0; i < fil.length; i++) {
	var curr = $('#inputs')[i].files[0].size;
	if(curr > AllowImgFileSize * 101376) { //当图片大于1兆提示
	layer.msg("图片文件大小超过限制 请上传小于99M的文件");
	} else {
	reads(fil[i]);
	img.push($('#inputs')[i].files[0]); //将传入的图片push到空对象中
	}
	}
	if(img.length >= 6) { //判断图片的数量，数量不能超过3张
	$('.uploadDIv').hide();
	} else {
	$('.uploadDIv').show();
	}
	console.log(img);
	});
	
	function reads(fil) {
	var reader = new FileReader();
	reader.readAsDataURL(fil);	 
	reader.onload = function() {
	document.getElementById("uploadBox").innerHTML = "<div class='divImg' id='uploadImg'><img src='" + reader.result + "' class='imgBiMG' style='max-height:250px;'></div>";
	}
	}
	})
 </script>
</html>