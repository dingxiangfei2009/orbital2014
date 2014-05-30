package edu.nus.comp.dotagrid.logic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;


public class GameFrame implements MouseListener{
	
	public int frameWidth;
	public int frameHeight;
	
	Color defaultColor = Color.GRAY;
	
	public static final int FRAME_BORDER_WIDTH = 20;
	public static final int FRAME_BORDER_HEIGHT = 20;
	
	// total grid space in the game frame
	public static final int GRID_ROW_NUMBER_IN_SCREEN = 17;
	public static final int GRID_COL_NUMBER_IN_SCREEN = 25;
	
	// space to draw the main game frame 
	public static final int FRAME_ROW_NUMBER_OCCUPIED = 14;
	public static final int FRAME_COL_NUMBER_OCCUPIED = 20;
	
	public double gridWidth = 1.0;
	public double gridHeight = 1.0;

	private int handXPos;
	private int handYPos;
	
	
	// an ArrayList of GameButton which store all the GameButtons on the GameFrame
	private ArrayList<GameButton> allGameButtons;
	
	// image of the character selected
	GameButton characterIcon;
	
	// Name, HP , MP
	GameButton characterName;
	GameButton characterHP;
	GameButton characterMP;
	
	// Strength, Agility, Intelligence
	GameButton characterStrength;
	GameButton characterAgility;
	GameButton characterIntelligence;
	
	// Attack, Defense
	GameButton characterAttack;
	GameButton characterDefense;
	
	// Level, Experience
	GameButton characterLevel;
	GameButton characterExperience;
	
	// item list
	GameButton[] items;
	public final int MAX_ITEM_NUMBER = 6;
	
	// skill list
	GameButton[] skills;
	public final int MAX_SKILL_NUMBER = 8;
	
	// version info
	GameButton versionID;
	
	// turns counter
	GameButton turnCounter;
	
	// KDA
	GameButton Kill;
	GameButton Death;
	GameButton Assist;
	
	// Money & Action Points
	GameButton money;
	GameButton actionPoints;
	
	// Action List 
	GameButton[] actionList;
	public final int MAX_ACTION_NUMBER = 16;
	
	// move mainGame buttons
	GameButton[] directions;
	public final int DIRECTION_NUMBER = 5;
	
	
	public GameFrame(){}
	

	public GameFrame(Graphics g, Frame frame) {
		this.frameWidth = frame.getWidth();
		this.frameHeight = frame.getHeight();
		
		gridWidth = (frameWidth - (2.0 * FRAME_BORDER_WIDTH)) / GRID_COL_NUMBER_IN_SCREEN;
		gridHeight = (frameHeight - (2.0 * FRAME_BORDER_HEIGHT)) / GRID_ROW_NUMBER_IN_SCREEN;
		
		allGameButtons = new ArrayList<GameButton>();
		
		// construct game frame

		// Initialize game buttons
		initializeAllButtons();
		
		// draw game frame
		displayAllButtons(g);
		
	}

	
	private void displayAllButtons(Graphics g) {
		g.setColor(defaultColor);
		
		// stuff displayed at the bottom of the screen
		
		//display allGameButtons
		for (int i=0; i<allGameButtons.size(); i++) {
			allGameButtons.get(i).drawRect(g);
			allGameButtons.get(i).drawString(g);
		}
		
		// display item list
		for (int i=0; i<MAX_ITEM_NUMBER; i++) {
			items[i].drawRect(g);
		}
		
		// display skill list
		for (int i=0; i<MAX_SKILL_NUMBER; i++) {
			skills[i].drawRect(g);
		}
		
		
		// stuff displayed at the right hand side of the screen
		
		//display allGameButtons
		for (int i=0; i<allGameButtons.size(); i++) {
			allGameButtons.get(i).drawRect(g);
			allGameButtons.get(i).drawString(g);
		}
		
		// display Action List 
		for(int i=0; i<MAX_ACTION_NUMBER; i++){
			actionList[i].drawRect(g);
		}
		
		// display move mainGame buttons
		for(int i=0; i<DIRECTION_NUMBER; i++){
			directions[i].drawRect(g);
		}
		
		
	}

	
	
	private void initializeAllButtons() {
		int startingYPos = FRAME_BORDER_HEIGHT + (int) (gridHeight * (FRAME_ROW_NUMBER_OCCUPIED + 1));
		int startingXPos = FRAME_BORDER_WIDTH + (int) (gridWidth * (FRAME_COL_NUMBER_OCCUPIED + 1));	
		
		// image of the character selected
		characterIcon = new GameButton("characterIcon", null, FRAME_BORDER_WIDTH, startingYPos, (int) (2 * gridWidth), (int) (2 * gridHeight));
		allGameButtons.add(characterIcon);
		
		// Name, HP , MP
		characterName = new GameButton("Name: ", null, FRAME_BORDER_WIDTH + (int) (2 * gridWidth + 0.5 * gridWidth), startingYPos, (int) (3.5 * gridWidth), (int) (2.0 * gridHeight / 3));
		characterHP = new GameButton("HP: ", null, FRAME_BORDER_WIDTH + (int) (2 * gridWidth + 0.5 * gridWidth), startingYPos + (int) (2.0 * gridHeight / 3), (int) (3.5 * gridWidth), (int) (2.0 * gridHeight / 3));
		characterMP = new GameButton("MP: ", null, FRAME_BORDER_WIDTH + (int) (2 * gridWidth + 0.5 * gridWidth), startingYPos + (int) (4.0 * gridHeight / 3), (int) (3.5 * gridWidth), (int) (2.0 * gridHeight / 3));
		
		allGameButtons.add(characterName);
		allGameButtons.add(characterHP);
		allGameButtons.add(characterMP);
		
		
		// Strength, Agility, Intelligence
		characterStrength = new GameButton("Strength: ", null, FRAME_BORDER_WIDTH + (int) ((2+3.5) * gridWidth + (2*0.5) * gridWidth), startingYPos, (int) (3.5 * gridWidth), (int) (2.0 * gridHeight / 3));
		characterAgility = new GameButton("Agility: ", null, FRAME_BORDER_WIDTH + (int) ((2+3.5) * gridWidth + (2*0.5) * gridWidth), startingYPos + (int) (2.0 * gridHeight / 3), (int) (3.5 * gridWidth), (int) (2.0 * gridHeight / 3));
		characterIntelligence = new GameButton("Intelligence: ", null, FRAME_BORDER_WIDTH + (int) ((2+3.5) * gridWidth + (2*0.5) * gridWidth), startingYPos + (int) (4.0 * gridHeight / 3), (int) (3.5 * gridWidth), (int) (2.0 * gridHeight / 3));

		allGameButtons.add(characterStrength);
		allGameButtons.add(characterAgility);
		allGameButtons.add(characterIntelligence);
		
		// Attack, Defense
		characterAttack = new GameButton("Attack: ", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5) * gridWidth + (3*0.5) * gridWidth), startingYPos, (int) (2.5 * gridWidth), (int) (2.0 * gridHeight / 2));
		characterDefense = new GameButton("Defense: ", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5) * gridWidth + (3*0.5) * gridWidth), startingYPos + (int) (2.0 * gridHeight / 2), (int) (2.5 * gridWidth), (int) (2.0 * gridHeight / 2));
		
		allGameButtons.add(characterAttack);
		allGameButtons.add(characterDefense);
		
		// Level, Experience
		characterLevel = new GameButton("Level: ", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5) * gridWidth + (4*0.5) * gridWidth), startingYPos, (int) (2.5 * gridWidth), (int) (2.0 * gridHeight / 3));
		characterExperience = new GameButton("Experience: ", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5) * gridWidth + (4*0.5) * gridWidth), startingYPos + (int) (2.0 * gridHeight / 3), (int) (2.5 * gridWidth), (int) (4.0 * gridHeight / 3));

		allGameButtons.add(characterLevel);
		allGameButtons.add(characterExperience);
		
		// Item List (numbers are marked from top to bottom first, then left to right)
		items = new GameButton[MAX_ITEM_NUMBER];
		
		items[0] = new GameButton("item", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5) * gridWidth + (5*0.5) * gridWidth), startingYPos, (int) gridWidth, (int) gridHeight);
		items[1] = new GameButton("item", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5) * gridWidth + (5*0.5) * gridWidth), startingYPos + (int) gridHeight, (int) gridWidth, (int) gridHeight);
		items[2] = new GameButton("item", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5) * gridWidth + (5*0.5) * gridWidth + gridWidth), startingYPos, (int) gridWidth, (int) gridHeight);
		items[3] = new GameButton("item", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5) * gridWidth + (5*0.5) * gridWidth + gridWidth), startingYPos + (int) gridHeight, (int) gridWidth, (int) gridHeight);
		items[4] = new GameButton("item", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5) * gridWidth + (5*0.5) * gridWidth + 2 * gridWidth), startingYPos, (int) gridWidth, (int) gridHeight);
		items[5] = new GameButton("item", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5) * gridWidth + (5*0.5) * gridWidth + 2 * gridWidth), startingYPos + (int) gridHeight, (int) gridWidth, (int) gridHeight);
			
		allGameButtons.add(items[0]);
		allGameButtons.add(items[1]);
		allGameButtons.add(items[2]);
		allGameButtons.add(items[3]);
		allGameButtons.add(items[4]);
		allGameButtons.add(items[5]);
		
		
		// Skill List (numbers are marked from top to bottom first, then left to right)
		skills = new GameButton[MAX_SKILL_NUMBER];
		
		skills[0] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth), startingYPos, (int) gridWidth, (int) gridHeight);
		skills[1] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth), startingYPos + (int) gridHeight, (int) gridWidth, (int) gridHeight);
		skills[2] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth + gridWidth), startingYPos, (int) gridWidth, (int) gridHeight);
		skills[3] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth + gridWidth), startingYPos + (int) gridHeight, (int) gridWidth, (int) gridHeight);
		skills[4] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth + 2 * gridWidth), startingYPos, (int) gridWidth, (int) gridHeight);
		skills[5] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth + 2 * gridWidth), startingYPos + (int) gridHeight, (int) gridWidth, (int) gridHeight);
		skills[6] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth + 3 * gridWidth), startingYPos, (int) gridWidth, (int) gridHeight);
		skills[7] = new GameButton("skill", null, FRAME_BORDER_WIDTH + (int) ((2+3.5+3.5+2.5+2.5+3) * gridWidth + (6*0.5) * gridWidth + 3 * gridWidth), startingYPos + (int) gridHeight, (int) gridWidth, (int) gridHeight);
				
		allGameButtons.add(skills[0]);
		allGameButtons.add(skills[1]);
		allGameButtons.add(skills[2]);
		allGameButtons.add(skills[3]);
		allGameButtons.add(skills[4]);
		allGameButtons.add(skills[5]);
		allGameButtons.add(skills[6]);
		allGameButtons.add(skills[7]);


		// Version Info
		versionID = new GameButton("C-DOTA 1.0", null, startingXPos, FRAME_BORDER_HEIGHT, (int) (2 * gridWidth), (int) gridHeight);
		allGameButtons.add(versionID);
		
		// turn count
		turnCounter = new GameButton("turnCounter", null, startingXPos + (int) (2 * gridWidth), FRAME_BORDER_HEIGHT, (int) (2 * gridWidth), (int) gridHeight);
		allGameButtons.add(turnCounter);
		
		// KDA
		Kill = new GameButton("Kill", null, startingXPos, FRAME_BORDER_HEIGHT + (int) (gridHeight + 0.5 * gridHeight), (int) (4.0 / 3 * gridWidth), (int) (0.5 * gridHeight));
		Death = new GameButton("Death", null, startingXPos + (int) (4.0 / 3 * gridWidth), FRAME_BORDER_HEIGHT + (int) (gridHeight + 0.5 * gridHeight), (int) (4.0 / 3 * gridWidth), (int) (0.5 * gridHeight));
		Assist = new GameButton("Assist", null, startingXPos + (int) (2 * 4.0 / 3 * gridWidth), FRAME_BORDER_HEIGHT + (int) (gridHeight + 0.5 * gridHeight), (int) (4.0 / 3 * gridWidth), (int) (0.5 * gridHeight));

		allGameButtons.add(Kill);
		allGameButtons.add(Death);
		allGameButtons.add(Assist);
		
		// Money & Action Points
		money = new GameButton("Money: ", null, startingXPos, FRAME_BORDER_HEIGHT + (int) (2.5 * gridHeight), (int) (2 * gridWidth), (int) gridHeight);
		actionPoints = new GameButton("Action Points: ", null, startingXPos + (int) (2 * gridWidth), FRAME_BORDER_HEIGHT + (int) (2.5 * gridHeight), (int) (2 * gridWidth), (int) gridHeight);

		allGameButtons.add(money);
		allGameButtons.add(actionPoints);
		
		// Action List 
		// (draw from left to right first, then top to bottom)
		actionList = new GameButton[MAX_ACTION_NUMBER];
		
		actionList[0] = new GameButton("action", null, startingXPos, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight), (int) gridWidth, (int) gridHeight);	
		actionList[1] = new GameButton("action", null, startingXPos + (int) gridWidth, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[2] = new GameButton("action", null, startingXPos + (int) (2 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[3] = new GameButton("action", null, startingXPos + (int) (3 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[4] = new GameButton("action", null, startingXPos, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 1.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[5] = new GameButton("action", null, startingXPos + (int) gridWidth, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 1.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[6] = new GameButton("action", null, startingXPos + (int) (2 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 1.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[7] = new GameButton("action", null, startingXPos + (int) (3 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 1.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[8] = new GameButton("action", null, startingXPos, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 2.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[9] = new GameButton("action", null, startingXPos + (int) gridWidth, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 2.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[10] = new GameButton("action", null, startingXPos + (int) (2 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 2.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[11] = new GameButton("action", null, startingXPos + (int) (3 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 2.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[12] = new GameButton("action", null, startingXPos, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 3.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[13] = new GameButton("action", null, startingXPos + (int) gridWidth, FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 3.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[14] = new GameButton("action", null, startingXPos + (int) (2 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 3.0 * gridHeight), (int) gridWidth, (int) gridHeight);
		actionList[15] = new GameButton("action", null, startingXPos + (int) (3 * gridWidth), FRAME_BORDER_HEIGHT + (int) (4.0 * gridHeight + 3.0 * gridHeight), (int) gridWidth, (int) gridHeight);
			

		allGameButtons.add(actionList[0]);
		allGameButtons.add(actionList[1]);
		allGameButtons.add(actionList[2]);
		allGameButtons.add(actionList[3]);
		allGameButtons.add(actionList[4]);
		allGameButtons.add(actionList[5]);
		allGameButtons.add(actionList[6]);
		allGameButtons.add(actionList[7]);
		allGameButtons.add(actionList[8]);
		allGameButtons.add(actionList[9]);
		allGameButtons.add(actionList[10]);
		allGameButtons.add(actionList[11]);
		allGameButtons.add(actionList[12]);
		allGameButtons.add(actionList[13]);
		allGameButtons.add(actionList[14]);
		allGameButtons.add(actionList[15]);
		
		// move mainGame buttons
		directions = new GameButton[DIRECTION_NUMBER];
		
		// Up button
		directions[0] = new GameButton("up", null, startingXPos + (int) (1.5 * gridWidth), startingYPos - (int) (4 * gridHeight), (int) gridWidth, (int) gridHeight);	
		
		// Left button
		directions[1] = new GameButton("left", null, startingXPos + (int) (0.5 * gridWidth), startingYPos - (int) (3 * gridHeight), (int) gridWidth, (int) gridHeight);
			
		// Game Icon
		directions[2] = new GameButton("game icon", null, startingXPos + (int) (1.5 * gridWidth), startingYPos - (int) (3 * gridHeight), (int) gridWidth, (int) gridHeight);	
		
		// Right button
		directions[3] = new GameButton("right", null, startingXPos + (int) (2.5 * gridWidth), startingYPos - (int) (3 * gridHeight), (int) gridWidth, (int) gridHeight);
		
		// Down button
		directions[4] = new GameButton("down", null, startingXPos + (int) (1.5 * gridWidth), startingYPos - (int) (2 * gridHeight), (int) gridWidth, (int) gridHeight);
		
		allGameButtons.add(directions[0]);
		allGameButtons.add(directions[1]);
		allGameButtons.add(directions[2]);
		allGameButtons.add(directions[3]);
		allGameButtons.add(directions[4]);
		
	}

	
	public void invokeEvent(int handXPos, int handYPos){
		
		//display allGameButtons
		for (int i=0; i<allGameButtons.size(); i++) {
			if (allGameButtons.get(i).checkEvent(handXPos, handYPos)) {
				allGameButtons.get(i).actionPerformed();
				break;
			}
		}
		
	}
	
	
	@Override
	public void mouseClicked(MouseEvent e) {
		handXPos = e.getXOnScreen();
		handYPos = e.getYOnScreen();
		
		invokeEvent(handXPos, handYPos);
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
