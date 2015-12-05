package src.handledb.createDb;

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
 * データベースにデータを格納する
 * @author murase
 *
 */
public class CreateDb extends HandleDbTemplateSuper{
	public static final String DBNAME = Config_createDB.DBNAME;//"osm_road_db";
	public static final String SCHEMA = Config_createDB.SCHEMA;//"stroke_v2";
	public static final String TBNAME = "stroke_table";
	public static final String USER = Config_createDB.USER;//"postgres";			// user name for DB.
	public static final String PASS = Config_createDB.PASS;//"usadasql";
	public static final String URL = Config_createDB.URL;//"rain2.elcom.nitech.ac.jp";
	public static final int PORT = Config_createDB.PORT;//5432;
	public static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;

	
	public CreateDb(){
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
					" link_ids text , " +
					" stroke_length double precision , " +
					" stroke_clazz int , "+
					" arc_series geometry(multilineString, "+GeometryTemplete.SRID_wd+")"+
					");" +
					""
					+" create index "+SCHEMA+"_"+TBNAME+"_stroke_mline_key on "+SCHEMA+"."+TBNAME+" using gist(arc_series);";
			System.out.println(command);
			insertInto(command);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	// stroke_id, link_ids, link_num, left, top, right, down.
	/**
	 * nnnnnnn_strokeの作成
	 */
	public void createStrokeTable(String link_ids, int stroke_class, ArrayList<ArrayList<Point2D.Double>> aStrokeMline){
		try {
			String command = "insert into " +
					SCHEMA+"."+TBNAME+
				" (link_ids, stroke_length, stroke_clazz, arc_series) "+
				" values(" + 
					"'"+link_ids+"',"+
					"st_length(st_transform("+GeometryTemplete.multiLineStringSegment(aStrokeMline, GeometryTemplete.SRID_wd)+", "+GeometryTemplete.SRID_wd_utm+")),"+
					stroke_class +","+
					""+GeometryTemplete.multiLineStringSegment(aStrokeMline, GeometryTemplete.SRID_wd)+
				"); ";
			
//			System.out.println(command);
			
			insertInto(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
