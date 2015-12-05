package src;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import src.handledb.createDb.CreateDb;
import src.handledb.createDb.CreateStroke;
import src.handledb.create_flattedStroke.MigrationMain;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		
		// 信号機で分割できているか確認  名工大周辺10000スケール.
//		Point2D aUpperLeftLngLat = new Point2D.Double(136.92445694157854, 35.15643596038752);
//		Point2D aLowerRightLngLat = new Point2D.Double(136.93446264468074, 35.14916408679892);

		// セグメントをつかえているか25万スケール.
//		Point2D aUpperLeftLngLat = new Point2D.Double(137.0163982356397, 35.337069152689935);
//		Point2D aLowerRightLngLat = new Point2D.Double(137.2109535737376, 35.17820224622059);
		// 吹上公園周辺(70000スケール).
//		Point2D aUpperLeftLngLat = new Point2D.Double(136.90674733220217,35.176903058731725);
//		Point2D aLowerRightLngLat = new Point2D.Double(136.96109942833746, 35.13226510284117);
//		Point2D aUpperLeftLngLat = new Point2D.Double(136.9242121026848, 35.16249863140581);
//		Point2D aLowerRightLngLat = new Point2D.Double(136.9436235655903, 35.146556492422825);
		// 兵庫から静岡くらい
//		Point2D aUpperLeftLngLat = new Point2D.Double(135.0083597450366, 36.73487588494921);
//		Point2D aLowerRightLngLat = new Point2D.Double(138.8535745887866, 33.58070296836687);
		
		
		///////////////////////////////////////////////
		///////////データベース作成////////////////////////////////////		
		///////////////////////////////////////////////
		CreateDb createDb = new CreateDb();
		createDb.startConnection();
		createDb.createTable();
		createDb.endConnection();
		
		// すべてのストローク作成.
		CreateStroke createStroke = new CreateStroke();
		
//		// 一部だけストローク生成
//		CreateStroke createStroke = new CreateStroke(aUpperLeftLngLat, aLowerRightLngLat);
		
		
		////////////////////////////////////
		////////////flatted_strokeの作成//////////////////////////
		////////////////////////////////////
		MigrationMain migrationMain = new MigrationMain();
		
	}
}
