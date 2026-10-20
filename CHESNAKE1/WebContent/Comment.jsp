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
   <div class="eval-msg-panel">
	      <div class="eval-msg-avator">
	      </div>
	      <div class="eval-msg-body">
	          <div class="eval-msg-title">
	             <div><%=comment.get("username") %></div>
	             <div><%=StringUtils.fmtDate(comment.get("send_time"))%></div>
	          </div>
	          <div class="eval-msg-text"><%=comment.get("content") %></div>
	      </div>
	      <div class="eval-msg-suffix">
	         <div class="eval-msg-stars">
	           <% 
	           int level=0;
	           if(comment.get("level")!=null)
	        	   level=Integer.parseInt(comment.get("level").toString());
	           for(int i=1;i<=5;i++){ %>
	           <img src="images/star-<%=level>=i?"in":"out"%>.png" style="width:24px;"/>
	           <%} %>
	         </div>
	         <div class="eval-msg-reply">
	            <a href="javascript:void(0)" onclick="reply(<%=comment.get("id")%>)">Comment</a> <br/><br/>
	            <a href="javascript:void(0)" onclick="report(<%=comment.get("id")%>)" style="background-color:#997CE8;border-radius: 10px;padding:5px 10px;color:#fff;margin-top:10px;">Report</a>
	         </div>
	      </div>
	   </div>
	   <%
	      List<Map<String,Object>> replyList=(List<Map<String,Object>>)comment.get("reply");
	      if(replyList!=null){
	    	  for(int j=0;j<replyList.size();j++){
	    		  Map<String,Object> reply=replyList.get(j);
	   %>
	   <div class="eval-msg-panel eval-msg-reply-panel">
	      <div class="eval-msg-avator">
	      </div>
	      <div class="eval-msg-body">
	          <div class="eval-msg-title">
	             <div><%=reply.get("username") %></div>
	             <div><%=StringUtils.fmtDate(reply.get("send_time"))%></div>
	          </div>
	          <div class="eval-msg-text"><%=reply.get("content") %></div>
	      </div>
	      <div class="eval-msg-suffix" style="justify-content: center;">
	         <div class="eval-msg-reply" style="color:#777;">
	            reply <br/><br/>
	           <a href="javascript:void(0)" onclick="report(<%=reply.get("id")%>)" style="background-color:#997CE8;border-radius: 10px;padding:5px 10px;color:#fff;margin-top:10px;">Report</a>
	         </div>
	      </div>
	   </div>
	   
	   <%}}}}%>