package servlet;

import java.io.IOException;
import java.util.ArrayList;
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

@WebServlet("/HomePageSearch")
public class HomePageSearchServlet extends HttpServlet {
	private static final long serialVersionUID = -8302811174707957625L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		String uname=(String)req.getSession().getAttribute("login.name");
	  if(StringUtils.isEmpty(uname)){
		  HttpUtils.redirectWithAlert(req, res, "login.jsp", "Login session invalid or not logged in!");
		  return;
	  }
		  
		req.setCharacterEncoding("UTF-8");
		String searchContent=req.getParameter("searchText");
//		System.out.println(searchContent);
		if(StringUtils.isEmpty(searchContent))
		{
			String referer=req.getHeader("referer");
			if(referer==null || referer.contains("HomePageSearch") || referer.contains("ShowHomePageSearchResult"))
			   res.sendRedirect("HomePage.jsp");
			else
				res.sendRedirect(referer);
			return;
		}
//        List<Map<String, Object>> list=ChesnakeDAO.list("select * from goods");
		List<Object> params=new ArrayList<>();
		StringBuffer buf=new StringBuffer();
		buf.append("select * from chesnake.goods WHERE LOWER(name) LIKE concat('%',?,'%')");
		params.add(searchContent);
		String brand=req.getParameter("brand");
		if(brand!=null &&!brand.equals("")) {
			buf.append(" and brand=? ");
			params.add(brand);
		}
		String type=req.getParameter("type");
		if(type!=null &&!type.equals("")) {
			buf.append(" and type=? ");
			params.add(type);
		}
		String origin=req.getParameter("origin");
		if(origin!=null && !origin.equals("")) {
			buf.append(" and origin=? ");
			params.add(origin);
		}
		String field=req.getParameter("field");
		String direction=req.getParameter("direction");
		if(field!=null && !field.equals("")) {
			// Whitelist: only allow sorting by known columns to avoid SQL injection
			if(!("score".equals(field)||"price".equals(field)||"sale_quantity".equals(field))) {
				field="score";
			}
			if(!"desc".equals(direction)) {
				direction="asc";
			}
			buf.append(" order by ").append(field).append(" ").append(direction);
		}
        List<Map<String, Object>> list=ChesnakeDAO.list(buf.toString(),params.toArray());
		if(list.size()==0) {
			String referer=req.getHeader("referer");
			if(referer==null || referer.contains("HomePageSearch") || referer.contains("ShowHomePageSearchResult")) referer="HomePage.jsp";
			HttpUtils.redirectWithAlert(req, res, referer, "No Goods Searched!");
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
