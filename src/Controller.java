import java.util.*;

// TODO: #1 Separate controller class and make packages
// TODO: #2 Make ArrayList<Tile> tiles to Tile[][] tiles. it makes better performance! (very very little...)

public class Controller {	
	public static final int TILE_COLUMN = 30;
	public static final int TILE_ROW = 16;
	public static final int MINE_AMOUNT = 99;
	public static final int SAFE_TILE_AMOUNT = (int)Math.sqrt(TILE_COLUMN*TILE_ROW)*2;

	
	private static Window window;
	private static Set<Tile> tiles;

	private static int markedTileAmount;
	private static int mineTileAmount;
	
	private static long gameStartedTimeMillies;
	private static long gameSetTimeMillies;

	
	public static interface ActionListener {
		void onInitialize();
		void onGameStart();
		void onOpen(Tile selectedTile);
		void onMark(int markedTileAmount);
		void onGameSet(boolean win);
	} private static ActionListener listener;
	
	
	// Method about getting tile(s)
	
	public static Tile getTile(int row, int column) throws IllegalTilePositionException {
		for (Tile tile : tiles)
			if (tile.getRow() == row && tile.getColumn() == column)
				return tile;

		throw new IllegalTilePositionException("Can't find tile which position is ("+column+", "+row+")!");
	}
	
	public static Tile getRandomTile() {
		while (true) {
			int position = (int)(Math.random()*(TILE_ROW*TILE_COLUMN));
			int row = position/TILE_COLUMN;
			int column = position%TILE_COLUMN;
			
			try {
				return getTile(row, column);
			} catch (IllegalTilePositionException e) { e.printStackTrace(); }
		}
	}
	
	public static List<Tile> getNearTiles(Tile selectedTile) {
		if (selectedTile == null)
			throw new IllegalArgumentException("Argument #1(Tile selectedTile) of Controller.getNearTiles() must not null!");
		
		List<Tile> result = new ArrayList<Tile>();
		
		for (int row = selectedTile.getRow()-1; row <= selectedTile.getRow()+1; row++) {
			// prevent to search non-existing tiles(Tile which out of bound)
			if (row < 0 || row >= TILE_ROW) continue;
			for (int column = selectedTile.getColumn()-1; column <= selectedTile.getColumn()+1; column++) {
				// prevent to search non-existing tiles(Tile which out of bound)
				if (column < 0 || column >= TILE_COLUMN) continue;
				// exclude itself
				if (row == selectedTile.getRow() && column == selectedTile.getColumn()) continue;
				
				try {
					result.add(getTile(row, column));
				} catch (IllegalTilePositionException e) { e.printStackTrace(); }
			}
		}
		
		return result;
	}

	public static Set<Tile> getTiles() {
		return tiles;
	}
	
	
	// Method about near tile(s)
	
	public static int getNearMineAmount(Tile selectedTile) {
		if (selectedTile == null)
			throw new IllegalArgumentException("Argument #1(Tile selectedTile) of Controller.getNearMineAmount() must not null!");
		
		int count = 0;
		for (Tile tile : getNearTiles(selectedTile))
			if (tile.isMine()) count++;
		return count;
	}
	
	public static int getNearMarkAmount(Tile selectedTile) {
		if (selectedTile == null)
			throw new IllegalArgumentException("Argument #1(Tile selectedTile) of Controller.getNearMineAmount() must not null!");
		
		int count = 0;
		for (Tile tile : getNearTiles(selectedTile))
			if (tile.isMarked()) count++;
		return count;
	}
	
	
	// Method about tile actions

	public static void openTile(int row, int column) throws IllegalTilePositionException { openTile(getTile(row, column)); }
	public static void openTile(Tile selectedTile) {
		if (selectedTile == null)
			throw new IllegalArgumentException("Argument #1(Tile selectedTile) of Controller.openTile() must not null!");
		
		if (!isGameStarted()) gameStart(selectedTile);
		
		int nearMarked = getNearMarkAmount(selectedTile);
		int nearMine = getNearMineAmount(selectedTile);
		if (selectedTile.open()) {
			if (selectedTile.isMine() && !isGameSet()) {
				gameSet(false);
			} else if (nearMine == 0) {
				for (Tile tile : getNearTiles(selectedTile)) openTile(tile);
			}
			
			if (listener != null) listener.onOpen(selectedTile);
		} else if (nearMine > 0 && nearMarked > 0 && nearMarked == nearMine) {
			for (Tile tile : getNearTiles(selectedTile)) {
				if (!tile.isOpened() && !tile.isMarked()) openTile(tile);
			}
		}
	}

	public static void markTile(int row, int column) throws IllegalTilePositionException { markTile(getTile(row, column)); }
	public static void markTile(Tile selectedTile) {
		if (selectedTile == null)
			throw new IllegalArgumentException("Argument #1(Tile selectedTile) of Controller.markTile() must not null!");
		
		if (selectedTile.mark()) {
			if (selectedTile.isMarked()) {
				markedTileAmount++;
				if (selectedTile.isMine()) mineTileAmount--;
			} else {
				markedTileAmount--;
				if (selectedTile.isMine()) mineTileAmount++;
			}
			
			if (listener != null) listener.onMark(markedTileAmount);
			
			if (markedTileAmount == MINE_AMOUNT && mineTileAmount == 0 && !isGameSet()) gameSet(true);
		}
	}

	
	// Method about game control
	
	public static void initialize() {
		tiles = new HashSet<Tile>();
		for (int row = 0; row < TILE_ROW; row++)
			for (int column = 0; column < TILE_COLUMN; column++)
				tiles.add(new Tile(row, column));
		
		markedTileAmount = 0;
		mineTileAmount = 0;
		
		gameStartedTimeMillies = -1;
		gameSetTimeMillies = -1;
		
		if (window == null) window = new Window();
		
		if (listener != null) listener.onInitialize();
	}
	
	public static boolean isGameStarted() { return gameStartedTimeMillies > 0; }
	public static boolean isGameSet() { return gameSetTimeMillies > 0; }
	
	public static long gameStartedTimeMillies() { return gameStartedTimeMillies; }
	public static long gameSetTimeMillies() { return gameSetTimeMillies; }
	
	@SuppressWarnings("unused")
	public static void gameStart(Tile selectedTile) {
		if (selectedTile == null)
			throw new IllegalArgumentException("Argument #1(Tile selectedTile) of Controller.gameStart() must not null!");
		
		if (isGameStarted()) {
			System.err.println("Can't start game. Game already started.");
			
			return;
		}
		
		if (MINE_AMOUNT >= TILE_ROW*TILE_COLUMN-SAFE_TILE_AMOUNT) {
			System.err.println("Can't start game. MINE_AMOUNT is too many!");
			
			return;
		}
		
		List<Tile> safeTiles = new ArrayList<>();
		safeTiles.add(selectedTile);
		
		while(safeTiles.size() < SAFE_TILE_AMOUNT) {
			Collections.shuffle(safeTiles);
			
			int row = safeTiles.get(0).getRow();
			int column = safeTiles.get(0).getColumn();
			
			try {
				Tile tile = selectedTile;
				switch ((int)(Math.random()*4)) {
				case 0:
					if (row-1 >= 0) {
						tile = getTile(row-1, column);
						
						break;
					}
				case 1:
					if (row+1 < TILE_ROW) {
						tile = getTile(row+1, column);
						
						break;
					}
				case 2:
					if (column-1 >= 0) {
						tile = getTile(row, column-1);
						
						break;
					}
				case 3:
					if (column+1 < TILE_COLUMN) {
						tile = getTile(row, column+1);
						
						break;
					}
				default: // System.err.println("Some error occured when making safe tiles! Position of last safe tile is ("+column+", "+row+")");
				}
				
				if (!safeTiles.contains(tile)) safeTiles.add(tile);
			} catch (IllegalTilePositionException e) { e.printStackTrace(); }
		}
		
		while(mineTileAmount < MINE_AMOUNT) {
			Tile tile = getRandomTile();
			
			if (!tile.isMine() && !safeTiles.contains(tile)) {
				tile.setMine(true);
				
				mineTileAmount++;
			}
		}
		
		gameStartedTimeMillies = System.currentTimeMillis();
		
		if (listener != null) listener.onGameStart();
	}
	
	public static void gameSet(boolean win) {
		if (isGameSet()) {
			System.err.println("Can't set game. Game already set.");
			
			return;
		}
		
		gameSetTimeMillies = System.currentTimeMillis();
		
		for (Tile tile : getTiles()) tile.open(true);

		if (listener != null) listener.onGameSet(win);
	}

	public static void setActionListener(ActionListener listener) { Controller.listener = listener; }
	
	
	// Tile class
	
	public static class Tile {
		private int row, column;
		private boolean mine = false;
		
		private boolean opened = false;
		private boolean marked = false;
		
		
		private Tile() {
			this(-1, -1);
		}
		
		private Tile(int row, int column) {
			this.row = row;
			this.column = column;
		}
		
		
		public int getRow() { return row; }
		public int getColumn() { return column; }
		
		public boolean isMine() { return mine; }
		private void setMine(boolean mine) {
			this.mine = mine;
		}
		
		
		public boolean isOpened() { return opened; }
		private boolean open() { return open(false); }
		private boolean open(boolean isForced) {
			if (isOpened() || (!isForced && isMarked())) return false;

			opened = true;

			return true;
		}
		
		public boolean isMarked() { return marked; }
		private boolean mark() {
			if (isOpened()) return false;

			marked = !marked;

			return true;
		}
		
		
		@Override public int hashCode() { return toString().hashCode(); }
		@Override public String toString() { return "("+column+", "+row+")"; }
	}
}