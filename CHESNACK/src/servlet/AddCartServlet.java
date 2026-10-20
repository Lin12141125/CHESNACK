package servlet;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import util.StringUtils;
import util.UserDAO;
import util.ChesnakeDAO;

@WebServlet("/addCart")
public class AddCartServlet extends HttpServlet {
	private static final long serialVersionUID = -8302811174707957625L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		res.setCharacterEncoding("UTF-8");
		String id=req.getParameter("id");
		String nStr=req.getParameter("n");
		int count=1;
		if(nStr!=null && nStr.matches("-?\\d+")) {
			count=Integer.parseInt(nStr);
		}
		String account=(String)req.getSession().getAttribute("login.account");
		if(StringUtils.isEmpty(account)) {
			res.getWriter().print("NotLogin");
			return;
		}
		Map<String,Object> item=ChesnakeDAO.find("SELECT * FROM goods WHERE id=?", id);
		if(item==null) {
			res.getWriter().print("Failure");
			return;
		}
		String strAmount=UserDAO.first("select amount from `cart` where email=? and good_id=? ", account,id);
		if(strAmount==null)
		    UserDAO.exec("insert into `cart`(email,good_id,amount,price,`time`) values(?,?,?,?,?)", account,id,count,Double.valueOf(item.get("price").toString())*count,new Date());
		else {
			int total=Integer.parseInt(strAmount)+count;
			String r=req.getParameter("r");
			if(r!=null && r.matches("-?\\d+"))
				total=Integer.parseInt(r);
			UserDAO.exec("update `cart` set amount=?,price=? where email=? and good_id=?", total,Double.valueOf(item.get("price").toString())*total,account,id);
		}
		res.getWriter().print("Success");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req,resp);
	}
}
