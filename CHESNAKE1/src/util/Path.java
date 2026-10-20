package util;

import javax.servlet.http.HttpServletRequest;

public class Path {
	//generate a link according(request,url)
	public static String link(HttpServletRequest request,String url) {
		if(url.startsWith("/")) {
			return request.getContextPath()+url;
		}
		else {
			return url;
		}
	}
	
	//Get the project run root directory path(absolute path)
	//C:\\xxx\\xxxx\\xxx\\
	public static String getRoot(HttpServletRequest req) {
		return req.getServletContext().getRealPath("/");
	}
}
