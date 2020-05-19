import javax.naming.InitialContext;

public class GameBoard {
	private int[][] grid, move_grid; 						// the grid that stores the pieces
	private Mouse mouse;
    private static final int INIT_WALLS = 10; //3
	private static final int MOVE_WEIGHT = 3;

	public GameBoard(int width, int height) {
		grid = new int[height][width];
		move_grid = new int[height][width];
		mouse = new Mouse(height/2, width/2);

		// Initialize starting positions
		grid[ mouse.getRow() ][ mouse.getCol() ] = 1;

        initMoveGrid( MOVE_WEIGHT );
		initWalls( INIT_WALLS );
	}

	private void initMoveGrid( int weight ) {
		for (int r = 1; r <= move_grid.length / 2; r++) {
			for (int c = 1; c <= move_grid[0].length / 2; c++) {
				if (r == 1 || c == 1) {
					replicateQuarters(r, c, weight);

				} else {
					replicateQuarters(r, c, findMinValue(r, c) + weight);
				}
			}
		}
		displayGrid( move_grid );
	}

	private void displayGrid(int[][] grid) {
		System.out.println("[DEBUG] grid :");
		for (int r = 0; r < grid.length; r++) {
			for (int c = 0; c < grid[0].length; c++) {
				System.out.print(grid[r][c] + "\t");
			}
			System.out.println();
		}
	}

	private void replicateQuarters(int r, int c, int val) {
		int other_row = move_grid.length-1 - r;
		int other_col = move_grid[0].length-1 - c;

		move_grid[r][c] = val;
		move_grid[r][other_col] = val;
		move_grid[other_row][c] = val;
		move_grid[other_row][other_col] = val;
	}

	private int findMinValue(int row, int col) {
		int min = Integer.MAX_VALUE;
		for (int r = row-1; r <= row+1; r++) {
			for (int c = col-1; c <= col+1 ; c++) {
				// skip non hexagonal moves
				if ( r != row ) {
					if ( row%2 == 0 && c == col+1 || row%2 == 1 && c == col-1) {
						continue;
					}
				}

				if ( isInGrid(r, c) ) {
					if ( move_grid[r][c] > 0 && move_grid[r][c] < min ){
						min = move_grid[r][c];
					}
				}
			}
		}
		return min;
	}

	private int[] findDirection(int row, int col) {
		int min = Integer.MAX_VALUE;
		int[] min_coords = new int[2];
		for (int r = row-1; r <= row+1; r++) {
			for (int c = col-1; c <= col+1 ; c++) {
				// skip non hexagonal moves
				if ( r != row ) {
					if ( row%2 == 0 && c == col+1 || row%2 == 1 && c == col-1) {
						continue;
					}
				}

				if ( isInGrid(r, c) && grid[r][c] == 0) {
					if ( move_grid[r][c] < min ){
						min = move_grid[r][c];
						min_coords[0] = r;
						min_coords[1] = c;
					}
				}
			}
		}
		return min_coords;
	}

	private void initWalls(int num) {
		int row=0, col=0;
		for (int i = 0; i < num; i++) {
			do {
				row = (int)( 1 + Math.random() * (grid.length - 2) );
				col = (int)( 1 + Math.random() * (grid[0].length - 2)  );
			} while (grid[row][col] != 0);
			addWall(row, col);
		}
	}

	// Make the requested move at (row, col) by changing the grid.
	// returns false if no move was made, true if the move was successful.
	public boolean moveMouse(int dest_row, int dest_col) {
		if ( !isInGrid(dest_row, dest_col) || grid[dest_row][dest_col] != 0 ||
				Math.abs(dest_row - mouse.getRow()) > 1  || Math.abs(dest_col - mouse.getCol()) > 1) {
			return false;
		}

		// Check diagonal moves
		if ( dest_row != mouse.getRow()	) {
			if ( dest_col%2 == 0 && dest_col < mouse.getCol() ||
				 dest_col%2 == 1 && dest_col > mouse.getCol() ) {
				return false;
			}
		}

        grid_move( mouse.getRow(), mouse.getCol(), dest_row, dest_col );
        mouse.move(dest_row, dest_col);
		return true;
	}

	public boolean moveMouse() {
		int[] dest = findDirection( mouse.getRow(), mouse.getCol() );
		grid_move( mouse.getRow(), mouse.getCol(), dest[0], dest[1] );
		mouse.move(dest[0], dest[1]);
		return true;
	}

	public boolean addWall(int dest_row, int dest_col) {
		if ( !isInInsideGrid(dest_row, dest_col) || grid[dest_row][dest_col] != 0 ) {
			return false;
		}

		grid[ dest_row ][ dest_col ] = 4; // wall
		updateMoveGrid(dest_row, dest_col, 5);
		return true;
	}

	private void updateMoveGrid(int row, int col, int radius) {
		for (int r = 1; r < move_grid.length-1; r++) {
			for (int c = 1; c < move_grid[0].length-1; c++) {
				int dist =  distance(row, col, r, c);
				if (dist <= radius) {
					move_grid[r][c] += radius - dist + 1;
				}
			}
		}
		displayGrid( move_grid );
	}

	private int distance(int row, int col, int r, int c) {
		return (int)( Math.sqrt( Math.pow(row - r, 2) + Math.pow(col - c, 2) ) );
	}

	public void grid_move(int row, int col, int dest_row, int dest_col) {
		int piece = grid[row][col];
		grid[ dest_row ][ dest_col ] = piece;
		grid[ row ][ col ] = 0;
	}

	/*
	 * Return true if the game is over. False otherwise.
	 */
	public boolean isGameOver() {
		// TODO add detection of mouse winning
		if (mouse.getRow() == 0 || mouse.getRow() == grid.length-1 || mouse.getCol() == 0 || mouse.getCol() == grid[0].length-1) {
			grid[ mouse.getRow() ][ mouse.getCol() ] = 2; // cheese
			return true;
		}
		return false;
	}

	public int[][] getGrid() {
		return grid;
	}

	// Return true if the row and column in location loc are in bounds for the grid
	public boolean isInGrid(int row, int col) {
		return row >=0 && row < grid.length && col >= 0 && col < grid[0].length;
	}

	public boolean isInInsideGrid(int row, int col) {
		return row >0 && row < grid.length-1 && col > 0 && col < grid[0].length-1;
	}
}
