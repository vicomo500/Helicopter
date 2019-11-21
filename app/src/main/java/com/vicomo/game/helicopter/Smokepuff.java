package com.vicomo.game.helicopter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Smokepuff extends GameObject{
    public int r;
    public Smokepuff(int x, int y){
        r = 5;
        super.x = x;
        super.y = y;
    }

    @Override
    public void update() {
        x -= 10;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x-r,y-r, r, paint);
        canvas.drawCircle(x-r+2,y-r-2, r, paint);
        canvas.drawCircle(x-r+4,y-r+1, r, paint);
    }

}
