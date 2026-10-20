package servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.ChesnakeDAO;
import util.StringUtils;
import util.UserDAO;

@WebServlet("/pay")
public class PayServlet extends HttpServlet {
	private static final long serialVersionUID = -8302811174707957625L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		String ids=req.getParameter("ids");
		
		String account=(String)req.getSession().getAttribute("login.account");
		if(StringUtils.isEmpty(account)) {
			res.getWriter().print("NotLogin");
			return;
		}
		if(StringUtils.isEmpty(ids)) {
			res.getWriter().print("Failure");
			return;
		}
		// Only keep numeric IDs separated by commas to avoid SQL injection
		ids=ids.replaceAll("[^0-9,]", "");
		if(StringUtils.isEmpty(ids)) {
			res.getWriter().print("Failure");
			return;
		}
		ChesnakeDAO.exec("update goods set score=score+3 where id in ("+ids+")");
		UserDAO.exec("insert into `order`(email,good_id,amount,price,`time`) select email,good_id,amount,price,`time` from cart where email=? and good_id in ("+ids+")",account);
		UserDAO.exec("delete from cart where email=? and good_id in ("+ids+")", account);
		
		res.getWriter().print("Success");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req,resp);
	}
}
