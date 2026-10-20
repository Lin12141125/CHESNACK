package servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import util.StringUtils;
import util.UserDAO;
import util.ChesnakeDAO;

@WebServlet("/addGoods")
@MultipartConfig
public class AddGoodsServlet extends HttpServlet {
	
	@Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    	
    	request.setCharacterEncoding("UTF-8");
    	response.setCharacterEncoding("UTF-8");
    	
		String path = getServletContext().getRealPath("/")+"\\images\\upload\\";
		File dir=new File(path);
		if(!dir.exists())
			dir.mkdir();
        
        String tradeName = request.getParameter("tradeName");
        String information = request.getParameter("information");
        String unitPrice = request.getParameter("unitPrice");
        String producer = request.getParameter("producer");
        String inventoryQuantity = request.getParameter("inventoryQuantity");
        String type=request.getParameter("type");
        String img1=null,img2=null;
        long time=System.currentTimeMillis();
        Part part=request.getPart("file1");
        if(part!=null && part.getSize()>0) {
            String extName=part.getSubmittedFileName();
            extName=extName.substring(extName.lastIndexOf(".")+1);
            img1=time+"."+extName;
            part.write(path+img1);
            img1="upload/"+img1;
        }
        part=request.getPart("file2");
        if(part!=null && part.getSize()>0) {
            String extName=part.getSubmittedFileName();
            extName=extName.substring(extName.lastIndexOf(".")+1);
            time=time+1;
            img2=time+"."+extName;
            part.write(path+img2);
            img2="upload/"+img2;
        }
        
        Map<String,Object> item=ChesnakeDAO.find("SELECT MAX(CAST(id AS SIGNED)) AS max_value FROM goods");
        int newId=1;
        if(item!=null && item.get("max_value")!=null) {
        	newId=Integer.valueOf(item.get("max_value").toString())+1;
        }
        
		String account=(String)request.getSession().getAttribute("login.account");
		if(StringUtils.isEmpty(account)) {
			response.getWriter().print("<script>alert('Please log in first!');window.location.href='login.jsp';</script>");
			return;
		}
        
		ChesnakeDAO.exec("insert into `goods`( id,type, brand, name, price, origin, net_content,sale_quantity,img1,img2,score) values(?,?,?,?,?,?,?,?,?,?,?)",
				newId,type,information,tradeName,unitPrice,producer,inventoryQuantity,0,img1,img2,0);
		
		response.getWriter().print("<script>alert('Success to add to warehouse');history.back();</script>");
		
		
    }
}
