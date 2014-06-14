package edu.nus.comp.dotagridandroid.logic;
import java.util.*;

import edu.nus.comp.dotagridandroid.ui.event.*;
public class GameMaster {
	public void applyRule(GameState stateMachine, String actionName, Map<String, Object> options) {
		switch (actionName) {
		case "ChooseGrid": {
			// TODO store and action verb
			// assume "Move"
			final String playerCharName = stateMachine.getPlayerCharacter(); 
			final Character playerChar = stateMachine.getCharacters().get(playerCharName);
			final int[] reqPos = (int[]) options.get("Coordinate");
			final int[] prevPos = stateMachine.getCharacterPositions().get(stateMachine.getPlayerCharacter());
			// calculate AP
			final double actionPointUsed = playerChar.APUsedInMovingOneGrid() * (Math.abs(reqPos[0] - prevPos[0]) + Math.abs(reqPos[1] - prevPos[1]));
			final double currentActionPoint = playerChar.getCurrentActionPoint() - actionPointUsed;
//			if (currentActionPoint < 0)
//				return;
			playerChar.setCurrentActionPoint((int) currentActionPoint);
			// move
			stateMachine.setCharacterPositions(playerCharName, (int[]) options.get("Coordinate"));
			Set<String> characters = Collections.singleton(playerCharName);
			Map<String, Object> updates = Collections.singletonMap("Characters", (Object) characters);
			stateMachine.notifyUpdate(updates);
			break;
		}
		}
	}
	// Hard coded rules go below
}
