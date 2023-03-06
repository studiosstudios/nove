package edu.cornell.gdiac.game.object;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.game.*;
import edu.cornell.gdiac.game.obstacle.*;

public class Spikes extends BoxObstacle {

    private PolygonShape sensorShape;

    private static final String sensorName = "spikesTop";

    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;

    public Spikes(float x, float y, float angle, TextureRegion texture, Vector2 scale, JsonValue data){
        super( x+data.get("offset").getFloat(0),
                y+data.get("offset").getFloat(1),
                texture.getRegionWidth()/scale.x,
                texture.getRegionHeight()/scale.y);
        assert angle % 90 == 0;
        this.data = data;
        setAngle((float) (angle * Math.PI/180));
        setBodyType(BodyDef.BodyType.StaticBody);
        setFixedRotation(true);
        setName("spikes");
        setDrawScale(scale);
        setTexture(texture);
    }

    public boolean activatePhysics(World world){
        if (!super.activatePhysics(world)) {
            return false;
        }

        //create top sensor
        Vector2 sensorCenter = new Vector2(0, getHeight()/2);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        sensorShape = new PolygonShape();
        sensorShape.setAsBox(getWidth()/2*data.getFloat("sensor_width_scale"),
                             getHeight()/2*data.getFloat("sensor_height_scale"),
                                 sensorCenter, 0.0f);
        sensorDef.shape = sensorShape;

        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());

        return true;
    }

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


    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape,Color.RED,getX(),getY(),getAngle(),drawScale.x,drawScale.y);
    }

}
