package com.ishidaw.snakefxgl;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.CollisionHandler;
import com.ishidaw.snakefxgl.Entities.CollectibleItems;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ishidaw.snakefxgl.Enums.EntityType;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getWorldProperties;

public class SnakeApplication extends GameApplication {

    Random random = new Random();

    private Entity snake;

    CollectibleItems appleItem = new CollectibleItems();

    // assets are 32x32, so it NEEDS to be 640 (32 * 20).
    static final int SCREEN_WIDTH = 640;
    static final int SCREEN_HEIGHT = 640;
    static final int DEFAULT_EATEN_APPLES = 0;
    static final int DEFAULT_BODY_PARTS = 1;
    static final int UNIT_SIZE = 32; // Cell size

    int applesEaten = DEFAULT_EATEN_APPLES;
    int bodyParts = DEFAULT_BODY_PARTS;

    private double moveTimer = 0;
    private double gameSpeed = 0.08;

    private final List<Entity> snakeUnits = new ArrayList<>();

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

//    public void createApple() {
//        int appleX = random.nextInt(SCREEN_WIDTH / UNIT_SIZE) * UNIT_SIZE;
//        int appleY = random.nextInt(SCREEN_HEIGHT / UNIT_SIZE) * UNIT_SIZE;
//
//        apple = FXGL.entityBuilder()
//                .type(EntityType.APPLE)
//                .at(appleX, appleY)
//                .viewWithBBox(new Rectangle(UNIT_SIZE, UNIT_SIZE, Color.RED))
//                .with(new CollidableComponent(true))
//                .buildAndAttach();
//    }

    public void snakeAddUnits() {
        Entity newSegment = FXGL.entityBuilder()
                .type(EntityType.PLAYER)
                .at(snakeUnits.get(bodyParts - 1).getX(), snakeUnits.get(bodyParts - 1).getY())
                .viewWithBBox(new Rectangle(UNIT_SIZE, UNIT_SIZE, Color.BLACK))
                .with(new CollidableComponent(true))
                .buildAndAttach();
        snakeUnits.add(newSegment);
    }

    @Override
    protected void initGame() {
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
        appleItem.createApple(SCREEN_WIDTH, SCREEN_WIDTH, UNIT_SIZE);
    }

    @Override
    protected void initInput() {
        FXGL.onKey(KeyCode.W, () -> {
            if (!running) return;
            if(snakeUnits.getFirst().getPosition().getY() > 0 && direction != "Down") direction = "Up"; // move UP
        });

        FXGL.onKey(KeyCode.S, () -> {
            if (!running) return;
            if(snakeUnits.getFirst().getPosition().getY() < SCREEN_HEIGHT - UNIT_SIZE && direction != "Up") direction = "Down"; // move DOWN
        });

        FXGL.onKey(KeyCode.D, () -> {
            if (!running) return;
            if(snakeUnits.getFirst().getPosition().getX() < SCREEN_WIDTH - UNIT_SIZE && direction != "Left") direction = "Right"; // move RIGHT
        });

        FXGL.onKey(KeyCode.A, () -> {
            if (!running) return;
            if(snakeUnits.getFirst().getPosition().getX() > 0 && direction != "Right") direction = "Left"; // move LEFT
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!running) return;

        moveTimer += tpf;
        if (moveTimer < gameSpeed) {
            return;
        }
        moveTimer = 0;

        double prevX = snakeUnits.getFirst().getX();
        double prevY = snakeUnits.getFirst().getY();

        switch (direction) {
            case "Up": snakeUnits.getFirst().translateY(-UNIT_SIZE); break;
            case "Down": snakeUnits.getFirst().translateY(UNIT_SIZE); break;
            case "Left": snakeUnits.getFirst().translateX(-UNIT_SIZE); break;
            case "Right": snakeUnits.getFirst().translateX(UNIT_SIZE); break;
            default: break;
        }

        for (int i = 1; i < bodyParts; i++) {
            double tempX = snakeUnits.get(i).getX();
            double tempY = snakeUnits.get(i).getY();
            snakeUnits.get(i).setPosition(prevX, prevY);
            prevX = tempX;
            prevY = tempY;
        }

        double headX = snakeUnits.getFirst().getX();
        double headY = snakeUnits.getFirst().getY();
        if (headX < 0 || headX >= SCREEN_WIDTH || headY < 0 || headY >= SCREEN_HEIGHT) {
            gameOver();
            return;
        }

        for (int i = 1; i < bodyParts; i++) {
            if (headX == snakeUnits.get(i).getX() && headY == snakeUnits.get(i).getY()) {
                gameOver();
                return;
            }
        }

    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("applesEatenFXGL", applesEaten);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.APPLE) {

            @Override
            protected void onCollisionBegin(Entity player, Entity apple) {
                if (player != snakeUnits.getFirst()) return;

                FXGL.inc("applesEatenFXGL", +1);
                apple.removeFromWorld();

                snakeAddUnits();
                bodyParts++;

                gameSpeed = Math.max(gameSpeed - 0.01, 0.05);
                appleItem.createApple(SCREEN_WIDTH, SCREEN_WIDTH, UNIT_SIZE);
            }
        });
    }

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

        snake.removeFromWorld();

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
