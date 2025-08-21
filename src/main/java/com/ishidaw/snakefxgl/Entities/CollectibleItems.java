package com.ishidaw.snakefxgl.Entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.ishidaw.snakefxgl.Enums.EntityType;

import java.util.Random;

public class CollectibleItems extends Entity {

    Random random = new Random();
    private Entity item;

    public void createApple(EntityType entityType, String texture, Snake snake) {
        var applePosition = new Object() {
            int appleX = 0;
            int appleY = 0;
        };

        boolean onSnake;

        // just get a random n where n in  [0, 16] * 64 + 64  by sooluckyseven

        do {

            // Returns one of {64, 128, 192, 256, 320, 384, 448, 512, 576, 640, 704, 768, 832, 896, 960}
            applePosition.appleX = random.nextInt(12) * 64;

            // Try to prevent to spawn on the first unit size of the screen height
            // Need to take notes on this: random.nextInt(11) * 64 + 64 -> 64 to 768
            // NextInt(11) means that I have 11 numbers, stating FROM 0 to 10
            // Every number gets this: 0*0+64=64, 10*64+64=704 and so on until 14*64+64=960
            // Returns one of {64, 128, 192, 256, 320, 384, 448, 512, 576, 640, 704, 768, 832, 896, 960}
            applePosition.appleY = random.nextInt(11) * 64 + 64;

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
