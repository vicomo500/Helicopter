package com.vicomo.game.helicopter;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread {
    private int FPS =30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    private static Canvas canvas;

    public MainThread(SurfaceHolder holder, GamePanel gamePanel){
        super();
        this.surfaceHolder = holder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run(){
        //Cap the FPS at 30
        long startTime;
        long timeMillis;
        long totalTime = 0;
        long waitTime = 0;
        int frameCount = 0;
        long targetTime = 1000/FPS;//how many millis each game loop riuns
        while (running){
            startTime = System.nanoTime();
            canvas = null;
            //try locking the canvas for pixel editing
            try{
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            }catch (Exception ignored){}
            finally {
                if(canvas != null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            //hw many sec it took to draw the game once
            timeMillis = (System.nanoTime() - startTime)/1000000;
            //hw long to wait before going thru the loop again
            waitTime = targetTime - timeMillis;
            //make the thread wait the wait time
            try{
                this.wait(waitTime);
            } catch (Exception ignored) {}
            totalTime = System.nanoTime()-startTime;
            frameCount++;
            if(frameCount == FPS){
                averageFPS = 1000/(totalTime/frameCount)/1000000;
                frameCount = 0;
                totalTime = 0;
                System.out.println(averageFPS);
            }
        }
    }

    public void setRunning(boolean b) {
        this.running = b;
    }
}
