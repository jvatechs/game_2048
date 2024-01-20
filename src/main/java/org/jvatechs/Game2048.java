package org.jvatechs;

import com.javarush.engine.cell.Color;
import com.javarush.engine.cell.Game;
import com.javarush.engine.cell.Key;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class Game2048 extends Game {
    private static final int SIDE = 4;
    private static final Color[] COLORS = Color.values();
    private static final int SIZE = Color.values().length;
    private static final Random RANDOM = new Random();
    private static final Color COLOR = COLORS[RANDOM.nextInt(SIZE)];
    private static final ArrayList<Integer> SHUFFLED_INDEXES = shuffleRandomlyColorsIndexes();
    private int[][] gameField = new int[SIDE][SIDE];
    private boolean isGameStopped = false;
    private int score;

    private static ArrayList<Integer> shuffleRandomlyColorsIndexes() {
        ArrayList<Integer> nums = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            nums.add(i);
        }
        Collections.shuffle(nums);
        return nums;
    }

    @Override
    public void initialize() {
        setScreenSize(SIDE, SIDE);
        createGame();
        drawScene();
    }

    private void createGame() {
        gameField = new int[SIDE][SIDE];
        createNewNumber();
        createNewNumber();

    }

    private void drawScene() {
        for (int i = 0; i < gameField.length; i++) {
            for (int j = 0; j < gameField[i].length; j++) {
                setCellColoredNumber(i, j, gameField[j][i]);
            }
        }
    }

    private void createNewNumber() {
        if (getMaxTileValue() == 2048) {
            win();
        }
        int x = getRandomNumber(SIDE);
        int y = getRandomNumber(SIDE);

        while (gameField[x][y] != 0) {
            x = getRandomNumber(SIDE);
            y = getRandomNumber(SIDE);
        }
        gameField[x][y] = getRandomNumber(10) == 9 ? 4 : 2;
    }

    private void setCellColoredNumber(int x, int y, int value) {
        Color currColor = getColorByValue(value);
        if (value == 0) {
            setCellValueEx(x, y, currColor, "");
        } else {
            setCellValueEx(x, y, currColor, String.valueOf(value));
        }

    }

    private Color getColorByValue(int value) {
        int[] matrixNums = {0, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};

        for (int i = 0; i < matrixNums.length; i++) {
            if (matrixNums[i] == value) {
                return COLORS[SHUFFLED_INDEXES.get(i)];
            }
        }

        return null;
    }

    private boolean compressRow(int[] row) {
        boolean moved = false;
        for (int i = 0; i < row.length; i++) {
            for (int j = i + 1; j < row.length; j++) {
                if (row[i] == 0 && row[j] != 0) {
                    int notZero = row[j];
                    row[j] = 0;
                    row[i] = notZero;
                    moved = true;
                    break;
                }
            }
        }
        return moved;
    }

    private boolean mergeRow(int[] row) {
        boolean changed = false;
        for (int i = 0; i < row.length; i++) {
            if (row[i] == 0) continue;
            if (i != row.length - 1 && row[i] == row[i + 1]) {
                row[i] = row[i] * 2;
                row[i + 1] = 0;
                changed = true;
                score += row[i];
                setScore(score);
            }
        }
        return changed;
    }

    @Override
    public void onKeyPress(Key key) {
        if (key == Key.SPACE && isGameStopped) {
            isGameStopped = false;
            createGame();
            drawScene();
            score = 0;
            setScore(score);
        }

        if (!canUserMove()) {
            gameOver();
        } else if (isGameStopped == false){
            switch (key) {
                case LEFT:
                    moveLeft();
                    drawScene();
                    break;
                case RIGHT:
                    moveRight();
                    drawScene();
                    break;
                case UP:
                    moveUp();
                    drawScene();
                    break;
                case DOWN:
                    moveDown();
                    drawScene();
                    break;
                default:
                    break;
            }
        }

    }

    private void moveLeft() {
        boolean result = false;
        for (int[] row : gameField) {
            boolean compressed1 = compressRow(row);
            boolean merged = mergeRow(row);
            boolean compressed2 = compressRow(row);
            if (compressed1 || merged || compressed2) {
                result = true;
            }
        }

        if (result) {
            createNewNumber();
        }

    }

    private void moveRight() {
        rotateClockwise();
        rotateClockwise();
        moveLeft();
        rotateClockwise();
        rotateClockwise();
    }

    private void moveUp() {
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
        moveLeft();
        rotateClockwise();
    }

    private void moveDown() {
        rotateClockwise();
        moveLeft();
        rotateClockwise();
        rotateClockwise();
        rotateClockwise();
    }

    private void rotateClockwise() {
        int[][] newBoard = new int[SIDE][SIDE];
        for (int i = 0; i < gameField.length; i++) {
            for (int j = 0; j < gameField[i].length; j++) {
                newBoard[j][SIDE - 1 - i] = gameField[i][j];
            }
        }
        gameField = newBoard;
    }

    private int getMaxTileValue() {
        return Arrays.stream(gameField).
                flatMapToInt(array -> Arrays.stream(array)).
                max().orElse(Integer.MIN_VALUE);
    }

    private void win() {
        isGameStopped = true;
        showMessageDialog(getColorByValue(2048), "CONGRATULATIONS!", Color.ORANGERED, 14);
    }

    private boolean canUserMove() {
        boolean haveZero = Arrays.stream(gameField).
                flatMapToInt(array -> Arrays.stream(array)).
                anyMatch(Integer -> Integer == 0);

        if (haveZero) return true;

        for (int i = 0; i < gameField.length; i++) {

            for (int j = 0; j < gameField[i].length; j++) {
                if (gameField[i][j] == 0) continue;
                if (j != gameField[i].length - 1 && gameField[i][j] == gameField[i][j + 1]) {
                    return true;
                }

                if(i != gameField.length - 1 && gameField[i][j] == gameField[i + 1][j]) {
                    return true;
                }
            }
        }

        return false;
    }

    private void gameOver() {
        isGameStopped = true;
        showMessageDialog(Color.BLACK, "GAME OVER", Color.INDIANRED, 14);
    }
}
