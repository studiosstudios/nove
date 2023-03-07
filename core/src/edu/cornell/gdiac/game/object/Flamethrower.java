package edu.cornell.gdiac.game.object;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;
import edu.cornell.gdiac.game.obstacle.ComplexObstacle;

import javax.swing.*;

public class Flamethrower extends ComplexObstacle {
    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;
    protected Flame flame;

    protected BoxObstacle flameBase;

    /** Whether the flamethrower object is on and shooting a flame */
    private boolean isShooting;

    /**
     * Returns true if the flamethrower if shooting
     *
     * @return true if the flamethrower is shooting
     */
    public boolean getShooting() {
        return isShooting;
    }
    /**
     * Sets whether the flamethrower object is firing
     *
     * @param shooting whether the flamethrower is firing
     */
    public void setShooting(boolean shooting) {
        isShooting = shooting;
    }

    public Flamethrower(JsonValue data, float x, float y, float angle, Vector2 scale, TextureRegion flamethrowerTexture, TextureRegion flameTexture) {
        super(x,y);
//        setName("flamethrower");
        assert angle % 90 == 0;
        this.data = data;
        this.setFixedRotation(false);
        flame = new Flame(x, y,angle, scale, flameTexture,data);

        flameBase = new BoxObstacle(x, y-(flame.getHeight()*0.65f), flamethrowerTexture.getRegionWidth()/scale.x, flamethrowerTexture.getRegionHeight()/scale.y);
        flameBase.setDrawScale(scale);
        flameBase.setTexture(flamethrowerTexture);
        flameBase.setBodyType(BodyDef.BodyType.StaticBody);
//        flameBase.setAngle((float) (angle * Math.PI/180));
        flameBase.setFriction(0f);
        flameBase.setRestitution(0f);
        flameBase.setName("flamethrower");
        flameBase.setDensity(0f);
        bodies.add(flameBase);

        bodies.add(flame);
//        this.setAngle((float) (angle * Math.PI/180));
    }

    /**
     * Creates the joints for this object.
     * <p>
     * This method is executed as part of activePhysics. This is the primary method to
     * override for custom physics objects.
     *
     * @param world Box2D world to store joints
     * @return true if object allocation succeeded
     */
    @Override
    protected boolean createJoints(World world) {
        assert bodies.size > 0;

        WeldJointDef jointDef = new WeldJointDef();

        jointDef.bodyA = flameBase.getBody();
        jointDef.bodyB = flame.getBody();
        jointDef.localAnchorA.set(new Vector2());
        jointDef.localAnchorB.set(new Vector2(0f, -flame.getHeight()*0.5f));
        jointDef.collideConnected = false;
        Joint joint = world.createJoint(jointDef);
        joints.add(joint);

        return true;
    }

    /**
     * Updates the object's physics state (NOT GAME LOGIC).
     *
     * We use this method to reset cooldowns.
     *
     * @param dt	Number of seconds since last animation frame
     */
    public void update(float dt) {

    }
}
