package com.ishidaw.snakefxgl.Entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.ishidaw.snakefxgl.Enums.EntityType;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class Snake {

    private Entity snake;
    private final List<Entity> snakeUnits = new ArrayList<>();

    public List<Entity> getSnakeUnits() {
        return snakeUnits;
    }

    public void createSnake(int bodyParts, int SCREEN_WIDTH, int SCREEN_HEIGHT, int UNIT_SIZE) {
        for (int i = 0; i < bodyParts; i++) {
            Entity segment = FXGL.entityBuilder()
                    .type(EntityType.PLAYER)
                    .at(SCREEN_WIDTH / 2 - i * UNIT_SIZE, SCREEN_HEIGHT / 2)
                    .viewWithBBox(new Rectangle(UNIT_SIZE, UNIT_SIZE, Color.GREEN))
                    .with(new CollidableComponent(true))
                    .buildAndAttach();
            snakeUnits.add(segment);
        }

        // Means that the snakeUnits[0] is the "head" -> snake
        snake = snakeUnits.getFirst();
    }

    public void snakeAddUnits(int bodyParts, int UNIT_SIZE) {
        Entity newSegment = FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(snakeUnits.get(bodyParts - 1).getX(), snakeUnits.get(bodyParts - 1).getY())
                .viewWithBBox(new Rectangle(UNIT_SIZE, UNIT_SIZE, Color.BLACK))
                .with(new CollidableComponent(true))
                .buildAndAttach();
        snakeUnits.add(newSegment);
    }

    public void removeSnake() {
        snake.removeFromWorld();
    }

}
