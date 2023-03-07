package edu.cornell.gdiac.game.object;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.game.obstacle.*;

public abstract class Activator extends PolygonObstacle {

    /** if the activator is activating objects*/
    protected boolean active;

    /** if the activator is currently being pressed*/
    protected boolean pressed;

    /** each activator has a unique string id specified in JSON*/
    protected final String id;

    /** The initializing data (to avoid magic numbers) */
    protected final JsonValue data;
    public boolean isActive(){ return active; }

    public boolean isPressed(){ return pressed; }

    public String getID(){ return id; }

    public void setPressed(boolean pressed){ this.pressed = pressed; }

    public abstract void updateActivated();

    public Activator(float x, float y, String id, TextureRegion texture, Vector2 scale, JsonValue data){
        super(data.get("body_shape").asFloatArray(),
                x+data.get("offset").getFloat(0),
                y+data.get("offset").getFloat(1));

        this.data = data;
        this.id = id;
        setBodyType(BodyDef.BodyType.StaticBody);
        active = false;
        setDrawScale(scale);
        setTexture(texture);
        setFixedRotation(true);
    }

}
