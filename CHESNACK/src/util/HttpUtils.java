package util;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HttpUtils {
	public static void redirect(HttpServletRequest req,HttpServletResponse res,String path) {
		try {
			if(!path.startsWith("/")) {
				res.sendRedirect(path);
			}
			else {
				res.sendRedirect(Path.link(req,path));
			}
		}catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void redirectWithAlert(HttpServletRequest req,HttpServletResponse res,String path,String mes) {
		alert(req,mes);
		try {
			if(!path.startsWith("/")) {
				res.sendRedirect(path);
			}
			else {
				res.sendRedirect(Path.link(req, path));
			}
		}catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void redirectWithAlert(HttpServletRequest req,HttpServletResponse res,String path,String mes,Map<String,String> params) {
		alert(req,mes);
		try {
			req.getSession().setAttribute("__params", params);
			if(!path.startsWith("/")) {
				res.sendRedirect(path);
			}
			else {
				res.sendRedirect(Path.link(req, path));
			}
		}catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void alert(HttpServletRequest req,String mes) {
		req.getSession().setAttribute("__page.alert", mes);
	}
	
	@SuppressWarnings("unchecked")
	public static String getSessionParam(HttpServletRequest req,String key) {
		HttpSession session=req.getSession();
		Map<String,String> params=(Map<String,String>)session.getAttribute("__params");
		if(params==null) {
			return "";
		}
		String value=params.get(key);
		if(value==null) {
			return "";
		}
		else {
			params.remove(key);
			return value;
		}
	}
}