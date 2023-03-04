package cz.cvut.fjfi.decin.mandalbrot2022;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class MandelbrotView extends View {
    RectF mc = null;
    Rect dc = null;
    Bitmap bitmap = null;
    boolean isGenerated = false;
    double progress = 0.0;
    ScaleGestureDetector sd;
    boolean drag = false;
    float startx = 0;
    float starty = 0;

    private float scaleFactor = 1.0f;

    public RectF getMc() {
        return mc;
    }

    public void setMc(RectF mc) {
        this.mc = mc;
        generateBitmap();
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    private void generateBitmap() {
        if (dc == null || mc == null) {
            return;
        }
        if(isGenerated)  {
            return;
        }
        isGenerated = true;
        MandelbrotTask task = new MandelbrotTask(this);
        task.execute(new Transformation(mc, dc));
    }

    public Rect getDc() {
        return dc;
    }

    public void setDc(Rect dc) {
        this.dc = dc;
        generateBitmap();
    }

    public MandelbrotView(Context context) {
        super(context);
        init(null, 0);
    }

    public MandelbrotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public MandelbrotView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        sd = new ScaleGestureDetector(getContext(), new ScaleListener());
        setMc(new RectF(-2, 1, 1, -1));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        setDc(new Rect(0,0, w-1, h-1 ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0, 0, new Paint());
        }
        else {
            Paint p = new Paint();
            p.setColor(Color.WHITE);
            p.setTextSize(50);
            canvas.drawText(String.format("Wait, please (%.0f%%)", progress * 100), 30, 60, p);
        }
    }

    public void setProgress(Double value) {
        progress = value;
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = sd.onTouchEvent(event);
        //TODO: podpora tažení: jen jeden prst, a dvě události DOWN a UP
        final int action = event.getActionMasked();
        if (event.getPointerCount() != 1 ) {
            drag = false;
            return result;
        }
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                drag = true;
                startx = event.getX();
                starty = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                if(drag) {
                    drag = false;
                    int dx = (int)(event.getX() - startx);
                    int dy = (int)(event.getY() - starty);

                    Transformation t = new Transformation(mc, dc);
                    Point d_delta = new Point(dx, dy);
                    PointF m_delta = t.vectorToMathematical(d_delta);
                    Log.d("drag d", d_delta.toString());
                    Log.d("drag before", mc.toString());
                    RectF newMc = new RectF(mc.left - m_delta.x, mc.top + m_delta.y,
                                             mc.right - m_delta.x, mc.bottom + m_delta.y);
                    Log.d("drag after", newMc.toString());
                    setMc(newMc);
                }
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();

            Log.d("scale", scaleFactor + "");
            float mwidth = 3 / scaleFactor;
            float mheight = 2 / scaleFactor;

            Transformation t = new Transformation(mc, dc);

            PointF center = new PointF(mc.left + mc.width() / 2, mc.top + mc.height() / 2);
            RectF newMc = new RectF(center.x - mwidth/2, center.y + mheight/2,
                    center.x + mwidth/2, center.y - mheight/2);
            setMc(newMc);
        }
    }
}