package edu.nus.comp.dotagrid.logic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;


public class NeutralCreepSpawnPoint {
	
	public static final int SPAWN_NC_ROUND_INDEX = 20;
	
	public static final int[] SMALL_NC_SPAWN_POS_1 = {32, 19};
	public static final int[] SMALL_NC_TARGET_POS_1 = {32, 21};
	public static final int[] SMALL_NC_SPAWN_POS_2 = {70, 81};
	public static final int[] SMALL_NC_TARGET_POS_2 = {68, 81};
	
	public static final int[] MED_NC_SPAWN_POS_1 = {50, 22};
	public static final int[] MED_NC_TARGET_POS_1 = {48, 23};
	public static final int[] MED_NC_SPAWN_POS_2 = {39, 31};
	public static final int[] MED_NC_TARGET_POS_2 = {39, 33};
	public static final int[] MED_NC_SPAWN_POS_3 = {50, 66};
	public static final int[] MED_NC_TARGET_POS_3 = {50, 68};
	public static final int[] MED_NC_SPAWN_POS_4 = {77, 76};
	public static final int[] MED_NC_TARGET_POS_4 = {75, 75};
	
	public static final int[] LARGE_NC_SPAWN_POS_1 = {19, 23};
	public static final int[] LARGE_NC_TARGET_POS_1 = {18, 24};
	public static final int[] LARGE_NC_SPAWN_POS_2 = {57, 29};
	public static final int[] LARGE_NC_TARGET_POS_2 = {56, 31};
	public static final int[] LARGE_NC_SPAWN_POS_3 = {42, 77};
	public static final int[] LARGE_NC_TARGET_POS_3 = {45, 78};
	public static final int[] LARGE_NC_SPAWN_POS_4 = {62, 76};
	public static final int[] LARGE_NC_TARGET_POS_4 = {63, 74};
	
	public static final int[] ANCIENT_NC_SPAWN_POS_1 = {33, 44};
	public static final int[] ANCIENT_NC_TARGET_POS_1 = {32, 42};
	public static final int[] ANCIENT_NC_SPAWN_POS_2 = {80, 63};
	public static final int[] ANCIENT_NC_TARGET_POS_2 = {80, 65};
	
	public static final int BLOCK_SPAWN_RANGE = 3;

	
	public static void spawnNewWave() {
		// check and spawn new wave of neutral creeps
		
		if (GameFrame.turn % SPAWN_NC_ROUND_INDEX == 0) {
			createWave();
		}
		
	}

	
	private static void createWave() {
		// create new wave of neutral creeps if there is no characters within spawn position's range
		checkAndSpawnNC(SMALL_NC_SPAWN_POS_1, 1, SMALL_NC_TARGET_POS_1);
		checkAndSpawnNC(SMALL_NC_SPAWN_POS_2, 1, SMALL_NC_TARGET_POS_2);
		
		checkAndSpawnNC(MED_NC_SPAWN_POS_1, 2, MED_NC_TARGET_POS_1);
		checkAndSpawnNC(MED_NC_SPAWN_POS_2, 2, MED_NC_TARGET_POS_2);
		checkAndSpawnNC(MED_NC_SPAWN_POS_3, 2, MED_NC_TARGET_POS_3);
		checkAndSpawnNC(MED_NC_SPAWN_POS_4, 2, MED_NC_TARGET_POS_4);
		
		checkAndSpawnNC(LARGE_NC_SPAWN_POS_1, 3, LARGE_NC_TARGET_POS_1);
		checkAndSpawnNC(LARGE_NC_SPAWN_POS_2, 3, LARGE_NC_TARGET_POS_2);
		checkAndSpawnNC(LARGE_NC_SPAWN_POS_3, 3, LARGE_NC_TARGET_POS_3);
		checkAndSpawnNC(LARGE_NC_SPAWN_POS_4, 3, LARGE_NC_TARGET_POS_4);
		
		checkAndSpawnNC(ANCIENT_NC_SPAWN_POS_1, 4, ANCIENT_NC_TARGET_POS_1);
		checkAndSpawnNC(ANCIENT_NC_SPAWN_POS_2, 4, ANCIENT_NC_TARGET_POS_2);
	}


	private static void checkAndSpawnNC(int[] spawnPos, int index, int[] targetPos) {
		// spawn NC from spawnPos based on index
		if (noCharaWithinRange(spawnPos)) {
			// prepare for spawning
			Queue<int[]> spawnPosQueue = new LinkedList<int[]>();
			spawnPosQueue.add(spawnPos);
			
			ArrayList<int[]> checkedPosition = new ArrayList<int[]>();
			
			Queue<NeutralCreep> tempCharaQueue = new LinkedList<NeutralCreep>();
			
			switch (index) {
				// small nc
				case 1 :
					NeutralCreepDatabase.createSmallNCWave(tempCharaQueue);
					break;
					
				// medium nc
				case 2 :
					NeutralCreepDatabase.createMediumNCWave(tempCharaQueue);
					break;
					
				// large nc
				case 3 : 
					NeutralCreepDatabase.createLargeNCWave(tempCharaQueue);
					break;
					
				// ancient nc
				case 4 :
					NeutralCreepDatabase.createAncientNCWave(tempCharaQueue);
					break;
			}

			addTargetPos(tempCharaQueue, targetPos);
			
			// spawn!
			LineCreepSpawnPoint.spawn(spawnPosQueue, checkedPosition, tempCharaQueue);
		}
	}


	private static void addTargetPos(Queue<NeutralCreep> tempCharaQueue, int[] targetPos) {
		// add target position to each character in the characterQueue
		ArrayList<int[]> targetPosArrayList = new ArrayList<int[]>();
		targetPosArrayList.add(targetPos);
		
		for (NeutralCreep element : tempCharaQueue) {
			element.setAItargetPos(targetPosArrayList);
		}
	}


	private static boolean noCharaWithinRange(int[] spawnPos) {
		// neutral creep spawn condition
		for (int i = -BLOCK_SPAWN_RANGE; i <= BLOCK_SPAWN_RANGE; i++) {
			for (int j = -BLOCK_SPAWN_RANGE; j <= BLOCK_SPAWN_RANGE; j++) {
				if (Math.abs(i) + Math.abs(j) <= BLOCK_SPAWN_RANGE) {
					if (spawnPos[0] + i >= 0 && spawnPos[0] + i <GridFrame.COLUMN_NUMBER 
							&& spawnPos[1] + j >= 0 && spawnPos[1] + j < GridFrame.ROW_NUMBER) {
						
						if (GridFrame.gridButtonMap[spawnPos[0] + i][spawnPos[1] + j].getIsMovable() && 
								GridFrame.gridButtonMap[spawnPos[0] + i][spawnPos[1] + j].getCharacter() != null) {
							return false;
						}
						
					}
				}
			}
		}
		return true;
	}
}
