package com.vicomo.game.helicopter;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.util.Random;

public class Missile extends GameObject {
    private int score;
    private int speed;
    private Random rand = new Random();
    private Animation animation = new Animation();
    private Bitmap spriteSheet;

    public Missile(Bitmap res, int x, int y, int w, int h, int s, int numFrames){
        super.x = x;
        super.y = y;
        width = w;
        height =h;
        score = s;
        speed = 7 + (int) (rand.nextDouble() * score/30);
        //cap missile speed
        if(speed > 40 ) speed = 40;
        Bitmap[] image =  new Bitmap[numFrames];
        spriteSheet = res;
        for(int i = 0; i < image.length; i++ ){
            image[i] = Bitmap.createBitmap(spriteSheet, 0, i*height, width, height );
        }
        animation.setFrames(image);
        animation.setDelay(100-speed);
    }
    @Override
    protected void update() {
        x -= speed;
        animation.update();
    }

    @Override
    protected void draw(Canvas canvas) {
        try{
            canvas.drawBitmap(animation.getImage(), x, y, null);
        }catch (Exception ignored){}
    }

    @Override
    public int getWidth(){
        //offset slightly for more realistic collision detection
        return width - 10;
    }
}
