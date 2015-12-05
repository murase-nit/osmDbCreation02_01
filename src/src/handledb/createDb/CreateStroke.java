package src.handledb.createDb;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.postgis.PGgeometry;

import src.handledb.GeometryTemplete;
import src.handledb.HandleDbTemplateSuper;
import src.handledb.global.Config_roadDB;


/**
 * 道路データからストロークを生成
 * @author murase
 *
 */
public class CreateStroke extends HandleDbTemplateSuper{
	
	private static final String DBNAME = Config_roadDB.DBNAME;	// Database Name
	private static final String TBNAME = Config_roadDB.TBNAME;
	private static final String TEMP_TBNAME = "tb_temp_link";	// テスト用.
	private static final String USER = Config_roadDB.USER;			// user name for DB.
	private static final String PASS = Config_roadDB.PASS;		// password for DB.
	private static final String URL = Config_roadDB.URL;
	private static final int PORT = Config_roadDB.PORT;
	private static final String DBURL = "jdbc:postgresql://"+URL+":"+PORT+"/" + DBNAME;
	
	/** 道なりと判断する角度 30=pi/6, 45=pi/4, 60=pi/3 */
	private static final double STROKE_ANGLE = Math.PI/4;

	// 1メッシュ内のストロークについて.
//	/** ストロークのジオメトリ */
//	private ArrayList<ArrayList<Line2D>> _strokeGeomArrayList = new ArrayList<>();	// ArrayList<Line2D>で1つのストローク.
//	/** ストロークのID */
//	private ArrayList<ArrayList<Integer>> _strokeIdArrayList = new ArrayList<>();
//	/** All segment */
//	public ArrayList<ArrayList<ArrayList<Point2D.Double>>> _strokeSegmentArrayList = new ArrayList<>();
//	/** ストロークの道路クラス(その中のリンクの多くがどの道路クラスになっているか) */
//	public ArrayList<Integer> _strokeClass = new ArrayList<>();
	
	// 1つのストロークについて.
	/** ある1つのストロークのリンクLine2D集合 */
	public ArrayList<Line2D> _oneStroke = new ArrayList<>();
	/**　ある1つのストロークのリンクpoint2D集合　*/
	public ArrayList<Point2D> _oneStrokePoint = new ArrayList<>();
	/** ある1つのストロークのリンクID集合 */
	public ArrayList<Integer> _oneStrokeLinkId = new ArrayList<>();
	/** one segment */
	public ArrayList<ArrayList<Point2D.Double>> _oneStrokeSegment = new ArrayList<>();
	/**  */
//	public HashMap<Integer, Integer> _oneStrokeClass = new HashMap<>();
	/**　ある1つのストロークの道路クラス　*/
	public int _oneStrokeClazz = -1;
	/** リンクIDの文字列系列 */
	public String _oneStrokeIdSeriesString = "";
	
	
	/**
	 * コンストラクタ
	 */
	public CreateStroke(){
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
		startConnection();
		// 一時テーブルの作成.
		createTmpTable();
		// ストロークの作成.
		oneMeshStroke();
		endConnection();
	}
	
	/**
	 * 必要なリンクだけの一時的なテーブルを作成する
	 */
	public void createTmpTable(){
		try{
			String statement = "";
			// リンクデータを取得.
			statement = " create temp table "+TEMP_TBNAME+"(" +
					"id serial, "+
					"link_id integer, " +
					"link_length double precision, " +
					"link_class_id integer, " +
					"link_line geometry, " +
					"seg_line geometry," +
					"oneway text); " +
					"create unique index "+TEMP_TBNAME+"_link_id on "+TEMP_TBNAME+"(link_id) ;" +
					"create unique index "+TEMP_TBNAME+"_id on "+TEMP_TBNAME+"(id) ;" +
					"create index "+TEMP_TBNAME+"_line on "+TEMP_TBNAME+" using gist(link_line);" +
					"create index tb_temp_seg_line on "+TEMP_TBNAME+" using gist(seg_line);";
			insertInto(statement);
			statement = " " +
					" insert into " +
						" "+TEMP_TBNAME+"(link_id, link_length, link_class_id, link_line, seg_line) " +
							" (select " +
								" id, km, clazz, st_setSRID(st_makeLine(st_makePoint(x1, y1), st_makePoint(x2, y2)), 4326), geom_way" +
							" from " +
								TBNAME +
							" where " +
								" clazz > 12) ";	// 高速道路は省く.) ;";
			System.out.println(statement);
			insertInto(statement);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * コンストラクタ
	 */
	public CreateStroke(Point2D aUpperLeftLngLat, Point2D aLowerRightLngLat){
		super(DBNAME, USER, PASS, DBURL, HandleDbTemplateSuper.POSTGRESJDBCDRIVER_STRING);
		
		
		startConnection();
		// 一時テーブルの作成.
		createTmpTable(aUpperLeftLngLat, aLowerRightLngLat);
		// ストロークの作成.
		oneMeshStroke();
		endConnection();
	}
	
	/**
	 * 必要なリンクだけの一時的なテーブルを作成する
	 * テスト用のストロークを作成するときに使う
	 */
	public void createTmpTable(Point2D aUpperLeftLngLat, Point2D aLowerRightLngLat){
		try{
			String statement = "";
			// リンクデータを取得.
			statement = " create temp table "+TEMP_TBNAME+"(" +
					"id serial, "+
					"link_id integer, " +
					"link_length double precision, " +
					"link_class_id integer, " +
					"link_line geometry, " +
					"seg_line geometry," +
					"oneway text); " +
					"create unique index "+TEMP_TBNAME+"_link_id on "+TEMP_TBNAME+"(link_id) ;" +
					"create unique index "+TEMP_TBNAME+"_id on "+TEMP_TBNAME+"(id) ;" +
					"create index "+TEMP_TBNAME+"_line on "+TEMP_TBNAME+" using gist(link_line);" +
					"create index tb_temp_seg_line on "+TEMP_TBNAME+" using gist(seg_line);";
			insertInto(statement);
			statement = " " +
					" insert into " +
						" "+TEMP_TBNAME+"(link_id, link_length, link_class_id, link_line, seg_line) " +
							" (select " +
								" id, km, clazz, st_setSRID(st_makeLine(st_makePoint(x1, y1), st_makePoint(x2, y2)), 4326), geom_way" +
							" from " +
//								TBNAME + " inner join "+TEMP_TBNAME+"_attr on "+TBNAME+".osm_id = "+TEMP_TBNAME+"_attr.osm_id"+
								TBNAME +
							" where " +
//								" clazz > 12 and " +	// 高速道路は省く.
								" st_intersects( "+
								"st_transform(" +
									"st_geomFromText('polygon(("+
									aUpperLeftLngLat.getX()+" "+aLowerRightLngLat.getY()+","+
									aLowerRightLngLat.getX()+" "+aLowerRightLngLat.getY()+","+
									aLowerRightLngLat.getX()+" "+aUpperLeftLngLat.getY()+","+
									aUpperLeftLngLat.getX()+" "+aUpperLeftLngLat.getY()+","+
									aUpperLeftLngLat.getX()+" "+aLowerRightLngLat.getY()+
									" ))',"+GeometryTemplete.SRID_jp+") " +
								","+GeometryTemplete.SRID_wd+") " +
							" ,geom_way) "
							+ " and clazz > 12); ";
			System.out.println(statement);
			insertInto(statement);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * ある範囲の中のすべてのストロークを求める
	 * @param aMeshCode
	 */
	public void oneMeshStroke(){
		int count = 0;// ストロークとして確定しているリンクの数.
		// すべてのリンクのIDを求める.
		HashMap<Integer, Integer> allLinkHashMap = new HashMap<>();	// ハッシュにして高速探索.
		int tableSize = 0;	// テーブルサイズ.
		
		try{
			String statement = 
					"select link_id " +
					" from " +
						" "+TEMP_TBNAME+" ";
			ResultSet rs = execute(statement);
			while(rs.next()){
				allLinkHashMap.put(rs.getInt("link_id"), rs.getInt("link_id"));
				tableSize++;
//				if(2089119 == rs.getInt("link_id")){
//					System.out.println("link ok");
//					System.exit(0);
//				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		CreateDb createDb = new CreateDb();
		createDb.startConnection();
		
		// allLinkIdのサイズが0になるまで，リンクを1つずつ取り出し，そのリンクからストロークを生成する.
		while(allLinkHashMap.size()>0){
			// tmpテーブルから1つ取り出す.
			int oneLinkId = allLinkHashMap.keySet().iterator().next();
			oneStrokeFromLink(oneLinkId, allLinkHashMap);	// 1つのリンクからストロークを求める.
			// ストロークとなったリンクをallLinkIdから削除.
			for(int i=0; i<_oneStrokeLinkId.size(); i++){
				if(allLinkHashMap.containsKey(_oneStrokeLinkId.get(i).intValue())){
					if(allLinkHashMap.remove(_oneStrokeLinkId.get(i).intValue()) == null){
						System.out.println("削除できていない");
					}
//					if(2089119 == _oneStrokeLinkId.get(i)){
//						System.out.println("##############link oko##########");
//						System.out.println("stroke id : " + count);
//						System.out.println(_oneStroke.get(i).getP1());
//						System.out.println(_oneStroke.get(i).getP2());
//						//System.exit(0);
//					}
					System.out.println(""+(count++)+"/"+tableSize);
//					break;
				}else{	// .
					System.out.println("ストロークに選ばれたけれどすでに別のストロークに登録されている");
				}
			}
			
			
			//String link_ids, 
			//double stroke_length, 
			//int stroke_class, ok
			//ArrayList<Line2D> aStrokeLine, 
			//ArrayList<Point2D> aStrokeMline

			
			/// データベース登録.
			//###################################
			createDb.createStrokeTable(_oneStrokeIdSeriesString, _oneStrokeClazz, _oneStrokeSegment);
			//###################################
			
			
		}
		createDb.endConnection();
	}
	/**
	 * 1つのリンクからストロークを求める.
	 * @param aLinkId 現在見ているリンクID
	 * @param aAllLinkId すべてのリンクID(ストロークとして確定しているリンクは除く)
	 */
	public void oneStrokeFromLink(int aLinkId, HashMap<Integer, Integer> aAllLinkHashMap){
		Line2D oneLine2d = new Line2D.Double();	// あるリンクのジオメトリ(Line2D).
		ArrayList<Point2D.Double> oneSegment2d = new ArrayList<>();
		Point2D.Double startPoint = new java.awt.geom.Point2D.Double();	// あるリンクのstartPoint.
		Point2D.Double endPoint = new java.awt.geom.Point2D.Double();	// あるリンクのendPoint.
		Line2D startSeg = new Line2D.Double();	// あるリンクのstart 側の最初のsegment.
		Line2D endSeg = new Line2D.Double();	// あるリンクのend側の最初のsegment.
		int clazz = -1;	// 道路クラス.
		
		LinkDataSet linkDataSet = new LinkDataSet(null, null, -1, null, -1);	// リンクに関する情報.
		try{
			// リンクIDからジオメトリを求める.
			String statementString=
					"select " +
					" st_asText(link_line) as linkLineString , seg_line, link_class_id" +
					" from "+TEMP_TBNAME+"  " +
					" where link_id = "+aLinkId+"  ";
			ResultSet rs = execute(statementString);
			if(rs.next()){
				oneLine2d = GeometryTemplete.parsingLine2d(rs.getString("linkLineString"));
				oneSegment2d = GeometryTemplete.getLineStringDataMultiPoint((PGgeometry)rs.getObject("seg_line"));
				startPoint = (Point2D.Double)oneLine2d.getP1();
				endPoint = (Point2D.Double)oneLine2d.getP2();
				clazz = rs.getInt("link_class_id");
			}
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
		
		ArrayList<Line2D> oneStroke = new ArrayList<>();	// 指定したリンクと道なり関係にあるリンクの集合.
		ArrayList<Integer> oneStrokeId = new ArrayList<>();	// そのlinkId.
		ArrayList<ArrayList<Point2D.Double>> oneStrokeSegment = new ArrayList<>();// 道なりになるストローク内のセグメント.
//		HashMap<Integer, Integer> oneStrokeClazzCategory = new HashMap<>();	// 道路クラスをカウント(<道路クラス，数>).
		// 最初のリンクデータ.
		oneStroke.add(oneLine2d);	// 最初のリンクを追加.
		oneStrokeSegment.add(oneSegment2d);
		oneStrokeId.add(aLinkId);	// 最初のリンクを追加.
//		System.out.println(oneStrokeClazzCategory.get(clazz));
//		oneStrokeClazzCategory.put(clazz, oneStrokeClazzCategory.get(clazz)==null? 1 : oneStrokeClazzCategory.get(clazz)+1);
		linkDataSet.nextLink = oneLine2d;
		linkDataSet.nextPoint = endPoint;
		linkDataSet.nextLinkId = aLinkId;
		linkDataSet.clazz = clazz;
		/////////////////////////////////////////////
		// distPoint側からやる.
		/////////////////////////////////////////////
		while(true){
//			System.out.println("focus LinkId "+oneStrokeId.get(oneStrokeId.size()-1));
//			System.out.println("distノード側から開始");
//			System.out.println("辿るもとのリンクID"+linkDataSet.nextLinkId);
			LinkDataSet distNodeNextLinkDataSet = getNextLink(linkDataSet.nextLink, linkDataSet.nextLinkId, linkDataSet.clazz);	// リンクを1つ指定して道なりに続く次のリンクを求める.
			if(distNodeNextLinkDataSet.nextLink != null){
				// ループしているか判定(重複判定).
				int prevNum = oneStrokeId.size();
				HashSet<Integer> hashSet = new HashSet<>();
				hashSet.addAll(oneStrokeId);
				hashSet.add(distNodeNextLinkDataSet.nextLinkId);
				int addedNum = hashSet.size();
				if(prevNum == addedNum){	// 重複している.
//					System.out.println("重複判定");
					break;
				}
				// nextLinkを後ろにたどって同じかどうか確かめる.
//				System.out.println("逆からたどる");
//				System.out.println("辿るもとのリンクID"+distNodeNextLinkDataSet.nextLinkId);
				Line2D nextReverseLink = new Line2D.Double(distNodeNextLinkDataSet.nextLink.getP2(), distNodeNextLinkDataSet.nextLink.getP1());
				LinkDataSet distNodeNextPrevLinkDataSet = getNextLink(nextReverseLink, distNodeNextLinkDataSet.nextLinkId, distNodeNextLinkDataSet.clazz);
//				System.out.println("nextprev "+distNodeNextPrevLinkDataSet.nextLinkId);
//				System.out.println("present "+linkDataSet.nextLinkId);
				if(distNodeNextPrevLinkDataSet.nextLinkId == linkDataSet.nextLinkId){	// 次のリンクから前のリンクに戻ったリンクが現在のリンクと同じか.
					// 登録しようとしているリンクが，すでにストロークになっていないか調べる.
					boolean breakFlg = true;
//					for(int i=0; i<aAllLinkHashMap.size(); i++){
						//if(distNodeNextPrevLinkDataSet.nextLinkId == aAllLinkId.get(i).intValue()){
						if(aAllLinkHashMap.containsKey(distNodeNextPrevLinkDataSet.nextLinkId)){
							breakFlg=false;
//							break;
						}
//					}
					if(breakFlg) break;
					// 正式に登録.
					oneStroke.add(distNodeNextLinkDataSet.nextLink);	// 後ろに追加.
					oneStrokeSegment.add(distNodeNextLinkDataSet.nextSegment);
					oneStrokeId.add(distNodeNextLinkDataSet.nextLinkId);// 後ろに追加.
//					oneStrokeClazzCategory.put(distNodeNextPrevLinkDataSet.clazz, oneStrokeClazzCategory.get(clazz)==null? 1 : oneStrokeClazzCategory.get(clazz)+1);
					// 次のリンクを辿る準備.
					linkDataSet.nextLink   = distNodeNextLinkDataSet.nextLink;
					linkDataSet.nextPoint  = distNodeNextLinkDataSet.nextPoint;
					linkDataSet.nextLinkId = distNodeNextLinkDataSet.nextLinkId;
					linkDataSet.clazz = distNodeNextPrevLinkDataSet.clazz;
				}else{	// 違う場合.
//					System.out.println("not defference");
					break;
				}
			}else{
//				System.out.println("dist側からの道なりリンク終了");
				break;
			}
		}
//		System.out.println("startpoint側から開始");
		// startPoint側から行う準備.
		linkDataSet.nextLink = new Line2D.Double(oneLine2d.getP2(), oneLine2d.getP1());//oneLine2d;
		linkDataSet.nextPoint = startPoint;
		linkDataSet.nextLinkId = aLinkId;
		linkDataSet.clazz = clazz;
		/////////////////////////////////////////////
		// startPoint側からもやる.
		/////////////////////////////////////////////
		while(true){
//			System.out.println("startノード側から開始 ");
			LinkDataSet startNodeNextLinkDataSet = getNextLink(linkDataSet.nextLink, linkDataSet.nextLinkId, linkDataSet.clazz);	// リンクを1つ指定して道なりに続く次のリンクを求める.
			if(startNodeNextLinkDataSet.nextLink != null){
				// ループしているか判定(重複判定).
				int prevNum = oneStrokeId.size();
				HashSet<Integer> hashSet = new HashSet<>();
				hashSet.addAll(oneStrokeId);
				hashSet.add(startNodeNextLinkDataSet.nextLinkId);
				int addedNum = hashSet.size();
				if(prevNum == addedNum){	// 重複している.
					break;
				}
				// nextLinkを後ろにたどって同じかどうか確かめる.
//				System.out.println("逆からたどる");
				Line2D nextReverseLink = new Line2D.Double(startNodeNextLinkDataSet.nextLink.getP2(), startNodeNextLinkDataSet.nextLink.getP1());
				LinkDataSet distNodeNextPrevLinkDataSet = getNextLink(nextReverseLink, startNodeNextLinkDataSet.nextLinkId, startNodeNextLinkDataSet.clazz);
//				System.out.println("nextprev "+distNodeNextPrevLinkDataSet.nextLinkId);
//				System.out.println("present "+ linkDataSet.nextLinkId);
				if(distNodeNextPrevLinkDataSet.nextLinkId == linkDataSet.nextLinkId){
					// 登録しようとしているリンクが，すでにストロークになっていないか調べる.
					boolean breakFlg = true;
//					for(int i=0; i<aAllLinkHashMap.size(); i++){
						//if(distNodeNextPrevLinkDataSet.nextLinkId == aAllLinkId.get(i).intValue()){
						if(aAllLinkHashMap.containsKey(distNodeNextPrevLinkDataSet.nextLinkId)){
							breakFlg=false;
//							break;
						}
//					}
					if(breakFlg) break;
					// 正式に登録.
					oneStroke.add(0, startNodeNextLinkDataSet.nextLink);	// 前に追加.
					oneStrokeSegment.add(0, startNodeNextLinkDataSet.nextSegment);
					oneStrokeId.add(0, startNodeNextLinkDataSet.nextLinkId);// 前に追加.
//					oneStrokeClazzCategory.put(startNodeNextLinkDataSet.clazz, oneStrokeClazzCategory.get(clazz)==null? 1 : oneStrokeClazzCategory.get(clazz)+1);
					// 次のリンクを辿る準備.
					linkDataSet.nextLink = startNodeNextLinkDataSet.nextLink;
					linkDataSet.nextPoint = startNodeNextLinkDataSet.nextPoint;
					linkDataSet.nextLinkId = startNodeNextLinkDataSet.nextLinkId;
					linkDataSet.clazz = startNodeNextLinkDataSet.clazz;
				}else{	// 違う場合.
//					System.out.println("not defference");
					break;
				}
			}else{
				break;
			}
		}
		
		
		// まだ残っているリンクであるか.
		_oneStroke = new ArrayList<>();
		_oneStrokeSegment = new ArrayList<>();
		_oneStrokeLinkId = new ArrayList<>();
		_oneStrokeClazz = -1;
		_oneStrokeIdSeriesString =  "";
		for(int i=0; i<oneStrokeId.size(); i++){
//			for(int j=0; j<aAllLinkHashMap.size(); j++){
				//if(oneStrokeId.get(i).intValue() == aAllLinkId.get(j).intValue()){
				if(aAllLinkHashMap.containsKey(oneStrokeId.get(i).intValue())){
					_oneStroke.add(oneStroke.get(i));
//					System.out.println("hashSize "+aAllLinkHashMap.size());
//					if(oneStrokeId.get(i).intValue() == 1638053){
//						System.out.println("%%%%%%found 1638053%%%%%%%%%");
//					}
					_oneStrokeSegment.add(oneStrokeSegment.get(i));
					_oneStrokeLinkId.add(oneStrokeId.get(i));
					_oneStrokeIdSeriesString += ""+ oneStrokeId.get(i) + ",";
				}
//			}
		}
		_oneStrokeIdSeriesString = _oneStrokeIdSeriesString.substring(0, _oneStrokeIdSeriesString.length()-1);	// 最後の1文字削除.
		_oneStrokeClazz = clazz;

		// リンクの道路クラスの多数派がストロークの道路クラスとする.
		
		
		
//		// データベース登録のための正式登録.
//		if(_oneStrokeLinkId.size() > 0){
//			_strokeGeomArrayList.add(_oneStroke);
//			_strokeSegmentArrayList.add(_oneStrokeSegment);
//			_strokeIdArrayList.add(_oneStrokeLinkId);
//		}
		
		
		
		
	}
	
	/**
	 * リンクを1つ指定して道なりに続く次のリンクを求める(distNodeと接続する次のリンクを求める)
	 * @param aLinkGeom 指定するリンク(有向辺(startNode, distNode))
	 * 
	 * 求める値　nextLink(道なりに続く次のリンク), nextPoint(次のendPoint), nextLinkId(次のリンクID)
	 */
	private LinkDataSet getNextLink(Line2D aLinkGeom, int aSourceLinkId, int aClazz){
//		System.out.println("getNextLink開始");
//		System.out.println("getNextLinkの引数 nextLink["+aLinkGeom.getP1()+" "+aLinkGeom.getP2()+"] nextPoint "+aEndPoint);
		String statement = "";
		// 指定したリンクと接するリンク.
		ArrayList<String> nearLinkStrings = new ArrayList<>();	// WKT形式の文字列.
		ArrayList<Line2D> nearLinkLine2d = new ArrayList<>();	// Line2D型.
		ArrayList<Integer> nearLinkId = new ArrayList<>();		// リンクID.
		ArrayList<Integer> nearLinkClazz = new ArrayList<>();	// 道路クラス.
		// 指定したリンクと接するセグメント(単体).
//		ArrayList<Line2D> nearSegmentEdge2d = new ArrayList<>();// 指定したリンクと接触する側のsegment.
		// 指定したリンクと接するセグメント(セグメント集合).
		ArrayList<ArrayList<Point2D.Double>> nearSegLinePoint2d = new ArrayList<>();	// point2d版.

		try{
			///////////////////////////////////////////////////////
			// あるpoint(endPoint)に接触するlinkを求める(自分自身を含まない).
			///////////////////////////////////////////////////////
			statement = "select " +
						" link_id, link_line, seg_line, st_asText(seg_line) as seg_line_text, link_class_id" +
					" from "+TEMP_TBNAME+" " +
					" where " +
						" st_touches(st_endPoint("+GeometryTemplete.lineString(aLinkGeom, GeometryTemplete.SRID_wd)+"), link_line) "+
							" and not(st_equals("+GeometryTemplete.lineString(aLinkGeom, GeometryTemplete.SRID_wd)+", link_line))" +
							"";
			ResultSet rs = execute(statement);
			
			while(rs.next()){
				if(rs.getInt("link_id") == aSourceLinkId){	// 自分自身のリンクを飛ばす.
					continue;
				}
				ArrayList<Line2D> segLine2d = new ArrayList<>();	// 道なりになるリンクのセグメント(multiline).
				nearLinkStrings.add(rs.getString("seg_line_text"));
				segLine2d = GeometryTemplete.getLineStringDataMulti((PGgeometry)rs.getObject("seg_line"));
				nearSegLinePoint2d.add(GeometryTemplete.getLineStringDataMultiPoint((PGgeometry)rs.getObject("seg_line")));
				nearLinkId.add(rs.getInt("link_id"));
				nearLinkClazz.add(rs.getInt("link_class_id"));
				nearLinkLine2d.add(GeometryTemplete.getLineStringData((PGgeometry)rs.getObject("link_line")));
			}
			
			
			///////////////////////////////////////////////////////
			// 次のリンクがないならそこで終わり.
			///////////////////////////////////////////////////////
			if(nearLinkId.size() == 0){	// 次がないなら終わり.
//				System.out.println("ここで行き止まりのリンク");
				return new LinkDataSet(null, null, -1, null, -1);
			///////////////////////////////////////////////////////
			// 1つだけで同じ道路クラスならそれが次のリンク.
			///////////////////////////////////////////////////////
			}else if(nearLinkStrings.size() == 1 && aClazz == nearLinkClazz.get(0)){	// 1つだけならそれが次のリンク.
				Line2D nextLink;
				Point2D.Double nextPoint;
				int nextLinkId;
				int nextClazz;
				if(nearLinkLine2d.get(0).getP1().getX() == aLinkGeom.getP2().getX() &&
						nearLinkLine2d.get(0).getP1().getY() == aLinkGeom.getP2().getY()){	// 向きを変えない.
					nextLink = new Line2D.Double(nearLinkLine2d.get(0).getP1(), nearLinkLine2d.get(0).getP2());
					nextPoint = (Point2D.Double)nextLink.getP2();
				}else{	// 向きかえる.
					nextLink = new Line2D.Double(nearLinkLine2d.get(0).getP2(), nearLinkLine2d.get(0).getP1());
					nextPoint = (Point2D.Double)nextLink.getP2();
					reverseArray(nearSegLinePoint2d.get(0));
				}
				nextLinkId = nearLinkId.get(0);
				nextClazz = nearLinkClazz.get(0);
				return new LinkDataSet(nextLink, nextPoint, nextLinkId, nearSegLinePoint2d.get(0), nextClazz);
			///////////////////////////////////////////////////////
			// 1つだけで同じ道路クラスでなければそこで終わり
			///////////////////////////////////////////////////////
			}else if(nearLinkStrings.size() == 1 && aClazz != nearLinkClazz.get(0)){
				return new LinkDataSet(null, null, -1, null, -1);
			}
			
			
			///////////////////////////////////////////////////////
			// 2つ以上の時、どれが次のリンクか調べる.
			// 各リンクとのなす角を求める.
			// なす角がSTROKE_ANGLE以下のリンクが2つ以上の時，道路クラスを調べる.
			///////////////////////////////////////////////////////
			ArrayList<Double> nearLinkAngle = new ArrayList<>();	// 隣接するリンクの角度.
			int countLowAngle = 0;	// なす角が一定以下のリンクの数.
			
			double minAngle = 999;	// 最も小さくなるなす角.
			Line2D minAngleLinkGeom = new Line2D.Double();	// その時のリンクのジオメトリ.
			int minAngleLinkId = -1;	// その時のリンクID.
			int minAngleLinkClazz = -1;	// 道路クラス.
			ArrayList<Point2D.Double> minAngleSegmentGeom = new ArrayList<>();	// segment. 
			for(int i=0; i<nearLinkId.size(); i++){
				// リンクnearLinkの有向辺.
				Point2D.Double tmpStartPoint = (Point2D.Double)nearLinkLine2d.get(i).getP1();
				Point2D.Double tmpDistPoint  = (Point2D.Double)nearLinkLine2d.get(i).getP2();
				if(tmpDistPoint.getX() == aLinkGeom.getP2().getX() &&
						tmpDistPoint.getY() == aLinkGeom.getP2().getY()){// 向きを変えるかどうか.
					nearLinkLine2d.set(i, new Line2D.Double(tmpDistPoint, tmpStartPoint));
					reverseArray(nearSegLinePoint2d.get(i));
					//System.out.println("$$$swapしてる$$$$$$$$$$$");
				}
				// 2つのリンクの方位角をそれぞれ求める.(segmentを使う)
				statement = "select " +
						" st_azimuth("+GeometryTemplete.point2dString((Point2D.Double)aLinkGeom.getP1(), GeometryTemplete.SRID_wd)+"," +
								" "+GeometryTemplete.point2dString((Point2D.Double)aLinkGeom.getP2(), GeometryTemplete.SRID_wd)+") as startAzimuth, " +
						" st_azimuth("+GeometryTemplete.point2dString((Point2D.Double)nearLinkLine2d.get(i).getP1(), GeometryTemplete.SRID_wd)+"," +
								" "+GeometryTemplete.point2dString((Point2D.Double)nearLinkLine2d.get(i).getP2(), GeometryTemplete.SRID_wd)+") as distAzimuth";
//				System.out.println("start "+aLinkSegmentGeom.getP1() + " " +aLinkSegmentGeom.getP2());
//				System.out.println("dist "+nearSegmentEdge2d.get(i).getP1() + " " + nearSegmentEdge2d.get(i).getP2());
				rs = execute(statement);
				double angle = 0;	// なす角.
				if(rs.next()){
					// 180度以上だったら360度で引く.
					angle = (angle = Math.abs(rs.getDouble("startAzimuth") - rs.getDouble("distAzimuth"))) > (Math.PI) ? 2*Math.PI - angle : angle ;
					nearLinkAngle.add(angle);
					if(angle < STROKE_ANGLE){
						countLowAngle++;
					}
//					System.out.println("リンクID"+nearLinkId.get(i));
//					System.out.println("angle "+Math.toDegrees(angle) + " startAzimuth "+Math.toDegrees(rs.getDouble("startAzimuth")) + " distAzimuth " +Math.toDegrees(rs.getDouble("distAzimuth")));
					
				}
			}
			
			
			// 道路クラスが同じで，なす角が小さいリンクを見つける.
			minAngle = 9999;
			for(int i=0; i<nearLinkStrings.size(); i++){
				if(nearLinkAngle.get(i) < STROKE_ANGLE  &&  aClazz == nearLinkClazz.get(i).intValue()){	// 道なりで同じ同クラスか.
					if(nearLinkAngle.get(i) < minAngle){
						// 道なりリンクの更新.
						minAngle = nearLinkAngle.get(i);
						minAngleLinkGeom = nearLinkLine2d.get(i);
						minAngleLinkId = nearLinkId.get(i);
						minAngleLinkClazz = nearLinkClazz.get(i);
						minAngleSegmentGeom = nearSegLinePoint2d.get(i);
					}
				}
			}
//			if(minAngle == 9999){	// angleの更新がなければ同じ道路クラスがなかった.
//				// 道路クラス関係なくなす角が小さいリンクを見つける.
//				for(int i=0; i<nearLinkStrings.size(); i++){
//					if(nearLinkAngle.get(i) < minAngle){	// なす角がより小さくなるか.
//						// 道なりリンクの更新.
//						minAngle = nearLinkAngle.get(i);
//						minAngleLinkGeom = nearLinkLine2d.get(i);
//						minAngleLinkId = nearLinkId.get(i);
//						minAngleLinkClazz = nearLinkClazz.get(i);
//						minAngleSegmentGeom = nearSegLinePoint2d.get(i);
//					}
//				}
//			}
			
			if(minAngle < STROKE_ANGLE){	// 道なりに続く道路がある時、それが次のリンク.
				Line2D nextLink = minAngleLinkGeom;
				Point2D.Double nextPoint = (Point2D.Double)minAngleLinkGeom.getP2();
				int nextLinkId = minAngleLinkId;
				int nextLinkClazz = minAngleLinkClazz;
				ArrayList<Point2D.Double> nextSegment = minAngleSegmentGeom;
				
//				System.out.println("2つ以上のリンクがあって，どちらかが道なりだった");
//				System.out.println("nextLink ["+nextLink.getP1()+" "+nextLink.getP2()+"] nextPoint "+nextPoint+" nextLinkId "+minAngleLinkId);
//				System.out.println();
				return new LinkDataSet(nextLink, nextPoint, nextLinkId, nextSegment, minAngleLinkClazz);
			}else{	// そこで行き止まり.
//				System.out.println("2つ以上のリンクがあったが行き止まり");
				return new LinkDataSet(null, null, -1, null, -1);
			}			
			
		}catch(Exception e){
			e.printStackTrace();
		}
//		System.out.println("------errorの可能性あり--------");
		return new LinkDataSet(null, null, -1, null, -1);
	}
	
	/**
	 * 
	 * @return
	 */
	private ArrayList<Point2D.Double> reverseArray(ArrayList<Point2D.Double> arr){
		ArrayList<Point2D.Double> tmp = new ArrayList<>();
		for(int i=0; i<arr.size(); i++){
			tmp.add(arr.get(arr.size()-i-1));
		}
		return tmp;
	}
	
	

}
