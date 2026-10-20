package servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.HttpUtils;
import util.UserDAO;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = -8302811174707957625L;
	
	@Override
	protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		String email=req.getParameter("account");
		String pwd=req.getParameter("psw");
		String identity=req.getParameter("identity");
		int role=-1;
		if("User".equals(identity)) {
			role=2;
		}else if("Administrator".equals(identity))
			role=1;
		
		Map<String,String> params=new HashMap<String,String>();
		params.put("account",email);
		
		Map<String,Object> ret=UserDAO.find("SELECT * FROM login WHERE email=? and role=? ", email, role);
		if(ret==null) {
			HttpUtils.redirectWithAlert(req, res, "login.jsp", "The account doesn't exist!", params);
			return;
		}
		
		String rightPwd=(String)ret.get("password");
		String username=(String)ret.get("username");
		if(!rightPwd.equals(pwd)) {
			params.put("psw",pwd);
			HttpUtils.redirectWithAlert(req, res, "login.jsp", "Wrong Password!", params);
			return;
		}
		
		req.getSession().setAttribute("login.name", username);
		req.getSession().setAttribute("login.account", email);
		req.getSession().setAttribute("login.role", role);
		HttpUtils.redirect(req, res, role==2?"HomePage.jsp":"administrator.jsp");
	}
}
