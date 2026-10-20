package servlet;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.StringUtils;
import util.UserDAO;

@WebServlet("/addFollow")
public class AddFollowServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		
		String loginName=(String)req.getSession().getAttribute("login.name");
		if(StringUtils.isEmpty(loginName)) {
			resp.getWriter().print("NotLogin");
			return;
		}
		
		String username=req.getParameter("username");
		if(loginName.equals(username)) {
			resp.getWriter().print("self follow");
			return;
		}
		Map<String,Object> user=UserDAO.find("select * from login where username=?", username);
		if(user==null) {
			resp.getWriter().print("Failure");
			return;
		}
		
		String account=user.get("email").toString();
		if(UserDAO.count("select count(*) from follow where account=? and followee=?", account,loginName)>0) {
			resp.getWriter().print("Already follow");
			return;
		}
		UserDAO.exec("insert into follow (account,followee) values(?,?)", account,loginName);
		resp.getWriter().print("Success");
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}

	
}
