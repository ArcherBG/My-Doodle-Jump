package com.example.game;

public class Player {
    private int x;
    private int y;
    private int viewWidth;
    private int viewHeight;

    public Player(int positionX, int positionY, int viewWidth, int viewHeight) {
        this.x = positionX;
        this.y = positionY;
        this.viewWidth = viewWidth;
        this.viewHeight = viewHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getViewWidth() { return viewWidth; }

    public int getViewHeight() { return viewHeight; }

    public void update(int x, int y) {
        this.x = (this.x > 0) ? x : 0;
        this.y = (this.y > 0) ? y : 0;
    }

}
