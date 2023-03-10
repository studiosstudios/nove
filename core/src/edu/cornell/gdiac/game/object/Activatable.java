package edu.cornell.gdiac.game.object;

import com.badlogic.gdx.physics.box2d.World;


/** interface implemented by all game objects that can be activated by buttons,
 * switches etc.*/
public interface Activatable {

    /** called whenever the object is activated */
    void activated(World world);

    /** called whenever the object is deactivated */
    void deactivated(World world);

    /** should only be used by ActivatableWrapper */
    void setActivated(boolean activated);

    boolean isActivated();
}
