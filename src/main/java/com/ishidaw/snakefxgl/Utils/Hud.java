package com.ishidaw.snakefxgl.Utils;

import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameScene;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;

public class Hud extends Node {
    // For some reason, just using the default Font.font("Gameplay.ttf") was not working AT ALL
    // So I get the font directly from the source
    Font gameFont = Font.loadFont(getClass().getResourceAsStream("/assets/ui/fonts/Gameplay.ttf"), 26);

    public void initBackground() {
        FXGL.entityBuilder()
                .view("background768.png")
                .buildAndAttach();
    }

    public void gameOverHUD() {
        FXGL.entityBuilder()
                .view("gameover768.png")
                .buildAndAttach();
    }

    public Text countdownHUD( int SCREEN_WIDTH, int SCREEN_HEIGHT) {
        Text counting = new Text();
        counting.setScaleX(4);
        counting.setScaleY(4);
        counting.setFill(Color.WHITE);
        counting.setFontSmoothingType(FontSmoothingType.LCD);
        counting.setFont(Font.font(18));
        counting.setTranslateX((SCREEN_WIDTH - counting.getLayoutBounds().getWidth()) / 2);
        counting.setTranslateY((double) SCREEN_HEIGHT / 4);

        counting.setFont(gameFont);
        counting.setStroke(Color.BLACK);
        counting.setStrokeWidth(1);

        return counting;
    }

    public void mainHUD() {
        Node topbar = FXGL.getAssetLoader().loadTexture("topbar768.png");
        Node pause = FXGL.getAssetLoader().loadTexture("pause768.png");

        getGameScene().addUINode(topbar);
        getGameScene().addUINode(pause);

        FXGL.getGameTimer().runOnceAfter(() -> {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.25), pause);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                getGameScene().removeUINode(pause);

                pause.setOpacity(1.0);
            });

            fadeOut.play();
        }, Duration.seconds(3));
    }


    public void scoreLabel() {
        Text scoreLabel = new Text();

        scoreLabel.setScaleX(1);
        scoreLabel.setScaleY(1);
        scoreLabel.setFill(Color.WHITE);
        scoreLabel.setFontSmoothingType(FontSmoothingType.LCD);
        scoreLabel.setFont(Font.font(20));
        scoreLabel.setTranslateX(64);
        scoreLabel.setTranslateY(42);

        scoreLabel.textProperty().bind(getWorldProperties().intProperty("applesEatenFXGL").asString());

        scoreLabel.setFont(gameFont);
        scoreLabel.setStroke(Color.BLACK);
        scoreLabel.setStrokeWidth(1);

        FXGL.getGameScene().addUINode(scoreLabel);
    }

    public void highScoreLabel() {
        Text highScoreLabel = new Text();

        highScoreLabel.setScaleX(1);
        highScoreLabel.setScaleY(1);
        highScoreLabel.setFill(Color.WHITE);
        highScoreLabel.setFontSmoothingType(FontSmoothingType.LCD);
        highScoreLabel.setFont(Font.font(20));
        highScoreLabel.setTranslateX(192);
        highScoreLabel.setTranslateY(42);

        highScoreLabel.textProperty().bind(getWorldProperties().intProperty("highScoreFXGL").asString());

        highScoreLabel.setFont(gameFont);
        highScoreLabel.setStroke(Color.BLACK);
        highScoreLabel.setStrokeWidth(1);

        FXGL.getGameScene().addUINode(highScoreLabel);
    }



    public void buildCustomHUD(Text scoreLabel) {
        FXGL.getGameScene().addUINode(scoreLabel);
    }

    public void removeCustomHUD(Text scoreLabel) {
        FXGL.getGameScene().removeUINode(scoreLabel);
    }
 }
