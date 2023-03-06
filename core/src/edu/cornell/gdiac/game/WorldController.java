/*
 * WorldController.java
 *
 * This is the most important new class in this lab.  This class serves as a combination 
 * of the CollisionController and GameplayController from the previous lab.  There is not 
 * much to do for collisions; Box2d takes care of all of that for us.  This controller 
 * invokes Box2d and then performs any after the fact modifications to the data 
 * (e.g. gameplay).
 *
 * If you study this class, and the contents of the edu.cornell.cs3152.physics.obstacles
 * package, you should be able to understand how the Physics engine works.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * LibGDX version, 2/6/2015
 */
package edu.cornell.gdiac.game;

import java.util.HashMap;
import java.util.Iterator;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.util.*;
import edu.cornell.gdiac.game.obstacle.*;

/**
 * Base class for a world-specific controller.
 *
 *
 * A world has its own objects, assets, and input controller.  Thus this is 
 * really a mini-GameEngine in its own right.  The only thing that it does
 * not do is create a GameCanvas; that is shared with the main application.
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class WorldController implements Screen {

	/** Width of the game world in Box2d units */
	protected static final float DEFAULT_WIDTH  = 32.0f;
	/** Height of the game world in Box2d units */
	protected static final float DEFAULT_HEIGHT = 18.0f;
	/** The default value of gravity (going down) */
	protected static final float DEFAULT_GRAVITY = -4.9f;

	/** Exit code for quitting the game */
	public static final int EXIT_QUIT = 0;
	/** Exit code for advancing to next level */
	public static final int EXIT_COUNT = 120;

	/** Listener that will update the player mode when we are done */
	private ScreenListener listener;

	/** Level controller */
	private LevelController levelController;

	/** The hashmap for texture regions */
	private HashMap<String, TextureRegion> textureRegionAssetMap;
	/** The hashmap for sounds */
	private HashMap<String, Sound> soundAssetMap;
	/** The hashmap for fonts */
	private HashMap<String, BitmapFont> fontAssetMap;
	/** The JSON value constants */
	private JsonValue constants;
//
//	/** The texture for walls and platforms */
//	protected TextureRegion earthTile;
//	/** The texture for the exit condition */
//	protected TextureRegion goalTile;
//	/** The font for giving messages to the player */
//	protected BitmapFont displayFont;
//
//	/** Texture asset for character avatar */
//	private TextureRegion avatarTexture;
//	/** Texture asset for the spinning barrier */
//	private TextureRegion barrierTexture;
//	/** Texture asset for the bullet */
//	private TextureRegion bulletTexture;
//	/** Texture asset for the bridge plank */
//	private TextureRegion bridgeTexture;
//
//	/** The jump sound.  We only want to play once. */
//	private Sound jumpSound;
//	private long jumpId = -1;
//	/** The weapon fire sound.  We only want to play once. */
//	private Sound fireSound;
//
//	private long plopId = -1;
//	private long fireId = -1;
//	/** The weapon pop sound.  We only want to play once. */
//	private Sound plopSound;
//
//	/** Physics constants for initialization */
//	private JsonValue constants;

	/**
	 * Returns the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers
	 *
	 * @return the canvas associated with this controller
	 */
	public GameCanvas getCanvas() {
		return levelController.getCanvas();
	}

	/**
	 * Sets the canvas associated with this controller
	 *
	 * The canvas is shared across all controllers.  Setting this value will compute
	 * the drawing scale from the canvas size.
	 *
	 * @param canvas the canvas associated with this controller
	 */
	public void setCanvas(GameCanvas canvas) {
		levelController.setCanvas(canvas);
	}
	
	/**
	 * Creates a new game world with the default values.
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 */
	protected WorldController() {
		this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT), 
			 new Vector2(0,DEFAULT_GRAVITY));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param width  	The width in Box2d coordinates
	 * @param height	The height in Box2d coordinates
	 * @param gravity	The downward gravity
	 */
	protected WorldController(float width, float height, float gravity) {
		this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
	}

	/**
	 * Creates a new game world
	 *
	 * The game world is scaled so that the screen coordinates do not agree
	 * with the Box2d coordinates.  The bounds are in terms of the Box2d
	 * world, not the screen.
	 *
	 * @param bounds	The game bounds in Box2d coordinates
	 * @param gravity	The gravitational force on this Box2d world
	 */
	protected WorldController(Rectangle bounds, Vector2 gravity) {
		levelController = new LevelController(bounds, gravity);
	}
	
	/**
	 * Dispose of all (non-static) resources allocated to this mode.
	 */
	public void dispose() {
		levelController.dispose();
	}

	/**
	 * Gather the assets for this controller.
	 *
	 * This method extracts the asset variables from the given asset directory. It
	 * should only be called after the asset directory is completed.
	 *
	 * @param directory	Reference to global asset manager.
	 */
	public void gatherAssets(AssetDirectory directory) {
		// Allocate the tiles
		// Creating the hashmaps
		textureRegionAssetMap = new HashMap<String, TextureRegion>();
		soundAssetMap = new HashMap<String, Sound>();
		fontAssetMap = new HashMap<String, BitmapFont>();
		// Filling hashmaps and constants
		textureRegionAssetMap.put("earthTile", new TextureRegion(directory.getEntry( "shared:earth", Texture.class )));
		textureRegionAssetMap.put("goalTile", new TextureRegion(directory.getEntry( "shared:goal", Texture.class )));
		fontAssetMap.put("retro", directory.getEntry( "shared:retro" ,BitmapFont.class));
		textureRegionAssetMap.put("cat", new TextureRegion(directory.getEntry("platform:dude",Texture.class)));
		textureRegionAssetMap.put("barrier", new TextureRegion(directory.getEntry("platform:barrier",Texture.class)));
		textureRegionAssetMap.put("bullet", new TextureRegion(directory.getEntry("platform:bullet",Texture.class)));
		textureRegionAssetMap.put("rope", new TextureRegion(directory.getEntry("platform:rope",Texture.class)));

		soundAssetMap.put("jump", directory.getEntry( "platform:jump", Sound.class ));
		soundAssetMap.put("pew", directory.getEntry( "platform:pew", Sound.class ));
		soundAssetMap.put("plop", directory.getEntry( "platform:plop", Sound.class ));

		constants =  directory.getEntry( "platform:constants", JsonValue.class );

		// Giving assets to levelController
		levelController.setAssets(textureRegionAssetMap, fontAssetMap, soundAssetMap, constants);

//		earthTile = new TextureRegion(directory.getEntry( "shared:earth", Texture.class ));
//		goalTile  = new TextureRegion(directory.getEntry( "shared:goal", Texture.class ));
//		displayFont = directory.getEntry( "shared:retro" ,BitmapFont.class);
//		avatarTexture  = new TextureRegion(directory.getEntry("platform:dude",Texture.class));
//		barrierTexture = new TextureRegion(directory.getEntry("platform:barrier",Texture.class));
//		bulletTexture = new TextureRegion(directory.getEntry("platform:bullet",Texture.class));
//		bridgeTexture = new TextureRegion(directory.getEntry("platform:rope",Texture.class));
//
//		jumpSound = directory.getEntry( "platform:jump", Sound.class );
//		fireSound = directory.getEntry( "platform:pew", Sound.class );
//		plopSound = directory.getEntry( "platform:plop", Sound.class );
//
//		constants = directory.getEntry( "platform:constants", JsonValue.class );
	}
	
	/**
	 * Returns whether to process the update loop
	 *
	 * At the start of the update loop, we check if it is time
	 * to switch to a new game mode.  If not, the update proceeds
	 * normally.
	 *
	 * @param dt	Number of seconds since last animation frame
	 * 
	 * @return whether to process the update loop
	 */
	public boolean preUpdate(float dt) {
		if (listener == null) {
			return true;
		}
		// Now it is time to maybe switch screens.
		if (levelController.preUpdate(dt)) {
			pause();
			listener.exitScreen(this, EXIT_QUIT);
			return false;
		} else if (levelController.getCountdown() > 0) {
			levelController.setCountdown(levelController.getCountdown()-1);
		} else if (levelController.getCountdown() == 0) {
			if (levelController.isFailure()) {
				levelController.reset();
			} else if (levelController.isComplete()) {
				pause();
				levelController.reset();
				return false;
			}
		} else if (levelController.checkFailure(dt)) {
			return false;
		}
		return true;
	}
	
	/**
	 * The core gameplay loop of this world.
	 *
	 * This method contains the specific update code for this mini-game. It does
	 * not handle collisions, as those are managed by the parent class WorldController.
	 * This method is called after input is read, but before collisions are resolved.
	 * The very last thing that it should do is apply forces to the appropriate objects.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt){
		levelController.update(dt);
	}

	/**
	 * Processes physics
	 *
	 * Once the update phase is over, but before we draw, we are ready to handle
	 * physics.  The primary method is the step() method in world.  This implementation
	 * works for all applications and should not need to be overwritten.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void postUpdate(float dt){
		levelController.postUpdate(dt);
	}
	/**
	 * Draw the physics objects to the canvas
	 *
	 * For simple worlds, this method is enough by itself.  It will need
	 * to be overridden if the world needs fancy backgrounds or the like.
	 *
	 * The method draws all objects in the order that they were added.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void draw(float dt) {
		levelController.draw(dt);
	}

	/**
	 * Called when the Screen is resized. 
	 *
	 * This can happen at any point during a non-paused state but will never happen 
	 * before a call to show().
	 *
	 * @param width  The new width in pixels
	 * @param height The new height in pixels
	 */
	public void resize(int width, int height) {
		// IGNORE FOR NOW
	}

	/**
	 * Called when the Screen should render itself.
	 *
	 * We defer to the other methods update() and draw().  However, it is VERY important
	 * that we only quit AFTER a draw.
	 *
	 * @param delta Number of seconds since last animation frame
	 */
	public void render(float delta) {

		if (preUpdate(delta)) {
			update(delta); // This is the one that must be defined.
			postUpdate(delta);
		}
		draw(delta);

	}

	/**
	 * Called when the Screen is paused.
	 * 
	 * This is usually when it's not active or visible on screen. An Application is 
	 * also paused before it is destroyed.
	 */
	public void pause() {
		// TODO Auto-generated method stub
	}

	/**
	 * Called when the Screen is resumed from a paused state.
	 *
	 * This is usually when it regains focus.
	 */
	public void resume() {
		// TODO Auto-generated method stub
	}
	
	/**
	 * Called when this screen becomes the current screen for a Game.
	 */
	public void show() {
		// Useless if called in outside animation loop
	}

	/**
	 * Called when this screen is no longer the current screen for a Game.
	 */
	public void hide() {
		// Useless if called in outside animation loop
	}

	/**
	 * Sets the ScreenListener for this mode
	 *
	 * The ScreenListener will respond to requests to quit.
	 */
	public void setScreenListener(ScreenListener listener) {
		this.listener = listener;
	}

	/**
	 * Resets the status of the game so that we can play again.
	 *
	 * This method disposes of the world and creates a new one.
	 */
	public void reset() {
		levelController.reset();
	}
}