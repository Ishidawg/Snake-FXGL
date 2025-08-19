package com.ishidaw.snakefxgl.Entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.ishidaw.snakefxgl.Enums.EntityType;

import java.util.ArrayList;
import java.util.List;

public class Snake {

    private Entity snake;
    private final List<Entity> snakeUnits = new ArrayList<>();

    public void createSnake(int bodyParts, int SCREEN_WIDTH, int SCREEN_HEIGHT, int UNIT_SIZE) {
        for (int i = 0; i < bodyParts; i++) {
            Entity segment = FXGL.entityBuilder()
                    .type(EntityType.PLAYER)
                    .at((double) SCREEN_WIDTH / 2 - i * UNIT_SIZE, (double) SCREEN_HEIGHT / 2)
                    .viewWithBBox("snake_head.png")
                    .with(new CollidableComponent(true))
                    .buildAndAttach();
            getSnakeUnits().add(segment);
        }

        // Means that the snakeUnits[0] is the "head" -> snake
        snake = getSnakeUnits().getFirst();
        snake.setRotation(270);
    }

    public void snakeAddUnits(int bodyParts) {
        Entity newSegment = FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(getSnakeUnits().get(bodyParts - 1).getX(), getSnakeUnits().get(bodyParts - 1).getY())
                .viewWithBBox("snake_body.png")
                .with(new CollidableComponent(true))
                .buildAndAttach();
        getSnakeUnits().add(newSegment);
    }   

    public List<Entity> getSnakeUnits() {
        return this.snakeUnits;
    }

    public void removeSnake() {
        this.snake.removeFromWorld();
    }

    public void setSnakeHead(double angle) {
        this.snake.setRotation(angle);
    }

    public void setSnakeBody(double angle) {
        for (int i = 1; i < snakeUnits.size(); i++) {
            this.snakeUnits.get(i).setRotation(angle);
        }
    }

    public void growSnake(int bodyParts) {
        snakeAddUnits(bodyParts);
    }

    public double snakeHeadX() {
        return getSnakeUnits().getFirst().getX();
    }

    public double snakeHeadY() {
        return getSnakeUnits().getFirst().getY();
    }

}
