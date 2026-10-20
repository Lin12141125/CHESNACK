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

@WebServlet("/community_comment")
public class CommunityCommentServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		String action=req.getParameter("action");
		if("list".equals(action)) {
			String id=req.getParameter("id");//post id
			StringBuffer sql=new StringBuffer("select * from `community_comment` where community_id=? ");
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
				req.getRequestDispatcher("Community_Comment.jsp").forward(req, resp);
				return;
			}else{
				resp.getWriter().print("<div style='color:#888;padding:10px;'>No comments yet</div>");
				return;
			}
		}else if("reply".equals(action)) {
			String id=req.getParameter("id");//post id
			String commentId=req.getParameter("commentId");
			if(commentId==null || commentId.equals("-1")) commentId=null;
			String txt=req.getParameter("content");
			String loginName=(String)req.getSession().getAttribute("login.name");
			if(StringUtils.isEmpty(loginName)) {
				resp.getWriter().print("NotLogin");
				return;
			}
			UserDAO.exec("insert into `community_comment`(username,content,send_time,community_id,pid) values(?,?,?,?,?)",
					loginName,txt,new Timestamp(System.currentTimeMillis()),id,commentId);
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
