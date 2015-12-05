package src.handledb.create_flattedStroke;

import java.sql.ResultSet;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import src.handledb.GeometryParsePostgres;
import src.handledb.GeometryTemplete;
import src.handledb.HandleDbTemplateSuper;
import src.handledb.global.Config_createDB;

/**
 * データベースをosm_road_db.stroke.stroke_tableに付加情報を追加
 * @author murase
 *
 */
public class AddFeatureCreateDb extends HandleDbTemplateSuper{
	
//	public static final String DBNAME = "osm_stroke_20150901";
//	public static final String SCHEMA = "public";
//	public static final String TBNAME = "flatted_stroke_table";
//	public static final String USER = "postgres";			// user name for DB.
//	public static final String PASS = "murase";
//	public static final String URL = "localhost";
//	public static final int PORT = 5432;
//	public static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	public static final String DBNAME = Config_createDB.DBNAME;//"osm_road_db";
	public static final String SCHEMA = Config_createDB.SCHEMA;//"stroke_v2";
	public static final String TBNAME = "flatted_stroke_table";
	public static final String USER = Config_createDB.USER;//"postgres";			// user name for DB.
	public static final String PASS = Config_createDB.PASS;//"usadasql";
	public static final String URL = Config_createDB.URL;//"rain2.elcom.nitech.ac.jp";
	public static final int PORT = Config_createDB.PORT;//5432;
	public static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	
	public AddFeatureCreateDb(){
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}
	
	/**
	 * テーブルの存在有無
	 * @param aTableName
	 * @param aHandleDbTemplate
	 * @return
	 */
	private boolean isTableExist(String aTableName){
		
		
		boolean bool = false;
		
		try{
			String statement = "SELECT relname FROM pg_class WHERE relkind = \'r\' AND relname = \'"+aTableName+"\'";
			ResultSet rs = execute(statement);
			while(rs.next()){
				return true;
			}
			return false;
		}catch(Exception e){
			e.printStackTrace();
		}
		return bool;
	}
	
	/**
	 * テーブル作成
	 * @param aMeshCode
	 */
	public void createTable(){
		
		if(isTableExist(""+SCHEMA+"."+TBNAME)){
			System.out.println("already exist"+ SCHEMA+"."+TBNAME);
			System.exit(0);
			return;
		}
		
		try{
			String command = "create table if not exists "+SCHEMA+"."+TBNAME+""+
					" (id serial not null primary key ," +
					" stroke_id int, "+
					" length double precision , " +
					" stroke_clazz int , " +
					" flatted_arc_series geometry(lineString, "+GeometryTemplete.SRID_wd+")"+
					");" +
					" create index "+SCHEMA+"_"+TBNAME+"_flatted_arc_series_key_aaa on "+SCHEMA+"."+TBNAME+" using gist(flatted_arc_series);";
			System.out.println(command);
			insertInto(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * データの追加
	 */
	public void createStrokeTable(int stroke_id , double length, int clazz ,ArrayList<Line2D> aFlattedStrokeGeom){
		try {
			String command = "insert into " +
					SCHEMA+"."+TBNAME+
				" (stroke_id, length, stroke_clazz, flatted_arc_series) "+
				" values(" + 
					""+stroke_id+", "+length+", "+clazz+", "+
					"st_lineMerge("+
						GeometryParsePostgres.multiLineString(aFlattedStrokeGeom, GeometryTemplete.SRID_wd)+
					")"+
				"); ";
//			System.out.println(command);
			insertInto(command);
		} catch (Exception e) {
			e.printStackTrace();
//			System.exit(0);
		}
		
	}
	
}
