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

@WebServlet("/comment")
public class CommentServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		String action=req.getParameter("action");
		if("list".equals(action)) {
			String id=req.getParameter("id");//goods id
			String level=req.getParameter("level"); //1-Great 2-Normal 3-Bad
			StringBuffer sql=new StringBuffer("select * from `comment` where goods_id=? ");
			if(level!=null && !level.equals("")) {
				if(level.equals("1")) {
					sql.append(" and (level=5 or level is null)");
				}else if(level.equals("2")) {
					sql.append(" and (level in (3,4)  or level is null)");
				}else if(level.equals("3")) {
					sql.append(" and (level in (1,2)  or level is null)");
				}
			}
			sql.append(" order by send_time desc ");
			List<Map<String, Object>> records=UserDAO.list(sql.toString(), id);
			if(records.size()>0) {
				List<Map<String,Object>> records2=new ArrayList<>();
				Map<String,Map<String,Object>> recordMap=new HashMap<>();
				List<Map<String,Object>> replies=new ArrayList<>();
				for(Map<String,Object> item :records) {
					Object pid=item.get("pid");
					if(pid==null) {
						records2.add(item);
						recordMap.put(item.get("id").toString(), item);
					}else
						replies.add(item);
				}
				for(Map<String,Object> reply : replies) {
					String pid=reply.get("pid").toString();
					Map<String,Object> parent=recordMap.get(pid);
					if(parent==null) continue;
					List<Map<String,Object>> replyList=(List<Map<String,Object>>) parent.get("reply");
					if(replyList==null) {
						replyList=new ArrayList<>();
						parent.put("reply", replyList);
					}
					replyList.add(reply);
				}
				req.setAttribute("list", records2);
				req.getRequestDispatcher("Comment.jsp").forward(req, resp);
				return;
			}else{
				resp.getWriter().print("<div style='color:#888;padding:10px;'>No comments yet</div>");
				return;
			}
		}else if("count".equals(action)) {
			String id=req.getParameter("id");//goods id
			List<Map<String,Object>> list=UserDAO.list("select level_name,count(*) count from (SELECT  (CASE WHEN `level`=5 THEN 'Great' WHEN `level` BETWEEN 3 AND 4 THEN 'Normal'ELSE 'Bad' END) AS level_name FROM `comment` where goods_id=? and pid is null ) t group by level_name", id);
			StringBuffer txt=new StringBuffer();
			for(int i=0;i<list.size();i++) {
				Map<String,Object> item=list.get(i);
				if(i>0)
					txt.append("|");
				txt.append(item.get("level_name").toString()).append(",").append(item.get("count"));
			}
			resp.getWriter().print(txt.toString());
			return;
		}else if("reply".equals(action)) {
			String id=req.getParameter("id");//goods id
			String commentId=req.getParameter("commentId");
			String txt=req.getParameter("content");
			String loginName=(String)req.getSession().getAttribute("login.name");
			if(StringUtils.isEmpty(loginName)) {
				resp.getWriter().print("NotLogin");
				return;
			}
			Map<String,Object> item=UserDAO.find("select order_id from `comment` where id=?",commentId);
			if(item==null) {
				resp.getWriter().print("Failure");
				return;
			}
			UserDAO.exec("insert into `comment`(username,content,send_time,goods_id,order_id,pid) values(?,?,?,?,?,?)",
					loginName,txt,new Timestamp(System.currentTimeMillis()),id,item.get("order_id"),commentId);
			resp.getWriter().print("Success");
			return;
		}else if("add".equals(action)) {
			String eval=req.getParameter("eval");
			String comment=req.getParameter("comment");
			String orderId=req.getParameter("orderId");
			 Map<String,Object> item=UserDAO.find("select t2.id goods_id from `order` t1, chesnake.goods t2 where t1.good_id=t2.id and t1.id=?", orderId);
			 if(item==null || item.get("goods_id")==null) {
				resp.getWriter().print("Failure");
				return;
			 }
			 String goodsId=item.get("goods_id").toString();
			 int count=UserDAO.count("select count(*) from `comment` where order_id=? and goods_id=?",orderId,goodsId);
			 if(count>0) {
				resp.getWriter().print("Exist");
				return;
			 }
			String loginName=(String)req.getSession().getAttribute("login.name");
			if(StringUtils.isEmpty(loginName)) {
				resp.getWriter().print("NotLogin");
				return;
			}
			UserDAO.exec("insert into `comment`(username,content,send_time,goods_id,order_id,level) values(?,?,?,?,?,?)",loginName,comment,new Timestamp(System.currentTimeMillis()),goodsId,orderId,eval);
			resp.getWriter().print("Success");
			return;
		}else if("report".equals(action)) {
			String id=req.getParameter("id");
			UserDAO.exec("update `comment` set reported=? where id=?", 1,id);
			resp.getWriter().print("Success");
			return;
		}else if("del".equals(action)) {
			String id=req.getParameter("id");
			UserDAO.exec("delete from `comment` where id=? and reported=1" , id);
			resp.getWriter().print("Success");
			return;
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(req, resp);
	}

	
}
