<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="java.text.DateFormat"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page import="util.StringUtils"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    <%
    DateFormat format=new SimpleDateFormat("yyyy/MM/dd");
      List<Map<String,Object>> list=(List<Map<String,Object>>)request.getAttribute("list");
      if(list!=null && list.size()>0){
    	  for(Map<String,Object> comment:list){
    %>
	        <div class="comment" style="margin-top:10px;">
	            <div class="circle1"></div>
	            <p id="grey"><%=comment.get("username") %>&nbsp&nbsp<%=StringUtils.fmtDate(comment.get("send_time"))%> <a href='javascript:void(0);' onclick="showCommentDialog('<%=comment.get("id")%>')">reply</a></p>
	            <p id="black"><%=comment.get("content") %></p>
	        </div>
	      
	   <%
	      List<Map<String,Object>> replyList=(List<Map<String,Object>>)comment.get("reply");
	      if(replyList!=null){
	    	  for(int j=0;j<replyList.size();j++){
	    		  Map<String,Object> reply=replyList.get(j);
	   %>	   
   	    <div class="reply">
            <div class="circle2"></div>
            <p id="black"><%=reply.get("content") %></p>
            <p id="grey">Reply</p>
        </div>
	 
	   <%}}}}%>