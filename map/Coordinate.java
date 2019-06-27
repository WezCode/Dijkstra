package map;

import java.util.*;

/**
 * @author Jeffrey Chan, Youhan Xia, Phuc Chu RMIT Algorithms & Analysis, 2019
 *         semester 1
 *         <p>
 *         Class representing a coordinate.
 */
public class Coordinate {
	/**
	 * row
	 */
	protected int r;

	/**
	 * column
	 */
	protected int c;

	/**
	 * Whether coordinate is impassable or not.
	 */
	protected boolean isImpassable;

	/**
	 * Terrain cost.
	 */
	protected int terrainCost;

	protected int costFromOrigin;

	protected ArrayList<Coordinate> pathFromOrigin;

	/**
	 * Construct coordinate (r, c).
	 *
	 * @param r
	 *            Row coordinate
	 * @param c
	 *            Column coordinate
	 */
	public Coordinate(int r, int c) {
		this(r, c, false);
	} // end of Coordinate()

	/**
	 * Construct coordinate (r,c).
	 *
	 * @param r
	 *            Row coordinate
	 * @param c
	 *            Column coordinate
	 * @param b
	 *            Whether coordiante is impassable.
	 */
	public Coordinate(int r, int c, boolean b) {
		this.r = r;
		this.c = c;
		this.isImpassable = b;
		this.terrainCost = 1;
		this.costFromOrigin = 0;
		this.pathFromOrigin = new ArrayList<Coordinate>();
	} // end of Coordinate()

	/**
	 * Default constructor.
	 */
	public Coordinate() {
		this(0, 0);
	} // end of Coordinate()

	//
	// Getters and Setters
	//

	public int getRow() {
		return r;
	}

	public int getColumn() {
		return c;
	}

	public int getCostFromOrigin() {
		return this.costFromOrigin;
	}

	public void setCostFromOrigin(int cost) {
		this.costFromOrigin = cost;
	}

	public ArrayList<Coordinate> getPathFromOrigin() {
		return this.pathFromOrigin;
	}

	public void setPathFromOrigin(ArrayList<Coordinate> path, Coordinate c) {
		if (this.pathFromOrigin.size() > 1) {
			this.pathFromOrigin.clear();
		}

		//I think this copies each 
		Iterator<Coordinate> pathIter = path.iterator();
		Coordinate testCoordinate = null;
		while (pathIter.hasNext()) {
			testCoordinate = pathIter.next();
			this.pathFromOrigin.add(testCoordinate);
		}
		this.pathFromOrigin.add(c);
	}
	
	public void clearPathFromOrigin()
	{
		this.pathFromOrigin.clear();
	}

	public void setImpassable(boolean impassable) {
		isImpassable = impassable;
	}

	public boolean getImpassable() {
		return isImpassable;
	}

	public void setTerrainCost(int cost) {
		terrainCost = cost;
	}

	public int getTerrainCost() {
		return terrainCost;
	}

	//
	// Override equals(), hashCode() and toString()
	//

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		if (o == null || getClass() != o.getClass())
			return false;

		Coordinate coord = (Coordinate) o;
		return r == coord.getRow() && c == coord.getColumn();
	} // end of equals()

	@Override
	public int hashCode() {
		return Objects.hash(r, c);
	} // end of hashCode()

	@Override
	public String toString() {
		return "(" + r + "," + c + "), " + isImpassable + ", " + terrainCost;
	} // end of toString()
} // end of class Coordinate
