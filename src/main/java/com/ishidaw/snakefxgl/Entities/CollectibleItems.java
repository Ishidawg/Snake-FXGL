package com.ishidaw.snakefxgl.Entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.ishidaw.snakefxgl.Enums.EntityType;

import java.util.Random;

public class CollectibleItems extends Entity {

    Random random = new Random();
    private Entity apple;

    public void createApple(int SCREEN_WIDTH, int SCREEN_HEIGHT, int UNIT_SIZE) {
        int appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
        int appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

        apple = FXGL.entityBuilder()
                .type(EntityType.APPLE)
                .at(appleX, appleY)
                .viewWithBBox("jewel.png")
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    public double itemX(Entity item) {
        return item.getX();
    }

    public double itemY(Entity item) {
        return item.getY();
    }
}
