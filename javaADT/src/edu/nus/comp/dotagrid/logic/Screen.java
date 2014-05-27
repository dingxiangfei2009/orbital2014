package edu.nus.comp.dotagrid.logic;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class Screen extends JPanel implements Runnable {

	Thread thread = new Thread(this);

	Frame frame;
	User user;
	
	WorldMap worldMap;
	WorldMapFile worldMapFile;
	

	private int fps = 0;

	// control the states of the game
	public int scene;

	public int hand = 0;
	public int handXPos = 0;
	public int handYPos = 0;
	
	public static final int ROW_NUMBER = 200;
	public static final int COLUMN_NUMBER = 200;
	
	public double gridWidth = 1.0;
	public double gridHeight = 1.0;
	
	public int gridRowNumberInScreen = 14;
	public int gridColNumberInScreen = 20;
	
	public boolean running = false;
	
	public int[][] map = new int[ROW_NUMBER][COLUMN_NUMBER];
	
	// default starting position: left bottom corner of the map
	public int currentGridXPos = 0;
	public int currentGridYPos = ROW_NUMBER - gridRowNumberInScreen - 1;
	
	// store terrain Images
	public Image[] terrain = new Image[100];

	// constructor
	public Screen(Frame frame) {
		this.frame = frame;

		this.frame.addKeyListener(new KeyHandler(this));
		this.frame.addMouseListener(new MouseHandler(this));
		this.frame.addMouseMotionListener(new MouseHandler(this));
		
		double gameFrameGridWidth = (frame.getWidth() - 2 * GameFrame.FRAME_BORDER_WIDTH) / GameFrame.GRID_COL_NUMBER_IN_SCREEN;
		double gameFrameGridHeight = (frame.getHeight() - 2 * GameFrame.FRAME_BORDER_HEIGHT) / GameFrame.GRID_ROW_NUMBER_IN_SCREEN;		
		gridWidth = (int) (GameFrame.FRAME_COL_NUMBER_OCCUPIED * gameFrameGridWidth / gridColNumberInScreen);
		gridHeight = (int) (GameFrame.FRAME_ROW_NUMBER_OCCUPIED * gameFrameGridHeight / gridRowNumberInScreen);
		
		thread.start();
	}

	public void paintComponent(Graphics g) {
		g.clearRect(0, 0, this.frame.getWidth(), this.frame.getHeight());

		if (scene == 0) {
			// load game
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, this.frame.getWidth(), this.frame.getHeight());

		} else if (scene == 1) {
			// start game!
			
			// draw game frame
			new GameFrame(g, this.frame);
			
			// game grid 
			g.setColor(Color.BLACK);
			
			for (int x=0; x<gridColNumberInScreen; x++) {
				for (int y=0; y<gridRowNumberInScreen; y++) {
					
					g.drawImage(terrain[map[x + currentGridXPos][y + currentGridYPos]],
							(int)(GameFrame.FRAME_BORDER_WIDTH + x * gridWidth),
							(int)(GameFrame.FRAME_BORDER_HEIGHT + y * gridHeight), (int) gridWidth,
							(int) gridHeight, null);
					
					g.drawRect((int)(GameFrame.FRAME_BORDER_WIDTH + x * gridWidth),
							(int)(GameFrame.FRAME_BORDER_HEIGHT + y * gridHeight), (int) gridWidth,
							(int) gridHeight);

				}
			}

		} else {
			g.setColor(Color.WHITE);
			g.fillRect(0, 0, this.frame.getWidth(), this.frame.getHeight());
		}

		// FPS at the bottom -> paint then clear the rectangle
		g.drawString(fps + "", 10, 10);

	} // end override method paintComponent

	// first time
	public void loadGame() {
		user = new User(this);
		worldMapFile = new WorldMapFile();
		
		for (int y = 0; y < 10; y++) {
			for (int x = 0; x < 10; x++) {
				int z = x + (y * 10);
				terrain[z] = new ImageIcon("res/WorldMap/terrian" + "/terrian" + z + ".png").getImage();
			}
		}

		running = true;
	}

	public void startGame(User user) {
		user.createPlayer();
		
		this.worldMap = worldMapFile.getWorldMap();
		this.map = this.worldMap.map;

		this.scene = 1; // enter game!
	}

	@Override
	public void run() {
		long lastFrame = System.currentTimeMillis();
		int frames = 0;

		loadGame();

		// game loop
		while (running) {
			repaint();

			frames++;

			if (System.currentTimeMillis() - 1000 >= lastFrame) {
				// has passed at least 1 second
				fps = frames;
				frames = 0;
				lastFrame = System.currentTimeMillis();
			}

			try {
				// to control the repaint rate
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.exit(0);
	} // end method run

	public class MouseHeld {
		boolean mouseDown = false;

		public void mouseMoved(MouseEvent e) {
			handXPos = e.getXOnScreen();
			handYPos = e.getYOnScreen();
		}

		public void updateMouse(MouseEvent e) {
			if (scene == 1) {
				if (mouseDown && hand == 0) {

				}
			}
		}

		public void mouseDown(MouseEvent e) {
			mouseDown = true;

			if (false) {}

			updateMouse(e);
		}
	} // end inner class MouseHeld

	
	public class KeyTyped {
		public void keyESC() {
			running = false;
		}

		public void keySPACE() {
			startGame(user);
		}
	} // end inner class KeyTyped

}
