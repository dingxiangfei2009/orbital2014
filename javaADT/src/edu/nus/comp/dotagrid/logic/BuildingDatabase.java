package edu.nus.comp.dotagrid.logic;

import java.util.ArrayList;

import javax.swing.JOptionPane;

public class BuildingDatabase {
	public static final int TOTAL_BUILDING_NUMBER = 6;
	
	// count for number of barracks which been destroyed
	public static int destroyedSentinelBarrackNumber = 0;
	public static int destroyedScourgeBarrackNumber = 0;
	
	public static Building[] buildingDatabase = new Building[TOTAL_BUILDING_NUMBER];
	
	// sentinel buildings
	public static int sentinelBaseXPos = 14;
	public static int sentinelBaseYPos = 84;
	public static int[] sentinelBasePos = {14, 84};
	
	public static int sentinelTopMeeleBarrackXPos = 5;
	public static int sentinelTopMeeleBarrackYPos = 68;
	public static boolean isDestroyedSentinelTopMeeleBarrack = false;
	
	public static int sentinelTopRangedBarrackXPos = 11; 
	public static int sentinelTopRangedBarrackYPos = 68;
	public static boolean isDestroyedSentinelTopRangedBarrack = false;
	
	public static int sentinelMidMeeleBarrackXPos = 20; 
	public static int sentinelMidMeeleBarrackYPos = 72;
	public static boolean isDestroyedSentinelMidMeeleBarrack = false;
	
	public static int sentinelMidRangedBarrackXPos = 26; 
	public static int sentinelMidRangedBarrackYPos = 78;
	public static boolean isDestroyedSentinelMidRangedBarrack = false;
	
	public static int sentinelBotMeeleBarrackXPos = 29; 
	public static int sentinelBotMeeleBarrackYPos = 87;
	public static boolean isDestroyedSentinelBotMeeleBarrack = false;
	
	public static int sentinelBotRangedBarrackXPos = 29;
	public static int sentinelBotRangedBarrackYPos = 93;
	public static boolean isDestroyedSentinelBotRangedBarrack = false;
	
	
	// scourge buildings
	public static int scourgeBaseXPos = 85;
	public static int scourgeBaseYPos = 15;
	public static int[] scourgeBasePos = {85, 15};
	
	public static int scourgeTopMeeleBarrackXPos = 70;
	public static int scourgeTopMeeleBarrackYPos = 6;
	public static boolean isDestroyedScourgeTopMeeleBarrack = false;
	
	public static int scourgeTopRangedBarrackXPos = 70;
	public static int scourgeTopRangedBarrackYPos = 12;
	public static boolean isDestroyedScourgeTopRangedBarrack = false;
	
	public static int scourgeMidMeeleBarrackXPos = 73;
	public static int scourgeMidMeeleBarrackYPos = 21;
	public static boolean isDestroyedScourgeMidMeeleBarrack = false;
	
	public static int scourgeMidRangedBarrackXPos = 79;
	public static int scourgeMidRangedBarrackYPos = 27;
	public static boolean isDestroyedScourgeMidRangedBarrack = false;
	
	public static int scourgeBotMeeleBarrackXPos = 88;
	public static int scourgeBotMeeleBarrackYPos = 31;
	public static boolean isDestroyedScourgeBotMeeleBarrack = false;
	
	public static int scourgeBotRangedBarrackXPos = 94; 
	public static int scourgeBotRangedBarrackYPos = 31;
	public static boolean isDestroyedScourgeBotRangedBarrack = false;
	
	
	public BuildingDatabase(){
		/*
		 * String name, 
		 * int bountyMoney, 
		 * int startingHP, 
		 * double startingPhysicalDefence, 
		 * int teamNumber
		 *
		*/
		
		Building sentinelBase = new Building("Sentinel Base", 0, 4250, 15, 1);
		ArrayList<int[]> tempList = new ArrayList<int[]>();
		tempList.add(TowerDatabase.sentinelLeftTower4Position);
		tempList.add(TowerDatabase.sentinelRightTower4Position);
		sentinelBase.setProtectionPosList(tempList);
		buildingDatabase[0] = sentinelBase;
		
		Building scourgeBase = new Building("Scourge Base", 0, 4250, 15, 2);
		ArrayList<int[]> tempList1 = new ArrayList<int[]>();
		tempList1.add(TowerDatabase.scourgeLeftTower4Position);
		tempList1.add(TowerDatabase.scourgeRightTower4Position);
		scourgeBase.setProtectionPosList(tempList1);
		buildingDatabase[1] = scourgeBase;
		
		Building sentinelMeeleBarrack = new Building("Sentinel Meele Barrack", 0, 1500, 15, 1);
		Building sentinelRangedBarrack = new Building("Sentinel Ranged Barrack", 0, 1500, 5, 1);
		
		buildingDatabase[2] = sentinelMeeleBarrack;
		buildingDatabase[3] = sentinelRangedBarrack;
		
		Building scourgeMeeleBarrack = new Building("Scourge Meele Barrack", 0, 1500, 15, 2);
		Building scourgeRangedBarrack = new Building("Scourge Ranged Barrack", 0, 1500, 5, 2);
		
		buildingDatabase[4] = scourgeMeeleBarrack;
		buildingDatabase[5] = scourgeRangedBarrack;
		
	}

	

	public static void isBarracksDestroyed() {
		// check if any of the barracks has been destroyed by the attack action
		
		// sentinel barracks
		
		// only check if barrack has not been destroyed
		if (!isDestroyedSentinelTopMeeleBarrack) {
			if (GridFrame.gridButtonMap[sentinelTopMeeleBarrackXPos][sentinelTopMeeleBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Sentinel Top Meele Barrack has been Destroyed!!!");
				isDestroyedSentinelTopMeeleBarrack = true;
				destroyedSentinelBarrackNumber++;
			}
		}	
		
		if (!isDestroyedSentinelTopRangedBarrack) {
			if (GridFrame.gridButtonMap[sentinelTopRangedBarrackXPos][sentinelTopRangedBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Sentinel Top Ranged Barrack has been Destroyed!!!");
				isDestroyedSentinelTopRangedBarrack = true;
				destroyedSentinelBarrackNumber++;
			}
		}
		
		if (!isDestroyedSentinelMidMeeleBarrack) {
			if (GridFrame.gridButtonMap[sentinelMidMeeleBarrackXPos][sentinelMidMeeleBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Sentinel Mid Meele Barrack has been Destroyed!!!");
				isDestroyedSentinelMidMeeleBarrack = true;
				destroyedSentinelBarrackNumber++;
			}
		}

		if (!isDestroyedSentinelMidRangedBarrack) {
			if (GridFrame.gridButtonMap[sentinelMidRangedBarrackXPos][sentinelMidRangedBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Sentinel Mid Ranged Barrack has been Destroyed!!!");
				isDestroyedSentinelMidRangedBarrack = true;
				destroyedSentinelBarrackNumber++;
			}
		}

		if (!isDestroyedSentinelBotMeeleBarrack) {
			if (GridFrame.gridButtonMap[sentinelBotMeeleBarrackXPos][sentinelBotMeeleBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Sentinel Bot Meele Barrack has been Destroyed!!!");
				isDestroyedSentinelBotMeeleBarrack = true;
				destroyedSentinelBarrackNumber++;
			}
		}
		
		if (!isDestroyedSentinelBotRangedBarrack) {
			if (GridFrame.gridButtonMap[sentinelBotRangedBarrackXPos][sentinelBotRangedBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Sentinel Bot Ranged Barrack has been Destroyed!!!");
				isDestroyedSentinelBotRangedBarrack = true;
				destroyedSentinelBarrackNumber++;
			}
		}
		
		if (destroyedSentinelBarrackNumber == 6) {
			System.out.println("Scourge will spawn super creeps!!!");
			LineCreep.levelScourge = 30;
		}
		
		// not possible scenario, report error
		if (destroyedSentinelBarrackNumber > 6) 
			JOptionPane.showMessageDialog(null, "An error has occured for value of [destroyedSentinelBarrackNumber]! = " + destroyedSentinelBarrackNumber);
		
		// scourge  barracks
		
		if (!isDestroyedScourgeTopMeeleBarrack) {
			if (GridFrame.gridButtonMap[scourgeTopMeeleBarrackXPos][scourgeTopMeeleBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Scourge Top Meele Barrack has been Destroyed!!!");
				isDestroyedScourgeTopMeeleBarrack = true;
				destroyedScourgeBarrackNumber++;
			}
		}
		
		if (!isDestroyedScourgeTopRangedBarrack) {
			if (GridFrame.gridButtonMap[scourgeTopRangedBarrackXPos][scourgeTopRangedBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Scourge Top Ranged Barrack has been Destroyed!!!");
				isDestroyedScourgeTopRangedBarrack = true;
				destroyedScourgeBarrackNumber++;
			}
		}
		
		if (!isDestroyedScourgeMidMeeleBarrack) {
			if (GridFrame.gridButtonMap[scourgeMidMeeleBarrackXPos][scourgeMidMeeleBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Scourge Mid Meele Barrack has been Destroyed!!!");
				isDestroyedScourgeMidMeeleBarrack = true;
				destroyedScourgeBarrackNumber++;
			}
		}
		
		if (!isDestroyedScourgeMidRangedBarrack) {
			if (GridFrame.gridButtonMap[scourgeMidRangedBarrackXPos][scourgeMidRangedBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Scourge Mid Ranged Barrack has been Destroyed!!!");
				isDestroyedScourgeMidRangedBarrack = true;
				destroyedScourgeBarrackNumber++;
			}
		}
		
		if (!isDestroyedScourgeBotMeeleBarrack) {
			if (GridFrame.gridButtonMap[scourgeBotMeeleBarrackXPos][scourgeBotMeeleBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Scourge Bot Meele Barrack has been Destroyed!!!");
				isDestroyedScourgeBotMeeleBarrack = true;
				destroyedScourgeBarrackNumber++;
			}
		}
		
		if (!isDestroyedScourgeBotRangedBarrack) {
			if (GridFrame.gridButtonMap[scourgeBotRangedBarrackXPos][scourgeBotRangedBarrackYPos].getCharacter().isAlive() == false){
				System.out.println("Scourge Bot Ranged Barrack has been Destroyed!!!");
				isDestroyedScourgeBotRangedBarrack = true;
				destroyedScourgeBarrackNumber++;
			}
		}
		
		if (destroyedScourgeBarrackNumber == 6) {
			System.out.println("Sentinel will spawn super creeps!!!");
			LineCreep.levelSentinel = 30;
		}
		
		// not possible scenario, report error
		if (destroyedScourgeBarrackNumber > 6) 
			JOptionPane.showMessageDialog(null, "An error has occured for value of [destroyedScourgeBarrackNumber]! = " + destroyedScourgeBarrackNumber);
		
		
	}
}
