package cz.cvut.fjfi.decin.mandalbrot2022;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class Transformation {
    public RectF mc;
    public Rect dc;

    public Transformation(RectF mc, Rect dc) {
        this.mc = mc;
        this.dc = dc;
    }

    PointF toMathematical(Point dp) {
        float x = mc.left + mc.width() * (float)(dp.x - dc.left) / dc.width();
        float y = mc.bottom - mc.height() * (float)(dp.y - dc.top) / dc.height();
        return new PointF(x, y);
    }

    PointF vectorToMathematical(Point dv) {
        float x = dv.x * mc.width() / dc.width();
        float y = dv.y * mc.height() / dc.height();
        return new PointF(x, y);
    }
}
