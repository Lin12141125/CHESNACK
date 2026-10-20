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

@WebServlet("/register")
public class RegisterServlet extends HttpServlet{
	private static final long serialVersionUID = -8302811174707957625L;
	
	@Override
	protected void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		String email=req.getParameter("account");
		String pwd=req.getParameter("psw");
		String username=req.getParameter("username");
		String identity=req.getParameter("identity");
		int role=-1;
		if("User".equals(identity)) {
			role=2;
		}else if("Administrator".equals(identity))
			role=1;
		int count=UserDAO.count("SELECT COUNT(*) FROM login WHERE email=?", email);
		if(count>0) {
			Map<String,String> params=new HashMap<String,String>();
			params.put("account",email);
			params.put("username",username);
			params.put("psw",pwd);
			params.put("cpsw",pwd);
			HttpUtils.redirectWithAlert(req, res, "register.jsp", "This email has been registed! Please try another one!", params);
			return;
		}
		UserDAO.exec("INSERT INTO login(email,username,password,role) VALUES(?,?,?,?)",email,username,pwd,role);
		HttpUtils.redirect(req, res, "login.jsp");
	}
}
