package compare.util;


import compare.configure.CSB2BEDIConfigQA;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionForQA {
	
	private static Logger logger = LogManager.getLogger(ConnectionForQA.class.getName());

	static int dbCount = 0;
	
	public Connection getB2BEDIQA1_DEV_DBConn() throws Exception {
		String envname = "ediqa1";
		//String dbip = "b2bdbqa4";
		String dbip = CSB2BEDIConfigQA.DBIP;
		String port = "1521";
	//	String sid = "b2bqa4";
		String sid = CSB2BEDIConfigQA.DBSID;
		String user = "b2b_app";
		String psw = "b2bapp";
		
		return getConnection(dbip, port, sid, user, psw, envname);
	}
	
	public Connection getB2BEDIQA1_DEVPLSQL_DBConn() throws Exception {
		String envname = "ediqa1";
		String dbip = CSB2BEDIConfigQA.DBIP;
		String port = "1521";
		String sid = CSB2BEDIConfigQA.DBSID;
		String user = "b2b_plsql";
		String psw = "b2bplsql";
		
		return getConnection(dbip, port, sid, user, psw, envname);
	}
	
	private Connection getConnection(String dbip, String port, String sid, String user, String psw, String envname) throws Exception {
		Connection conn=null;
		String url = "jdbc:oracle:thin:@"+dbip+":"+port+":"+sid;
		String className="oracle.jdbc.driver.OracleDriver";
		Class.forName(className);
		conn=DriverManager.getConnection(url, user, psw);
		
		dbCount++;
		logger.info("("+dbCount+") >>> DB connection ready, ip: "+dbip+", sid: "+sid+", user: "+user);
		
		return conn;
	}
}
