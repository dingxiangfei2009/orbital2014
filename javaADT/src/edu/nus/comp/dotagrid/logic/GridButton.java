package edu.nus.comp.dotagrid.logic;

import java.util.Random;

public class GridButton {
	
	// mark if any character can move across the grid 
	private boolean isMovable = false;
	
	// mark if the grid is able to block sight for characters
	private boolean isBlockSight = false;
	
	// mark if the character is a hero
	private boolean isHero = false;
	
	// mark if the character is player's hero
	private boolean isPlayer = false;
	
	private Character character = null; 
	
	private GridButtonActions gridButtonActions = null;
	
	
	// constructor
	public GridButton(int imageNumber){
		/* 
		 * imageNumber :
		 * 1 : grass
		 * 2 : road
		 * 3 : river
		 * 4 : tree 
		 * 5 : fence
		 * 6 : cliff
		 * 
		 * 20 : Sentinel Tower 1
		 * 21 : Sentinel Tower 2
		 * 22 : Sentinel Tower 3
		 * 23 : Sentinel Tower 4
		 * 
		 * 24 : Scourge Tower 1
		 * 25 : Scourge Tower 2
		 * 26 : Scourge Tower 3
		 * 27 : Scourge Tower 4
		 * 
		 * 28 : sentinel fountain
		 * 29 : scourge fountain
		 * 
		 * 30 : sentinelBase
		 * 31 : scourgeBase
		 * 
		 * 32 : sentinelMeeleBarrack
		 * 33 : sentinelRangedBarrack
		 * 
		 * 34 : scourgeMeeleBarrack
		 * 35 : scourgeRangedBarrack
		 * 
		 * 41 : small neutral creep spawn point
		 * 42 : medium neutral creep spawn point
		 * 43 : large neutral creep spawn point
		 * 44 : ancient neutral creep spawn point
		 * 
		 * 80 : line creep spawn point
		 * 99 : player's hero spawn point
		 * 
		 */
		

		if (imageNumber == 1 || imageNumber == 2 || imageNumber == 3) {
			this.setIsMovable(true);
		}
		
		if (imageNumber == 4) {
			this.setCharacter(new Tree());
			this.setBlockSight(true);
		}
	}
	
	
	public GridButton(GridButton gridButton) {
		this.setBlockSight(gridButton.isBlockSight());
		this.setIsMovable(gridButton.getIsMovable());
		this.setIsPlayer(gridButton.getIsPlayer());
		this.setIsHero(gridButton.getIsHero());
		this.setCharacter(gridButton.getCharacter());
		this.gridButtonActions = null;
	}
	
	public GridButton(Character chara) {
		this.setIsMovable(true);
		this.setIsPlayer(false);
		this.setBlockSight(false);
		
		if (chara instanceof Hero) {
			this.setIsHero(true);
		} else {
			this.setIsHero(false);
		}
		
		this.setCharacter(chara);
		this.gridButtonActions = null;
	}


	public void actionPerformed(){
		// initialize grid button actions
		if (GridFrame.getPreviouslySelectedXPos() == -1 || GridFrame.getPreviouslySelectedYPos() == -1 ) {
			gridButtonActions = new GridButtonActions(GridFrame.getSelectedXPos(), GridFrame.getSelectedYPos(), 0, 0);
		} else {
			gridButtonActions = new GridButtonActions(GridFrame.getSelectedXPos(), GridFrame.getSelectedYPos(), 
					GridFrame.getPreviouslySelectedXPos(), GridFrame.getPreviouslySelectedYPos());
		}
		
		// execute player's hero's action if previously selected character is controlled by player
		if (GameButtonActions.readyToAct == true) {
			gridButtonActions.updateWhenSomeActionsInvoked();
		} else {
			gridButtonActions.updateWhenNoActionInvoked();
		}	
		
	}
	
	
	public void setCharacter(Character character){
		this.character = character;
	}
	
	public Character getCharacter(){
		return character;
	}

	public boolean getIsPlayer() {
		return isPlayer;
	}

	public void setIsPlayer(boolean isPlayer) {
		this.isPlayer = isPlayer;
	}
	
	public boolean getIsHero() {
		return isHero;
	}

	public void setIsHero(boolean isHero) {
		this.isHero = isHero;
	}

	public boolean getIsMovable() {
		return isMovable;
	}

	public void setIsMovable(boolean isMovable) {
		this.isMovable = isMovable;
	}


	public boolean isBlockSight() {
		return isBlockSight;
	}


	public void setBlockSight(boolean isBlockSight) {
		this.isBlockSight = isBlockSight;
	}

}
