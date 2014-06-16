package edu.nus.comp.dotagrid.logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class LineCreepSpawnPoint {
	
	public static final int SENTINEL_TOP_LINE_SPAWN_X_POS = 10;
	public static final int SENTINEL_TOP_LINE_SPAWN_Y_POS = 147;
	
	public static final int SENTINEL_MID_LINE_SPAWN_X_POS = 36;
	public static final int SENTINEL_MID_LINE_SPAWN_Y_POS = 163;
	
	public static final int SENTINEL_BOT_LINE_SPAWN_X_POS = 46;	
	public static final int SENTINEL_BOT_LINE_SPAWN_Y_POS = 184;
	
	public static final int SCOURGE_TOP_LINE_SPAWN_X_POS = 152;	
	public static final int SCOURGE_TOP_LINE_SPAWN_Y_POS = 15;

	public static final int SCOURGE_MID_LINE_SPAWN_X_POS = 163;
	public static final int SCOURGE_MID_LINE_SPAWN_Y_POS = 36;
	
	public static final int SCOURGE_BOT_LINE_SPAWN_X_POS = 189;	
	public static final int SCOURGE_BOT_LINE_SPAWN_Y_POS = 51;
	
	public static int MEELE_CREEP_NUMBER = 4;
	public static int RANGED_CREEP_NUMBER = 1;
	public static int SIEGE_CREEP_NUMBER = 1;


	public static void spawnNewWave() {
		/* 
		 * check and spawn new wave of creeps  :
		 * 
		 * spawn new normal wave every 10 turns
		 * spawn new extended wave every 50 turns
		*/

		
		if (GameFrame.turn % 10 == 0) {
			createWave();
		}
		
	}



	private static void createWave() {
		// initialize checkedPosition
		ArrayList<int[]> checkedPosition = new ArrayList<int[]>();
		
		LineCreepDatabase lineCreeps = new LineCreepDatabase();
		
		// each spawn point spawn MEELE_CREEP_NUMBER meele creeps + RANGED_CREEP_NUMBER ranged creep
		Queue <LineCreep> SentinelCreeps = new LinkedList<LineCreep>();
		Queue <LineCreep> ScourgeCreeps = new LinkedList<LineCreep>();

		
		// add meele creeps
		for (int i=0; i<MEELE_CREEP_NUMBER; i++) {
			SentinelCreeps.add(lineCreeps.lineCreepDatabase[0]);
			ScourgeCreeps.add(lineCreeps.lineCreepDatabase[3]);
		}
		
		// add ranged creeps
		for (int i=0; i<RANGED_CREEP_NUMBER; i++) {
			SentinelCreeps.add(lineCreeps.lineCreepDatabase[1]);
			ScourgeCreeps.add(lineCreeps.lineCreepDatabase[4]);
		}
		
		// add siege creeps every 50 turns
		if (GameFrame.turn % 50 == 0) {
			for (int i=0; i<SIEGE_CREEP_NUMBER; i++) {
				SentinelCreeps.add(lineCreeps.lineCreepDatabase[2]);
				ScourgeCreeps.add(lineCreeps.lineCreepDatabase[5]);
			}
		}
		
		
		// make copies for creep queue		
		LinkedList<LineCreep> SentinelCreeps1 = new LinkedList<LineCreep>();
		LinkedList<LineCreep> SentinelCreeps2 = new LinkedList<LineCreep>();
		LinkedList<LineCreep> SentinelCreeps3 = new LinkedList<LineCreep>();
		SentinelCreeps1.addAll(SentinelCreeps);
		SentinelCreeps2.addAll(SentinelCreeps);
		SentinelCreeps3.addAll(SentinelCreeps);
		
		LinkedList<LineCreep> ScourgeCreeps1 = new LinkedList<LineCreep>();
		LinkedList<LineCreep> ScourgeCreeps2 = new LinkedList<LineCreep>();
		LinkedList<LineCreep> ScourgeCreeps3 = new LinkedList<LineCreep>();
		ScourgeCreeps1.addAll(ScourgeCreeps);
		ScourgeCreeps2.addAll(ScourgeCreeps);
		ScourgeCreeps3.addAll(ScourgeCreeps);
		
		
		
		// spawn sentinel line creeps
		
		int[] sentinelTopPosition = {SENTINEL_TOP_LINE_SPAWN_X_POS, SENTINEL_TOP_LINE_SPAWN_Y_POS};
		Queue<int[]> sentinelTop = new LinkedList<int[]>(); 
		sentinelTop.add(sentinelTopPosition);
		
		spawn(sentinelTop, checkedPosition, SentinelCreeps1);
		
		int[] sentinelMidPosition = {SENTINEL_MID_LINE_SPAWN_X_POS, SENTINEL_MID_LINE_SPAWN_Y_POS};
		Queue<int[]> sentinelMid = new LinkedList<int[]>(); 
		sentinelMid.add(sentinelMidPosition);
		
		spawn(sentinelMid, checkedPosition, SentinelCreeps2);
		
		int[] sentinelBotPosition = {SENTINEL_BOT_LINE_SPAWN_X_POS, SENTINEL_BOT_LINE_SPAWN_Y_POS};
		Queue<int[]> sentinelBot = new LinkedList<int[]>(); 
		sentinelBot.add(sentinelBotPosition);
		
		spawn(sentinelBot, checkedPosition, SentinelCreeps3);
		
		
		// spawn scourge line creeps
		
		int[] scourgeTopPosition = {SCOURGE_TOP_LINE_SPAWN_X_POS, SCOURGE_TOP_LINE_SPAWN_Y_POS};
		Queue<int[]> scourgeTop = new LinkedList<int[]>(); 
		scourgeTop.add(scourgeTopPosition);
		
		spawn(scourgeTop, checkedPosition, ScourgeCreeps1);
		
		int[] scourgeMidPosition = {SCOURGE_MID_LINE_SPAWN_X_POS, SCOURGE_MID_LINE_SPAWN_Y_POS};
		Queue<int[]> scourgeMid = new LinkedList<int[]>(); 
		scourgeMid.add(scourgeMidPosition);
		
		spawn(scourgeMid, checkedPosition, ScourgeCreeps2);
		
		int[] scourgeBotPosition = {SCOURGE_BOT_LINE_SPAWN_X_POS, SCOURGE_BOT_LINE_SPAWN_Y_POS};
		Queue<int[]> scourgeBot = new LinkedList<int[]>(); 
		scourgeBot.add(scourgeBotPosition);
		
		spawn(scourgeBot, checkedPosition, ScourgeCreeps3);
				
	}



	private static <T extends Character> void spawn(Queue<int[]> positionQueue, ArrayList<int[]> checkedPosition, Queue<T> characterQueue) {
		// this method :
		// put all characters in the characterQueue into non-occupied grid position nearest to position : (XPos, YPos)
		// the ArrayList checkedPosition is to store check positions
		
		// base case :
		if (characterQueue.isEmpty() == true) {
			return;
			
		} else {
			
			// add surrounding grids into positionQueue
			if (!isChecked(checkedPosition, positionQueue.peek()[0]+1, positionQueue.peek()[1])){
				int[] newPos = {positionQueue.peek()[0]+1, positionQueue.peek()[1]};
				positionQueue.add(newPos);
			}
			
			if (!isChecked(checkedPosition, positionQueue.peek()[0], positionQueue.peek()[1]+1)){
				int[] newPos = {positionQueue.peek()[0], positionQueue.peek()[1]+1};
				positionQueue.add(newPos);
			}
			
			if (!isChecked(checkedPosition, positionQueue.peek()[0]-1, positionQueue.peek()[1])){
				int[] newPos = {positionQueue.peek()[0]-1, positionQueue.peek()[1]};
				positionQueue.add(newPos);
			}
			
			if (!isChecked(checkedPosition, positionQueue.peek()[0], positionQueue.peek()[1]-1)){
				int[] newPos = {positionQueue.peek()[0], positionQueue.peek()[1]-1};
				positionQueue.add(newPos);
			}
			
			// add the current position into checked queue
			checkedPosition.add(positionQueue.peek());
		
			// check is current position is suitable for adding a character
			if (GridFrame.gridButtonMap[positionQueue.peek()[0]][positionQueue.peek()[1]].getIsMovable() == true 
					&& GridFrame.gridButtonMap[positionQueue.peek()[0]][positionQueue.peek()[1]].getIsOccupied() == false) {
				// can only place the character onto a movable and non-occupied grid
				
				// change the grid, add a character!
				GridFrame.gridButtonMap[positionQueue.peek()[0]][positionQueue.peek()[1]].setIsOccupied(true);
				GridFrame.gridButtonMap[positionQueue.peek()[0]][positionQueue.poll()[1]].setCharacter(characterQueue.poll());
			} else {
				// discard the position
				positionQueue.poll();
			}
			
			// recursive call!
			spawn(positionQueue, checkedPosition, characterQueue);
		}
	}



	private static boolean isChecked(ArrayList<int[]> checkedPosition, int XPos, int YPos) {
		// each int[] in checkedPosition stores a pair of xpos and ypos
		boolean isChecked = false;
		
		// XPos and YPos need to be within range
		if (XPos >= 0 && XPos <GridFrame.COLUMN_NUMBER 
				&& YPos >= 0 && YPos <GridFrame.ROW_NUMBER){
			// check if the position has been visited before
			for (int[] element : checkedPosition){
				if (element[0] == XPos && element[1] == YPos){
					isChecked = true;
					break;
				}
			}
		} else{
			// not within range! unable to visit!
			isChecked = true;
		}
		
		return isChecked;
	}
	
	
}