package com.ishidaw.snakefxgl;

import com.almasb.fxgl.app.CursorInfo;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.ishidaw.snakefxgl.Entities.CollectibleItems;
import com.ishidaw.snakefxgl.Entities.Snake;
import com.ishidaw.snakefxgl.Utils.Hud;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.Map;

import com.ishidaw.snakefxgl.Enums.EntityType;

public class SnakeApplication extends GameApplication {

    Snake snakePlayer = new Snake();
    CollectibleItems appleItem = new CollectibleItems();
    Hud hud = new Hud();

    // assets are 32x32, so it NEEDS to be multiple of 2 (32 * 20).
    // bunch of constants
    static final int SCREEN_WIDTH = 1024;
    static final int SCREEN_HEIGHT = 1024;
    static final int DEFAULT_EATEN_APPLES = 0;
    static final int DEFAULT_BODY_PARTS = 1;
    static final int UNIT_SIZE = 32; // Cell size

    int applesEaten = DEFAULT_EATEN_APPLES;
    int bodyParts = DEFAULT_BODY_PARTS;
    int updatedScore = 0;

    // Keeps snake under controlled speed
    private double moveTimer = 0; // Just iterate elapsed time
    private double gameSpeed = 0.10; // Seconds between snakes moves

    boolean running = true;
    String direction = "Down"; // Start direction

    // Need this to concatenate Score + updatedScore
    Text mainHUD = hud.defaultHUD(SCREEN_WIDTH, 2, 2, updatedScore);

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(SCREEN_WIDTH);
        settings.setHeight(SCREEN_HEIGHT);
        settings.setVersion("");
        settings.setTitle("Snake");
        settings.setTicksPerSecond(60);
        settings.setGameMenuEnabled(false);
        settings.setMainMenuEnabled(false);
        settings.setDefaultCursor(new CursorInfo("empty_cursor.png", 0, 0)); // I don't know how to set the cursor to invisible...
        System.out.println(settings.getDefaultCursor());
    }

    @Override
    protected void initGame() {
        hud.initBackground();
        snakePlayer.createSnake(bodyParts, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);
        appleItem.createApple(SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE, EntityType.ITEM, "apple.png");
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

//         This is the "auto movement" behind the snake, and how speedy is based on the while loop within the Move Timer and GameSpeed
        while (moveTimer >= gameSpeed && steps < maxSteps) {
            moveTimer -= gameSpeed;
            moveOneStep();
            steps++;
        }
    }

    public void setUpdatedScore() {
        updatedScore = FXGL.geti("applesEatenFXGL");
        hud.removeCustomHUD(mainHUD);
        mainHUD = hud.defaultHUD(SCREEN_WIDTH, 2, 2, updatedScore);
        hud.buildCustomHUD(mainHUD);
    }


    private void moveOneStep() {
        checkSnakeCollision();
        checkItemCollision(appleItem, snakePlayer);
    }

    public void directions() {
        switch (direction) {
            case "Up": snakePlayer.getSnakeUnits().getFirst().translateY(-UNIT_SIZE); break;
            case "Down": snakePlayer.getSnakeUnits().getFirst().translateY(UNIT_SIZE); break;
            case "Left": snakePlayer.getSnakeUnits().getFirst().translateX(-UNIT_SIZE); break;
            case "Right": snakePlayer.getSnakeUnits().getFirst().translateX(UNIT_SIZE); break;
            default: break;
        }
    }

    public void checkSnakeCollision() {
        // Save the previous head position, so I can use it later to move the body segments
        double prevX = snakePlayer.snakeHeadX();
        double prevY = snakePlayer.snakeHeadY();

        directions();

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
    }

    public void checkItemCollision(CollectibleItems item, Snake snake) {
        double itemX = item.itemX();
        double itemY = item.itemY();

        double snakeX = snake.snakeHeadX();
        double snakeY = snake.snakeHeadY();

        if (itemX == snakeX && itemY == snakeY) {
            item.removeItem();
            item.createApple(SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE, EntityType.ITEM, "apple.png");

            FXGL.inc("applesEatenFXGL", +1);

            setUpdatedScore();


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
        hud.buildCustomHUD(mainHUD); // Call through this function, so I can update the score value
    }

    private void gameOver() {
        running = false;

        snakePlayer.removeSnake();

        direction = " ";

        hud.gameOverHUD(
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                "GAME OVER!",
                Color.GREEN,
                2,
                2
        );
    }
}
