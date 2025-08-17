package com.ishidaw.snakefxgl.Utils;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;

public class Hud {

    public void initBackground() {
        FXGL.entityBuilder()
                .view("background.png")
                .buildAndAttach();
    }

    public void gameOverHUD(
            int SCREEN_WIDTH,
            int SCREEN_HEIGHT,
            String message,
            Color color,
            int scaleX,
            int scaleY
    ) {
        Text gameOverText = new Text(message);
        gameOverText.setScaleX(scaleX);
        gameOverText.setScaleY(scaleY);
        gameOverText.setFill(color);
        gameOverText.setTranslateX((SCREEN_WIDTH - gameOverText.getLayoutBounds().getWidth()) / 2);
        gameOverText.setTranslateY((double) SCREEN_HEIGHT / 2);
        FXGL.getGameScene().addUINode(gameOverText);
    }

    public void defaultHUD(int SCREEN_WIDTH) {
        Text scoreLabel = new Text();
        scoreLabel.setScaleX(2);
        scoreLabel.setScaleY(2);
        scoreLabel.setFill(Color.WHITE);
        scoreLabel.setTranslateX((SCREEN_WIDTH - scoreLabel.getLayoutBounds().getWidth()) / 2);
        scoreLabel.setTranslateY(20);

        scoreLabel.textProperty().bind(getWorldProperties().intProperty("applesEatenFXGL").asString());

        FXGL.getGameScene().addUINode(scoreLabel);
    }
}
