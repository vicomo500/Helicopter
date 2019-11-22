package com.vicomo.game.helicopter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class TopBorder extends GameObject{
    private Bitmap image;

    public TopBorder(Bitmap res, int x, int y, int h ) {
        this.width = 20;
        this.height = h;
        this.x = x;
        this.y = y;
        dx = GamePanel.MOVE_SPEED;
        image = Bitmap.createBitmap(res, 0,0,width, height);
    }

    @Override
    protected void update() {
        x += dx;
    }

    @Override
    protected void draw(Canvas canvas) {
        try{
            canvas.drawBitmap(image, x, y, null);
        }catch (Exception ignored){}
    }
}
