package com.thkmon.blogparser.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.HashMap;

import com.thkmon.blogparser.common.Const;

public class SimpleDBUtil {
	
	private static final HashMap<String, String> optionProperties = Const.optionProperties;
	
	// Cafe24 MySQL 에서 AWS MariaDB로 변경
	private static final String jdbcUrl =
	"jdbc:mysql://" + optionProperties.get("url") + ":" + optionProperties.get("port") + "/" + optionProperties.get("db") + "?"
	+ "userUnicode=true&characterEncoding=utf8"
	+ "&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC"
	
	// Sun Nov 13 07:50:57 KST 2016 WARN: Establishing SSL connection without server's identity verification is not recommended.
	// According to MySQL 5.5.45+, 5.6.26+ and 5.7.6+ requirements SSL connection must be established by default if explicit option isn't set.
	// For compliance with existing applications not using SSL the verifyServerCertificate property is set to 'false'.
	// You need either to explicitly disable SSL by setting useSSL=false, or set useSSL=true and provide truststore for server certificate verification.
	
	+ "&useSSL=true";
	
	private static final String dbUser = optionProperties.get("user");
	private static final String dbPass = optionProperties.get("password");
	
	
	public static Connection getConnection() {
		Connection conn = null;
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			// 데이터베이스 커넥션 생성
			conn = DriverManager.getConnection(jdbcUrl, dbUser, dbPass);
			conn.setAutoCommit(false);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return conn;
	}
	
	
	public static void close(PreparedStatement pst) {
		try {
			if (pst != null) {
				pst.close();
			}
			
		} catch (Exception e) {
			pst = null;
		}
	}
	
	
	public static void rollbackAndClose(Connection conn) {
		
		try {
			if (conn != null) {
				conn.rollback();
			}
			
		} catch (Exception e) {
		}
		
		try {
			if (conn != null) {
				conn.close();
			}
			
		} catch (Exception e) {
			conn = null;
			
		}
	}
	
	
	public static void rollbackOnly(Connection conn) {
		
		try {
			if (conn != null) {
				conn.rollback();
			}
			
		} catch (Exception e) {
		}
	}
	
	
	public static void commitAndClose(Connection conn) {
		
		try {
			if (conn != null) {
				conn.commit();
			}
			
		} catch (Exception e) {
		}
		
		try {
			if (conn != null) {
				conn.close();
			}
			
		} catch (Exception e) {
			conn = null;
		}
	}
}
