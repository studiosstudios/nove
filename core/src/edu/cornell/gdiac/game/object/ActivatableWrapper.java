package edu.cornell.gdiac.game.object;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;

/** wraps an Activatable to have defined behaviour when interacting with Activators*/
public class ActivatableWrapper {

    public Activatable object;

    private final boolean initialActivation;

    public ActivatableWrapper(Activatable object, JsonValue data){
        this.object = object;

        if (data == null) {
            object.setActivated(false);
        } else {
            object.setActivated(data.getBoolean("active", false));
        }
        initialActivation = object.isActivated();

    }

    /**
     * Sets the active status of the object based on the output from an activator/s.
     * @param activator  whether the corresponding activators are active
     */
    public void updateActivated(boolean activator, World world){
        boolean next = initialActivation ^ activator;
        if (next && !object.isActivated()) {
            //state switch from inactive to active
            object.setActivated(true);
            object.activated(world);
        } else if (!next && object.isActivated()){
            //state switch from active to inactive
            object.setActivated(false);
            object.deactivated(world);
        }
    }

    public void setActivated(boolean activated) {object.setActivated(activated);}
    public boolean isActivated() { return object.isActivated(); }

}
