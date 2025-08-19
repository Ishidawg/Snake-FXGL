package com.ishidaw.snakefxgl.Entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.ishidaw.snakefxgl.Enums.EntityType;

import java.util.Random;

public class CollectibleItems extends Entity {

    Random random = new Random();
    private Entity item;

    public void createApple(int SCREEN_WIDTH, int SCREEN_HEIGHT, int UNIT_SIZE, EntityType entityType, String texture, Snake snake) {
        var applePosition = new Object() {
            int appleX = 0;
            int appleY = 0;
        };

        boolean onSnake;

        do {
            applePosition.appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
            applePosition.appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;

            // to ensure that an item (apple) will not spawn inside the snake
            onSnake = snake.getSnakeUnits().stream().anyMatch(segment ->
                    segment.getX() == applePosition.appleX && segment.getY() == applePosition.appleY
            );
        } while (onSnake);

        item = FXGL.entityBuilder()
                .type(entityType)
                .at(applePosition.appleX, applePosition.appleY)
                .viewWithBBox(texture)
                .with(new CollidableComponent(true))
                .buildAndAttach();
    }

    public double itemX() {
        return this.item.getX();
    }

    public double itemY() {
        return this.item.getY();
    }

    public void removeItem() {
        this.item.removeFromWorld();
    }

}
