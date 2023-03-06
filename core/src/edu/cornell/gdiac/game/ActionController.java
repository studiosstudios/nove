package edu.cornell.gdiac.game;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import edu.cornell.gdiac.game.object.Cat;

public class ActionController {

    private InputController inputController;
//    private AIController aiController;


    public ActionController() {

    }


    public void updateAvatar(float dt, Cat avatar) {
        // Process actions in object model
        avatar.setMovement(InputController.getInstance().getHorizontal() * avatar.getForce());
        avatar.setJumping(InputController.getInstance().didPrimary());
        avatar.setShooting(InputController.getInstance().didSecondary());
    }


}
