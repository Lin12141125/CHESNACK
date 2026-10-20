<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ include file="session.jsp" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>WAREHOUSE</title>
    <link rel="stylesheet" href="style10.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
	<%@ include file="alert.jsp"%>
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
<script>
   function showImg(t,c){
	   let file=t.files[0];
	   if(!/image\/\w+/.test(file.type)) {
			alert("请确保文件为图像类型");
			return false;
		}
		var reader = new FileReader();
		reader.readAsDataURL(file);
		reader.onload = function() {
			document.getElementById("uploadDiv"+c).innerHTML="<img src='" + this.result + "' style='width:100%;'/>";
		}
   }
</script>
<body>
    <div class="subnav">
        <div class="myname">
            <img src="素材/白底logo.png" alt="头像" width="80px" height="75px">
            <p>Administrator</p>
        </div>
        <div class="dashboard">
            <img src="素材/dashboard(1).png" width="40px" height="40px">
            <a href="administrator.jsp" target="_blank">Dashboard</a>
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
            <a href="administrator-warehouse.jsp" target="_blank">Warehouse</a>
        </div>
        <div class="report">
            <img src="素材/设置.png" width="40px" height="40px">
            <a href="administrator-report.jsp" target="_blank">Report</a>
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
        <a href="administrator-warehouse.jsp"><img src="素材/返回.png" alt="return" width="40px" height="40px" id="return"></a>
        <form action="addGoods" method="post" style="margin-bottom:50px;" enctype="multipart/form-data">
            <p>Trade name: </p>&nbsp&nbsp&nbsp<input type="text" name="tradeName"><br>
            <p>Brand: </p>&nbsp&nbsp&nbsp<input type="text" name="information"><br>
            <p>Unit Price: </p>&nbsp&nbsp&nbsp<input type="text" name="unitPrice"><br>
            <p>Origin: </p>&nbsp&nbsp&nbsp<input type="text" name="producer"><br>
            <p>Net Content: </p>&nbsp&nbsp&nbsp<input type="text" name="inventoryQuantity"> <br>
            <p>Type: </p>&nbsp&nbsp&nbsp
            <select name="type">  
			  <option value="dried meat">Dried Meat</option>  
			  <option value="chili">Chili</option>  
			  <option value="confectionery">Confectionery</option>  
			  <option value="bread">Bread</option>  
			  <option value="nut">Nut</option>  
			  <option value="puffed food">Puffed Food</option>  
			  <option value="candy">Candy</option>  
			  <option value="chocolate">Chocolate</option>  
			  <option value="biscuits">Biscuits</option>  
			</select><br/>
			<div class="uploadDiv-container">
			<div class="uploadDIv" id="uploadDiv1" onclick="document.getElementById('inputs1').click();">
                    <span>+</span>
             </div>
             <div class="uploadDIv" id="uploadDiv2" onclick="document.getElementById('inputs2').click();" style="margin-left:20px;">
                    <span>+</span>
             </div>
             </div>
            <input type="file" name="file1" multiple="" id="inputs1" accept="image/*" class="fileTest" onchange="showImg(this,1)" style="display:none;">
            <input type="file" name="file2" multiple="" id="inputs2" accept="image/*" class="fileTest" onchange="showImg(this,2)" style="display:none;">
            <input type="submit" value="Save" name="save">
        </form>
    </div>
</body>
</html>