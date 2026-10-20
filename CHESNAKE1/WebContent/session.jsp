<%@page import="util.HttpUtils"%>
<%@page import="util.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
  String uname=(String)session.getAttribute("login.name");
  if(StringUtils.isEmpty(uname)){
	  HttpUtils.redirectWithAlert(request, response, "login.jsp", "Login session invalid or not logged in!");
	  return;
  }
  String account=(String)session.getAttribute("login.account");
  if(StringUtils.isEmpty(account)){
	  HttpUtils.redirectWithAlert(request, response, "login.jsp", "Login session invalid or not logged in!");
	  return;
  }
%>