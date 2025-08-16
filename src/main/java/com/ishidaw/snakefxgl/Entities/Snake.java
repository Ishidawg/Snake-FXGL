package com.ishidaw.snakefxgl.Entities;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Snake {

    public Entity snake;

    protected Entity initGame() {
        snake = FXGL.entityBuilder()
                .at(300, 300)
                .view(new Rectangle(25, 25, Color.BLUEVIOLET))
                .buildAndAttach();

        return snake;
    }
}
