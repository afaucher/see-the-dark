package com.mygdx.game.ai;

import com.badlogic.gdx.ai.steer.SteeringAcceleration;
import com.badlogic.gdx.ai.steer.SteeringBehavior;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.mygdx.game.TwoAxisControl;
import com.mygdx.game.ship.Ship;

public class ShipSteeringEntity extends GenericSteerable {
    
    private Ship ship;
    private TwoAxisControl controls;
    private SteeringBehavior<Vector2> steeringBehavior;
    private static final SteeringAcceleration<Vector2> steeringOutput = new SteeringAcceleration<Vector2>(new Vector2());
    
    public ShipSteeringEntity(Ship ship, TwoAxisControl controls) {
        this.ship = ship;
        this.controls = controls; 
        
        //FIXME: These are completely bogus numbers and may not be used
        this.setMaxAngularAcceleration(10f);
        this.setMaxAngularSpeed(10f);
        this.setMaxLinearAcceleration(10f);
        this.setMaxLinearSpeed(10f);
    }
    
    public void setSteeringBehavior(SteeringBehavior<Vector2> steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }
    
    public void update(float seconds) {
        //FIXME: This is currently keeping the AI from burning itself out
        final float L_FRACTION = 0.1f;
        
        if (steeringBehavior != null) {
            steeringBehavior.calculateSteering(steeringOutput);
            
            //This is completely bogus
            float turn = steeringOutput.angular > 0 ? 1 : (steeringOutput.angular < 0 ? -1 : 0);
            float go = steeringOutput.linear.y > 0 ? L_FRACTION : (steeringOutput.linear.y < 0 ? -L_FRACTION : 0);
            this.controls.setX(turn);
            this.controls.setY(go);
        }
    }

    @Override
    public float getAngularVelocity() {
        Body body = ship.getBody();
        return body.getAngularVelocity();
    }

    @Override
    public Vector2 getLinearVelocity() {
        Body body = ship.getBody();
        return body.getLinearVelocity();
    }

    @Override
    public float getOrientation() {
        Body body = ship.getBody();
        return body.getAngle();
    }

    @Override
    public Vector2 getPosition() {
        Body body = ship.getBody();
        return body.getPosition();
    }
    
    


}
