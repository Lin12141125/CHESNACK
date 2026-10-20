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

@WebServlet("/comDelete")
public class ComDeleteServlet extends HttpServlet {
	
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
		
		String path = getServletContext().getRealPath("/")+"\\images\\upload\\";
		File dir=new File(path);
		if(!dir.exists())
			dir.mkdir();
    	
    	String id = request.getParameter("id");
        Map<String,Object> post=UserDAO.find("select img1 from `community` where id=?", id);
        if(post!=null && post.get("img1")!=null) {
        	String fileName=(String)post.get("img1");
        	fileName=path+ fileName.substring(fileName.lastIndexOf("/")+1);
            File file=new File(fileName);
            if(file.exists()) file.delete();
        }
		UserDAO.exec("delete from  `community` where id=?", id);
		response.getWriter().print("<script>alert('Success to delete post');location.href='community-personal.jsp'</script>");
		
		
    }
	

	
}

