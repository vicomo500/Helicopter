package com.vicomo.game.helicopter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class BottomBorder extends GameObject{
    private Bitmap image;

    public BottomBorder(Bitmap res, int x, int y) {
        height = 200;
        width = 20;

        this.x = x;
        this.y = y;
        dx = GamePanel.MOVE_SPEED;

        image = Bitmap.createBitmap(res, 0, 0, width, height);
    }

    @Override
    protected void update() {
        x += dx;
    }

    @Override
    protected void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null );
    }
}
