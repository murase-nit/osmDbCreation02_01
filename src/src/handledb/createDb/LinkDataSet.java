package src.handledb.createDb;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;


/**
 * 次のリンクを求めるために必要なデータ構造
 * @author murase
 *
 */
public class LinkDataSet {
	
	/** 次のリンク  (startNode, distNode)の有向辺*/
	public Line2D nextLink = null;
	/** 次のリンクと接する点 */
	public Point2D.Double nextPoint = null;
	/** 次のリンクID */
	public int nextLinkId = -1;
	/** segment */
	public ArrayList<Point2D.Double> nextSegment= null;
	/** リンクのクラス */
	public int clazz = -1;
	
//	/** 次のリンクの前のリンク (startNode, distNode)の有向辺*/
//	public Line2D nextPrevLink = null;
//	/** 次のリンクの前のリンクと接する点 */
//	public Point2D.Double nextPrevPoint = null;
//	/** 次のリンクの前のリンクID */
//	public int nextPrevLinkId=-1;
	
	
	
	
//	public LinkDataSet(Line2D aNextLink, Point2D.Double aNextPoint, int aNextLinkId){
//		nextLink = aNextLink;
//		nextPoint = aNextPoint;
//		nextLinkId = aNextLinkId;
//	}
	public LinkDataSet(Line2D aNextLink, Point2D.Double aNextPoint, int aNextLinkId, ArrayList<Point2D.Double> aNextSegment, int aClazz){
		nextLink = aNextLink;
		nextPoint = aNextPoint;
		nextLinkId = aNextLinkId;
		nextSegment = aNextSegment;
		clazz = aClazz;
	}
	
}
