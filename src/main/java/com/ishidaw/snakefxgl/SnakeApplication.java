package com.ishidaw.snakefxgl;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.ishidaw.snakefxgl.Entities.CollectibleItems;
import com.ishidaw.snakefxgl.Entities.Snake;
import com.ishidaw.snakefxgl.Utils.Hud;
import com.ishidaw.snakefxgl.Utils.Play;
import javafx.animation.FadeTransition;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.ishidaw.snakefxgl.Enums.EntityType;
import javafx.util.Duration;

public class SnakeApplication extends GameApplication {

    Snake snakePlayer = new Snake();
    CollectibleItems appleItem = new CollectibleItems();
    Hud hud = new Hud();
    Play play = new Play();

    // bunch of constants
    static final int SCREEN_WIDTH = 768;
    static final int SCREEN_HEIGHT = 768;
    static final int DEFAULT_BODY_PARTS = 4;
    static final int UNIT_SIZE = 64; // Cell size
    static final int DEFAULT_SCORE = 0;
    static final double DEFAULT_TIMER = 0;
    static final double DEFAULT_SPEED = 0.12;
    static final int DEFAULT_COUNTDOWN = 3;

    int bodyParts = DEFAULT_BODY_PARTS;
    int score = DEFAULT_SCORE;
    int highScore = DEFAULT_SCORE;

    // Keeps snake under controlled speed
    private double moveTimer = DEFAULT_TIMER; // Just iterate elapsed time
    private double gameSpeed = DEFAULT_SPEED; // Seconds between snakes moves

    private final Deque<String> inputQueue = new ArrayDeque<>(); // A FIFO array of inputs, like in dark souls that you press O and X, so you get rolling and then drinking an estus

    boolean running = true;
    boolean isGameOver = false;
    boolean isGamePaused = false;

    boolean isCountingDown = true;
    int countdown = DEFAULT_COUNTDOWN;

    String direction = "Down"; // Start direction

    // Workaround to make count timer refresh on screen
    Text countHUD = hud.countdownHUD(SCREEN_WIDTH, SCREEN_HEIGHT);

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(SCREEN_WIDTH);
        settings.setHeight(SCREEN_HEIGHT);
        settings.setVersion("");
        settings.setTitle("Snake");
        settings.setTicksPerSecond(60);
        settings.setGameMenuEnabled(false);
        settings.setMainMenuEnabled(false);
        settings.setFullScreenAllowed(true);
        settings.setAppIcon("snake_head.png");
    }

    @Override
    protected void initGame() {
        FXGL.getGameScene().getRoot().setCursor(javafx.scene.Cursor.NONE);
        hud.initBackground();

        // Pauses the game to make countdown make sense... prob there is a function to do it, but I cant see on wiki
        running = false;
        setCountingDown();

        snakePlayer.createSnake(bodyParts, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);
        appleItem.createApple(EntityType.ITEM, "apple.png", snakePlayer);
        play.playBGM("soundtrack.wav");

        hud.mainHUD();
        hud.scoreLabel();
        hud.highScoreLabel();

        // Built the countdown the first time
        // When clicked R, the restart functions handles it
        hud.buildCustomHUD(countHUD);
    }

    private void enqueueDirection(String enqueueDir) {
        if (!running) return;
        
        // Easy FIFO DataStructure
        // Peaks lastInput, if the lastInput has a value, then "first out" it
        String lastInput = inputQueue.peekLast();
        String runInput = (lastInput != null) ? lastInput : this.direction;
        
        if (isOpposite(runInput, enqueueDir)) return;

        // inputs remember
        int MAX_BUFFERED_INPUTS = 4;
        if (inputQueue.size() < MAX_BUFFERED_INPUTS) {
            inputQueue.addLast(enqueueDir);
        }
    }

    private boolean isOpposite(String currentInput, String currentDirection) {
        return ("Up".equals(currentInput) && "Down".equals(currentDirection)) ||
                ("Down".equals(currentInput) && "Up".equals(currentDirection)) ||
                ("Left".equals(currentInput) && "Right".equals(currentDirection)) ||
                ("Right".equals(currentInput) && "Left".equals(currentDirection));
    }

    private void applyBufferDirection() {
        String nextDirection = inputQueue.pollFirst();
        if (nextDirection == null) return;

        if (isOpposite(this.direction, nextDirection)) return;
        this.direction = nextDirection;

        switch (nextDirection) {
            case "Up":
                snakePlayer.setSnakeHead(90);
                snakePlayer.setSnakeBody(90);
                break;
            case "Down":
                snakePlayer.setSnakeHead(270);
                snakePlayer.setSnakeBody(270);
                break;
            case "Left":
                snakePlayer.setSnakeHead(360);
                snakePlayer.setSnakeBody(360);
                break;
            case "Right":
                snakePlayer.setSnakeHead(180);
                snakePlayer.setSnakeBody(180);
                break;
        }
    }

    @Override
    protected void initInput() {
        // The running check is to ignore inputs if it's game over
        FXGL.onKeyDown(KeyCode.W, () -> {
            if (!running) return;
            enqueueDirection("Up");
        });

        FXGL.onKeyDown(KeyCode.S, () -> {
            if (!running) return;
            enqueueDirection("Down");
        });

        FXGL.onKeyDown(KeyCode.D, () -> {
            if (!running) return;
            enqueueDirection("Right");
        });

        FXGL.onKeyDown(KeyCode.A, () -> {
            if (!running) return;
            enqueueDirection("Left");
        });
        FXGL.onKeyDown(KeyCode.R, () -> {
            if (!running && !isCountingDown && !isGamePaused) restartGame();
        });
        FXGL.onKeyDown(KeyCode.P, () -> { if (!isGameOver && !isCountingDown) pauseGame();});
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!running) return;

        moveTimer += tpf;

        int steps = 0;

        // This is the "auto movement" behind the snake, and how speedy is based on the while loop within the Move Timer and GameSpeed
        // Also make it process only the max const per frame
        // inputs to process in one frame
        int MAX_CATCHUP_STEPS = 2;
        while (moveTimer >= gameSpeed && steps < MAX_CATCHUP_STEPS) {
            moveTimer -= gameSpeed;
            steps++;

            moveOneStep();

            if (!running) break;
        }
    }

    private void moveOneStep() {
        applyBufferDirection();
        checkSnakeCollision();
        if (!running) return;

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
        if (headX < 0 || headX >= SCREEN_WIDTH || headY < 64 || headY >= SCREEN_HEIGHT) {
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

            snake.growSnake(bodyParts);
            bodyParts++;
            score++;

            gameSpeed -= 0.001;
        }
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("applesEatenFXGL", score);
        vars.put("highScoreFXGL", highScore);
    }

    private void setHighScore() {
        if (score > highScore) highScore = score;
        FXGL.set("highScoreFXGL", highScore);
    }

    // I was pretending to have the gameOver and restarGame inside a class GameState, but was resulting in a cyclic dependence
    // I'm not proud of this...
    private void gameOver() {
        running = false;
        isGameOver = true;
        inputQueue.clear();

        snakePlayer.removeSnake();

        direction = " ";

        play.stopBGM();

        hud.gameOverHUD();
        setHighScore();
    }

    private void restartGame() {
        inputQueue.clear();
        FXGL.getGameWorld().getEntitiesCopy().forEach(Entity::removeFromWorld);

        hud.buildCustomHUD(countHUD);
        isCountingDown = true;
        setCountingDown();
        isGameOver = false;
        isGamePaused = false;
        moveTimer = DEFAULT_TIMER;
        gameSpeed = DEFAULT_SPEED;

        score = DEFAULT_SCORE;
        bodyParts = DEFAULT_BODY_PARTS;
        direction = "Down";

        FXGL.set("applesEatenFXGL", DEFAULT_SCORE);

        hud.initBackground();
        snakePlayer = new Snake();
        snakePlayer.createSnake(bodyParts, SCREEN_WIDTH, SCREEN_HEIGHT, UNIT_SIZE);

        appleItem = new CollectibleItems();
        appleItem.createApple(EntityType.ITEM, "apple.png", snakePlayer);

        play.playBGM("soundtrack.wav");

        hud.mainHUD();
        hud.scoreLabel();
        hud.highScoreLabel();
    }

    private void pauseGame() {
        running = running ? false : true;
        isGamePaused = isGamePaused ? false : true;
    }

    private  void setCountingDown() {
        countHUD.setText(String.valueOf(countdown));

        FXGL.getGameTimer().runOnceAfter(() -> countHUD.setText("2"), Duration.seconds(1));

        FXGL.getGameTimer().runOnceAfter(() -> countHUD.setText("1"), Duration.seconds(2));

        FXGL.getGameTimer().runOnceAfter(() -> {
            countHUD.setText("0");

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.25), countHUD);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                hud.removeCustomHUD(countHUD);
                running = true;
                isCountingDown = false;

                countHUD.setOpacity(1.0); // Need to re-set the opacity, cuz it holds the state, if you don't, the restar countdown will not work
            });

            fadeOut.play();

        }, Duration.seconds(3));
    }
}
