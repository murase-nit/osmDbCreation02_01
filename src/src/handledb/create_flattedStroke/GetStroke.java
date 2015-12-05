package src.handledb.create_flattedStroke;

import java.awt.geom.Line2D;
import java.beans.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.postgis.PGgeometry;

import src.handledb.GeometryParsePostgres;
import src.handledb.GeometryTemplete;
import src.handledb.HandleDbTemplateSuper;
import src.handledb.global.Config_createDB;


/**
 * osm_stroke_db,osm_stroke_tableからストロークデータの取得
 * @author murase
 *
 */
public class GetStroke extends HandleDbTemplateSuper {

//	public static final String DBNAME = "osm_stroke_20150901";
//	public static final String SCHEMA = "public";
//	public static final String TBNAME = "osm_stroke_table1";
//	public static final String USER = "postgres";			// user name for DB.
//	public static final String PASS = "murase";
//	public static final String URL = "localhost";
//	public static final int PORT = 5432;
//	public static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;	
	
	public static final String DBNAME = Config_createDB.DBNAME;//"osm_road_db";
	public static final String SCHEMA = Config_createDB.SCHEMA;//"stroke_v2";
	public static final String TBNAME = "stroke_table";
	public static final String USER = Config_createDB.USER;//"postgres";			// user name for DB.
	public static final String PASS = Config_createDB.PASS;//"usadasql";
	public static final String URL = Config_createDB.URL;//"rain2.elcom.nitech.ac.jp";
	public static final int PORT = Config_createDB.PORT;//5432;
	public static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;

	
	public String strokeString = "";
	public int clazz = -1;
	public String flattedstrokeString = "";
	
	public GetStroke(){
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
	}
	
	/**
	 * データ数を求める
	 */
	public int getnum(){
		int num=0;
		try{
			String stmt = "";
			stmt = "select count(*) from "+SCHEMA+"."+TBNAME+"";
			System.out.println(stmt);
			ResultSet rs = execute(stmt);
			while(rs.next()){
				num = rs.getInt(1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return num;
	}

	
	/**
	 * i番目のストロークを取得する
	 * @param offset
	 */
	public String getStrokeString_i(int offset){
		try{
			String s = "select st_asText(arc_series) as strokeString, stroke_clazz from "+SCHEMA+"."+TBNAME+" where id = "+offset+"";
			ResultSet rs = execute(s);
			if(rs.next()){
				strokeString = rs.getString("strokeString");
				clazz = rs.getInt("stroke_clazz");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return strokeString;
	}
	
	/**
	 * linestringの形に変更(linestringにできないときは分割する)
	 * @param strokeString
	 */
	// TODO linestringにできないストロークをどのように扱うか
	public ArrayList<ArrayList<Line2D>> flattenStroke(String strokeString){
		ArrayList<ArrayList<Line2D>> flattedStrokeGeom = new ArrayList<>();
		try{
			String s = "select st_lineMerge(st_geomFromtext('"+strokeString+"', "+GeometryTemplete.SRID_wd+"))";
//			System.out.println(s);
			ResultSet rs = execute(s);
			if(rs.next()){
				//flattedStrokeStrings = rs.getObject(1);
				flattedStrokeGeom = GeometryParsePostgres.getMulitLimeStringDatas((PGgeometry)rs.getObject(1));
			}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		return flattedStrokeGeom;
	}
	
	/**
	 * 長さを求める
	 * @param flattedstrokeString
	 */
	public double getLength(ArrayList<Line2D> aStrokeGeom){
		double length = -1;
		try{
			String s = "select " +
					" st_length( " +
						" st_transform("+
							" st_lineMerge( "+
								GeometryParsePostgres.multiLineString(aStrokeGeom, GeometryTemplete.SRID_wd)+
							")"+
						", "+GeometryTemplete.SRID_wd_utm+")"+
					") as len;";
//			System.out.println(s);
			ResultSet rs = execute(s);
			if(rs.next()){
				length = rs.getDouble("len");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return length;
	}
	
}
