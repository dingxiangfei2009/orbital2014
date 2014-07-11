package edu.nus.comp.dotagridandroid.logic;

public class GameCharacterAutomaton {
	public static void autoAction(GameState stateMachine, String character) {
		if (stateMachine.isExtensionEnabled())
			stateMachine.getExtensionEngine();
		else
			switch (stateMachine.getCharacterType(character)) {
			case GameObject.GAMEOBJECT_TYPE_HERO:
				autoActionHero(stateMachine, character);
				break;
			case GameObject.GAMEOBJECT_TYPE_LINECREEP:
				autoActionLinecreep(stateMachine, character);
				break;
			case GameObject.GAMEOBJECT_TYPE_TOWER:
				autoActionTower(stateMachine, character);
				break;
			}
	}
	
	// game state:
	// getCharacterAtPosition(int[] {x, y}) returns character name (String)
	// setCharacterPosition(String character, int[] pos {x, y}
	// get/setCharacterProperty(String character, String property[, Object value])
	// getTerrain() return float[]
	// getTerrainHeight(int[] {x, y}) return float
	// getTerrainType(int[] {x, y}) return int enum in GameState
	
	// put game character shared auto routine below
	
	// TODO
	
	// put specialised auto routine below
	
	// calculate greatest benefit, attacking tower is highest, then hero, and the least linecreeps
	
	private static void autoActionHero(GameState stateMachine, String character) {
		// Hero AI here
		// Move to target
		double moveTargetBenefit = 0;
		// Move to enemy
		double moveEnemyBenefit = 0;
	}
	
	private static void autoActionLinecreep(GameState stateMachine, String character) {
		// Linecreep AI here
	}
	
	private static void autoActionTower(GameState stateMachine, String character) {
		// Tower AI here
	}
}