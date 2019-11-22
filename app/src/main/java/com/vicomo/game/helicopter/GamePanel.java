package com.vicomo.game.helicopter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
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
    private ArrayList<TopBorder> topBorder;
    private ArrayList<BottomBorder> bottomBorder;
    private Explosion explosion;
    private int maxBorderHeight, minBorderHeight, progressDenom = 20 /*increase to slow down difficulty progression, decrease to speed up difficulty progression*/ ;
    private boolean topDown = true, bottomDown = true, newGameCreated, reset, dissappear, started;
    private long smokeStartTime, missileStartTime, startReset;
    private Random rand = new Random();
    private int best;

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        bg = new Background(BitmapFactory.decodeResource( getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource( getResources(), R.drawable.helicopter),65, 25, 3 );
        smoke = new ArrayList<>();
        missiles = new ArrayList<>();
        topBorder = new ArrayList<>();
        bottomBorder = new ArrayList<>();
        smokeStartTime = missileStartTime = System.nanoTime();
        mainThread = new MainThread(getHolder(), this);
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
                mainThread = null;
            }catch (InterruptedException e){e.printStackTrace();}
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(!player.isPlaying() && newGameCreated && reset){
                player.setPlaying(true);
                player.setUp(true);
            }
//            else{
//                player.setUp(true);
//            }
            if(player.isPlaying()){
                if(!started)started =true;
                reset =false;
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
            if(bottomBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }
            if(topBorder.isEmpty()){
                player.setPlaying(false);
                return;
            }
            bg.update();
            player.update();
            //calculate the threshold of height the border ca n have based on the score max and min border height
            // are updates and the border switched direction either max or min is met
            maxBorderHeight = 30+player.getScore()/progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 the screen
            if(maxBorderHeight > HEIGHT/4) maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+player.getScore()/progressDenom;
            //check top border collision
            for (int i = 0; i < topBorder.size(); i++){
                if(collision(topBorder.get(i), player)){
                    player.setPlaying(false);
                }
            }
            //check bottom border collision
            for (int i = 0; i < bottomBorder.size(); i++){
                if(collision(bottomBorder.get(i), player)){
                    player.setPlaying(false);
                }
            }
            //update top border
            this.updateTopBorder();
            //update bottom border
            this.updateBottomBorder();
            //add smoke missiles on timer
            long missilesElapsed = (System.nanoTime() - missileStartTime)/1000000;
            if(missilesElapsed > (2000 - player.getScore()/4)){
                //first missiles always goes down the middle
                if(missiles.size() == 0){
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13 ));
                }else {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, (int)rand.nextDouble()*(HEIGHT - (maxBorderHeight*2)+maxBorderHeight), 45, 15, player.getScore(), 13 ));
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
        }else {
            player.resetDY();
            if(!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(),
                        player.getY()-30, 100, 100,25);
            }
            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset)/1000000;
            if(resetElapsed > 2500 &&!newGameCreated){
                newGame();
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
            if(!dissappear){
                player.draw(canvas);
            }
            for(Smokepuff puff: smoke){
                puff.draw(canvas);
            }
            for(Missile m: missiles){
                m.draw(canvas);
            }
            for(TopBorder tb: topBorder){
                tb.draw(canvas);
            }
            for(BottomBorder bb: bottomBorder){
                bb.draw(canvas);
            }
            //draw Explosion
            if(started){
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);

        }
    }

    private void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: "+(player.getScore()*3), 10, HEIGHT-10, paint);
        canvas.drawText("BEST: "+best, WIDTH -215, HEIGHT-10, paint);
        if(!player.isPlaying()&& newGameCreated&&reset){
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH /2-50, HEIGHT/2, paint1);
            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH /2-50, HEIGHT/2+20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH /2-50, HEIGHT/2+40, paint1);
        }
    }

    public void updateBottomBorder(){
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if(player.getScore()%40 == 0){
            bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    bottomBorder.get(bottomBorder.size()-1).getX()+20,
                    (int)(rand.nextDouble()*maxBorderHeight)+(HEIGHT-maxBorderHeight)));
        }
        //update bottom border
        for (int i = 0;i < bottomBorder.size(); i++ ){
            bottomBorder.get(i).update();
            //if border is moving off screen, remove it and add a corresponding new one
            if(bottomBorder.get(i).getX() < -20){
                bottomBorder.remove(i);
                //determine if border will be moving up or down
                if(bottomBorder.get(bottomBorder.size()-1).getHeight()>=maxBorderHeight){
                    bottomDown = false;
                }
                if(bottomBorder.get(bottomBorder.size()-1).getHeight()<=minBorderHeight){
                    bottomDown = true;
                }
                if(bottomDown){
                    bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            bottomBorder.get(bottomBorder.size()-1).getX()+20,
                            bottomBorder.get(bottomBorder.size()-1).getY()+1));
                }else{
                    bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            bottomBorder.get(bottomBorder.size()-1).getX()+20,
                            bottomBorder.get(bottomBorder.size()-1).getY()-1));
                }
            }
        }

    }
    public void updateTopBorder(){
        //every 50 points, insert randomly placed top blocks that break the pattern
        if(player.getScore()%50 == 0){
            topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    topBorder.get(topBorder.size()-1).getX()+20, 0, (int)((rand.nextDouble()*maxBorderHeight)+1 )));
        }
        for(int i = 0; i < topBorder.size(); i++){
            topBorder.get(i).update();
            if(topBorder.get(i).getX() < -20){
                //remove element of arraylist, replace it by adding a new one
                topBorder.remove(i);
                //calculate topdown which determines the direction the border is moving (up or down)
                if(topBorder.get(topBorder.size()-1).getHeight()>=maxBorderHeight){
                    topDown = false;
                }
                if(topBorder.get(topBorder.size()-1).getHeight()<=minBorderHeight){
                    topDown = true;
                }
                //new border added will have a larger height
                if(topDown){
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topBorder.get(topBorder.size()-1).getX()+20, 0,
                            topBorder.get(topBorder.size()-1).getHeight()+1));
                //new border added will have a smaller height
                }else{
                    topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topBorder.get(topBorder.size()-1).getX()+20, 0,
                            topBorder.get(topBorder.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void newGame(){
        dissappear = false;
        bottomBorder.clear();
        topBorder.clear();
        missiles.clear();
        smoke.clear();
        player.resetDY();
        minBorderHeight =5;
        maxBorderHeight =30;
        player.resetScore();
        player.setY(HEIGHT/2);
        if(player.getScore() > best){
            best = player.getScore();
        }
        //create the initial borders
        for (int i =0; i*20<WIDTH+40;i++){
            //1st top border created
            if(i == 0){
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,0,10));
            }else{
                topBorder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,0,topBorder.get(i-1).getHeight()+1));
            }
        }
        //initial bottom border
        for(int i = 0; i*10<WIDTH*40; i++){
            if(i == 0){
                bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,HEIGHT-minBorderHeight));
                //adding borders until the screen is filled
            }else{
                bottomBorder.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20,bottomBorder.get(i-1).getY()));
            }
        }
        newGameCreated =true;
    }
}
