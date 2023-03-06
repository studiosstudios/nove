/*
 * PlatformController.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.physics.box2d.*;
import edu.cornell.gdiac.game.object.*;

import edu.cornell.gdiac.assets.AssetDirectory;
import edu.cornell.gdiac.game.obstacle.*;
import edu.cornell.gdiac.util.PooledList;
import edu.cornell.gdiac.util.ScreenListener;

import java.util.HashMap;
import java.util.Iterator;

import static edu.cornell.gdiac.game.WorldController.EXIT_QUIT;

/**
 * Gameplay specific controller for the platformer game.  
 *
 * You will notice that asset loading is not done with static methods this time.  
 * Instance asset loading makes it easier to process our game modes in a loop, which 
 * is much more scalable. However, we still want the assets themselves to be static.
 * This is the purpose of our AssetState variable; it ensures that multiple instances
 * place nicely with the static assets.
 */
public class LevelController implements ContactListener {
    /** The default sound volume */
    private float volume;

    private long jumpId = -1;
    private long plopId = -1;
	private long fireId = -1;
    /** Reference to the character avatar */
    private Cat avatar;
    /** Reference to the goalDoor (for collision detection) */
    private BoxObstacle goalDoor;

    /** Mark set to handle more sophisticated collision callbacks */
    protected ObjectSet<Fixture> sensorFixtures;

    /** The amount of time for a physics engine step. */
    public static final float WORLD_STEP = 1/60.0f;
    /** Number of velocity iterations for the constrain solvers */
    public static final int WORLD_VELOC = 6;
    /** Number of position iterations for the constrain solvers */
    public static final int WORLD_POSIT = 2;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** The default value of gravity (going down) */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** All the objects in the world. */
    protected PooledList<Obstacle> objects  = new PooledList<Obstacle>();
    /** Queue for adding objects */
    protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();

    /** The Box2D world */
    protected World world;
    /** The boundary of the world */
    protected Rectangle bounds;
    /** The world scale */
    protected Vector2 scale;
    /** Whether we have completed this level */
    private boolean complete;
    /** Whether we have failed at this world (and need a reset) */
    private boolean failed;
    /** Countdown active for winning or losing */
    private int countdown;
    /** Exit code for advancing to next level */
    public static final int EXIT_COUNT = 120;
    /** Whether or not debug mode is active */
    private boolean debug;

    /** texture region map */
    private HashMap<String, TextureRegion> textureRegionAssetMap;
    /** sound map */
    private HashMap<String, Sound> soundAssetMap;
    /** font map */
    private HashMap<String, BitmapFont> fontAssetMap;
    /** constants */
    private JsonValue JSONconstants;

    /**
     * Returns true if debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @return true if debug mode is active.
     */
    public boolean isDebug( ) {
        return debug;
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown(int countdown){
        this.countdown = countdown;
    }

    /**
     * Creates and initialize a new instance of the platformer game
     *
     * The game has default gravity and other settings
     */
    public LevelController(Rectangle bounds, Vector2 gravity) {
        world = new World(gravity,false);

        complete = false;
        failed = false;
        countdown = -1;
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1,1);
        debug  = false;
        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();
    }

    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity() );

        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();

        world = new World(gravity,false);
        world.setContactListener(this);
        setComplete(false);
        setFailure(false);
        populateLevel();
    }

    public void setAssets(HashMap<String, TextureRegion> tMap, HashMap<String, BitmapFont> fMap,
                            HashMap<String, Sound> sMap, JsonValue constants){
        textureRegionAssetMap = tMap;
        fontAssetMap = fMap;
        soundAssetMap = sMap;
        JSONconstants = constants;
    }

    /**
     * Lays out the game geography.
     */
    private void populateLevel() {
        // Add level goal
        float dwidth  = textureRegionAssetMap.get("goalTile").getRegionWidth()/scale.x;
        float dheight = textureRegionAssetMap.get("goalTile").getRegionHeight()/scale.y;

        JsonValue goal = JSONconstants.get("goal");
        JsonValue goalpos = goal.get("pos");
        goalDoor = new BoxObstacle(goalpos.getFloat(0),goalpos.getFloat(1),dwidth,dheight);
        goalDoor.setBodyType(BodyDef.BodyType.StaticBody);
        goalDoor.setDensity(goal.getFloat("density", 0));
        goalDoor.setFriction(goal.getFloat("friction", 0));
        goalDoor.setRestitution(goal.getFloat("restitution", 0));
        goalDoor.setSensor(true);
        goalDoor.setDrawScale(scale);
        goalDoor.setTexture(textureRegionAssetMap.get("goalTile"));
        goalDoor.setName("goal");
        addObject(goalDoor);

        String wname = "wall";
        JsonValue walljv = JSONconstants.get("walls");
        JsonValue defaults = JSONconstants.get("defaults");
        for (int ii = 0; ii < walljv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(walljv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat( "density", 0.0f ));
            obj.setFriction(defaults.getFloat( "friction", 0.0f ));
            obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
            obj.setDrawScale(scale);
            obj.setTexture(textureRegionAssetMap.get("earthTile"));
            obj.setName(wname+ii);
            addObject(obj);
        }

        String pname = "platform";
        JsonValue platjv = JSONconstants.get("platforms");
        for (int ii = 0; ii < platjv.size; ii++) {
            PolygonObstacle obj;
            obj = new PolygonObstacle(platjv.get(ii).asFloatArray(), 0, 0);
            obj.setBodyType(BodyDef.BodyType.StaticBody);
            obj.setDensity(defaults.getFloat( "density", 0.0f ));
            obj.setFriction(defaults.getFloat( "friction", 0.0f ));
            obj.setRestitution(defaults.getFloat( "restitution", 0.0f ));
            obj.setDrawScale(scale);
            obj.setTexture(textureRegionAssetMap.get("earthTile"));
            obj.setName(pname+ii);
            addObject(obj);
        }

        // This world is heavier
        world.setGravity( new Vector2(0,defaults.getFloat("gravity",0)) );

        // Create dude
        dwidth  = textureRegionAssetMap.get("cat").getRegionWidth()/scale.x;
        dheight = textureRegionAssetMap.get("cat").getRegionHeight()/scale.y;
        avatar = new Cat(JSONconstants.get("dude"), dwidth, dheight);
        avatar.setDrawScale(scale);
        avatar.setTexture(textureRegionAssetMap.get("cat"));
        addObject(avatar);

        // Create rope bridge
        dwidth  = textureRegionAssetMap.get("rope").getRegionWidth()/scale.x;
        dheight = textureRegionAssetMap.get("rope").getRegionHeight()/scale.y;
        RopeBridge bridge = new RopeBridge(JSONconstants.get("bridge"), dwidth, dheight);
        bridge.setTexture(textureRegionAssetMap.get("rope"));
        bridge.setDrawScale(scale);
        addObject(bridge);

        // Create spinning platform
        dwidth  = textureRegionAssetMap.get("barrier").getRegionWidth()/scale.x;
        dheight = textureRegionAssetMap.get("barrier").getRegionHeight()/scale.y;
        Spinner spinPlatform = new Spinner(JSONconstants.get("spinner"),dwidth,dheight);
        spinPlatform.setDrawScale(scale);
        spinPlatform.setTexture(textureRegionAssetMap.get("barrier"));
        addObject(spinPlatform);

        volume = JSONconstants.getFloat("volume", 1.0f);
    }

    /**
     * Returns whether the player has lost
     *
     * @param dt	Number of seconds since last animation frame
     *
     * @return whether the player has lost
     */
    public boolean checkFailure(float dt) {
        if (!isFailure() && avatar.getY() < -1) {
            setFailure(true);
            return true;
        }
        return false;
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
    public void update(float dt) {
        // Process actions in object model
        avatar.setMovement(InputController.getInstance().getHorizontal() *avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        avatar.setShooting(InputController.getInstance().didSecondary());

        // Add a bullet if we fire
        if (avatar.isShooting()) {
            createBullet();
        }

        avatar.applyForce();
        if (avatar.isJumping()) {
            jumpId = playSound( soundAssetMap.get("jump"), jumpId, volume );
        }
    }

    /**
     * Add a new bullet to the world and send it in the right direction.
     */
    private void createBullet() {
        JsonValue bulletjv = JSONconstants.get("bullet");
        float offset = bulletjv.getFloat("offset",0);
        offset *= (avatar.isFacingRight() ? 1 : -1);
        float radius = textureRegionAssetMap.get("bullet").getRegionWidth()/(2.0f*scale.x);
        WheelObstacle bullet = new WheelObstacle(avatar.getX()+offset, avatar.getY(), radius);

        bullet.setName("bullet");
        bullet.setDensity(bulletjv.getFloat("density", 0));
        bullet.setDrawScale(scale);
        bullet.setTexture(textureRegionAssetMap.get("bullet"));
        bullet.setBullet(true);
        bullet.setGravityScale(0);

        // Compute position and velocity
        float speed = bulletjv.getFloat( "speed", 0 );
        speed  *= (avatar.isFacingRight() ? 1 : -1);
        bullet.setVX(speed);
        addQueuedObject(bullet);

        fireId = playSound( soundAssetMap.get("pew"), fireId );
    }

    /**
     * Remove a new bullet from the world.
     *
     * @param  bullet   the bullet to remove
     */
    public void removeBullet(Obstacle bullet) {
        bullet.markRemoved(true);
        plopId = playSound( soundAssetMap.get("plop"), plopId );
    }


    /**
     * Callback method for the start of a collision
     *
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle)body1.getUserData();
            Obstacle bd2 = (Obstacle)body2.getUserData();

            // Test bullet collision with world
            if (bd1.getName().equals("bullet") && bd2 != avatar) {
                removeBullet(bd1);
            }

            if (bd2.getName().equals("bullet") && bd1 != avatar) {
                removeBullet(bd2);
            }

            // See if we have landed on the ground.
            if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                    (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // Check for win condition
            if ((bd1 == avatar   && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
                setComplete(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     *
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Object bd1 = body1.getUserData();
        Object bd2 = body2.getUserData();

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }
    }

    /** Unused ContactListener method */
    public void postSolve(Contact contact, ContactImpulse impulse) {}
    /** Unused ContactListener method */
    public void preSolve(Contact contact, Manifold oldManifold) {}

    /**
     * Called when the Screen is paused.
     *
     * We need this method to stop all sounds when we pause.
     * Pausing happens when we switch game modes.
     */
    public void pause() {
        soundAssetMap.get("jump").stop(jumpId);
        soundAssetMap.get("plop").stop(plopId);
        soundAssetMap.get("fire").stop(fireId);
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
        canvas.clear();

        canvas.begin();
        for(Obstacle obj : objects) {
            obj.draw(canvas);
        }
        canvas.end();

        if (debug) {
            canvas.beginDebug();
            for(Obstacle obj : objects) {
                obj.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        // Final message
        if (complete && !failed) {
            fontAssetMap.get("retro").setColor(Color.YELLOW);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("VICTORY!", fontAssetMap.get("retro"), 0.0f);
            canvas.end();
        } else if (failed) {
            fontAssetMap.get("retro").setColor(Color.RED);
            canvas.begin(); // DO NOT SCALE
            canvas.drawTextCentered("FAILURE!", fontAssetMap.get("retro"), 0.0f);
            canvas.end();
        }
    }

    /**
     * Returns the canvas associated with this controller
     *
     * The canvas is shared across all controllers
     *
     * @return the canvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
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
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/bounds.getWidth();
        this.scale.y = canvas.getHeight()/bounds.getHeight();
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        for(Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        objects.clear();
        addQueue.clear();
        world.dispose();
        objects = null;
        addQueue = null;
        bounds = null;
        scale  = null;
        world  = null;
        canvas = null;
    }


    /**
     * Returns true if the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @return true if the level is completed.
     */
    public boolean isComplete( ) {
        return complete;
    }

    /**
     * Sets whether the level is completed.
     *
     * If true, the level will advance after a countdown
     *
     * @param value whether the level is completed.
     */
    public void setComplete(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        complete = value;
    }

    /**
     * Returns true if the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @return true if the level is failed.
     */
    public boolean isFailure( ) {
        return failed;
    }

    /**
     * Sets whether the level is failed.
     *
     * If true, the level will reset after a countdown
     *
     * @param value whether the level is failed.
     */
    public void setFailure(boolean value) {
        if (value) {
            countdown = EXIT_COUNT;
        }
        failed = value;
    }

    /**
     * @param dt
     * @return whether to exit the screen
     */
    public boolean preUpdate(float dt){
        InputController input = InputController.getInstance();
        input.readInput(bounds, scale);

        // Toggle debug
        if (input.didDebug()) {
            debug = !debug;
        }

        // Handle resets
        if (input.didReset()) {
            reset();
        }

        // Now it is time to maybe switch screens.
        return input.didExit();
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
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!addQueue.isEmpty()) {
            addObject(addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP,WORLD_VELOC,WORLD_POSIT);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }
    }

    /**
     * Immediately adds the object to the physics world
     *
     * param obj The object to add
     */
    protected void addObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        objects.add(obj);
        obj.activatePhysics(world);
    }

    /**
     * Returns true if the object is in bounds.
     *
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     *
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x+bounds.width);
        boolean vert  = (bounds.y <= obj.getY() && obj.getY() <= bounds.y+bounds.height);
        return horiz && vert;
    }

    /**
     *
     * Adds a physics object in to the insertion queue.
     *
     * Objects on the queue are added just before collision processing.  We do this to
     * control object creation.
     *
     * param obj The object to add
     */
    public void addQueuedObject(Obstacle obj) {
        assert inBounds(obj) : "Object is not in bounds";
        addQueue.add(obj);
    }


    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound		The sound asset to play
     * @param soundId	The previously playing sound instance
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId) {
        return playSound( sound, soundId, 1.0f );
    }


    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound		The sound asset to play
     * @param soundId	The previously playing sound instance
     * @param volume	The sound volume
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop( soundId );
        }
        return sound.play(volume);
    }

}