package edu.cornell.gdiac.game.obstacle;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;


/** This class currently does nothing and is not used, but it will represent the root
 * of all objects in the level. It currently just manages activation state logic and
 * serves as a way to have all objects in a level in a single array.
 */
public abstract class GameObject {
    /** if the object is initially activated */
    private boolean initialActivation;

    /** if the object is currently activated */
    protected boolean activated;

    /** The initialization data of this game object */
    protected JsonValue objectData;

    /** constants shared by all objects of this class (to avoid magic numbers) */
    protected static JsonValue objectConstants;

    protected GameObject(){

    }

    /**
     * Sets the active status of the object based on the output from an activator/s.
     * @param activator  whether the corresponding activators are active
     */
    public void updateActivated(boolean activator, World world){
        boolean next = initialActivation ^ activator;
        if (next && !activated) {
            //state switch from inactive to active
            activated = true;
            activated(world);
        } else if (!next && activated){
            //state switch from active to inactive
            activated = false;
            deactivated(world);
        }
    }

    public void setActivated(boolean activated) {this.activated = activated;}
    public boolean isActivated() { return activated; }

    /** method called when object switches from inactive to active */
    public abstract void activated(World world);

    /** method called when object switches from active to inactive */
    public abstract void deactivated(World world);

    public static void setConstants(JsonValue constants){ objectConstants = constants; }

    public void init(){
        if (objectData == null) {
            activated = false;
        } else {
            activated = objectData.getBoolean("active", false);
            initialActivation = activated;
        }
    }

}
