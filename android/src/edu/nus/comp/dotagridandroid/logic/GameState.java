package edu.nus.comp.dotagridandroid.logic;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.*;

import android.content.Context;
import android.graphics.*;
import edu.nus.comp.dotagridandroid.Closeable;
import edu.nus.comp.dotagridandroid.ui.renderers.scenes.SceneRenderer;
import edu.nus.comp.dotagridandroid.ui.renderers.*;
import edu.nus.comp.dotagridandroid.ui.event.*;

public class GameState implements Closeable {
	private int gridWidth, gridHeight;
	private float[] terrain;
	private Context context;
	private boolean initialised = false, initialising = false;
	private Thread initialisationProcess;
	private SceneRenderer currentSceneRenderer;
	private Map<String, Character> chars;
	private Map<String, GameObject> objs;
	private Map<String, int[]> objPositions;
	private Map<GridPointIndex, String> posReverseLookup;
	private Map<String, FloatBuffer[]> objModels;
	private Map<String, Texture2D> objTextures, objThumbnail;
	private Map<String, Item> itemShop;
	// game rule object
	private GameMaster gameMaster;
	private String playerCharacter;
	private int[] chosenGrid;
	public GameState() {
	}
	public void setContext(Context context) {
		this.context = context;
	}
	public void initialise(String playerCharacter) {
		this.playerCharacter = playerCharacter;
		if (initialised || initialising)
			return;
		chars = new ConcurrentHashMap<>();
		objs = new ConcurrentHashMap<>();
		objPositions = new ConcurrentHashMap<>();
		objModels = new ConcurrentHashMap<>();
		objTextures = new ConcurrentHashMap<>();
		objThumbnail = new ConcurrentHashMap<>();
		posReverseLookup = new ConcurrentHashMap<>();
		itemShop = new ConcurrentHashMap<>();
		initialisationProcess = new Thread() {
			@Override
			public void run() {
				initialising = true;
				chosenGrid = new int[2];
				gameMaster = new GameMaster();
				// TODO load characters
				chars.put("MyHero", new Hero("MyHero", 1, 0, "strength",
						100,
						100,
						100,
						2,
						100,
						100,
						100,
						100,
						1,
						100,
						100,
						100,
						100,
						100,
						100,
						100));
				chars.put("MyHero2", new Hero("MyHero", 1, 0, "strength",
						100,
						100,
						100,
						2,
						100,
						100,
						100,
						100,
						2,
						100,
						100,
						100,
						100,
						100,
						100,
						100));
				setCharacterPositions("MyHero", new int[]{0, 0});
				setCharacterPositions("MyHero2", new int[]{19,19});
				// TODO load character models
				chars.get("MyHero").setCharacterImage("MyHeroModel");	// actually this refers to an entry in objModels called MyHeroModel and a texture named MyHeroModel
				chars.get("MyHero2").setCharacterImage("MyHeroModel");
				objModels.put("MyHeroModel", new FloatBuffer[]{
						// 0: vertex
						BufferUtils.createFloatBuffer(36 * 4).put(new float[]{
								-1,1,1,1, -1,-1,1,1, 1,-1,1,1, 1,-1,1,1, 1,1,1,1, -1,1,1,1,
								1,1,1,1, 1,-1,1,1, 1,-1,-1,1, 1,-1,-1,1, 1,1,-1,1, 1,1,1,1,
								1,1,-1,1, 1,-1,-1,1, -1,-1,-1,1, -1,-1,-1,1, -1,1,-1,1, 1,1,-1,1,
								-1,1,-1,1, -1,-1,-1,1, -1,-1,1,1, -1,-1,1,1, -1,1,1,1, -1,1,-1,1,
								-1,1,-1,1, -1,1,1,1, 1,1,1,1, 1,1,1,1, 1,1,-1,1, -1,1,-1,1,
								-1,-1,1,1, -1,-1,-1,1, 1,-1,-1,1, 1,-1,-1,1, 1,-1,1,1, -1,-1,1,1
						}),
						// 1: texture
						BufferUtils.createFloatBuffer(36 * 2).put(new float[]{
								0,0, 0,1, 1,1, 1,1, 1,0, 0,0,
								0,0, 0,1, 1,1, 1,1, 1,0, 0,0,
								0,0, 0,1, 1,1, 1,1, 1,0, 0,0,
								0,0, 0,1, 1,1, 1,1, 1,0, 0,0,
								0,0, 0,1, 1,1, 1,1, 1,0, 0,0,
								0,0, 0,1, 1,1, 1,1, 1,0, 0,0,
						}),
						// 2: normal - vec4 - important!
						BufferUtils.createFloatBuffer(36 * 4).put(new float[]{
								0,0,1,0, 0,0,1,0, 0,0,1,0, 0,0,1,0, 0,0,1,0, 0,0,1,0,
								1,0,0,0, 1,0,0,0, 1,0,0,0, 1,0,0,0, 1,0,0,0, 1,0,0,0,
								0,0,-1,0, 0,0,-1,0, 0,0,-1,0, 0,0,-1,0, 0,0,-1,0, 0,0,-1,0,
								-1,0,0,0, -1,0,0,0, -1,0,0,0, -1,0,0,0, -1,0,0,0, -1,0,0,0,
								0,1,0,0, 0,1,0,0, 0,1,0,0, 0,1,0,0, 0,1,0,0, 0,1,0,0,
								0,-1,0,0, 0,-1,0,0, 0,-1,0,0, 0,-1,0,0, 0,-1,0,0, 0,-1,0,0
						})
				});
				// end
				chosenGrid = objPositions.get(GameState.this.playerCharacter).clone();
				initialised = true;
				initialising = false;
			}
		};
		initialisationProcess.start();
		// load resources
		System.gc();
		Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), edu.nus.comp.dotagridandroid.R.drawable.reimu_original);
		Texture2D tex = new Texture2D (tempBitmap);
		tempBitmap.recycle();
		objTextures.put("MyHeroModel", tex);
		objTextures.put("GridMapBackground", tex);
		try {initialisationProcess.join();} catch (Exception e) {e.printStackTrace();}
		initialising = false;
	}
	
	@Override
	public void close() {
		// release resources
		gameMaster = null;
		chars = null;
		objs = null;
		objPositions = null;
		objModels = null;
		for (Texture2D tex : objTextures.values())
			tex.close();
		for (Texture2D tex : objThumbnail.values())
			tex.close();
		objThumbnail = objTextures = null;
		itemShop = null;
		initialised = false;
	}
	
	public boolean isInitialised() {
		try {initialisationProcess.join();} catch (Exception e) {e.printStackTrace();}
		return initialised;
	}
	
	public void startTimer() {
		if (!initialised)
			return;
	}
	
	public void stopTimer() {
		if (!initialised)
			return;
	}

	public int getGridWidth() {
		return gridWidth;
	}

	public void setGridWidth(int gridWidth) {
		this.gridWidth = gridWidth;
	}

	public int getGridHeight() {
		return gridHeight;
	}

	public void setGridHeight(int gridHeight) {
		this.gridHeight = gridHeight;
	}

	public float[] getTerrain() {
		return isInitialised() ? terrain : null;
	}

	public void setTerrain(float[] terrain) {
		this.terrain = terrain;
	}
	
	// characters
	public String getPlayerCharacterName() {
		return playerCharacter;
	}
	
	public Map<String, Character> getCharacters() {
		return isInitialised() ? Collections.unmodifiableMap(chars) : null;
	}
	
	public Map<String, int[]> getCharacterPositions() {
		return isInitialised() ? Collections.unmodifiableMap(objPositions) : null;
	}
	
	protected void setCharacterPositions(String name, int[] position) {
		if (chars.containsKey(name)) {
			if (position != null && position.length == 2) {
				final GridPointIndex key = new GridPointIndex(position);
				if (posReverseLookup.containsKey(key))
					return;	// failed
				posReverseLookup.remove(key);
				posReverseLookup.put(key, name);
				objPositions.put(name, position.clone());
			} else {
				final GridPointIndex key = new GridPointIndex(objPositions.remove(name));
				posReverseLookup.remove(key);
			}
		}
	}
	
	public FloatBuffer[] getCharacterModel(String name) {
		return isInitialised() ? objModels.get(name).clone() : null;
	}
	
	public Texture2D getModelTexture(String name) {
		return isInitialised() ? objTextures.get(name) : null;
	}
	
	public Texture2D getModelThumbnail(String name) {
		return isInitialised() ? objThumbnail.get(name) : null;
	}
	
	public Map<String, Boolean> areActionPossible (Map<String, Map<String, Object>> actions) {
		Map<String, Boolean> possible = new HashMap<>();
		for (Map.Entry<String, Map<String,Object>> action : actions.entrySet())
			possible.put(action.getKey(), gameMaster.requestActionPossible(this, action.getKey(), action.getValue()));
		return Collections.unmodifiableMap(possible);
	}
	
	protected int[] getChosenGrid () {
		return chosenGrid.clone();
	}
	
	// interface interactions
	public void setCurrentSceneRenderer (SceneRenderer renderer) {
		currentSceneRenderer = renderer;
	}
	public void notifyUpdate (Map<String, Object> updates) {
		if (currentSceneRenderer != null)
			currentSceneRenderer.notifyUpdate(updates);
	}

	public void processEvent(ControlEvent e) {
		// TODO apply rules
		switch (e.extendedType) {
		// interface
		case "ChooseGrid":
			this.chosenGrid = (int[]) e.data.extendedData.get("Coordinates");
			gameMaster.applyRule(this, "ChooseGrid", e.data.extendedData);
			currentSceneRenderer.passEvent(e);
			break;
		case "RequestActionDetail":
			break;
		case "RequestItemList":
			break;
		case "RequestActionList":
			break;
		case "Cancel":
			this.chosenGrid = new int[] {-1,-1};
			gameMaster.applyRule(this, "Cancel", null);	// bounce back
			break;
		// game action
		case "GameAction":
			// send to game master
			gameMaster.applyRule(this, "GameAction", e.data.extendedData);
			break;
		case "GamePause":
			// pause game
			break;
		case "GameResume":
			break;
		case "GameSave":
			break;
		case "GameExit":
			break;
		case "TestButton":
			System.out.println("Test Button Pressed");
			break;
		}
		/// Use Hard code game rules
	}
}
