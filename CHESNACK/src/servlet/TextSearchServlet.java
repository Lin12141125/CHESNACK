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

@WebServlet("/TextSearch")
public class TextSearchServlet extends HttpServlet {
	private static final long serialVersionUID = -8302811174707957625L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		String uname=(String)req.getSession().getAttribute("login.name");
	  if(StringUtils.isEmpty(uname)){
		  HttpUtils.redirectWithAlert(req, res, "login.jsp", "Login session invalid or not logged in!");
		  return;
	  }
		  
		req.setCharacterEncoding("UTF-8");
		String searchText=req.getParameter("search");
		if(StringUtils.isEmpty(searchText))
		{
			res.sendRedirect("HomePage.jsp");
			return;
		}
        List<Map<String, Object>> list=ChesnakeDAO.list("select * from goods where LOWER(name) LIKE concat('%',?,'%')", searchText);
		if(list.size()==0) {
			HttpUtils.redirectWithAlert(req, res, "HomePage.jsp", "No Goods Searched!");
			return;
		}
	    req.setAttribute("list", list);
	    res.setCharacterEncoding("UTF-8");
	    req.getRequestDispatcher("ShowHomePageSearchResult.jsp").include(req, res);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(req,resp);
	}
}
