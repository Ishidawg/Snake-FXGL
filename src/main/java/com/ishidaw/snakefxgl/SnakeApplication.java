package com.ishidaw.snakefxgl;

import com.almasb.fxgl.app.CursorInfo;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.TimerAction;
import com.ishidaw.snakefxgl.Entities.CollectibleItems;
import com.ishidaw.snakefxgl.Entities.Snake;
import com.ishidaw.snakefxgl.Utils.Hud;
import com.ishidaw.snakefxgl.Utils.Play;
import javafx.animation.FadeTransition;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.util.Map;

import com.ishidaw.snakefxgl.Enums.EntityType;
import javafx.util.Duration;

public class SnakeApplication extends GameApplication {

    Snake snakePlayer = new Snake();
    CollectibleItems appleItem = new CollectibleItems();
    Hud hud = new Hud();
    Play play = new Play();

    // bunch of constants
    static final int SCREEN_WIDTH = 1024;
    static final int SCREEN_HEIGHT = 1024;
    static final int DEFAULT_EATEN_APPLES = 0;
    static final int DEFAULT_BODY_PARTS = 4;
    static final int UNIT_SIZE = 64; // Cell size
    static final int DEFAULT_SCORE = 0;
    static final double DEFAULT_TIMER = 0;
    static final double DEFAULT_SPEED = 0.12;
    static final int DEFAULT_COUNTDOWN = 3;

    int applesEaten = DEFAULT_EATEN_APPLES;
    int bodyParts = DEFAULT_BODY_PARTS;
    int updatedScore = DEFAULT_SCORE;

    // Keeps snake under controlled speed
    private double moveTimer = DEFAULT_TIMER; // Just iterate elapsed time
    private double gameSpeed = DEFAULT_SPEED; // Seconds between snakes moves

    boolean running = true;
    boolean isGameOver = false;

    boolean isCountingDown = true;
    int countdown = DEFAULT_COUNTDOWN;

    String direction = "Down"; // Start direction

    // Need this right bellow to concatenate Score + updatedScore
    Text mainHUD = hud.defaultHUD(SCREEN_WIDTH, updatedScore);

    // Workaround to make count timer refresh on screen
    Text countHUD = hud.countdownHUD(countdown, SCREEN_WIDTH, SCREEN_HEIGHT);

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
    }

    @Override
    protected void initGame() {
        hud.initBackground();

        // Pauses the game to make countdown make sense... prob there is a function to do it, but I cant see on wiki
        running = false;
        setCountingDown();

        snakePlayer.createSnake(bodyParts, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);
        appleItem.createApple(EntityType.ITEM, "apple.png", snakePlayer);
        play.playBGM("soundtrack.wav");

        // Built the countdown the first time
        // When clicked R, the restart functions handles it
        hud.buildCustomHUD(countHUD);
    }

    // default angle 180
    public void playerMovementUp() {
        direction = "Up";
        snakePlayer.setSnakeHead(90);
        snakePlayer.setSnakeBody(90);
    }

    public void playerMovementDown() {
        direction = "Down";
        snakePlayer.setSnakeHead(270);
        snakePlayer.setSnakeBody(270);
    }

    public void playerMovementRight() {
        direction = "Right";
        snakePlayer.setSnakeHead(180);
        snakePlayer.setSnakeBody(180);
    }

    public void playerMovementLeft() {
        direction = "Left";
        snakePlayer.setSnakeHead(360);
        snakePlayer.setSnakeBody(360);
    }

    @Override
    protected void initInput() {
        // The running check is to ignore inputs if it's game over
        FXGL.onKeyDown(KeyCode.W, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getY() > 0 && !direction.equals("Down")) playerMovementUp();
        });

        FXGL.onKeyDown(KeyCode.S, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getY() < SCREEN_HEIGHT - UNIT_SIZE && !direction.equals("Up")) playerMovementDown();
        });

        FXGL.onKeyDown(KeyCode.D, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getX() < SCREEN_WIDTH - UNIT_SIZE && !direction.equals("Left")) playerMovementRight();
        });

        FXGL.onKeyDown(KeyCode.A, () -> {
            if (!running) return;
            if(snakePlayer.getSnakeUnits().getFirst().getPosition().getX() > 0 && !direction.equals("Right")) playerMovementLeft();
        });
        FXGL.onKeyDown(KeyCode.R, () -> {
            if (!running && !isCountingDown) restartGame();
        });
        FXGL.onKeyDown(KeyCode.P, () -> { if (!isGameOver) pauseGame();});
    }

    @Override
    protected void onUpdate(double tpf) { // tpf is approx 0.0167, frame limit = 60
        if (!running) return;
        moveTimer += tpf;

        int maxSteps = 10;
        int steps = 0;

        // This is the "auto movement" behind the snake, and how speedy is based on the while loop within the Move Timer and GameSpeed
        while (moveTimer >= gameSpeed && steps < maxSteps) {
            moveTimer -= gameSpeed;
            moveOneStep();
            steps++;
        }
    }

    public void setUpdatedScore() {
        updatedScore = FXGL.geti("applesEatenFXGL");
        hud.removeCustomHUD(mainHUD);
        mainHUD = hud.defaultHUD(SCREEN_WIDTH, updatedScore);
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
            FXGL.play("hit.wav");
            return;
        }

        // Check if head hits some body part
        for (int i = 1; i < bodyParts; i++) {
            if (headX == snakePlayer.getSnakeUnits().get(i).getX() && headY == snakePlayer.getSnakeUnits().get(i).getY()) {
                gameOver();
                FXGL.play("hit.wav");
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
            item.createApple(EntityType.ITEM, "apple.png", snakePlayer);

            FXGL.inc("applesEatenFXGL", +1);

            setUpdatedScore();

            snake.growSnake(bodyParts);
            bodyParts++;

            gameSpeed -= 0.002;
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

    // I was pretending to have the gameOver and restarGame inside a class GameState, but was resulting in a cyclic dependence
    // I'm not proud of this...
    private void gameOver() {
        running = false;
        isGameOver = true;

        snakePlayer.removeSnake();

        direction = " ";

        play.stopBGM();

        hud.gameOverHUD();
    }

    private void restartGame() {
        FXGL.getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

        hud.buildCustomHUD(countHUD);
        setCountingDown();
        isGameOver = false;
        moveTimer = DEFAULT_TIMER;
        gameSpeed = DEFAULT_SPEED;

        updatedScore = DEFAULT_SCORE;
        applesEaten = DEFAULT_EATEN_APPLES;
        bodyParts = DEFAULT_BODY_PARTS;
        direction = "Down";

        FXGL.set("applesEatenFXGL", DEFAULT_SCORE);

        hud.initBackground();
        snakePlayer = new Snake();
        snakePlayer.createSnake(bodyParts, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);

        appleItem = new CollectibleItems();
        appleItem.createApple(EntityType.ITEM, "apple.png", snakePlayer);

        play.playBGM("soundtrack.wav");

        hud.removeCustomHUD(mainHUD);
        mainHUD = hud.defaultHUD(SCREEN_WIDTH, updatedScore);
        hud.buildCustomHUD(mainHUD);
    }

    private void pauseGame() {
        running = running ? false : true;
    }

    private  void setCountingDown() {
        countHUD.setText(String.valueOf(countdown));

        FXGL.getGameTimer().runOnceAfter(() -> {
            countHUD.setText("2");
        }, Duration.seconds(1));

        FXGL.getGameTimer().runOnceAfter(() -> {
            countHUD.setText("1");
        }, Duration.seconds(2));

        FXGL.getGameTimer().runOnceAfter(() -> {
            countHUD.setText("0");

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.25), countHUD);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                hud.removeCustomHUD(countHUD);
                running = true;
                isCountingDown = false;

                countHUD.setOpacity(1.0); // Need to re-set the opacity, cuz it holds the state, if dont, the restar cuntdown will not work
            });

            fadeOut.play();

        }, Duration.seconds(3));
    }
}
