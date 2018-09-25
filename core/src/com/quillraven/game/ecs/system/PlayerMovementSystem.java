package com.quillraven.game.ecs.system;


import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.math.Vector2;
import com.quillraven.game.core.ecs.component.Box2DComponent;
import com.quillraven.game.core.input.EKey;
import com.quillraven.game.core.input.InputManager;
import com.quillraven.game.core.input.KeyInputListener;
import com.quillraven.game.ecs.component.PlayerComponent;

import static com.quillraven.game.core.input.EKey.*;

public class PlayerMovementSystem extends IteratingSystem implements KeyInputListener {
    private final ComponentMapper<PlayerComponent> playerCmpMapper;
    private final ComponentMapper<Box2DComponent> b2dCmpMapper;
    private boolean directionChange;
    private int xFactor;
    private int yFactor;

    public PlayerMovementSystem(final ComponentMapper<PlayerComponent> playerCmpMapper, final ComponentMapper<Box2DComponent> b2dCmpMapper) {
        super(Family.all(Box2DComponent.class, PlayerComponent.class).get());
        this.playerCmpMapper = playerCmpMapper;
        this.b2dCmpMapper = b2dCmpMapper;
        directionChange = false;
        xFactor = 0;
        yFactor = 0;
    }

    @Override
    protected void processEntity(final Entity entity, final float deltaTime) {
        final Box2DComponent b2dCmp = b2dCmpMapper.get(entity);
        final PlayerComponent playerCmp = playerCmpMapper.get(entity);

        if (playerCmp.sleepTime > 0) {
            playerCmp.sleepTime -= deltaTime;
            b2dCmp.body.setActive(playerCmp.sleepTime <= 0);
            return;
        }

        if (directionChange) {
            directionChange = false;
            playerCmp.speed.x = xFactor * playerCmp.maxSpeed;
            playerCmp.speed.y = yFactor * playerCmp.maxSpeed;
        }

        final Vector2 worldCenter = b2dCmp.body.getWorldCenter();
        b2dCmp.body.applyLinearImpulse((playerCmp.speed.x - b2dCmp.body.getLinearVelocity().x) * b2dCmp.body.getMass(), (playerCmp.speed.y - b2dCmp.body.getLinearVelocity().y) * b2dCmp.body.getMass(), worldCenter.x, worldCenter.y, true);
        b2dCmp.positionBeforeUpdate.set(b2dCmp.body.getPosition());
    }

    @Override
    public void keyDown(final InputManager manager, final EKey key) {
        switch (key) {
            case LEFT:
                directionChange = true;
                xFactor = -1;
                break;
            case RIGHT:
                directionChange = true;
                xFactor = 1;
                break;
            case UP:
                directionChange = true;
                yFactor = 1;
                break;
            case DOWN:
                directionChange = true;
                yFactor = -1;
                break;
        }
    }

    @Override
    public void keyUp(final InputManager manager, final EKey key) {
        switch (key) {
            case LEFT:
                directionChange = true;
                xFactor = manager.isKeyDown(RIGHT) ? 1 : 0;
                break;
            case RIGHT:
                directionChange = true;
                xFactor = manager.isKeyDown(LEFT) ? -1 : 0;
                break;
            case UP:
                directionChange = true;
                yFactor = manager.isKeyDown(DOWN) ? -1 : 0;
                break;
            case DOWN:
                directionChange = true;
                yFactor = manager.isKeyDown(UP) ? 1 : 0;
                break;
        }
    }
}
