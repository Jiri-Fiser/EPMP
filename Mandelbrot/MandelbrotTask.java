package cz.cvut.fjfi.decin.mandalbrot2022;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;

public class MandelbrotTask extends AsyncTask<Transformation, Double, Bitmap> {
    MandelbrotView view;

    public MandelbrotTask(MandelbrotView view) {
        this.view = view;
    }

    @Override
    protected Bitmap doInBackground(Transformation... transformations) {
        return createMandelbrot(transformations[0]);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        view.bitmap = bitmap;
        view.invalidate();
        view.isGenerated = false;
    }

    private Bitmap createMandelbrot(Transformation transformation) {
        Rect dc = transformation.dc;

        int max_iteration = 256;
        int palette[] = new int[max_iteration+1];
        for(int i=0; i<=max_iteration; i++) {
            palette[i] = Color.rgb(i%256, (i*3)%256, (i*7)%256);
        }

        Bitmap b = Bitmap.createBitmap(dc.width(), dc.height(), Bitmap.Config.RGB_565);

        for(int dx=dc.left; dx < dc.right; dx++) {
            if (dx % 50 == 0) {
                publishProgress((double) (dx - dc.left) / dc.width());
            }
            for (int dy = dc.top; dy < dc.bottom; dy++) {
                PointF p = transformation.toMathematical(new Point(dx, dy));
                float x0 = p.x;
                float y0 = p.y;

                float x = 0;
                float y = 0;
                int iteration = 0;

                while (x * x + y * y <= 2 * 2 && iteration < max_iteration) {
                    float xtemp = x * x - y * y + x0;
                    y = 2 * x * y + y0;
                    x = xtemp;
                    iteration++;
                }

                int color = palette[iteration];
                b.setPixel(dx, dy, color);
            }
        }

        return b;
    }


    @Override
    protected void onProgressUpdate(Double... values) {
        view.setProgress(values[0]);
    }
}
