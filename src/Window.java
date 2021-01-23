import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// TODO: #1 Show time elapse (use Thread, etc...)
// TODO: #2 Make some effect (if it possible, but it's not important to do)

@SuppressWarnings("serial")
public class Window extends JFrame {
	public static final int TILE_SIZE = 50;

	private int selectedColumn = -1;
	private int selectedRow = -1;
	
	public Window() {
		super();
		
		final Container container = new MinesweeperUI();
		container.setLayout(null);
		container.setPreferredSize(new Dimension(Controller.TILE_COLUMN*TILE_SIZE, Controller.TILE_ROW*TILE_SIZE));
		container.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent event) {
				selectedColumn = event.getX() / TILE_SIZE;
				selectedRow = event.getY() / TILE_SIZE;
				
				container.repaint();
				
				// System.out.println("Mouse pressed");
			}
			
			@Override
			public void mouseReleased(MouseEvent event) {
				int column = event.getX() / TILE_SIZE;
				int row = event.getY() / TILE_SIZE;

				if (row == selectedRow && column == selectedColumn) {
					try {
						Controller.Tile tile = Controller.getTile(row, column);
	
						if (event.getButton() == MouseEvent.BUTTON1) {
							Controller.openTile(tile);
						} else if (event.getButton() == MouseEvent.BUTTON3) {
							Controller.markTile(tile);
						}
					} catch(IllegalTilePositionException e) { e.printStackTrace(); }
				}
				
				container.repaint();
				
				selectedColumn = -1;
				selectedRow = -1;

				// System.out.println("Mouse released");
			}
		});
		
		setContentPane(container);
		
		pack();

		Controller.setActionListener(new Controller.ActionListener() {
			@Override
			public void onInitialize() {
				setTitle("Minesweeper");
			}
			@Override
			public void onGameStart() {
				setTitle("Minesweeper / Mines left: "+Controller.MINE_AMOUNT);
			}
			@Override
			public void onOpen(Controller.Tile selectedTile) {
				
			}
			@Override
			public void onMark(int markedTileCount) {
				setTitle("Minesweeper / Mines left: "+(Controller.MINE_AMOUNT-markedTileCount));
			}
			@Override
			public void onGameSet(boolean win) {
				setTitle("Minesweeper / "+(win ? "WIN! :)" : "LOSE... :("));
			}
		});
		

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_SPACE) {
					Controller.initialize();
					
					container.repaint();
				} else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
					System.exit(0);
				}
			}
		});
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		setResizable(false);
		setVisible(true);
	}
	
	private boolean isMousePressed() { return selectedRow >= 0 && selectedColumn >= 0; }
	
	private class MinesweeperUI extends Container {
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			g.setFont(g.getFont().deriveFont(Font.BOLD));
			g.setFont(g.getFont().deriveFont(25f));
			for (int row = 0; row < Controller.TILE_ROW; row++) {
				for (int column = 0; column < Controller.TILE_COLUMN; column++) {
					try {
						Controller.Tile tile = Controller.getTile(row, column);
						
						int tileX = column*TILE_SIZE;
						int tileY = row*TILE_SIZE;

						if (tile.isOpened()) {
							g.setColor(Color.WHITE);
							g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);

							String tileString = "";
							if (tile.isMine()) {
								tileString = "+";
								g.setColor(Color.BLACK);
							} else if (Controller.getNearMineAmount(tile) > 0) {
								tileString = Integer.toString(Controller.getNearMineAmount(tile));
								switch (Controller.getNearMineAmount(tile)) {
								case 1: g.setColor(Color.BLUE); break;
								case 2: g.setColor(Color.GREEN); break;
								case 3: g.setColor(Color.ORANGE); break;
								case 4: g.setColor(Color.CYAN); break;
								case 5: g.setColor(Color.RED); break;
								case 6: g.setColor(Color.YELLOW); break;
								case 7: g.setColor(Color.GRAY); break;
								case 8: g.setColor(Color.BLACK); break;
								}
							}

							drawString(g, tileString, tileX, tileY);
						} else {
							if (isMousePressed()) {
								// make value about Controller.getTile(...)... (simplization)
								if (!tile.isOpened() && column == selectedColumn && row == selectedRow) {
									g.setColor(Color.GRAY);
									g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
								} else if (Controller.getTile(selectedRow, selectedColumn).isOpened() && Controller.getNearTiles(Controller.getTile(selectedRow, selectedColumn)).contains(Controller.getTile(row, column)) && !Controller.getTile(row, column).isMarked()) {
									g.setColor(Color.GRAY);
									g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
								} else {
									g.setColor(Color.LIGHT_GRAY);
									g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
								}
							} else {
								g.setColor(Color.LIGHT_GRAY);
								g.fillRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
							}
							
							if (tile.isMarked()) {
								g.setColor(Color.RED);
								drawString(g, ">", tileX, tileY);
							}
						}
						
						g.setColor(Color.GRAY);
						g.drawRect(tileX, tileY, TILE_SIZE, TILE_SIZE);
					} catch (IllegalTilePositionException e) { e.printStackTrace(); }
				}
			}
		}
		
		public void drawString(Graphics g, String text, int x, int y) {
			// Create new font
			Font font = new Font("Arial", Font.BOLD, 32);
			// Get the FontMetrics
		    FontMetrics metrics = g.getFontMetrics(font);
		    // Determine the X coordinate for the text
		    int stringX = x + (TILE_SIZE - metrics.stringWidth(text)) / 2;
		    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		    int stringY = y + ((TILE_SIZE - metrics.getHeight()) / 2) + metrics.getAscent();
		    // Set the font
		    g.setFont(font);
		    // Draw the String
		    g.drawString(text, stringX, stringY);
		}
	}
}
