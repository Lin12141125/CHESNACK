package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChesnakeDAO {

	private static String driver;
	private static String url;
	private static String user;
	private static String password;
    
    //static block
    //Execute only once
	static {
		try {
			driver = "com.mysql.cj.jdbc.Driver";
            Class.forName(driver);
			url = "jdbc:mysql://localhost:3306/chesnake?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8&allowPublicKeyRetrieval=true";
			user = "root";
			password = "lyzhmlzq1217.";
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//get connected
    public static Connection getConnection() {
        //driver
		Connection con = null;
		try {
            //connect Database
            //Statement createStatement():
            //Create a Statement object to send the SQL statement to the database
			con = DriverManager.getConnection(url, user, password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			
			throw new RuntimeException("Database connection failure",e);
		}
        //Return connection object
		return con;
	}


	/**
	 * execute the query statement
     * return the collection list
	 * @param sqlStr
	 * @param params
	 * @return
	 */
	public static List<Map<String,Object>> list(String sqlStr, Object... params) {
		System.out.println("Execute a SQL statement: "+sqlStr);
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		Connection conn = getConnection();
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
            stmt = conn.prepareStatement(sqlStr);
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			rs = stmt.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colNum = rsmd.getColumnCount();

			while (rs.next()) {
				Map<String,Object> item=new HashMap<>();
				for (int i = 1; i <= colNum; i++) {
					String label=rsmd.getColumnLabel(i);
					item.put(label,rs.getObject(i));
				}
				retList.add(item);
			}
		} catch (SQLException e) {
            throw new RuntimeException("SQL execution failure",e);
		} finally {
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
				rs = null;
				stmt = null;
				conn = null;
			} catch (SQLException e) {
                throw new RuntimeException("SQL execution failure",e);
			}
		}

		return retList;
	}

	/**
     * Excute INSERT, UPDATE, DELETE
	 * @param sqlStr
	 * @param params
	 * @return
	 */
	public static int exec(String sqlStr, Object... params) {
		System.out.println("Execute a SQL statement: "+sqlStr);
		int rows = 0;
		Connection conn = getConnection();

		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sqlStr);
			for (int i = 0; i < params.length; i++) {
				stmt.setObject(i + 1, params[i]);
			}
			rows = stmt.executeUpdate();
		} catch (SQLException e) {			
			throw new RuntimeException("SQL execution failure",e);
		} finally {
			try {
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
				stmt = null;
				conn = null;
			} catch (SQLException e) {
                throw new RuntimeException("SQL execution failure",e);
			}
		}
		return rows;
	}

	/**
	 * Get the data for the first field in the first row
	 * @param sqlStr
	 * @param params
	 * @return
	 */
	public static String first(String sqlStr, Object... params) {		
		Map<String,Object> item=find(sqlStr,params);
		if(item==null||item.size()==0) return null;
		Object ret=null;
		for(Object value : item.values()) {
			ret=value;break;
		}
		if(ret==null)
			return null;
		else
			return ret.toString();
	}
	
	/**
	 * Execute statistical query statements
	 * @param sqlStr
	 * @param params
	 * @return
	 */
	public static Integer count(String sqlStr,Object...params) {
		String str=first(sqlStr,params);
		if(StringUtils.isEmpty(str))
			return null;
		else
			return Integer.valueOf(str);
	}

	/**
	 * Get the data of the first row
	 * @param sqlStr
	 * @param params
	 * @return
	 */
	public static Map<String,Object> find(String sqlStr, Object... params) {
		List<Map<String,Object>> list = list(sqlStr, params);
		if(list.size()==0)
			return null;
		else
			return list.get(0);
	}

	/**
	 * Execute statements in batches
	 * @param sqlStrArr
	 * @return
	 */
	public static void executeUpdateBatch(String[] sqlStrArr) {

		Connection conn = getConnection();
		Statement stmt = null;
		try {
			conn.setAutoCommit(false); //Set to manual commit transaction mode
			stmt = conn.createStatement();
		} catch (SQLException e) {			
			throw new RuntimeException("SQL execution failure",e);
		}

		for (int i = 0; i < sqlStrArr.length; i++) {
			String sqlStr = sqlStrArr[i];
			System.out.println("Execute a SQL statement: "+sqlStr);
			try {
				stmt.executeUpdate(sqlStr);
			} catch (SQLException e) {
				try{ conn.rollback(); }catch(Exception e2) {}
				throw new RuntimeException("SQL execution failure",e);
			}
		}
		
		try {
			conn.commit();
		} catch (SQLException e) {
			throw new RuntimeException("SQL execution failure",e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
					stmt = null;
				} catch (SQLException e) {
					throw new RuntimeException("SQL execution failure",e);
				}
			}
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
					conn = null;
				} catch (SQLException e) {
					throw new RuntimeException("SQL execution failure",e);
				}
			}
		}
		
	}
}