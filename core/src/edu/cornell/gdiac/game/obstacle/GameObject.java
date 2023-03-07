package edu.cornell.gdiac.game.obstacle;

import com.badlogic.gdx.utils.JsonValue;


/** This class currently does nothing and is not used, but it will represent the root
 * of all objects in the level. It currently just manages activation state logic and
 * serves as a way to have all objects in a level in a single array.
 */
public abstract class GameObject {
    /** if the object is initially activated */
    private final boolean initialActivation;

    /** if the object is currently activated */
    private boolean activated;

    /** The initializing data (to avoid magic numbers) */
    private final JsonValue data;

    public GameObject(boolean activated, JsonValue data){
        this.data = data;
        this.activated = activated;
        initialActivation = activated;
    }

    /**
     * Sets the active status of the object.
     * @param activator  whether the corresponding activators are active
     */
    public void setActive(boolean activator){

        boolean next = initialActivation ^ activator;
        if (next && !activated) {
            //state switch from inactive to active
            activated = true;
            activated();
        } else if (!next && activated){
            //state switch from active to inactive
            activated = false;
            deactivated();
        }
    }

    public abstract void activated();

    public abstract void deactivated();

    public abstract void reset();
}
