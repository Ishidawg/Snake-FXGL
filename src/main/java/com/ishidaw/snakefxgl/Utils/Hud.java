package com.ishidaw.snakefxgl.Utils;

import com.almasb.fxgl.dsl.FXGL;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;

public class Hud extends Node {

    public void initBackground() {
        FXGL.entityBuilder()
                .view("background.png")
                .buildAndAttach();
    }

    public void gameOverHUD() {
        FXGL.entityBuilder()
                .view("gameover.png")
                .buildAndAttach();
    }

    public Text defaultHUD(
            int SCREEN_WIDTH,
            int scoreUpdatedValue
    ) {
        StringBuilder finalScore = new StringBuilder();
        Text scoreLabel = new Text();
        // For some reason, just using the default Font.font("Gameplay.ttf") was not working AT ALL
        // So I get the font directly from the source
        Font gameFont = Font.loadFont(getClass().getResourceAsStream("/assets/ui/fonts/Gameplay.ttf"), 26);

        scoreLabel.setScaleX(2);
        scoreLabel.setScaleY(2);
        scoreLabel.setFill(Color.WHITE);
        scoreLabel.setFontSmoothingType(FontSmoothingType.LCD);
        scoreLabel.setFont(Font.font(18));
        scoreLabel.setTranslateX((SCREEN_WIDTH - scoreLabel.getLayoutBounds().getWidth()) / 2);
        scoreLabel.setTranslateY(50);

        finalScore.append("SCORE ").append(scoreUpdatedValue);
        scoreLabel.setText(String.valueOf(finalScore));
        FXGL.centerTextX(scoreLabel, 0, SCREEN_WIDTH);

        scoreLabel.setFont(gameFont);
        scoreLabel.setStroke(Color.BLACK);
        scoreLabel.setStrokeWidth(1);

        return scoreLabel;
    }

    public void buildCustomHUD(Text scoreLabel) {
        FXGL.getGameScene().addUINode(scoreLabel);
    }

    public void removeCustomHUD(Text scoreLabel) {
        FXGL.getGameScene().removeUINode(scoreLabel);
    }
 }
