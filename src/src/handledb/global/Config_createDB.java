package src.handledb.global;

public class Config_createDB {
	
	public static final String DBNAME = "osm_road_db";	// Database Name
	public static final String SCHEMA = "stroke_v2";
	public static final String USER = "postgres";			// user name for DB.
	public static final String PASS = "usadasql";		// password for DB.
	public static final String URL = "rain2.elcom.nitech.ac.jp";
	public static final int PORT = 5432;
	public static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
}
