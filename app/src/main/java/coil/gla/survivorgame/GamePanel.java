package coil.gla.survivorgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by GalAmitai-PC on 21-Dec-16.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    private MainThread thread;
    private Background bg;
    public static final int WIDTH = 800;
    public static final int HEIGHT = 500;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long enemyStartTime;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Enemy> enemies;
    private ArrayList<TopBorder> topBorders;
    private ArrayList<BottomBorder> bottomBorders;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean bottomDown = true;
    private boolean newGameCreated;
    private boolean firstGame = true;
    // Increase to slow down difficulty progressionm
    private int progressDenom = 20;
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best = 0;
    private SoundPlayer soundHit;
    public static final String PREFS = "sharedPrefs";

    public GamePanel(Context context) {
        super(context);
        soundHit = new SoundPlayer(context);
        // Add the callback to th SurfaceView to intercept events
        getHolder().addCallback(this);
        // Make gamePanel focusable so it can handle event
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.player), 62, 48, 2);
        smoke = new ArrayList<Smokepuff>();
        enemies = new ArrayList<Enemy>();
        topBorders = new ArrayList<TopBorder>();
        bottomBorders = new ArrayList<BottomBorder>();
        enemyStartTime = System.nanoTime();
        smokeStartTime = System.nanoTime();


        // Load Preferences
        SharedPreferences score = getContext().getSharedPreferences(PREFS,0);
        int userScore = score.getInt("bestScore",0);
        best = userScore;


        thread = new MainThread(getHolder(), this);
        // Start Game Loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if (player.getPlaying()){
                if(!started) started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {

            if(bottomBorders.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if(topBorders.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            // Calculate the threshold of height the border can have base on the score
            maxBorderHeight = 30 + player.getScore() / progressDenom;

            // cap max border height so that borders can only take up a total of 1/2 the screen
            if(maxBorderHeight > HEIGHT / 4) maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progressDenom;

            // check top border colision
            for(int i=0 ; i < topBorders.size() ; i++) {
                if(collision(topBorders.get(i),player)) {
                    player.setPlaying(false);
                }
            }

            // check bottom border collision
            for(int i=0 ; i < bottomBorders.size() ; i++) {
                if(collision(bottomBorders.get(i),player)) {
                    player.setPlaying(false);
                }
            }
            // update Top Border
            this.updateTopBorder();

            // update Bottom Border
            this.updateBottomBorder();

            // add missiles on timer
            long enemyElapsed = (System.nanoTime() - enemyStartTime) / 1000000;
            if (enemyElapsed > (2000 - player.getScore() / 4)) {
                // first airplane always goes down the middle
                if (enemies.size() == 0) {
                    enemies.add(new Enemy(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, HEIGHT / 2, 92, 19, player.getScore(), 11));
                }
                else {
                    enemies.add(new Enemy(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10,
                            (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight),
                            92, 19, player.getScore(), 11));
                }

                // reset timer
                enemyStartTime = System.nanoTime();
            }
            // loop through every missle
            for (int i = 0; i < enemies.size(); i++) {
                enemies.get(i).update();
                //update missle
                if(collision(enemies.get(i),player)) {
                    enemies.remove(i);
                    soundHit.playHitSound();
                    player.setPlaying(false);
                    break;
                }
                //if missle out of screen
                if (enemies.get(i).getX() < -100) {
                    enemies.remove(i);
                    break;
                }
            }


            // add puffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (elapsed > 120) {
                smoke.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }
            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < -10) {
                    smoke.remove(i);
                }
            }
        }
        else {
            player.resetDY();
            if(!reset){
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion),
                    player.getX()-20,player.getY()-20, 128, 128, 10);
            }
            explosion.update();

            long resetElapsed = (System.nanoTime() - startReset) / 1000000;
            if((resetElapsed > 2500 && !newGameCreated) || firstGame) {
                if(firstGame) {
                    explosion.setPlayedOnce(true);
                }
                newGame();
                firstGame = false;
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (canvas != null) {
            final float scaleFactorX =  (float)getWidth()/WIDTH;
            final float scaleFactorY =  (float)getHeight()/HEIGHT;
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if(!dissapear) {
                player.draw(canvas);
            }
            for (Smokepuff sp: smoke) {
                sp.draw(canvas);
            }
            for (Enemy m: enemies) {
                m.draw(canvas);
            }
            for (TopBorder tb: topBorders) {
                tb.draw(canvas);
            }
            for (BottomBorder bb: bottomBorders) {
                bb.draw(canvas);
            }
            if(started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    public boolean collision(GameObject a, GameObject b) {
        if(Rect.intersects(a.getRectangle(),b.getRectangle())) {
            soundHit.playHitSound();
            return true;
        }
        return false;
    }

    public void updateTopBorder() {
        // Every 50 points insert randomly placed bottom blocks that break the pattern
        if (player.getScore() % 50 == 0) {
            topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    topBorders.get(topBorders.size()-1).getX()+20, 0, (int)(rand.nextDouble()*maxBorderHeight)+1));
        }
        for (int i = 0; i < topBorders.size(); i++) {
            topBorders.get(i).update();
            if (topBorders.get(i).getX() < -20) {
                topBorders.remove(i);
                // Calc topdown which determines the direction the border is moving (up or down)
                if (topBorders.get(topBorders.size() - 1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (topBorders.get(topBorders.size() - 1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }
                if (topDown) {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topBorders.get(topBorders.size()-1).getX()+20, 0, topBorders.get(topBorders.size()-1).getHeight()+1));
                } else {
                    topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            topBorders.get(topBorders.size()-1).getX()+20, 0, topBorders.get(topBorders.size()-1).getHeight()-1));
                }
            }
        }
    }

    public void updateBottomBorder() {
        // Every 40 points insert randomly placed top blocks that break the pattern
        if (player.getScore() % 40 == 0) {
            bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    bottomBorders.get(bottomBorders.size()-1).getX()+20, (int)(rand.nextDouble()*maxBorderHeight)+HEIGHT-maxBorderHeight));
        }
        for (int i = 0; i < bottomBorders.size(); i++) {
            bottomBorders.get(i).update();
            if (bottomBorders.get(i).getX() < -20) {
                bottomBorders.remove(i);
                if (bottomDown) {
                    bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            bottomBorders.get(bottomBorders.size()-1).getX()+20, bottomBorders.get(bottomBorders.size()-1).getY()+1));
                } else {
                    bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                            bottomBorders.get(bottomBorders.size()-1).getX()+20, bottomBorders.get(bottomBorders.size()-1).getY()-1));
                }
            }
        }
        // Calc topdown which determines the direction the border is moving (up or down)
        if (bottomBorders.get(bottomBorders.size()-1).getY() <= HEIGHT-maxBorderHeight) {
            bottomDown = true;
        }
        if (bottomBorders.get(bottomBorders.size()-1).getY() >= HEIGHT-minBorderHeight) {
            bottomDown = false;
        }
    }

    public void newGame() {
        dissapear = false;

        topBorders.clear();
        bottomBorders.clear();
        enemies.clear();
        smoke.clear();
        minBorderHeight = 5;
        maxBorderHeight = 30;
        player.setY(HEIGHT/2);
        player.resetDY();
        if(player.getScore() > best) {
            best = player.getScore();
            SharedPreferences sharedPref = getContext().getSharedPreferences(PREFS,0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("bestScore", best);
            editor.commit();
        }
        player.resetScore();


        // Creater initial borders
        // Top border
        for (int i = 0; i*20 < WIDTH+40; i++) {
            if (i == 0) {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, 0, 10));
            } else {
                topBorders.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, 0, topBorders.get(i-1).getHeight()+1));
            }
        }
        // Bottom border
        for (int i = 0; i*20 < WIDTH+40; i++) {
            if (i == 0) {
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, HEIGHT-minBorderHeight));
            } else {
                bottomBorders.add(new BottomBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick), i*20, bottomBorders.get(i-1).getY()-1));
            }
        }
        newGameCreated = true;
    }
    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
        canvas.drawText("Distance: " + (player.getScore()), 10, HEIGHT - 10, paint);
        canvas.drawText("Best Score: " + best, WIDTH - 235, HEIGHT - 10, paint);

        if(!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }
    }

}
