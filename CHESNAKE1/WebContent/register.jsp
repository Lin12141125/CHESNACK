<%@ page import="util.HttpUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register</title>
    <link rel="stylesheet" href="style1.css">
    <script src="<%=request.getContextPath()%>/js/jquery-3.5.1.min.js"></script>
    <%@ include file="alert.jsp" %>
    <script>
    	function checkform(){
    		let email=$("input[name='account']").val().trim();
    		let pwd1=$("input[name='psw']").val().trim();
    		let pwd2=$("input[name='cpsw']").val().trim();
    		if(email==''){
    			alert("The Email can't be empty!");
    			return false;
    		}
    		if(email.indexOf("@")==-1 || email.indexOf(".com")==-1){
        		alert("You need to enter your email! (end with xx@xx.com)!");
        		return false;
        	}
        	if(pwd1==''){
        		alert("The password can't be empty!");
        		return false;
        	}
        	if(pwd2==''){
        		alert("Please confirm the password!");
        		return false;
        	}
        	if(pwd2!=pwd1){
        		alert("Passwords are not same, please reenter!");
        		return false;
        	}
        	return true;
    	}
    </script>
</head>
<body>
    <div class="background-image"></div>
    <div class="register">
        <p class="title">Register</p>
        <form action="register" method="post" onsubmit="return checkform()">
            <span class="account">Account: 
            <input type="text" name="account" placeholder="E-mail" value="<%=HttpUtils.getSessionParam(request,"account")%>"></span><br>
            <span class="uname">User name: 
            <input type="text" name="username" value="<%=HttpUtils.getSessionParam(request,"username")%>"></span><br>
            <span class="password">Password: 
            <input type="password" name="psw" value="<%=HttpUtils.getSessionParam(request,"psw")%>"></span><br>
            <span class="confirm">Confirm: 
            <input type="password" name="cpsw" value="<%=HttpUtils.getSessionParam(request,"cpsw")%>"></span><br>
            <span class="identity">Choose your identity: <br>
               <input type="radio" name="identity" value="User" checked class="radio"> User
                <input type="radio" name="identity" value="Administrator" class="radio"> Administrator <br>
            </span><br>
            <input type="submit" name="submit" value="CONFIRM" class="submit">
        </form>
        <br/><Br/><br/><Br/> 
         Already have an account? Back to <a href=login.jsp  style="display:inline;position:static;">login</a>
    </div>
</body>
</html>