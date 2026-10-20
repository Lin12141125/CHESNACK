package servlet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.ChesnakeDAO;
import util.HttpUtils;
import util.StringUtils;
import util.UserDAO;

@WebServlet("/deleteCart")
public class DeleteCartServlet extends HttpServlet{
	private static final long serialVersionUID = -8302811174707957625L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		
		String uname=(String)req.getSession().getAttribute("login.name");
	    if(StringUtils.isEmpty(uname)){
		   HttpUtils.redirectWithAlert(req, res, "login.jsp", "Login session invalid or not logged in!");
		   return;
	    }
	    String email=(String)req.getSession().getAttribute("login.account");
	    String id=req.getParameter("id");
	    int c=UserDAO.exec("delete from `cart` where id=? and email=? ", id, email);
	    if(c>0)
	    	res.getWriter().print("Success");
	    else
	    	res.getWriter().print("Failure");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req,resp);
	}
}
