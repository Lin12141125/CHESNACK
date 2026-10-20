<%@page import="util.HttpUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="style1.css">
    <script src="<%=request.getContextPath() %>/js/jquery-3.5.1.min.js"></script>
    <%@ include file="alert.jsp" %>
    <script>
    	function checkform(){
    		let email=$("input[name='account']").val().trim();
    		let pwd1=$("input[name='psw']").val().trim();
    		if(email==''){
    			alert("The Email can't be empty!");
    			return false;
    		}
    		if(pwd1==''){
    			alert("The password can't be empty!");
    			return false;
    		}
    		return true;
    	}
    </script>
</head>
<body>
    <div class="background-image"></div>
    <div class="logo">
        <img src="素材/白底logo.png" width="130px" height="100px">
    </div>
    <div class="register">
        <p class="title">Login</p>
        <%
            String success=(String)request.getAttribute("email");
            if(success!=null){
            	out.println(success);
            }
            String email=(String)request.getAttribute("email");
            if(email==null){
            	email="";
            }
        %>
        <form action="login" method="post" onsubmit="return checkform()">
            <span class="account">Account: 
                <input type="text" name="account" placeholder="E-mail" value="<%=HttpUtils.getSessionParam(request,"account")%>"></span><br>
            <span class="password">Password: 
                <input type="password" name="psw" value="<%=HttpUtils.getSessionParam(request,"psw")%>"></span><br>
            <span class="identity">Choose your identity: <br>
               <input type="radio" name="identity" value="User" checked class="radio"> User
                <input type="radio" name="identity" value="Administrator" class="radio"> Administrator <br>
            </span><br>
            <input type="submit" name="submit" value="SIGN-IN" class="submit">
        </form>
        <p class="forget">Forget Password?</p>
        <a href="register.jsp" target="_blank">Create a new account</a>
    </div>
</body>
</html>
