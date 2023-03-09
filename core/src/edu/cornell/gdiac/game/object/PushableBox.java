package edu.cornell.gdiac.game.object;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import edu.cornell.gdiac.game.obstacle.BoxObstacle;

public class PushableBox extends BoxObstacle {


    public PushableBox(TextureRegion texture, Vector2 scale, JsonValue data){
        super(texture.getRegionWidth()/scale.x,
                texture.getRegionHeight()/scale.y);

        setBodyType(BodyDef.BodyType.DynamicBody);
        setFixedRotation(true);
        setName("box");
        setDrawScale(scale);
        setTexture(texture);;

        this.objectData = data;

        setFriction(objectData.getFloat("friction", 0));
        setDensity(objectData.getFloat("density", 0));
        setMass(objectData.getFloat("mass", 0));

        init();
    }

    @Override
    public void activated(World world){
        setBodyType(BodyDef.BodyType.KinematicBody);
    }

    @Override
    public void deactivated(World world){
        setBodyType(BodyDef.BodyType.DynamicBody);
    }

    @Override
    public void init(){
        setX(objectData.get("pos").getFloat(0)+objectConstants.get("offset").getFloat(0));
        setY(objectData.get("pos").getFloat(1)+objectConstants.get("offset").getFloat(1));
    }

}
