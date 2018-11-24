package com.example.game;

public class Player {
    private int x;
    private int y;
    private int radius;

    public Player(int viewWidth, int viewHeight, int radius) {
        this.x = viewWidth;
        this.y = viewHeight;
        this.radius = radius;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void update(int x, int y) {
        this.x = (this.x > 0) ? x : 0;
        this.y = (this.y > 0) ? y : 0;
    }

}
