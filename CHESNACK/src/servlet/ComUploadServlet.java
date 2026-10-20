package servlet;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;

import util.StringUtils;
import util.UserDAO;
import util.ChesnakeDAO;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;

@WebServlet("/comUpload")
@MultipartConfig
public class ComUploadServlet extends HttpServlet {
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
		
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		String path = getServletContext().getRealPath("/")+"\\images\\upload\\";
		File dir=new File(path);
		if(!dir.exists())
			dir.mkdir();
    	
    	String title = request.getParameter("title");
        String content = request.getParameter("content");
        
        String img1=null;
        long time=System.currentTimeMillis();
        Part part=request.getPart("file");
        if(part!=null && part.getSize()>0) {
            String extName=part.getSubmittedFileName();
            extName=extName.substring(extName.lastIndexOf(".")+1);
            img1=time+"."+extName;
            part.write(path+img1);
            img1="upload/"+img1;
        }

        
        Map<String,Object> item=UserDAO.find("SELECT MAX(id) AS max_value FROM community;");
        
		String account = (String)request.getSession().getAttribute("login.account");
		String loginName = (String)request.getSession().getAttribute("login.name");
		
		
		if(StringUtils.isEmpty(account)) {
			response.getWriter().print("<script>alert('Please log in first!');window.location.href='login.jsp';</script>");
			return;
		}
        
		UserDAO.exec("insert into `community`( id, title, content, send_time, username, account,img1) values(?,?,?,?,?,?,?)", item.get("max_value")==null?1:(Integer.valueOf(item.get("max_value").toString()) +1),title,content,new Timestamp(System.currentTimeMillis()),loginName,account,img1);
		
		response.getWriter().print("<script>alert('Success to upload post');history.back();</script>");
		
		
    }
	

	
}

