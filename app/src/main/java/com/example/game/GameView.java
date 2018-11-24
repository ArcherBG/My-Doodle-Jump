package com.example.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    private final static String TAG = GameView.class.getSimpleName();

    private Context context;
    private SurfaceHolder surfaceHolder;
    private Paint paint;
    private Thread gameThread;
    private boolean running;
    private int viewWidth;
    private int viewHeight;
    private Player player;
    private Bitmap playerBitmap;
    private Bitmap moveLeftBitmap;
    private Bitmap moveRightBitmap;
    private boolean movePlayerRight;
    private boolean movePlayerLeft;

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
    }

    @Override
    public void run() {
        Canvas canvas;
        while (running) {
            if (surfaceHolder.getSurface().isValid()) {
                canvas = surfaceHolder.lockCanvas();
                canvas.save();
                canvas.drawColor(Color.BLUE);
                if (movePlayerRight) {
                    player.update(player.getX() + 10, player.getY());
                    canvas.drawBitmap(playerBitmap, player.getX(), player.getY(), paint);
                } else if (movePlayerLeft) {
                    player.update(player.getX() - 10, player.getY());
                }
                canvas.drawBitmap(playerBitmap, player.getX(), player.getY(), paint);
                canvas.drawBitmap(moveLeftBitmap, 200, 1450, paint);
                canvas.drawBitmap(moveRightBitmap, 950, 1450, paint);

                canvas.restore();
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        player = new Player(viewWidth / 2, viewHeight - 300, 50);
        playerBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_mylocation);
        moveLeftBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_input_add);
        moveRightBitmap = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_input_add);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        Log.d(TAG, "X: " + x + " Y: " + y);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // If right button is clicked raise flag
                if (x > 900 && x < 1000
                        && y > 1300 && y < 1500) {
                    movePlayerRight = true;
                }
                // If left button is clicked raise flag
                if (x > 100 && x < 300
                        && y > 1300 && y < 1500) {
                    movePlayerLeft = true;
                }
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

