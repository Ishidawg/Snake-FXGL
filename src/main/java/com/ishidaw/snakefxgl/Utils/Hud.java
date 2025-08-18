package com.ishidaw.snakefxgl.Utils;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class Hud extends Node {

    public void initBackground() {
        FXGL.entityBuilder()
                .view("vector_backgroud.png")
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

    public Text defaultHUD(
            int SCREEN_WIDTH,
            int scaleX,
            int scaleY,
            int scoreUpdatedValue
    ) {
        StringBuilder finalScore = new StringBuilder();
        Text scoreLabel = new Text();
        scoreLabel.setScaleX(scaleX);
        scoreLabel.setScaleY(scaleY);
        scoreLabel.setFill(Color.WHITE);
        scoreLabel.setTranslateX((SCREEN_WIDTH - scoreLabel.getLayoutBounds().getWidth()) / 2);
        scoreLabel.setTranslateY(20);

        finalScore.append("score: ").append(scoreUpdatedValue);

        scoreLabel.setText(String.valueOf(finalScore));

        return scoreLabel;
    }

    public void buildCustomHUD(Text scoreLabel) {
        FXGL.getGameScene().addUINode(scoreLabel);
    }

    public void removeCustomHUD(Text scoreLabel) {
        FXGL.getGameScene().removeUINode(scoreLabel);
    }
 }
