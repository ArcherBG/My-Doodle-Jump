package com.example.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.Queue;

public class GameView extends SurfaceView implements Runnable {
    private final static String TAG = GameView.class.getSimpleName();
    private static final int CREATE_OBSTACLE_EVERY = 2000; // in ms
    private static final int PLAYER_MOVE_MY = 15; // in pixels
    private int obstacleMoveBy = 10; // in pixels

    private Context context;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Thread gameThread;
    private boolean running;
    private int viewWidth;
    private int viewHeight;
    private int buttonOffset;
    private Player player;
    private Bitmap backgroundBitmap;
    private Bitmap playerBitmap;
    private Bitmap moveLeftBitmap;
    private Bitmap moveRightBitmap;
    private Queue<Rect> obstacles;
    private boolean movePlayerRight;
    private boolean movePlayerLeft;
    private int buttonY;
    private int leftButtonX;
    private int rightButtonX;
    private long prevTimeSpawn;
    private long prevTimeMoved;
    private Bitmap restartBitmap;
    private int restartX;
    private int restartY;

    public GameView(Context context) {
        super(context);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        surfaceHolder = getHolder();
        paint = new Paint();
        paint.setColor(Color.DKGRAY);
        obstacles = new LinkedList<Rect>();
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long prevTimeDifficultyIncreased = startTime;
        Canvas canvas;
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();
                canvas.save();
                canvas.drawBitmap(backgroundBitmap, 0, 0, paint);

                // Increase difficulty level every second
                if ((System.currentTimeMillis() - prevTimeDifficultyIncreased) > 1000) {
                    obstacleMoveBy += 1;
                    prevTimeDifficultyIncreased = System.currentTimeMillis();
                }

                createObstacles();
                moveObstacles();
                // Draw obstacles
                for (Rect obstacle : obstacles) {
                    paint.setColor(Color.DKGRAY);
                    canvas.drawRect(obstacle, paint);
                }
                removeObstaclesOutOfScreen();

                Paint textPaint = new Paint();
                textPaint.setColor(getResources().getColor(R.color.colorPrimary));
                textPaint.setTextSize(55f);
                // Calculate a score based on how long the player is alive
                String score = "Score: " + ((System.currentTimeMillis() - startTime) / 10);
                canvas.drawText(score, viewWidth - (viewWidth / 3), viewHeight / 10, textPaint);

                if (movePlayerRight) {
                    int newX;
                    if ((player.getX() + player.getViewWidth() + PLAYER_MOVE_MY) > viewWidth) {
                        newX = viewWidth - player.getViewWidth();
                    } else {
                        newX = player.getX() + PLAYER_MOVE_MY;
                    }
                    player.update(newX, player.getY());
                } else if (movePlayerLeft) {
                    int newX = ((player.getX() - PLAYER_MOVE_MY) > 0) ? player.getX() - PLAYER_MOVE_MY : 0;
                    player.update(newX, player.getY());
                }
                canvas.drawBitmap(playerBitmap, player.getX(), player.getY(), paint);

                // Set buttons on screen
                buttonY = viewHeight - (moveLeftBitmap.getHeight() + buttonOffset);
                leftButtonX = buttonOffset;
                rightButtonX = viewWidth - (moveRightBitmap.getWidth() + buttonOffset);
                canvas.drawBitmap(moveLeftBitmap, leftButtonX, buttonY, paint);
                canvas.drawBitmap(moveRightBitmap, rightButtonX, buttonY, paint);

                if (hasPlayerCollided()) {
                    // Write game over and stop the game
                    paint.setColor(getResources().getColor(R.color.red));
                    paint.setTextSize(100f);
                    int right = (int) ((viewWidth / 2) - (paint.measureText("Game Over") / 2));
                    canvas.drawBitmap(backgroundBitmap, 0, 0, paint);
                    canvas.drawText("Game Over", right, viewHeight / 2, paint);

                    canvas.drawBitmap(restartBitmap, restartX, restartY, paint);
                    running = false;
                }

                canvas.restore();
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void removeObstaclesOutOfScreen() {
        while (obstacles.peek() != null) {
            if (obstacles.peek().top > viewHeight) {
                obstacles.remove();
            } else {
                break;
            }
        }
    }

    private void createObstacles() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - prevTimeSpawn > CREATE_OBSTACLE_EVERY) {
            prevTimeSpawn = currentTime;

            int left = (int) (Math.random() * 1000);
            int right = (int) (left + (Math.random() * 1000)); // right > left
            right = (right - left < 300) ? right * 2 : right; // Increase rect length if it is too short
            obstacles.add(new Rect(left, 0, right, 50));
        }
    }

    private void moveObstacles() {

        // Move obstacles at 60 fps
        long currentTime = System.currentTimeMillis();
        if (System.currentTimeMillis() - prevTimeMoved > 1000 / 60) {
            prevTimeMoved = currentTime;

            for (Rect obstacle : obstacles) {
                obstacle.set(
                        obstacle.left,
                        obstacle.top + obstacleMoveBy,
                        obstacle.right,
                        obstacle.bottom + obstacleMoveBy);
            }
        }
    }

    private boolean hasPlayerCollided() {
        for (Rect obstacle : obstacles) {
            if (obstacle.intersect(player.getX(), player.getY(), player.getX() + player.getViewWidth(), player.getY() + player.getViewHeight())) {
                return true;
            }
        }
        return false;
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(viewWidth, viewHeight, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.backgound);

        moveLeftBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_icon_left);
        moveRightBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_icon_right);
        buttonOffset = (viewWidth / 15);

        // Set up player image and position
        playerBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_icon_player);
        int playerLocationX = (viewWidth + playerBitmap.getWidth()) / 2;
        int playerLocationY = viewHeight - ((buttonOffset * 2) + moveLeftBitmap.getHeight() + playerBitmap.getHeight());
        player = new Player(playerLocationX, playerLocationY, playerBitmap.getWidth(), playerBitmap.getHeight());

        restartBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_refresh);
        restartX = (viewWidth / 2) - (restartBitmap.getWidth() / 2);
        restartY = viewHeight / 2 + 150;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.d(TAG, "X: " + x + " Y: " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // If left button is clicked raise flag
                if ((x > leftButtonX) && (x < leftButtonX + moveLeftBitmap.getWidth())
                        && (y > buttonY) && (y < buttonY + moveLeftBitmap.getWidth())) {
                    Log.d(TAG, "Left Btn click");
                    movePlayerLeft = true;
                }

                // If right button is clicked raise flag
                if ((x > rightButtonX) && (x < rightButtonX + moveRightBitmap.getWidth())
                        && (y > buttonY) && (y < buttonY + moveRightBitmap.getWidth())) {
                    Log.d(TAG, "Right Btn click");
                    movePlayerRight = true;
                }

                // TODO Handle restart button click
                invalidate();
                break;
            }
            case MotionEvent.ACTION_UP: {
                movePlayerRight = false;
                movePlayerLeft = false;
                invalidate();
                break;
            }
        }
        return true;
    }

    public void pause() {
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
        }
    }

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
}

