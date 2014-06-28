package edu.nus.comp.dotagridandroid.appsupport;

import org.json.*;

import java.util.*;

import edu.nus.comp.dotagridandroid.logic.*;
import edu.nus.comp.dotagridandroid.Closeable;

public class ExtensionEngine implements Closeable {
	static {
		System.loadLibrary("appsupport");
	}
	private long ptr;
	private GameState state;
	private static final native void loadScript(long ptr, String script);
	private static final native void execute(long ptr);
	private final native void applyRule(long ptr, String character, String action, String options);
	
	protected ExtensionEngine(long ptr) {
		this.ptr = ptr;
	}
	
	@Override
	public void close () {
		AppNativeAPI.destroyExtensionEngine(ptr);
	}
	
	public void loadScript(String script) {
		loadScript(ptr, script);
	}
	
	public void execute() {
		execute(ptr);
	}
	
	public void attachGameState(GameState state) {
		this.state = state;
	}
	
	public void applyRule(String character, String action, String options) {
		applyRule(ptr, character, action, options);
	}
	
	private void notifyUpdate(String updates) {
		try {
			JSONObject object = new JSONObject(updates);
			Map<String, Object> map = JsonConverter.JsonToMap(object);
			state.notifyUpdate(map);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void turnNextRound() {
		state.turnNextRound();
	}
	
	private String getCharacterPosition(String characters) {
		try {
			JSONArray array = new JSONArray(characters);
			JSONObject object = new JSONObject();
			for (int i = 0; i < array.length(); i++)
				object.put(array.getString(i), JsonConverter.ArrayToJson(state.getCharacterPositions().get(array.getString(i))));
			return object.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void setCharacterPosition(String character, String position) {
		try {
			System.out.println("setCharacterPosition JAVA called");
			JSONArray array = new JSONArray(position);
			if (array.length() != 2)
				return;
			int[] pos = {array.getInt(0), array.getInt(1)};
			state.setCharacterPosition(character, pos);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private String getSelectedGrid() {
		return JsonConverter.ArrayToJson(state.getChosenGrid()).toString();
	}
}