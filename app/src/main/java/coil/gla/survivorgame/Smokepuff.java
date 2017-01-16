package coil.gla.survivorgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by GalAmitai-PC on 23-Dec-16.
 */

public class Smokepuff extends GameObject {
    public int r;

    public Smokepuff(int x, int y) {
        r = 5;
        super.x = x + 20;
        super.y = y + 40;
    }
    public void update() {
        x -= 5;
    }
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x-r, y-r, r, paint);
        canvas.drawCircle(x-r+3, y-r-2, r, paint);
        canvas.drawCircle(x-r+4, y-r-4, r, paint);
    }

}
