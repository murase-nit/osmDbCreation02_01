package src.handledb.create_flattedStroke;

import java.awt.geom.Line2D;
import java.util.ArrayList;



/**
 * flattedStrokeの作成
 * @author Administrator
 *
 */
public class MigrationMain {

	/**
	 * @param args
	 */
	public MigrationMain() {
		
		
		AddFeatureCreateDb addFeatureCreateDb = new AddFeatureCreateDb();
		addFeatureCreateDb.startConnection();
		addFeatureCreateDb.createTable();
		
		GetStroke getStroke = new GetStroke();
		getStroke.startConnection();
		int dataNum = getStroke.getnum();
		
		for(int i=0+1; i<dataNum+1; i++){
			String oneStrokeString = getStroke.getStrokeString_i(i);
			ArrayList<ArrayList<Line2D>> flattedStrokeGeom  = getStroke.flattenStroke(oneStrokeString);
			ArrayList<Double> length = new ArrayList<>();
			for(int j=0; j<flattedStrokeGeom.size(); j++){
				length.add(getStroke.getLength(flattedStrokeGeom.get(j)));
			}
			for(int j=0; j<flattedStrokeGeom.size(); j++){
				addFeatureCreateDb.createStrokeTable(i, length.get(j), getStroke.clazz, flattedStrokeGeom.get(j));
			}
			if(i%1000 == 0){
				System.out.println(i+"/"+dataNum);
			}
		}
		
		
		addFeatureCreateDb.endConnection();
		getStroke.endConnection();
		System.out.println("finish");
		
	}

}
