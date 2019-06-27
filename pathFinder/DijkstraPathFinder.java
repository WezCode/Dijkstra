package pathFinder;

import map.Coordinate;
import map.PathMap;

import java.util.*;

public class DijkstraPathFinder implements PathFinder {
	
	private PathMap pathMap;
	private List<Coordinate> unvisited;
	private List<Coordinate> visited;
	private int rows;
	private int cols;
	private int originX;
	private int originY;
	private int subCost;
	private Coordinate currentCoordinate;
	private int coordinatesVisited;

	public DijkstraPathFinder(PathMap map) {
		
		this.pathMap = map;
		this.rows = map.sizeR;
		this.cols = map.sizeC;
		
		this.unvisited = new ArrayList<Coordinate>();
		this.visited = new ArrayList<Coordinate>();
		
		initUnvisited();
		this.currentCoordinate = null;
		this.subCost = 0;
		this.coordinatesVisited = 0;
	}

	// Creates the initial set of unvisited cells
	public void initUnvisited() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (!pathMap.cells[r][c].getImpassable()) {
					unvisited.add(pathMap.cells[r][c]);
				}
			}
		}
	}

	@Override
	public List<Coordinate> findPath() {
		long start = System.nanoTime();
		List<Coordinate> minPath = new ArrayList<Coordinate>();
		
		int minCostOfWPPath = -1;
		int WPPathCost = 0;
		ArrayList<Coordinate> subPath = new ArrayList<Coordinate>();
		ArrayList<Coordinate> sumPath = new ArrayList<Coordinate>();

		ArrayList<ArrayList> combinations = new ArrayList<ArrayList>();
		combinations = this.createCombinations(this.pathMap.getWayPointCells());

		// For each wayPointPath in combinations superArray
		for (ArrayList list : combinations) {
			sumPath = new ArrayList<Coordinate>();
			WPPathCost = 0;
		
			//Calculate the sumPath for the entire wayPointPath as well as the cost
			for (int i = 0; i < list.size() - 1; i++) {
				// Find the cost of subPath and add to WPPathCosts
				// Calculate subPath from ith element to i+1 element
				subPath = findSubPath((Coordinate) list.get(i), (Coordinate) list.get(i + 1));
				// This can be way more efficient but it calculates the cost of
				WPPathCost += this.subCost;
				if (i == 0) {
					sumPath = initArrayList(subPath);
				} else {
					// Add the subPath to the sumPath
					sumPath = addArrayList(sumPath, subPath);
				}
			}
			// If there has been no previous sumPaths, set minCostOfWPPath and minPath to this one
			if (minCostOfWPPath == -1) {
				minCostOfWPPath = WPPathCost;
				minPath = sumPath;
			} else {
				// If the new WPPathCost is smaller, set the new values for 
				// minCostOfWPPath and minPath
				if (WPPathCost < minCostOfWPPath) {
					minCostOfWPPath = WPPathCost;
					minPath = sumPath;
				}
			}
		}
		long end = System.nanoTime();
		//System.out.println("Time in milliseconds: " + (end-start)/1000000);
		this.coordinatesVisited = minPath.size();
		return minPath;
	}

	//Returns the pathFrom an origin coordinate to a destination coordinate
	public ArrayList<Coordinate> findSubPath(Coordinate origin, Coordinate dest) {
		this.originX = origin.getColumn();
		this.originY = origin.getRow();
		ArrayList<Coordinate> path = new ArrayList<Coordinate>();

		// Reset Paths
		if (unvisited.size() == 0) {
			this.initUnvisited();
			this.resetCoordinatePathsAndCost();
		}

		this.currentCoordinate = this.pathMap.cells[origin.getRow()][origin.getColumn()];
		this.pathMap.cells[origin.getRow()][origin.getColumn()].setPathFromOrigin(path, currentCoordinate);

		this.checkCurrentCoordinate(currentCoordinate);
		this.loopVisited();

		if (this.unvisited.isEmpty()) {
			path = this.pathMap.cells[dest.getRow()][dest.getColumn()].getPathFromOrigin();
			this.subCost = this.pathMap.cells[dest.getRow()][dest.getColumn()].getCostFromOrigin(); 
		}
		return path;
	}

	/*Generates all the combinations of paths between an origin cell, n amount 
	 * of waypoints and the destination cells
	 */
	public ArrayList<ArrayList> createCombinations(List<Coordinate> wayPointCells) {
		ArrayList<ArrayList> wayPointCombinations = new ArrayList<ArrayList>();	
		//Loop through each origin cell and each dest cell
		for (int a = 0; a < this.pathMap.getOriginCells().size(); a++) {
			for (int b = 0; b < this.pathMap.getDestCells().size(); b++) {
				ArrayList<Coordinate> wayPointArray = new ArrayList<Coordinate>();

				// Copy elements in wayPointCells to wayPointArray
				wayPointArray.add(this.pathMap.getOriginCells().get(a));
				for (int t = 0; t < wayPointCells.size(); t++) {
					wayPointArray.add(t + 1, wayPointCells.get(t));
				}

				wayPointArray.add(this.pathMap.getDestCells().get(b));

				if (wayPointArray.size() > 3) {
					Coordinate temp = null;
					for (int j = 1; j <= wayPointArray.size() - 2; j++) {
						for (int i = 1; i < wayPointArray.size() - 2; i++) {
							temp = wayPointArray.get(i);
							wayPointArray.set(i, wayPointArray.get(i + 1));
							wayPointArray.set(i + 1, temp);

							ArrayList<Coordinate> wayPointArrayTemp = new ArrayList<Coordinate>();

							for (int k = 0; k < wayPointArray.size(); k++) {
								wayPointArrayTemp.add(wayPointArray.get(k));
							}
							wayPointCombinations.add(wayPointArrayTemp);
						}
					}
				} else {
					wayPointCombinations.add(wayPointArray);
				}
			}
		}
		return wayPointCombinations;
	}

	public Coordinate getCoordinate(List<Coordinate> list, int i) {
		int x = list.get(i).getColumn();
		int y = list.get(i).getRow();
		return this.pathMap.cells[y][x];
	}

	public void checkCurrentCoordinate(Coordinate c) {
		this.currentCoordinate = c;
		int x = c.getColumn();
		int y = c.getRow();

		// Update neighbour cost and path of current coordinate
		this.updateNeighbours(x, y);

		// set current coordinate to visited
		if (this.inUnvisited(c)) {
			this.unvisited.remove(this.getIndexOfCoordinate(c));
			this.visited.add(c);
		}
	}

	// Find new currentCoordinate
	public void loopVisited() {
		
		//Iterator to loop through visited set
		Iterator<Coordinate> visitedIter = visited.iterator();
		Coordinate testCoordinate = null;
		while (visitedIter.hasNext()) {
			testCoordinate = visitedIter.next();

			int x = testCoordinate.getColumn();
			int y = testCoordinate.getRow();
			visitedIter.remove();

			List<Coordinate> neighbours = this.getNeighbours(x, y);

			// If any neighbour is unvisited, make that our current coordinate
			for (Coordinate neighbour : neighbours) {
				if (this.inUnvisited(neighbour)) {
					this.checkCurrentCoordinate(neighbour);
					this.loopVisited();
				}
			}
		}
	}

	//Returns indexOfCoordinate in unvisited set
	public int getIndexOfCoordinate(Coordinate coordinate) {
		int index = 0;
		for (int i = 0; i < unvisited.size(); i++) {
			if (unvisited.get(i).getColumn() == coordinate.getColumn()
					&& unvisited.get(i).getRow() == coordinate.getRow()) {
				index = i;
				// Early termination
				i = unvisited.size();
			}
		}
		return index;
	}

	//Updates the currentCoordinates' neighbours' costFromOrigin and pathFromOrigin
	public void updateNeighbours(int x, int y) {
		boolean leftCheck = true;
		boolean rightCheck = true;
		boolean upCheck = true;
		boolean downCheck = true;

		//Check if there is a valid neighbour for each direction
		if (x - 1 == this.originX && y == this.originY) {
			leftCheck = false;
		}
		if (x + 1 == this.originX && y == this.originY) {
			rightCheck = false;
		}
		if (x == this.originX && y + 1 == this.originY) {
			upCheck = false;
		}
		if (x == this.originX && y - 1 == this.originY) {
			downCheck = false;
		}

		int newCost = 0;
		ArrayList<Coordinate> path = this.pathMap.cells[y][x].getPathFromOrigin();
		if (leftCheck) {
			//Check bounds
			if (x - 1 >= 0) {
				if (!this.pathMap.cells[y][x - 1].getImpassable()) {
					newCost = this.pathMap.cells[y][x].getCostFromOrigin()
							+ this.pathMap.cells[y][x - 1].getTerrainCost();
					boolean b = false;
					if (newCost < this.pathMap.cells[y][x - 1].getCostFromOrigin()) {
						b = true;
					}
					if (b || this.pathMap.cells[y][x - 1].getCostFromOrigin() == 0) {
						this.pathMap.cells[y][x - 1].setCostFromOrigin(newCost);
						this.pathMap.cells[y][x - 1].setPathFromOrigin(path, this.pathMap.cells[y][x - 1]);
						if (b) {
							this.updateNeighbours(x - 1, y);
						}
					}
				}
			}
		}
		if (rightCheck) {
			if (x + 1 < this.cols) {
				if (!this.pathMap.cells[y][x + 1].getImpassable()) {
					newCost = this.pathMap.cells[y][x].getCostFromOrigin()
							+ this.pathMap.cells[y][x + 1].getTerrainCost();
					boolean b = false;
					if (newCost < this.pathMap.cells[y][x + 1].getCostFromOrigin()) {
						b = true;
					}
					if (newCost < this.pathMap.cells[y][x + 1].getCostFromOrigin()
							|| this.pathMap.cells[y][x + 1].getCostFromOrigin() == 0) {
						this.pathMap.cells[y][x + 1].setCostFromOrigin(newCost);
						this.pathMap.cells[y][x + 1].setPathFromOrigin(path, this.pathMap.cells[y][x + 1]);
						if (b) {
							this.updateNeighbours(x + 1, y);
						}
					}
				}
			}
		}
		if (upCheck) {
			if (y + 1 < this.rows) {
				if (!this.pathMap.cells[y + 1][x].getImpassable()) {
					newCost = this.pathMap.cells[y][x].getCostFromOrigin()
							+ this.pathMap.cells[y + 1][x].getTerrainCost();
					boolean b = false;
					if (newCost < this.pathMap.cells[y + 1][x].getCostFromOrigin()) {
						b = true;
					}
					if (newCost < this.pathMap.cells[y + 1][x].getCostFromOrigin()
							|| this.pathMap.cells[y + 1][x].getCostFromOrigin() == 0) {
						this.pathMap.cells[y + 1][x].setCostFromOrigin(newCost);
						this.pathMap.cells[y + 1][x].setPathFromOrigin(path, this.pathMap.cells[y + 1][x]);
						if (b) {
							this.updateNeighbours(x, y + 1);
						}
					}
				}
			}
		}
		if (downCheck) {
			if (y - 1 >= 0) {
				if (!this.pathMap.cells[y - 1][x].getImpassable()) {
					newCost = this.pathMap.cells[y][x].getCostFromOrigin()
							+ this.pathMap.cells[y - 1][x].getTerrainCost();
					boolean b = false;
					if (newCost < this.pathMap.cells[y - 1][x].getCostFromOrigin()) {
						b = true;
					}
					if (newCost < this.pathMap.cells[y - 1][x].getCostFromOrigin()
							|| this.pathMap.cells[y - 1][x].getCostFromOrigin() == 0) {
						this.pathMap.cells[y - 1][x].setCostFromOrigin(newCost);
						this.pathMap.cells[y - 1][x].setPathFromOrigin(path, this.pathMap.cells[y - 1][x]);
						if (b) {
							this.updateNeighbours(x, y - 1);
						}
					}
				}
			}
		}
	}

	//Checks if unvisited set contains a specific coordinate
	public boolean inUnvisited(Coordinate coordinate) {
		boolean retVal = false;
		for (int i = 0; i < unvisited.size(); i++) {
			if (unvisited.get(i).getColumn() == coordinate.getColumn()
					&& unvisited.get(i).getRow() == coordinate.getRow()) {
				retVal = true;
			}
		}
		return retVal;
	}

	//Returns a list of all the neighbours
	public List<Coordinate> getNeighbours(int x, int y) {
		List<Coordinate> neighbours = new ArrayList<Coordinate>();
		// add left neighbour
		if (x - 1 >= 0) {
			Coordinate leftNeighbour = new Coordinate(y, x - 1);
			if (!leftNeighbour.getImpassable()) {
				neighbours.add(leftNeighbour);
			}
		}
		// add right neighbour
		if (x + 1 < this.cols) {
			Coordinate rightNeighbour = new Coordinate(y, x + 1);
			if (!rightNeighbour.getImpassable()) {
				neighbours.add(rightNeighbour);
			}
		}
		// add down neighbour
		if (y - 1 >= 0) {
			Coordinate downNeighbour = new Coordinate(y - 1, x);
			if (!downNeighbour.getImpassable()) {
				neighbours.add(downNeighbour);
			}
		}
		// add up neighbour
		if (y + 1 < this.rows) {
			Coordinate upNeighbour = new Coordinate(y + 1, x);
			if (!upNeighbour.getImpassable()) {
				neighbours.add(upNeighbour);
			}
		}
		return neighbours;
	}

	@Override
	public int coordinatesExplored() {
		return this.coordinatesVisited;
	} // end of cellsExplored()

	//Returns a mergedArray, takes in two sub arrays
	public ArrayList<Coordinate> addArrayList(ArrayList<Coordinate> sumPath, ArrayList<Coordinate> subPath) {
		ArrayList<Coordinate> mergedArrayList = new ArrayList<Coordinate>();
		for (int i = 0; i < sumPath.size(); i++) {
			mergedArrayList.add(sumPath.get(i));
		}
		for (int j = 1; j < subPath.size(); j++) {
			mergedArrayList.add(subPath.get(j));
		}

		return mergedArrayList;
	}

	public ArrayList<Coordinate> initArrayList(ArrayList<Coordinate> path) {
		ArrayList<Coordinate> mergedArrayList = new ArrayList<Coordinate>();
		for (int i = 0; i < path.size(); i++) {
			mergedArrayList.add(path.get(i));
		}
		return mergedArrayList;
	}
	
	public void resetCoordinatePathsAndCost() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (!pathMap.cells[r][c].getImpassable()) {
					pathMap.cells[r][c].clearPathFromOrigin();
					pathMap.cells[r][c].setCostFromOrigin(0);
				}
			}
		}
	}
} 
