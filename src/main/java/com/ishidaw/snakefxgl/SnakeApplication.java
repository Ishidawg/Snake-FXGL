package com.ishidaw.snakefxgl;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
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
    static final int SCREEN_WIDTH = 512;
    static final int SCREEN_HEIGHT = 512;
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
        initBackground();
        snakePlayer.createSnake(bodyParts, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);
        appleItem.createApple(SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE, EntityType.ITEM, "jewel.png");
    }

    // default angle 180
    public void playerMovementUp() {
        direction = "Up";
        snakePlayer.setSnakeHead(90);
    }

    public void playerMovementDown() {
        direction = "Down";
        snakePlayer.setSnakeHead(270);
    }

    public void playerMovementRight() {
        direction = "Right";
        snakePlayer.setSnakeHead(180);
    }

    public void playerMovementLeft() {
        direction = "Left";
        snakePlayer.setSnakeHead(360);
    }

    @Override
    protected void initInput() {
        // The running check is to ignore inputs if it's game over
        FXGL.onKey(KeyCode.W, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getY() > 0 && !direction.equals("Down")) playerMovementUp();
        });

        FXGL.onKey(KeyCode.S, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getY() < SCREEN_HEIGHT - UNIT_SIZE && !direction.equals("Up")) playerMovementDown();
        });

        FXGL.onKey(KeyCode.D, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getX() < SCREEN_WIDTH - UNIT_SIZE && !direction.equals("Left")) playerMovementRight();
        });

        FXGL.onKey(KeyCode.A, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getX() > 0 && !direction.equals("Right")) playerMovementLeft();
        });
    }

    @Override
    protected void onUpdate(double tpf) { // tpf is approx 0.0167, frame limit = 60
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

    public void initBackground() {
        FXGL.entityBuilder()
                .view("background.png")
                .buildAndAttach();
    }

    private void moveOneStep() {
        // Save the previous head position, so I can use it later to move the body segments
        double prevX = snakePlayer.snakeHeadX();
        double prevY = snakePlayer.snakeHeadY();

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
        double headX = snakePlayer.snakeHeadX();
        double headY = snakePlayer.snakeHeadY();
        if (headX < 0 || headX >= SCREEN_WIDTH || headY < 0 || headY >= SCREEN_HEIGHT) {
            gameOver();
            return;
        }

        // Check if head hits some body part
        for (int i = 1; i < bodyParts; i++) {
            if (headX == snakePlayer.getSnakeUnits().get(i).getX() && headY == snakePlayer.getSnakeUnits().get(i).getY()) {
                gameOver();
                return;
            }
        }

        checkItemCollision(appleItem, snakePlayer);
    }

    public void checkItemCollision(CollectibleItems item, Snake snake) {
        double itemX = item.itemX();
        double itemY = item.itemY();

        double snakeX = snake.snakeHeadX();
        double snakeY = snake.snakeHeadY();

        if (itemX == snakeX && itemY == snakeY) {
            item.removeItem();
            item.createApple(SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE, EntityType.ITEM, "jewel.png");

            FXGL.inc("applesEatenFXGL", +1);

            snake.growSnake(bodyParts);
            bodyParts++;

            gameSpeed = Math.max(gameSpeed - 0.01, 0.02);
        }
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("applesEatenFXGL", applesEaten);
    }

    @Override
    protected void initUI() {
        Text scoreLabel = new Text();
        scoreLabel.setScaleX(2);
        scoreLabel.setScaleY(2);
        scoreLabel.setFill(Color.WHITE);
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
        gameOverText.setFill(Color.WHITE);
        gameOverText.setTranslateX((SCREEN_WIDTH - gameOverText.getLayoutBounds().getWidth()) / 2);
        gameOverText.setTranslateY((double) SCREEN_HEIGHT / 2);
        FXGL.getGameScene().addUINode(gameOverText);
    }
}
