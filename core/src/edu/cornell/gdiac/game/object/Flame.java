package edu.cornell.gdiac.game.object;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.game.GameCanvas;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;

public class Flame extends BoxObstacle {
    private PolygonShape sensorShape;

    private Vector2 scale;

    private static final String sensorName = "flameSensor";

    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;
    /**
     * Returns the name of the top sensor
     *
     * This is used by ContactListener
     *
     * @return the name of the ground sensor
     */
    public static String getSensorName() {
        return sensorName;
    }
    public Flame(float x, float y, float angle, Vector2 scale, TextureRegion texture, JsonValue data) {
        super(x, y, texture.getRegionWidth()/scale.x, texture.getRegionHeight()/scale.y);
        this.data = data;
        this.scale = scale;
        assert angle % 90 == 0;
//        setAngle((float) (angle * Math.PI/180));
        setBodyType(BodyDef.BodyType.StaticBody);
//        setFixedRotation(true);
        setName("flame");
        setDrawScale(scale);
        setTexture(texture);
        setSensor(true);
    }

    public boolean activatePhysics(World world) {
        if (!super.activatePhysics(world)) {
            return false;
        }
        Vector2 sensorCenter = new Vector2(0, 0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(getWidth()*0.5f*0.9f,
                getHeight()*0.5f,
                sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());
        return true;
    }
    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape, Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }
}
