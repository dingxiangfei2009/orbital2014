package edu.nus.comp.dotagridandroid.ui.renderers;

import java.nio.*;
import java.util.*;
import java.util.concurrent.*;

import edu.nus.comp.dotagridandroid.MainRenderer;
import edu.nus.comp.dotagridandroid.appsupport.GridRendererGraphicsImpl;
import edu.nus.comp.dotagridandroid.logic.*;
import edu.nus.comp.dotagridandroid.ui.event.ControlEvent;
import static android.opengl.GLES20.*;
import static edu.nus.comp.dotagridandroid.math.RenderMaths.*;
@SuppressWarnings("unchecked")
public class GridRenderer implements Renderer {
	public static final float BOARD_Z_COORD = .05f;
	public static final float BASE_ZOOM_FACTOR = 0.5f;
	public static final int SHADOW_MAP_TEXTURE_DIMENSION = 2048;
	private static final float LIGHT_MAX_RADIUS = 5;
	private static final float LIGHT_HEIGHT_OFFSET = 1.5f;
	
	private GridRendererGraphicsImpl graphicsImpl;
	private GLResourceManager vBufMan;
	private Map<String, Texture2D> textures;
	private final int rows, columns;
	private GameLogicManager manager;
	private MainRenderer.GraphicsResponder responder;
	private Texture2D mapTexture;
	
	private boolean ready = false;
	// configurations
	// aspect ratio - width : height
	private float ratio;
	// camera - x, y, z, lookX, lookY, lookZ, near, far
	private final float[] cameraParams = {
			0, 0, 0.8f,	// 0: posX, 1: posY, 2: posZ
			0, 0, .01f,	// 3: lookX, 4: lookY, 5: lookZ
			0, 1, 0,	// 6: upX, 7: upY, 8: upZ
			.01f, 5, PI / 3f	// 9: near (const), 10: far (const), 11: FoV (in radians, const)
	};
	// map rotation - in radians
	private float mapRotation = 0;
	// possessing resources
	private GenericProgram gridProgram, mapProgram, shadowProgram, shadowObjProgram;
	private float[]
			model = IdentityMatrix4x4(),
			view = IdentityMatrix4x4(),
			projection = IdentityMatrix4x4();
	private float[] selectGridMatrix, gridLines, terrain;
	// temporary map characteristics - do not use after init
	private float[] mapTerrain, mapNormalCoord, mapTextureCoord;
	private int[] gridLinesIndex;
	// map buffers
	private int mapVBO;
	private FloatBuffer mapBuf;//, mapTerrainBuf, mapNormalCoordBuf, mapTextureCoordBuf;
	// resource - renderers
	private NormalGenerator normalGen;
	// gesture states
	private boolean hasSelection = false;
	private boolean processingTranslation = false, processingPerspective = false;
	// translation
	private final float[] translationDeltaCoord = {-1,-1};
	// perspective
	private final float[] perspectiveStartCoord = {-1,-1,-1,-1};
	private final float[] perspectiveLastCoord = {0,0,0,0};
	private final float[] perspectiveLookAt = {0,0};
	private float perspectiveStartDeltaVecX, perspectiveStartDeltaVecY, perspectiveRotationAngle, perspectiveCameraZoom;
	// for tap monitoring - calculate intervals between double taps
	private final long[] tapTimeQueue = {0,0,0};
	private final float[] tapXQueue = {0,0,0}, tapYQueue = {0,0,0};
	private final byte tapQueueLength = 3;
	private byte tapQueueHead = 0;
	private boolean doubleTap = false;
	// ray tracing
	private float[] orgGridPoint;
	private int[] orgGridIndex;
	private int[][] highlightedGridIndex;
	// light sources
	public final int MAX_LIGHT_SOURCES = CommonShaders.MAX_LIGHT_SOURCE;
	private final float[] lightObserver = {0,0,0};
	// light config - 0: source.x, 1: source.y, 2: source.z, 3: color.r, 4: color.g, 5: color.b, 6: specular, 7: attenuation, 8: sight
	private final Map<String, float[]> lightSrc = new ConcurrentHashMap<>(), lightViews = new ConcurrentHashMap<>();
	private final Map<String, int[]> lightGridPosition = new ConcurrentHashMap<>();
	private final Map<String, Integer> lightGridRange = new ConcurrentHashMap<>();
	private final Map<String, Boolean> lightDirty = new ConcurrentHashMap<>(), lightOn = new ConcurrentHashMap<>();
	// light - shadowMaps
	private int frameBuf, renderBuf;
	private final Map<String, Integer> shadowMaps = new ConcurrentHashMap<>();
	private final float[] lightProjection;
	// drawables
	private final Map<String, Integer> drawableModelHandlers = new ConcurrentHashMap<>();
	private final Map<String, Integer> drawableModelSizes = new ConcurrentHashMap<>();
	private final Map<String, String> drawableTextures = new ConcurrentHashMap<>();
	private final Map<String, float[]> drawableModels = new ConcurrentHashMap<>();
	private final Map<String, Boolean> drawableVisible = new ConcurrentHashMap<>();
	private final Map<String, int[]> drawableGridPositions = new ConcurrentHashMap<>();
	// multithreading
	private Thread computeTask;
	// map resolution for interpolation
	private final int resolution;
	public GridRenderer (int columns, int rows, float[] terrain) {
		graphicsImpl = new GridRendererGraphicsImpl();
		if (columns * rows > 5000)
			resolution = 2;
		else if (columns * rows > 1000)
			resolution = 4;
		else if (columns * rows > 200)
			resolution = 8;
		else
			resolution = 16;
		mapTerrain = new float[(rows * resolution + 1) * (columns * resolution + 1) * 4];
		mapTextureCoord = new float[(rows * resolution + 1) * (columns * resolution + 1) * 2];
		mapNormalCoord = new float[(rows * resolution + 1) * (columns * resolution + 1) * 2];
		this.terrain = terrain;
		this.rows = rows;
		this.columns = columns;
		gridProgram = new GenericProgram(CommonShaders.VS_IDENTITY, CommonShaders.FS_IDENTITY);
		// configure map
		mapProgram = new GenericProgram (CommonShaders.VS_IDENTITY_SPECIAL_LIGHTING, CommonShaders.FS_IDENTITY_SPECIAL_LIGHTING);
		shadowProgram = new GenericProgram (CommonShaders.VS_IDENTITY_SPECIAL_SHADOW, CommonShaders.FS_IDENTITY_SPECIAL_SHADOW);
		shadowObjProgram = new GenericProgram (CommonShaders.VS_IDENTITY_SPECIAL_LIGHTING_UNIFORMNORMAL, CommonShaders.FS_IDENTITY_SPECIAL_LIGHTING_UNIFORMNORMAL);
		// coord configurations
		selectGridMatrix = FlatMatrix4x4Multiplication(FlatScalingMatrix4x4(1f/columns, 1f/rows, 1), FlatTranslationMatrix4x4(1,1,0));
		// calculate model
		calculateModel();
		// board IS at the origin so not need translation
		computeTask = new Thread() {
			@Override
			public void run() {
				prepareGrid();
			}
		};
		computeTask.start();
		// calculate view
		calculateView();
		// shadow mapping - resource generator
		final float lightRadius;
		if (rows > columns)
			lightRadius = BASE_ZOOM_FACTOR * LIGHT_MAX_RADIUS * 2 / columns;
		else
			lightRadius = BASE_ZOOM_FACTOR * LIGHT_MAX_RADIUS * 2 / rows;
		lightProjection = FlatPerspectiveMatrix4x4(.01f, 2, -lightRadius, lightRadius, lightRadius, -lightRadius);
		int[] buf = new int[1];
		glGenBuffers(1, buf, 0);
		mapVBO = buf[0];
		glGenFramebuffers(1, buf, 0);
		frameBuf = buf[0];
		glGenRenderbuffers(1, buf, 0);
		renderBuf = buf[0];
		glBindRenderbuffer(GL_RENDERBUFFER, renderBuf);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, SHADOW_MAP_TEXTURE_DIMENSION, SHADOW_MAP_TEXTURE_DIMENSION);
	}
	@Override
	public void setGraphicsResponder(MainRenderer.GraphicsResponder mainRenderer) {
		responder = mainRenderer;
	}
	@Override
	public void setMVP(float[] model, float[] view, float[] projection) {}
	@Override
	public void setGLResourceManager(GLResourceManager manager) {
		vBufMan = manager;
	};
	@Override
	public void setFrameBufferHandler(int framebuffer) {}
	@Override
	public void setTexture2D(Map<String, Texture2D> textures) {
		this.textures = textures;
	}
	@Override
	public void setAspectRatio(float ratio) {
		// projection parameters - constant for this aspect ratio
		this.ratio = ratio;
		final float lens_radius = cameraParams[9] * (float) Math.tan(cameraParams[11] / 2);
		if (ratio > 1)
			projection = FlatPerspectiveMatrix4x4(cameraParams[9], cameraParams[10], -lens_radius, lens_radius, lens_radius / ratio, -lens_radius / ratio);
		else
			projection = FlatPerspectiveMatrix4x4(cameraParams[9], cameraParams[10], -lens_radius * ratio, lens_radius * ratio, lens_radius, -lens_radius);
		graphicsImpl.setProjectionMatrix(projection);
//		textRender.setAspectRatio(ratio);
	}
	@Override
	public void setGameLogicManager(GameLogicManager manager) {
		this.manager = manager;
	}
	private float bicubic(float f00, float f10, float f01, float f11, float x, float y) {
		return Vector4CubicInterpolation(new float[]{
				Vector4CubicInterpolation(new float[]{f00, f10, 0, 0}, x),
				Vector4CubicInterpolation(new float[]{f01, f11, 0, 0}, x),
				0, 0
		}, y);
	}
	private void prepareGrid() {
		float[] v = new float[4 * 2 * (rows + columns)];
		int c = 0;
		for (int i = 0; i <= columns; i++) {
			v[c++] = 2f / columns * i - 1;
			v[c++] = 1;
			v[c++] = 1;
			v[c++] = 1;
		}
		for (int i = 0; i <= columns; i++) {
			v[c++] = 2f / columns * i -1;
			v[c++] = -1;
			v[c++] = 1;
			v[c++] = 1;
		}
		for (int i = 1; i < rows; i++) {
			v[c++] = -1;
			v[c++] = 2f / rows * i - 1;
			v[c++] = 1;
			v[c++] = 1;
		}
		for (int i = 1; i < rows; i++) {
			v[c++] = 1;
			v[c++] = 2f / rows * i - 1;
			v[c++] = 1;
			v[c++] = 1;
		}
		gridLines = v;
		c = 0;
		int[] idx = new int[2 * (rows + columns + 2)];
		for (int i = 0; i <= columns; i++) {
			idx[c++] = i;
			idx[c++] = columns + i + 1;
		}
		idx[c++] = 0;
		idx[c++] = columns;
		idx[c++] = columns + 1;
		idx[c++] = 2 * columns + 1;
		for (int i = 2; i <= rows; i++) {
			idx[c++] = 2 * columns + i;
			idx[c++] = 2 * columns + i + rows - 1;
		}
		gridLinesIndex = idx;
		final int arrWidth = columns * resolution + 1;
		// Terrain Intrapolation - Bilinear
		for (int i = 0; i < rows - 1; i++) {
			final int offset = i * arrWidth * resolution;
			for (int j = 0; j < columns - 1; j++) {
				int cellOffset = offset + j * resolution + resolution / 2 * arrWidth + resolution / 2;
				for (int s = 0; s <= resolution; s++)
					for (int t = 0; t <= resolution; t++) {
						mapTerrain[4 * (cellOffset + arrWidth * s + t)] = (j + t / (float) resolution + .5f) / columns * 2 - 1;
						mapTerrain[4 * (cellOffset + arrWidth * s + t) + 1] = (i + s / (float) resolution + .5f) / rows * 2 - 1;
						mapTerrain[4 * (cellOffset + arrWidth * s + t) + 2]
								= bicubic(terrain[i * columns + j], terrain[i * columns + j + 1],
										terrain[(i + 1) * columns + j], terrain[(i + 1) * columns + j + 1],
										t / (float) resolution, s / (float) resolution);
						mapTerrain[4 * (cellOffset + arrWidth * s + t) + 3] = 1;
					}
			}
		}
		// Edges
		for (int s = 0; s <= resolution / 2; s++)
			for (int t = 0; t <= resolution / 2; t++) {
				int offset = arrWidth * s + t;
				// bottom left
				mapTerrain[4 * offset] = t / (float) resolution / columns * 2 - 1;
				mapTerrain[4 * offset + 1] = s / (float) resolution / rows * 2 - 1;
				mapTerrain[4 * offset + 2] = terrain[0];
				mapTerrain[4 * offset + 3] = 1;
				// bottom right
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2)]
						= (columns - 1 + t / (float) resolution + .5f) / columns * 2 - 1;
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2) + 1]
						= s / (float) resolution / rows * 2 - 1;
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2) + 2]
						= terrain[columns - 1];
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2) + 3]
						= 1;
				//top left
				offset += (rows - 1) * arrWidth * resolution + resolution / 2 * arrWidth;
				mapTerrain[4 * offset] = t / (float) resolution / columns * 2 - 1;
				mapTerrain[4 * offset + 1] = (rows - 1 + .5f + s / (float) resolution) / rows * 2 - 1;
				mapTerrain[4 * offset + 2] = terrain[(rows - 1) * columns];
				mapTerrain[4 * offset + 3] = 1;
				// top right
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2)]
						= (columns - 1 + t / (float) resolution + .5f) / columns * 2 - 1;
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2) + 1]
						= (rows - 1 + .5f + s / (float) resolution) / rows * 2 - 1;
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2) + 2]
						= terrain[rows * columns - 1];
				mapTerrain[4 * (offset + (columns - 1) * resolution + resolution / 2) + 3]
						= 1;
			}
		for (int i = 0; i < rows - 1; i++) {
			final int offset = (i * resolution + resolution / 2) * arrWidth;
			for (int s = 0; s <= resolution; s++)
				for (int t = 0; t <= resolution / 2; t++) {
					final int cellOffset = offset + s * arrWidth + t;
					// left side
					mapTerrain[4 * cellOffset] = t / (float) resolution / columns * 2 - 1;
					mapTerrain[4 * cellOffset + 1] = (i + .5f + s / (float) resolution) / rows * 2 - 1;
					mapTerrain[4 * cellOffset + 2]
							= bicubic(terrain[i * columns], terrain[i * columns],
									terrain[(i + 1) * columns], terrain[(i + 1) * columns],
									0, s / (float) resolution);
					mapTerrain[4 * cellOffset + 3] = 1;
					// right side
					mapTerrain[4 * (cellOffset + (columns - 1) * resolution + resolution / 2)]
							= (columns - 1 + .5f + t / (float) resolution) / columns * 2 - 1;
					mapTerrain[4 * (cellOffset + (columns - 1) * resolution + resolution / 2) + 1]
							= (i + .5f + s / (float) resolution) / rows * 2 - 1;
					mapTerrain[4 * (cellOffset + (columns - 1) * resolution + resolution / 2) + 2]
							= bicubic (terrain[(i + 1) * columns - 1], terrain[(i + 1) * columns - 1],
									terrain[(i + 2) * columns - 1], terrain[(i + 2) * columns - 1],
									0, s / (float) resolution);
					mapTerrain[4 * (cellOffset + (columns - 1) * resolution + resolution / 2) + 3]
							= 1;
				}
		}
		for (int i = 0; i < columns - 1; i++) {
			final int offset = i * resolution + resolution / 2;
			for (int s = 0; s <= resolution / 2; s++)
				for (int t = 0; t <= resolution; t++) {
					final int cellOffset = offset + s * arrWidth + t;
					// bottom side
					mapTerrain[4 * cellOffset] = (i + .5f + t / (float) resolution) / columns * 2 - 1;
					mapTerrain[4 * cellOffset + 1] = s / (float) resolution / rows * 2 - 1;
					mapTerrain[4 * cellOffset + 2]
							= bicubic (terrain[i], terrain[i + 1], terrain[i], terrain[i + 1],
									t / (float) resolution, 0);
					mapTerrain[4 * cellOffset + 3] = 1;
					// top side
					mapTerrain[4 * (cellOffset + (rows - 1) * resolution * arrWidth + resolution * arrWidth / 2)]
							= (i + .5f + t / (float) resolution) / columns * 2 - 1;
					mapTerrain[4 * (cellOffset + (rows - 1) * resolution * arrWidth + resolution * arrWidth / 2) + 1]
							= (rows - 1 + .5f + s / (float) resolution) / rows * 2 - 1;
					mapTerrain[4 * (cellOffset + (rows - 1) * resolution * arrWidth + resolution * arrWidth / 2) + 2]
							= bicubic (terrain[(rows - 1) * columns + i], terrain[(rows - 1) * columns + i + 1],
									terrain[(rows - 1) * columns + i], terrain[(rows - 1) * columns + i + 1],
									t / (float) resolution, 0);
					mapTerrain[4 * (cellOffset + (rows - 1) * resolution * arrWidth + resolution * arrWidth / 2) + 3]
							= 1;
				}
		}
		// generate texture and normal coords for map drawing, as well as indexes
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				for (int s = 0; s <= resolution; s++)
					for (int t = 0; t <= resolution; t++) {
						mapNormalCoord[2 * (i * resolution * arrWidth + s * arrWidth + j * resolution + t)]
								= (j + t / (float) resolution) / columns;
						mapNormalCoord[2 * (i * resolution * arrWidth + s * arrWidth + j * resolution + t) + 1]
								= (i + s / (float) resolution) / rows;
						mapTextureCoord[2 * (i * resolution * arrWidth + s * arrWidth + j * resolution + t)]
								= (j + t / (float) resolution) / columns;
						mapTextureCoord[2 * (i * resolution * arrWidth + s * arrWidth + j * resolution + t) + 1]
								= 1 - mapNormalCoord[2 * (i * resolution * arrWidth + s * arrWidth + j * resolution + t) + 1];
					}
		c = 0;
		mapBuf = BufferUtils.createFloatBuffer(8 * rows * resolution * arrWidth * 2);
		for (int i = 0; i < rows * resolution; i++) {
			if ((i & 0x1) > 0)	// odd
				for (int j = columns * resolution; j >= 0; j--) {
					mapBuf.put(mapTerrain, 4 * (i * arrWidth + j), 4)
						.put(mapTextureCoord, 2 * (i * arrWidth + j), 2)
						.put(mapNormalCoord, 2 * (i * arrWidth + j), 2);
					mapBuf.put(mapTerrain, 4 * ((i + 1) * arrWidth + j), 4)
						.put(mapTextureCoord, 2 * ((i + 1) * arrWidth + j), 2)
						.put(mapNormalCoord, 2 * ((i + 1) * arrWidth + j), 2);
				}
			else
				for (int j = 0; j <= columns * resolution; j++) {
					mapBuf.put(mapTerrain, 4 * (i * arrWidth + j), 4)
						.put(mapTextureCoord, 2 * (i * arrWidth + j), 2)
						.put(mapNormalCoord, 2 * (i * arrWidth + j), 2);
					mapBuf.put(mapTerrain, 4 * ((i + 1) * arrWidth + j), 4)
						.put(mapTextureCoord, 2 * ((i + 1) * arrWidth + j), 2)
						.put(mapNormalCoord, 2 * ((i + 1) * arrWidth + j), 2);
				}
		}
	}
	private void prepareObjects() {
		final GameState state = manager.getCurrentGameState();
		final Map<String, GameCharacter> chars = state.getCharacters();
		final String mainCharacter = state.getPlayerCharacterName();
		final int mainCharacterTeamNumber = chars.get(mainCharacter).getTeamNumber();
		// TODO draw character
		// if friendly, always display
		// if enemy, display in close proximity
		// for now, just draw everything
		for (String name : chars.keySet()) {
			lightOn.put(name, false);
			graphicsImpl.setLightOn(name, false);
			final String charModelName = chars.get(name).getCharacterImage();
			final int[] pos = state.getCharacterPosition(name);
			if (pos == null || !chars.get(name).isAlive()) {
				drawableVisible.put(name, false);
				graphicsImpl.setDrawableVisible(name, false);
				continue;
			}
			drawableModelHandlers.put(name, state.getCharacterModel(charModelName));
			drawableModelSizes.put(name, state.getCharacterModelSize(charModelName));
			drawableTextures.put(name, charModelName);
			drawableVisible.put(name, true);	// applicable for visibility skill
			drawableGridPositions.put(name, pos);
			final float scalingFactor = Math.min(1f / columns, 1f / rows);
			drawableModels.put(name, FlatMatrix4x4Multiplication(
					FlatTranslationMatrix4x4(2f / columns * pos[0] - 1,2f / rows * pos[1] - 1, terrain[pos[0] + pos[1] * columns]),
					scalingFactor < BOARD_Z_COORD / 2 ? FlatScalingMatrix4x4(scalingFactor, scalingFactor, scalingFactor / BOARD_Z_COORD / 2) : FlatScalingMatrix4x4(BOARD_Z_COORD / 2, BOARD_Z_COORD / 2, .5f),
					FlatTranslationMatrix4x4(1,1,1)));
			graphicsImpl.setDrawable(name, state.getModelTexture(drawableTextures.get(name)).getTexture(),
					drawableModelHandlers.get(name), drawableModelSizes.get(name), drawableModels.get(name), pos);
			graphicsImpl.setDrawableVisible(name, true);
			// configure light
			final float[] lightPos
				= FlatMatrix4x4Vector4Multiplication(model,
						new float[]{
							2 * (.5f + pos[0]) / columns - 1,
							2 * (.5f + pos[1]) / rows - 1,
							LIGHT_HEIGHT_OFFSET + terrain[pos[0] + pos[1] * columns],	// 1 should be changed
							1});
			if (chars.get(name).getTeamNumber() == mainCharacterTeamNumber) {
				final float[] config = {
						lightPos[0] / lightPos[3], lightPos[1] / lightPos[3], lightPos[2] / lightPos[3],
						1, 1, 1,	// TODO change to hero's color and sight
						5, 2, chars.get(name).getSight() / (float) (rows > columns ? rows : columns)};
				final float hpRatio = chars.get(name).getCurrentHP() / (float) chars.get(name).getMaxHP();
				if (hpRatio > .5f)
					config[5] = hpRatio * 2 - 1;
				else if (hpRatio > .3f) {
					config[4] = (hpRatio - .3f) / .2f;
					config[5] = 0;
				} else {
					config[3] = hpRatio / .3f;
					config[4] = 0;
					config[5] = 0;
				}
				lightSrc.put(name, config);
				lightOn.put(name, chars.get(name).isAlive());
				lightGridPosition.put(name, pos);
				lightGridRange.put(name, chars.get(name).getSight());
				graphicsImpl.setLight(name, lightSrc.get(name), lightOn.get(name), pos, lightGridRange.get(name));
			}
		}
		// put all lightSrc dirty and check visibility
		for (String name : lightSrc.keySet()) 
			if (lightOn.get(name))
				lightDirty.put(name, true);
		for (String drawableName : drawableVisible.keySet())
			if (drawableVisible.get(drawableName)) {
				boolean visible = false;
				for (String light : lightSrc.keySet())
					visible |= lightOn.get(light) && Math.pow(lightGridPosition.get(light)[0] - drawableGridPositions.get(drawableName)[0], 2)
							+ Math.pow(lightGridPosition.get(light)[1] - drawableGridPositions.get(drawableName)[1], 2)
							<= Math.pow(lightGridRange.get(light), 2);
				drawableVisible.put(drawableName, visible);
				graphicsImpl.setDrawableVisible(drawableName, visible);
			}
	}
	@Override
	public void setRenderReady() {
	}
	
	@Override
	public void notifyUpdate(Map<String, Object> updates) {
		GameState state = manager.getCurrentGameState();
		final int[] pos = state.getCharacterPosition(state.getPlayerCharacterName());
		highlightedGridIndex = null;
		if ("CLEAR-DRAWABLE".equals(updates.get("APPLICATION")))
			graphicsImpl.clearDrawable();
		if (updates.containsKey("Characters"))
			prepareObjects();
		if (updates.containsKey("HighlightGrid")) {
			int c;
			List<Object> grids = (List<Object>) updates.get("HighlightGrid");
			if (pos != null) {
				highlightedGridIndex = new int[grids.size() + 1][2];
				highlightedGridIndex[0][0] = pos[0];
				highlightedGridIndex[0][1] = pos[1];
				c = 1;
			} else {
				highlightedGridIndex = new int[grids.size()][2];
				c = 0;
			}
			for (Object grid : grids) {
				highlightedGridIndex[c][0] = ((Number) ((List<Object>) grid).get(0)).intValue();
				highlightedGridIndex[c][1] = ((Number) ((List<Object>) grid).get(1)).intValue();
				c++;
			}
			graphicsImpl.setHighlightedGrid(true, highlightedGridIndex);
		} else if (pos != null)
			graphicsImpl.setHighlightedGrid(true, new int[][]{pos});
		else
			graphicsImpl.setHighlightedGrid(false, null);
		responder.updateGraphics();
	}
	@Override
	public boolean getReadyState() {
		if (!ready) {
			try {computeTask.join();} catch (InterruptedException e) {e.printStackTrace();}
			int[] buf = new int[2];
			glGenBuffers(2, buf, 0);
			
			glBindBuffer(GL_ARRAY_BUFFER, buf[0]);
			glBufferData(GL_ARRAY_BUFFER, Float.SIZE / 8 * gridLines.length, BufferUtils.createFloatBuffer(gridLines.length).put(gridLines).position(0), GL_STATIC_DRAW);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buf[1]);
			ShortBuffer idxDirectBuffer = BufferUtils.createShortBuffer(gridLinesIndex.length);
			for (int i = 0; i < gridLinesIndex.length; i++)
				idxDirectBuffer.put((short) (0xffff & gridLinesIndex[i]));
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, Short.SIZE / 8 * gridLinesIndex.length, idxDirectBuffer.position(0), GL_STATIC_DRAW);
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
			
			vBufMan.setVertexBuffer("GridPointBuffer", gridLines);
			vBufMan.setIndexBuffer("GridPointMeshIndex", gridLinesIndex);
			mapTexture = manager.getCurrentGameState().getModelTexture("GridMapBackground");
			normalGen = new NormalGenerator(columns, rows, resolution, mapTerrain, model, mapTexture.getWidth(), mapTexture.getHeight());
			normalGen.setRenderReady();
			glBindBuffer(GL_ARRAY_BUFFER, mapVBO);
			glBufferData(GL_ARRAY_BUFFER, Float.SIZE / 8 * 8 * (rows * resolution * (resolution * columns + 1) * 2), mapBuf.position(0), GL_STATIC_DRAW);
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			
			graphicsImpl.initialise(columns, rows, CommonShaders.VS_IDENTITY, CommonShaders.FS_IDENTITY, CommonShaders.VS_IDENTITY_SPECIAL_LIGHTING, CommonShaders.FS_IDENTITY_SPECIAL_LIGHTING, CommonShaders.VS_IDENTITY_SPECIAL_SHADOW, CommonShaders.FS_IDENTITY_SPECIAL_SHADOW, CommonShaders.VS_IDENTITY_SPECIAL_LIGHTING_UNIFORMNORMAL, CommonShaders.FS_IDENTITY_SPECIAL_LIGHTING_UNIFORMNORMAL, resolution, buf[0], buf[1], mapTexture.getTexture(), mapVBO, normalGen.getNormalTexture(), lightProjection);
			
			mapBuf = null;
			gridLines = null; gridLinesIndex = null;
			mapTerrain = mapNormalCoord = mapTextureCoord = null;
			prepareObjects();
			ready = true;
		}
		for (String light : lightDirty.keySet())
			if (lightDirty.get(light)) {
				configureShadow(light);
				lightDirty.put(light, false);
			}
		return true;
	}
	/*// draw functions
	private void drawGrid() {
		int vOffset = vBufMan.getVertexBufferOffset("GridPointBuffer"),
			iOffset = vBufMan.getIndexBufferOffset("GridPointMeshIndex");
		int vPosition = glGetAttribLocation(gridProgram.getProgramId(), "vPosition"),
			mModel = glGetUniformLocation(gridProgram.getProgramId(), "model"),
			mView = glGetUniformLocation(gridProgram.getProgramId(), "view"),
			mProjection = glGetUniformLocation(gridProgram.getProgramId(), "projection"),
			vColor = glGetUniformLocation(gridProgram.getProgramId(), "vColor");
		glUseProgram(gridProgram.getProgramId());
		glUniformMatrix4fv(mModel, 1, false, model, 0);
		glUniformMatrix4fv(mView, 1, false, view, 0);
		glUniformMatrix4fv(mProjection, 1, false, projection, 0);
		glUniform4f(vColor, 1, 0, 0, 1);
		glBindBuffer(GL_ARRAY_BUFFER, vBufMan.getVertexBuffer());
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vBufMan.getIndexBuffer());
		glVertexAttribPointer(vPosition, 4, GL_FLOAT, false, 0, vOffset);
		glEnableVertexAttribArray(vPosition);
		glDrawElements(GL_LINES, 2 * (columns + rows + 2), GL_UNSIGNED_SHORT, iOffset);
		glDisableVertexAttribArray(vPosition);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
	}
	private void drawMap() {
		final float[] matMVP = FlatMatrix4x4Multiplication(projection, view, model); 
		// map
		int vPosition = glGetAttribLocation(mapProgram.getProgramId(), "vPosition"),
			mModel = glGetUniformLocation(mapProgram.getProgramId(), "model"),
			mView = glGetUniformLocation(mapProgram.getProgramId(), "view"),
			mProjection = glGetUniformLocation(mapProgram.getProgramId(), "projection"),
			cameraPosition = glGetUniformLocation(mapProgram.getProgramId(), "camera"),
			textureLocation = glGetUniformLocation(mapProgram.getProgramId(), "texture"),
			normalLocation = glGetUniformLocation(mapProgram.getProgramId(), "normal"),
			shadowLocation = glGetUniformLocation(mapProgram.getProgramId(), "shadow"),
			textureCoord = glGetAttribLocation(mapProgram.getProgramId(), "textureCoord"),
			normalCoord = glGetAttribLocation(mapProgram.getProgramId(), "normalCoord"),
			source = glGetUniformLocation(mapProgram.getProgramId(), "light.source"),
			color = glGetUniformLocation(mapProgram.getProgramId(), "light.color"),
			specular = glGetUniformLocation(mapProgram.getProgramId(), "light.specular"),
			attenuation = glGetUniformLocation(mapProgram.getProgramId(), "light.attenuation"),
			sight = glGetUniformLocation(mapProgram.getProgramId(), "light.sight"),
			mLight = glGetUniformLocation(mapProgram.getProgramId(), "lightTransform");
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		boolean firstTime = true;
		glBlendFunc(GL_ONE, GL_ONE);
		glUseProgram(mapProgram.getProgramId());
		glUniformMatrix4fv(mModel, 1, false, model, 0);
		glUniformMatrix4fv(mView, 1, false, view, 0);
		glUniformMatrix4fv(mProjection, 1, false, projection, 0);
		// texture and normal
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, normalGen.getNormalTexture());
		glUniform1i(normalLocation, 0);
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, mapTexture.getTexture());
		glUniform1i(textureLocation, 1);	// changed from 1
		glUniform1i(shadowLocation, 2);
		// vertex attributes
		glBindBuffer(GL_ARRAY_BUFFER, mapVBO);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glVertexAttribPointer(vPosition, 4, GL_FLOAT, false, Float.SIZE / 8 * 8, 0);
		glVertexAttribPointer(textureCoord, 2, GL_FLOAT, false, Float.SIZE / 8 * 8, 4 * Float.SIZE / 8);
		glVertexAttribPointer(normalCoord, 2, GL_FLOAT, false, Float.SIZE / 8 * 8, 6 * Float.SIZE / 8);
		glEnableVertexAttribArray(vPosition);
		glEnableVertexAttribArray(textureCoord);
		glEnableVertexAttribArray(normalCoord);
		// light configurations
		for (Map.Entry<String, float[]> entry : lightSrc.entrySet())
			if (lightOn.get(entry.getKey())) {
				final float[] centrePoint = FlatMatrix4x4Vector4Multiplication(
						matMVP,
						new float[] {(lightGridPosition.get(entry.getKey())[0] + .5f) * 2 / columns - 1, (lightGridPosition.get(entry.getKey())[1] + .5f) * 2 / rows - 1, 1, 1});
				if (centrePoint[0] / centrePoint[3] > 1 || centrePoint[0] / centrePoint[3] < -1 ||
						centrePoint[1] / centrePoint[3] > 1 || centrePoint[1] / centrePoint[3] < -1 ||
						centrePoint[2] / centrePoint[3] > 1 || centrePoint[2] / centrePoint[3] < -1)
					continue;
				
				glActiveTexture(GL_TEXTURE2);
				glBindTexture(GL_TEXTURE_2D, shadowMaps.get(entry.getKey()));
				final float[] config = entry.getValue();
				glUniformMatrix4fv(mLight, 1, false, FlatMatrix4x4Multiplication(lightProjection, lightViews.get(entry.getKey())), 0);
				glUniform3f(cameraPosition, lightObserver[0] + config[0], lightObserver[1] + config[1], lightObserver[2] + config[2]);
				glUniform3f(source, config[0], config[1], config[2]);
				glUniform3f(color, config[3], config[4], config[5]);
				glUniform1f(specular, config[6]);
				glUniform1f(attenuation, config[7]);
				glUniform1f(sight, config[8]);
				glDrawArrays(GL_TRIANGLE_STRIP, 0, rows * resolution * (columns * resolution + 1) * 2);
				if (firstTime) {
					glBlendFunc(GL_ONE, GL_ONE);
					firstTime = false;
				}
			}
		glDisableVertexAttribArray(vPosition);
		glDisableVertexAttribArray(textureCoord);
		glDisableVertexAttribArray(normalCoord);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		// drawables
		glUseProgram(shadowObjProgram.getProgramId());
		vPosition = glGetAttribLocation(shadowObjProgram.getProgramId(), "vPosition");
		mModel = glGetUniformLocation(shadowObjProgram.getProgramId(), "model");
		mView = glGetUniformLocation(shadowObjProgram.getProgramId(), "view");
		mProjection = glGetUniformLocation(shadowObjProgram.getProgramId(), "projection");
		cameraPosition = glGetUniformLocation(shadowObjProgram.getProgramId(), "camera");
		textureLocation = glGetUniformLocation(shadowObjProgram.getProgramId(), "texture");
		final int vNormal = glGetAttribLocation(shadowObjProgram.getProgramId(), "normal");
		shadowLocation = glGetUniformLocation(shadowObjProgram.getProgramId(), "shadow");
		textureCoord = glGetAttribLocation(shadowObjProgram.getProgramId(), "textureCoord");
		source = glGetUniformLocation(shadowObjProgram.getProgramId(), "light.source");
		color = glGetUniformLocation(shadowObjProgram.getProgramId(), "light.color");
		specular = glGetUniformLocation(shadowObjProgram.getProgramId(), "light.specular");
		attenuation = glGetUniformLocation(shadowObjProgram.getProgramId(), "light.attenuation");
		sight = glGetUniformLocation(shadowObjProgram.getProgramId(), "light.sight");
		mLight = glGetUniformLocation(shadowObjProgram.getProgramId(), "lightTransform");
		glUniformMatrix4fv(mView, 1, false, view, 0);
		glUniformMatrix4fv(mProjection, 1, false, projection, 0);
		for (String key : drawableModelHandlers.keySet())
			if (drawableVisible.get(key)) {
				final float[] centrePoint = FlatMatrix4x4Vector4Multiplication(
						matMVP,
						new float[] {(drawableGridPositions.get(key)[0] + .5f) * 2 / columns - 1, (drawableGridPositions.get(key)[1] + .5f) * 2 / rows - 1, 1, 1});
				if (centrePoint[0] / centrePoint[3] > 1 || centrePoint[0] / centrePoint[3] < -1 ||
						centrePoint[1] / centrePoint[3] > 1 || centrePoint[1] / centrePoint[3] < -1 ||
						centrePoint[2] / centrePoint[3] > 1 || centrePoint[2] / centrePoint[3] < -1)
					continue;
				firstTime = true;
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				
				glUniformMatrix4fv(mModel, 1, false, FlatMatrix4x4Multiplication(model, drawableModels.get(key)), 0);
				glBindBuffer(GL_ARRAY_BUFFER, drawableModelHandlers.get(key));
				glVertexAttribPointer(vPosition, 4, GL_FLOAT, false, Float.SIZE / 8 * 10, 0);
				glVertexAttribPointer(textureCoord, 2, GL_FLOAT, false, Float.SIZE / 8 * 10, Float.SIZE / 8 * 4);
				glVertexAttribPointer(vNormal, 4, GL_FLOAT, false, Float.SIZE / 8 * 10, Float.SIZE / 8 * 6);
				glEnableVertexAttribArray(vPosition);
				glEnableVertexAttribArray(vNormal);
				glEnableVertexAttribArray(textureCoord);
				glActiveTexture(GL_TEXTURE0);
				glBindTexture(GL_TEXTURE_2D, manager.getCurrentGameState().getModelTexture(drawableTextures.get(key)).getTexture());
				glUniform1i(textureLocation, 0);
				glUniform1i(shadowLocation, 1);
				for (Map.Entry<String, float[]> entry : lightSrc.entrySet())
					if (lightOn.get(entry.getKey())) {
						glActiveTexture(GL_TEXTURE1);
						glBindTexture(GL_TEXTURE_2D, shadowMaps.get(entry.getKey()));
						final float[] config = entry.getValue();
						glUniformMatrix4fv(mLight, 1, false, FlatMatrix4x4Multiplication(lightProjection, lightViews.get(entry.getKey())), 0);
						glUniform3f(cameraPosition, lightObserver[0] + config[0], lightObserver[1] + config[1], lightObserver[2] + config[2]);
						glUniform3f(source, config[0], config[1], config[2]);
						glUniform3f(color, config[3], config[4], config[5]);
						glUniform1f(specular, config[6]);
						glUniform1f(attenuation, config[7]);
						glUniform1f(sight, config[8]);
						glDrawArrays(GL_TRIANGLES, 0, drawableModelSizes.get(key));
						if (firstTime) {
							glBlendFunc(GL_ONE, GL_ONE);
							firstTime = false;
						}
					}
				glDisableVertexAttribArray(vPosition);
				glDisableVertexAttribArray(vNormal);
				glDisableVertexAttribArray(textureCoord);
			}
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	private void drawSelection() {
		float[] mat;
		int vPosition = glGetAttribLocation(gridProgram.getProgramId(), "vPosition"),
				mModel = glGetUniformLocation(gridProgram.getProgramId(), "model"),
				mView = glGetUniformLocation(gridProgram.getProgramId(), "view"),
				mProjection = glGetUniformLocation(gridProgram.getProgramId(), "projection"),
				vColor = glGetUniformLocation(gridProgram.getProgramId(), "vColor"),
				vOffset = vBufMan.getVertexBufferOffset("GenericFullSquare"),
				iOffset = vBufMan.getIndexBufferOffset("GenericFullSquareIndex");
		glUseProgram(gridProgram.getProgramId());
		glUniformMatrix4fv(mView, 1, false, view, 0);
		glUniformMatrix4fv(mProjection, 1, false, projection, 0);
		glBindBuffer(GL_ARRAY_BUFFER, vBufMan.getVertexBuffer());
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vBufMan.getIndexBuffer());
		glVertexAttribPointer(vPosition, 4, GL_FLOAT, false, 0, vOffset);
		glEnableVertexAttribArray(vPosition);
		if (hasSelection) {
			mat = FlatMatrix4x4Multiplication(
					model,
					FlatTranslationMatrix4x4(2f/columns * orgGridIndex[0]-1, 2f/rows * orgGridIndex[1]-1, 1),
					selectGridMatrix);
			glUniformMatrix4fv(mModel, 1, false, mat, 0);
			glUniform4f(vColor, 1, 0, 0, .2f);
			glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, iOffset);
		}
		if (highlightedGridIndex != null) {
			glUniform4f(vColor, 0, 0, 1, .2f);
			for (int[] idx : highlightedGridIndex) {
				if (idx == null || idx.length != 2)
					continue;
				mat = FlatMatrix4x4Multiplication(
					model,
					FlatTranslationMatrix4x4(2f/columns * idx[0]-1, 2f/rows * idx[1]-1, 1),
					selectGridMatrix);
				glUniformMatrix4fv(mModel, 1, false, mat, 0);
				glDrawElements(GL_TRIANGLE_STRIP, 4, GL_UNSIGNED_SHORT, iOffset);
			}
		}
		glDisableVertexAttribArray(vPosition);
	}
	*/
	@Override
	public void draw() {
//		drawMap();
//		drawGrid();
//		drawSelection();
		long timer = System.currentTimeMillis();
		graphicsImpl.draw();
		System.out.println("Timer " + (System.currentTimeMillis() - timer));
	}
	// shadow generation
	private void configureShadow (String name) {
		graphicsImpl.configureShadow(name);
//		if (!shadowMaps.containsKey(name)) {
//			// generate texture
//			int[] v = new int[1];
//			glGenTextures(1, v, 0);
//			shadowMaps.put(name, v[0]);
//			// configure
//			glBindTexture(GL_TEXTURE_2D, v[0]);
//			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, SHADOW_MAP_TEXTURE_DIMENSION, SHADOW_MAP_TEXTURE_DIMENSION, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);
//			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
//			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//			glBindTexture(GL_TEXTURE_2D, 0);
//		}
//		final float[] lightConfig = lightSrc.get(name);
//		final float[] lightView = FlatTranslationMatrix4x4(-lightConfig[0], -lightConfig[1], -lightConfig[2]);
//		lightViews.put(name, lightView);
//		glBindFramebuffer(GL_FRAMEBUFFER, frameBuf);
//		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, shadowMaps.get(name), 0);
//		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuf);
//		glViewport(0, 0, SHADOW_MAP_TEXTURE_DIMENSION, SHADOW_MAP_TEXTURE_DIMENSION);
//		glClearColor(0, 0, 0, 1);
//		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//		glUseProgram(shadowProgram.getProgramId());
//		final int
//			vPosition = glGetAttribLocation(shadowProgram.getProgramId(), "vPosition"),
//			mModel = glGetUniformLocation(shadowProgram.getProgramId(), "model"),
//			mView = glGetUniformLocation(shadowProgram.getProgramId(), "view"),
//			mProjection = glGetUniformLocation(shadowProgram.getProgramId(), "projection");
//		glUniformMatrix4fv(mModel, 1, false, model, 0);
//		glUniformMatrix4fv(mView, 1, false, lightView, 0);
//		glUniformMatrix4fv(mProjection, 1, false, lightProjection, 0);
//		glBindBuffer(GL_ARRAY_BUFFER, mapVBO);
//		glVertexAttribPointer(vPosition, 4, GL_FLOAT, false, Float.SIZE / 8 * 8, 0);
//		glEnableVertexAttribArray(vPosition);
//		glDrawArrays(GL_TRIANGLE_STRIP, 0, rows * resolution * (columns * resolution + 1) * 2);
//		glDisableVertexAttribArray(vPosition);
//		glBindBuffer(GL_ARRAY_BUFFER, 0);
//		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
//		// drawables
//		for (String key : drawableModelHandlers.keySet())
//			if (drawableVisible.get(key)
//					&& Math.pow(lightGridPosition.get(name)[0] - drawableGridPositions.get(key)[0], 2)
//							+ Math.pow(lightGridPosition.get(name)[1] - drawableGridPositions.get(key)[1], 2) <= Math.pow(lightGridRange.get(name), 2)) {
//				glUniformMatrix4fv(mModel, 1, false, FlatMatrix4x4Multiplication(model, drawableModels.get(key)), 0);
//				glBindBuffer(GL_ARRAY_BUFFER, drawableModelHandlers.get(key));
//				glVertexAttribPointer(vPosition, 4, GL_FLOAT, false, Float.SIZE / 8 * 10, 0);
//				glEnableVertexAttribArray(vPosition);
//				glDrawArrays(GL_TRIANGLES, 0, drawableModelSizes.get(key));
//				glDisableVertexAttribArray(vPosition);
//			}
//		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	// coordinate calculations
	private void calculateModel() {
		if (rows > columns)
			model = FlatScalingMatrix4x4(columns / (float) rows * BASE_ZOOM_FACTOR, BASE_ZOOM_FACTOR, BOARD_Z_COORD);
		else if (rows < columns)
			model = FlatScalingMatrix4x4(BASE_ZOOM_FACTOR, rows / (float) columns * BASE_ZOOM_FACTOR, BOARD_Z_COORD);
		else
			model = FlatScalingMatrix4x4(BASE_ZOOM_FACTOR, BASE_ZOOM_FACTOR, BOARD_Z_COORD);
		graphicsImpl.setModelMatrix(model);
	}
	private void calculateView () {
		if (processingTranslation)
			view = FlatTranslationMatrix4x4(-cameraParams[0]-translationDeltaCoord[0],-cameraParams[1]-translationDeltaCoord[1],-cameraParams[2]);
		else
			view = FlatTranslationMatrix4x4(-cameraParams[0],-cameraParams[1],-cameraParams[2]);
		if (processingPerspective)
			view = FlatMatrix4x4Multiplication(view, FlatRotationMatrix4x4(mapRotation + perspectiveRotationAngle, 0, 0, 1));
		else
			view = FlatMatrix4x4Multiplication(view, FlatRotationMatrix4x4(mapRotation, 0, 0, 1));
		if (processingPerspective) {
			view = FlatMatrix4x4Multiplication(FlatTranslationMatrix4x4(0,0,-perspectiveCameraZoom),view);
		}
		float[] lookVec;
		if (processingPerspective)
			lookVec = NormalisedVector3 (
					new float[]{cameraParams[3]-cameraParams[0]+perspectiveLookAt[0], cameraParams[4]-cameraParams[1]+perspectiveLookAt[1], cameraParams[5]-cameraParams[2]-perspectiveCameraZoom});
		else
			lookVec = NormalisedVector3 (
					new float[]{cameraParams[3]-cameraParams[0], cameraParams[4]-cameraParams[1], cameraParams[5]-cameraParams[2]});
		view = FlatMatrix4x4Multiplication(
				FlatRotationMatrix4x4((float) Math.acos(-lookVec[2]), -lookVec[1], lookVec[0], 0)
				,view);
		view = FlatMatrix4x4Multiplication(
				FlatRotationMatrix4x4((float) Math.acos(cameraParams[7]), -cameraParams[8], 0, cameraParams[6])
				,view);
		graphicsImpl.setViewMatrix(view);
	}
	// event interpreters
	@Override
	public boolean passEvent(ControlEvent e) {
		// Remember: normalise
		if (e.type == ControlEvent.TYPE_DOWN) {
			// unknown just yet
			return true;
		} else if (e.type == ControlEvent.TYPE_DRAG) {
			// tap with movements
			if (e.data.eventTime - e.data.startTime > ControlEvent.DRAG_TIME_LIMIT ||
					Math.abs(e.data.deltaX) > ControlEvent.TAP_DRIFT_LIMIT / ratio ||
					Math.abs(e.data.deltaY) > ControlEvent.TAP_DRIFT_LIMIT * ratio) {
				if (e.data.pointerCount == 1)
					onProcessingTranslation(e);
				else if (e.data.pointerCount == 2)
					onProcessingPerspective(e);
				else if (e.data.pointerCount == 3)
					onProcessingAttackAngle(e);
				responder.updateGraphics();
			}
			return true;
		} else if (e.type == ControlEvent.TYPE_CLEAR) {
			if (Math.abs(e.data.deltaX) < ControlEvent.TAP_DRIFT_LIMIT / ratio && Math.abs(e.data.deltaY) < ControlEvent.TAP_DRIFT_LIMIT * ratio) {
				// tap
				if (e.data.wasMultiTouch) {
					// DO NOTHING
				} else if (e.data.eventTime - e.data.startTime > ControlEvent.TAP_LONG_TIME_LIMIT) {
					// long tap
				} else {
					// short tap
					if (e.data.eventTime - tapTimeQueue[tapQueueHead] > ControlEvent.TAP_DOUBLE_TIME_LIMIT ||
							Math.abs(tapXQueue[tapQueueHead] - e.data.x[0]) > ControlEvent.TAP_DOUBLE_DRIFT_LIMIT / ratio ||
							Math.abs(tapYQueue[tapQueueHead] - e.data.y[0]) > ControlEvent.TAP_DOUBLE_DRIFT_LIMIT * ratio) {
						final ControlEvent passedEvt = new ControlEvent(e);
						new Thread() {
							private ControlEvent evt;
							// <init>
							{
								evt = passedEvt;
							}
							@Override
							public void run() {
								try{
									Thread.sleep(ControlEvent.TAP_DOUBLE_TIME_LIMIT);
								} catch (Exception e) {}
								if (doubleTap) {
									doubleTap = false;
									onDoubleTap(evt);
								} else
									onSingleTap(evt);
							}
						}.start();
					} else
						doubleTap = true;
					tapQueueHead = (byte) ((tapQueueHead + 1) % tapQueueLength);
					tapTimeQueue[tapQueueHead] = e.data.eventTime;
					tapXQueue[tapQueueHead] = e.data.x[0]; tapYQueue[tapQueueHead] = e.data.y[0];
				}
			}
			onProcessingTranslationDone(e);
			onProcessingPerspectiveDone(e);
			onProcessingAttackAngleDone(e);
			responder.updateGraphics();
			return e.data.pointerCount > 1;
		}
		return false;
	}
	private void onDoubleTap(ControlEvent e) {
		// reset camera
		highlightedGridIndex = null;
		cameraParams[0] = cameraParams[1] = cameraParams[3] = cameraParams[4] = cameraParams[6] = cameraParams[8] = 0;
		cameraParams[2] = .8f;
		cameraParams[5] = cameraParams[9] = .01f;
		cameraParams[10] = 5;
		cameraParams[11] = PI / 3f;
		mapRotation = 0;
		calculateView();
		responder.updateGraphics();
	}
	private void onSingleTap(ControlEvent e) {
		// test
		manager.playAssetSound("click");
		rayTrace(e);
	}
	private void onProcessingAttackAngleDone(ControlEvent e) {
		
	}
	private void onProcessingPerspectiveDone(ControlEvent e) {
		if (processingPerspective) {
			// finalise
			cameraParams[2] += perspectiveCameraZoom;
			cameraParams[3] += perspectiveLookAt[0];
			cameraParams[4] += perspectiveLookAt[1];
			mapRotation += perspectiveRotationAngle;
			perspectiveRotationAngle = 0;
			processingPerspective = false;
			calculateView();
		}
	}
	private void onProcessingTranslationDone(ControlEvent e) {
		if (processingTranslation) {
			cameraParams[0] -= e.data.deltaX;
			cameraParams[1] -= e.data.deltaY;
			cameraParams[3] -= e.data.deltaX;
			cameraParams[4] -= e.data.deltaY;
			processingTranslation = false;
			calculateView();
		}
	}
	private void onProcessingAttackAngle(ControlEvent e) {
		
	}
	private void onProcessingPerspective(ControlEvent e) {
		if (e.data.pointerCount != 2)
			return;
		if (processingTranslation)
			onProcessingTranslationDone(e);
		if (!processingPerspective) {
			perspectiveStartCoord[0] = e.data.x[0];
			perspectiveStartCoord[1] = e.data.y[0];
			perspectiveStartCoord[2] = e.data.x[1];
			perspectiveStartCoord[3] = e.data.y[1];
			final float deltaX = perspectiveStartCoord[2] - perspectiveStartCoord[0],
					deltaY = perspectiveStartCoord[3] - perspectiveStartCoord[1],
					len = (float) Math.hypot(deltaX, deltaY);
			perspectiveStartDeltaVecX = deltaX / len;
			perspectiveStartDeltaVecY = deltaY / len;
			processingPerspective = true;
		}
		perspectiveLastCoord[0] = e.data.x[0];
		perspectiveLastCoord[1] = e.data.y[0];
		perspectiveLastCoord[2] = e.data.x[1];
		perspectiveLastCoord[3] = e.data.y[1];
		final float deltaX = perspectiveLastCoord[2] - perspectiveLastCoord[0],
				deltaY = perspectiveLastCoord[3] - perspectiveLastCoord[1],
				lenLast = (float) Math.hypot(deltaX, deltaY);
		final float perspectiveLastDeltaVecX = deltaX / lenLast,
				perspectiveLastDeltaVecY = deltaY / lenLast;
		// map rotation
		perspectiveRotationAngle = (float) Math.acos(perspectiveStartDeltaVecX * perspectiveLastDeltaVecX + perspectiveStartDeltaVecY * perspectiveLastDeltaVecY);
		if (perspectiveStartDeltaVecX * perspectiveLastDeltaVecY - perspectiveStartDeltaVecY * perspectiveLastDeltaVecX < 0)
			perspectiveRotationAngle = -perspectiveRotationAngle;
		// zoom factor
		final float lenFirst = (float) Math.hypot(perspectiveStartCoord[2] - perspectiveStartCoord[0], perspectiveStartCoord[3] - perspectiveStartCoord[1]);
		final float zoomDelta = lenFirst - lenLast;
		// TODO: Camera Parameter Calibration
		if (zoomDelta + cameraParams[2] < BOARD_Z_COORD + cameraParams[9])
			perspectiveCameraZoom = BOARD_Z_COORD + cameraParams[9] - cameraParams[2];
		else if (zoomDelta + cameraParams[2] > BOARD_Z_COORD + cameraParams[10])
			perspectiveCameraZoom = BOARD_Z_COORD + cameraParams[10] - cameraParams[2];
		else
			perspectiveCameraZoom = zoomDelta;
		// look-at displacement
		perspectiveLookAt[0] = Math.scalb(perspectiveStartCoord[0] + perspectiveStartCoord[2] - perspectiveLastCoord[0] - perspectiveLastCoord[2], -1);
		perspectiveLookAt[1] = Math.scalb(perspectiveStartCoord[1] + perspectiveStartCoord[3] - perspectiveLastCoord[1] - perspectiveLastCoord[3], -1);
//		calculateModel();
		calculateView();
	}
	private void onProcessingTranslation(ControlEvent e) {
		if (e.data.pointerCount != 1)
			return;
		if (processingPerspective)
			onProcessingPerspectiveDone(e);
		if (!processingTranslation) {
			processingTranslation = true;
		}
		translationDeltaCoord[0] = -e.data.deltaX;
		translationDeltaCoord[1] = -e.data.deltaY;
		calculateView();
	}
	// ray trace code
	private void rayTrace(ControlEvent e) {
		// Note: start with normalized device coordinates
		calculateModel();
		calculateView();
		final float[] projPoint = {e.data.x[0] * cameraParams[9], e.data.y[0] * cameraParams[9], -cameraParams[9], cameraParams[9]};
		final float[] orgProjectedPoint = FlatMatrix4x4Vector4Multiplication (FlatInverseMatrix4x4(projection), projPoint);
		final float[] worldCoord = FlatMatrix4x4Vector4Multiplication(
				FlatMatrix4x4Multiplication(FlatRotationMatrix4x4(perspectiveRotationAngle+mapRotation, 0, 0, 1),	// undo the inverted view's overkill
				FlatInverseMatrix4x4(view)),
				orgProjectedPoint);
		final float lambda = (BOARD_Z_COORD - worldCoord[2]) / (cameraParams[2] - worldCoord[2]);
		if (Math.abs(lambda) > ERROR)
			orgGridPoint = new float[] {
				lambda * cameraParams[0] + (1-lambda) * worldCoord[0],
				lambda * cameraParams[1] + (1-lambda) * worldCoord[1],
				BOARD_Z_COORD, 1
			};
		else
			orgGridPoint = new float[] {worldCoord[0], worldCoord[1], BOARD_Z_COORD, 1};
		orgGridPoint = FlatMatrix4x4Vector4Multiplication(
				FlatMatrix4x4Multiplication(FlatInverseMatrix4x4(model),
				FlatRotationMatrix4x4(-perspectiveRotationAngle-mapRotation, 0, 0, 1)),	// alright... lets put it back again
				orgGridPoint);
		// report coordinate
		orgGridIndex = new int[]{
				(int) Math.floor(Math.scalb(orgGridPoint[0] + 1, -1) * columns),
				(int) Math.floor(Math.scalb(orgGridPoint[1] + 1, -1) * rows)
				};
		hasSelection = (orgGridIndex[0] >= 0 && orgGridIndex[0] < columns && orgGridIndex[1] >= 0 && orgGridIndex[1] < rows);
		graphicsImpl.setSelectedGrid(hasSelection, orgGridIndex);
		if (hasSelection) {
			ControlEvent newevt = new ControlEvent(ControlEvent.TYPE_INTERPRETED, e.data);
			newevt.extendedType = "ChooseGrid";
			newevt.data.extendedData.put("Coordinates", orgGridIndex.clone());
			manager.processEvent(newevt);
		} else {
			ControlEvent newevt = new ControlEvent(ControlEvent.TYPE_INTERPRETED, e.data);
			newevt.extendedType = "Cancel";
			manager.processEvent(newevt);
		}
		responder.updateGraphics();
	}
	@Override
	public void close() {
		// delete buffers
//		graphicsImpl.close();
		mapProgram.close();
		gridProgram.close();
		shadowProgram.close();
		shadowObjProgram.close();
		glDeleteRenderbuffers(1, new int[]{renderBuf}, 0);
		glDeleteFramebuffers(1, new int[]{frameBuf}, 0);
		glDeleteBuffers(1, new int[]{mapVBO}, 0);
		int[] v = new int[shadowMaps.size()];
		int c = 0;
		for (Map.Entry<String, Integer> entry : shadowMaps.entrySet())
			v[c++] = entry.getValue();
		glDeleteTextures(c, v, 0);
	}
}
