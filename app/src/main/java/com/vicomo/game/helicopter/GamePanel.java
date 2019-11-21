package com.vicomo.game.helicopter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public final static int WIDTH = 856, HEIGHT = 480, MOVE_SPEED = -5;
    private MainThread mainThread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private long smokeStartTime, missileStartTime;
    private Random rand = new Random();

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        mainThread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource( getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource( getResources(), R.drawable.helicopter),65, 25, 3 );
        smoke = new ArrayList<>();
        missiles = new ArrayList<>();
        smokeStartTime = missileStartTime = System.nanoTime();
        //safely start the game loop
        mainThread.setRunning(true);
        mainThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000){
            counter++;
            try{
                mainThread.setRunning(false);
                mainThread.join();
                retry = false;
            }catch (InterruptedException e){e.printStackTrace();}
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(!player.isPlaying()){
                player.setPlaying(true);
            }else{
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction() == MotionEvent.ACTION_UP){
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if(player.isPlaying()){
            bg.update();
            player.update();
            //add smoke missiles on timer
            long missilesElapsed = (System.nanoTime() - missileStartTime)/1000000;
            if(missilesElapsed > (2000 - player.getScore()/4)){
                //first missiles always goes down the middle
                if(missiles.size() == 0){
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13 ));
                }else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, (int)rand.nextDouble()*((HEIGHT)), 45, 15, player.getScore(), 13 ));
                }
                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop throuh every missile & check collision & remove
            for(int i = 0; i < missiles.size(); i++ ){
                //update missile
                missiles.get(i).update();
                if(collision( missiles.get(i), player)){
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }
                //remove missile if it is way off the screen
                if(missiles.get(i).getX() < -100){
                    missiles.remove(i);
                    break;
                }

            }
            //add smoke puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime)/1000000;
            if(elapsed > 120){
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }
            for(int i = 0; i<smoke.size(); i++){
                smoke.get(i).update();
                if(smoke.get(i).getX() < -10)
                    smoke.remove(i);
            }
        }
    }

    private boolean collision(GameObject a, GameObject b) {
        if(Rect.intersects(a.getRect(), b.getRect()))
            return true;
        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final float scaleFactorX = getWidth()/(WIDTH*1.f);
        final float scaleFactorY = getHeight()/(HEIGHT*1.f);
        if(canvas != null) {
            final int savedState =  canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);
            for(Smokepuff puff: smoke){
                puff.draw(canvas);
            }
            for(Missile m: missiles){
                m.draw(canvas);
            }
            canvas.restoreToCount(savedState);
        }
    }
}
