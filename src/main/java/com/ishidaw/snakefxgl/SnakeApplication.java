package com.ishidaw.snakefxgl;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ishidaw.snakefxgl.Entities.CollectibleItems;
import com.ishidaw.snakefxgl.Entities.Snake;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Map;

import com.ishidaw.snakefxgl.Enums.EntityType;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;

public class SnakeApplication extends GameApplication {

    Snake snakePlayer = new Snake();
    CollectibleItems appleItem = new CollectibleItems();

    // assets are 32x32, so it NEEDS to be multiple of 2 (32 * 20).
    // bunch of constants
    static final int SCREEN_WIDTH = 1024;
    static final int SCREEN_HEIGHT = 1024;
    static final int DEFAULT_EATEN_APPLES = 0;
    static final int DEFAULT_BODY_PARTS = 1;
    static final int UNIT_SIZE = 32; // Cell size

    int applesEaten = DEFAULT_EATEN_APPLES;
    int bodyParts = DEFAULT_BODY_PARTS;

    // Keeps snake under controlled speed
    private double moveTimer = 0; // Just iterate elapsed time
    private double gameSpeed = 0.10; // Seconds between snakes moves

    boolean running = true;
    String direction = "Down"; // Start direction

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(SCREEN_WIDTH);
        settings.setHeight(SCREEN_HEIGHT);
        settings.setVersion("");
        settings.setTitle("Snake");
        settings.setTicksPerSecond(60);
    }

    @Override
    protected void initGame() {
        snakePlayer.createSnake(bodyParts, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);
        appleItem.createApple(SCREEN_WIDTH, SCREEN_WIDTH, UNIT_SIZE);
    }

    @Override
    protected void initInput() {
        // The running check is to ignore inputs if it's game over
        FXGL.onKey(KeyCode.W, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getY() > 0 && direction != "Down") direction = "Up"; // move UP
        });

        FXGL.onKey(KeyCode.S, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getY() < SCREEN_HEIGHT - UNIT_SIZE && direction != "Up") direction = "Down"; // move DOWN
        });

        FXGL.onKey(KeyCode.D, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getX() < SCREEN_WIDTH - UNIT_SIZE && direction != "Left") direction = "Right"; // move RIGHT
        });

        FXGL.onKey(KeyCode.A, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getX() > 0 && direction != "Right") direction = "Left"; // move LEFT
        });
    }

    @Override
    protected void onUpdate(double tpf) { // tpf is apprx 0.0167, framelimit = 60
        if (!running) return;

        moveTimer += tpf;

        int maxSteps = 5;
        int steps = 0;

        // This is the "auto movement" behind the snake, and how speedy is based on the while loop within the Move Timer and GameSpeed
        while (moveTimer >= gameSpeed && steps < maxSteps) {
            moveTimer -= gameSpeed;
            moveOneStep();
            steps++;
        }

    }



    private void moveOneStep() {
        // Save the previous head position, so I can use it later to move the body segments
        double prevX = snakePlayer.getSnakeUnits().getFirst().getX();
        double prevY = snakePlayer.getSnakeUnits().getFirst().getY();

        switch (direction) {
            case "Up": snakePlayer.getSnakeUnits().getFirst().translateY(-UNIT_SIZE); break;
            case "Down": snakePlayer.getSnakeUnits().getFirst().translateY(UNIT_SIZE); break;
            case "Left": snakePlayer.getSnakeUnits().getFirst().translateX(-UNIT_SIZE); break;
            case "Right": snakePlayer.getSnakeUnits().getFirst().translateX(UNIT_SIZE); break;
            default: break;
        }

        // Use the head position to move each body part onto it
        for (int i = 1; i < bodyParts; i++) {
            double tempX = snakePlayer.getSnakeUnits().get(i).getX();
            double tempY = snakePlayer.getSnakeUnits().get(i).getY();
            snakePlayer.getSnakeUnits().get(i).setPosition(prevX, prevY);
            prevX = tempX;
            prevY = tempY;
        }

        // Check if head hits screen bounds
        double headX = snakePlayer.getSnakeUnits().getFirst().getX();
        double headY = snakePlayer.getSnakeUnits().getFirst().getY();
        if (headX < 0 || headX >= SCREEN_WIDTH || headY < 0 || headY >= SCREEN_HEIGHT) {
            gameOver();
            return;
        }

        // Check if head hits some bodypart
        for (int i = 1; i < bodyParts; i++) {
            if (headX == snakePlayer.getSnakeUnits().get(i).getX() && headY == snakePlayer.getSnakeUnits().get(i).getY()) {
                gameOver();
                return;
            }
        }
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("applesEatenFXGL", applesEaten);
    }

    // Custom check "collision" -> check the position of CollectibleItem and Snake


//    @Override
    ////    protected void initPhysics() {
    ////        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.APPLE) {
    ////
    ////            @Override
    ////            protected void onCollisionBegin(Entity player, Entity apple) {
    ////                // Collision counts only if it's the head [0]
    ////                if (player != snakePlayer.getSnakeUnits().getFirst()) return;
    ////
    ////                FXGL.inc("applesEatenFXGL", +1);
    ////                apple.removeFromWorld();
    ////
    ////                snakePlayer.snakeAddUnits(bodyParts, UNIT_SIZE);
    ////                bodyParts++;
    ////
    ////                gameSpeed = Math.max(gameSpeed - 0.01, 0.05);
    ////                appleItem.createApple(SCREEN_WIDTH, SCREEN_WIDTH, UNIT_SIZE);
    ////            }
    ////        });
    ////    }

    @Override
    protected void initUI() {
        Text scoreLabel = new Text();
        scoreLabel.setScaleX(2);
        scoreLabel.setScaleY(2);
        scoreLabel.setFill(Color.BLACK);
        scoreLabel.setTranslateX((SCREEN_WIDTH - scoreLabel.getLayoutBounds().getWidth()) / 2);
        scoreLabel.setTranslateY(20);

        scoreLabel.textProperty().bind(getWorldProperties().intProperty("applesEatenFXGL").asString());

        FXGL.getGameScene().addUINode(scoreLabel);
    }

    private void gameOver() {
        running = false;

        snakePlayer.removeSnake();

        direction = " ";

        Text gameOverText = new Text("GAME OVER!");
        gameOverText.setScaleX(2);
        gameOverText.setScaleY(2);
        gameOverText.setFill(Color.BLACK);
        gameOverText.setTranslateX((SCREEN_WIDTH - gameOverText.getLayoutBounds().getWidth()) / 2);
        gameOverText.setTranslateY(SCREEN_HEIGHT / 2);
        FXGL.getGameScene().addUINode(gameOverText);
    }
}
