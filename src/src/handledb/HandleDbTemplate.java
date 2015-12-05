package src.handledb;

import java.awt.geom.Point2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * データベースを扱うためのテンプレート処理
 * @author murase
 *
 */
public class HandleDbTemplate {
	private static final String MySQLJDBCDriver = "org.gjt.mm.mysql.Driver";
	public static final String POSTGRESJDBCDRIVER_STRING = "org.postgresql.Driver";
	// 下記の変数を正しく設定する
	// DBNAME, DBDIR, USER, PASS, JDBCDriver, DBURL

	// MySQL 用デフォルト
	// Eclipse で MySQL を使いたいときは，次の手順で，WebContent\WEB-INF\lib にインポートしておく．
	// http://dev.mysql.com/downloads/connector/ から，Connector/J をダウンロード
	// c:\Program Files\Java\mysql-connector-java-5.1.7\ に置く．
	//     WebContent\WEB-INF\lib を右クリック．「一般」→「ファイルシステム」
	//     その後インポートすべきファイルとして，次のファイルを指定
	//       c:\Program Files\Java\mysql-connector-java-5.1.7\mysql-connector-java-5.1.7-bin.jar を追加
	//jdbc:mysql://amon2.elcom.nitech.ac.jp:3306/blue_db@root@usadasql
	private String DBNAME;// = "blue_db"; // Database Name
	private String USER;// = "root"; // user name for DB.
	private String PASS;// = "usadasql"; // password for DB.hoge$#34hoge5
	private String JDBCDriver = MySQLJDBCDriver;
	private String DBURL;// = "jdbc:mysql://amon2.elcom.nitech.ac.jp:3306/" + DBNAME;
	public Statement stmt = null;
	
	private Point2D.Double _upperLeft;	// 画面左上の緯度経度.
	private Point2D.Double _lowerRight;	// 画面右下の緯度経度.
	
	// コンストラクタ　データベースの設定.
	public HandleDbTemplate(String aDbName, String aUser, String aPass, String aDbUrl) {
		DBNAME = aDbName;
		USER = aUser;
		PASS = aPass;
		DBURL = aDbUrl;
	}
	// コンストラクタ　データベースの設定.
	public HandleDbTemplate(String aDbName, String aUser, String aPass, String aDbUrl, String JDBCDriver) {
		DBNAME = aDbName;
		USER = aUser;
		PASS = aPass;
		DBURL = aDbUrl;
		this.JDBCDriver = JDBCDriver;
		
	}

	
	public Point2D.Double getUpperLeft(){
		return _upperLeft;
	}
	
	public Point2D.Double getLowerRight(){
		return _lowerRight;
	}
	
	
	// ここから先はコピペのプログラム.
	/*
	 *  Service Functions
	 *  ここから先は，決まり文句を関数化したもの．
	 */
 	public Connection conn = null;
	//
	// database open
	//
	public void connect() 
	throws SQLException, ClassNotFoundException {
		try {
			// JDBC Driver Loading
			Class.forName(JDBCDriver).newInstance();
			System.setProperty("jdbc.driver",JDBCDriver);
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
		}
		try {
			// Connection
			if ( USER.isEmpty() && PASS.isEmpty() ) {
				conn = DriverManager.getConnection(DBURL);			
			}
			else {
				Properties prop = new Properties();
				prop.put("user", USER);
				prop.put("password", PASS);
				conn = DriverManager.getConnection(DBURL,prop);	
			}
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
		}
	}
	//
	// database close
	//
	public void disconnect()
	    throws SQLException {
		try {
			conn.close();
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
		}
	}
	//
	// execute
	//
	public ResultSet execute(String sql)
	    throws SQLException {
		try {	
			conn.setReadOnly(true);
			// Execute 'commit' Automatically after each SQL
			// conn.setAutoCommit(true);		 
			// Query Exection 		 
			stmt = conn.createStatement();
			return stmt.executeQuery(sql);
		}
		catch (Exception e) {
			// Error Message and Error Code
			System.out.print(e.toString());
			if (e instanceof SQLException) {
				System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
			}
			// Print Stack Trace
			e.printStackTrace();
			if (conn != null) {
				conn.rollback();
				conn.close();
			}
			return null;
		}
	}
	
	// 
	// insert into 用  (データベースへデータの挿入)
	// 
	public int insertInto(String sql)
		    throws SQLException {
			try {	
				conn.setReadOnly(false);
				// Execute 'commit' Automatically after each SQL
				// conn.setAutoCommit(true);		 
				// Query Exection 		 
				stmt = conn.createStatement();
				return stmt.executeUpdate(sql);
			}
			catch (Exception e) {
				// Error Message and Error Code
				System.out.print(e.toString());
				if (e instanceof SQLException) {
					System.out.println("Error Code:" + ((SQLException)e).getErrorCode());
				}
				// Print Stack Trace
				e.printStackTrace();
				if (conn != null) {
					conn.rollback();
					conn.close();
				}
				return -1;
			}
		}
	
}
