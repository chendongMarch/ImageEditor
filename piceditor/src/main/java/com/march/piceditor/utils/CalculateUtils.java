package com.march.piceditor.utils;

import android.view.MotionEvent;

import com.march.piceditor.common.model.Point;

/**
 * CreateAt : 7/20/17
 * Describe :
 *
 * @author chendong
 */
public class CalculateUtils {

    public static float calculateFingersDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static float calculateFingersDistance(MotionEvent event) {
        return calculateFingersDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }


    public static float calculateRotation(float x1, float y1, float x2, float y2) {
        double radians = Math.atan2(y2 - y1, x2 - x1);
        return (float) Math.toDegrees(radians);
    }

    public static float calculateRotation(MotionEvent event) {
        return calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
    }


//    先寫函數 W(p,q)= W( (px,py), (qx,qy) )= px*qy-py*qx
//    之後: (C點不必用)
//    v=(vx, vy)=(Px-Ax, Py-Ay);
//    a=(ax, ay)=(Bx-Ax, By-Ay);
//    b=(bx, by)=(Dx-Ax, Dy-Ay);
//    h=W(v,b)/W(a,b);
//    k=W(a,v)/W(a,b);
//    在內部 <==> ( 0<h && h<1 && 0<k && k<1 )

    private static float W(Point p, Point q) {
        return p.x * q.y - p.y * q.x;
    }

    public static boolean isRectContainsPoint(Point A, Point B, Point C, Point D, Point P) {
        Point v = new Point(P.x - A.x, P.y - A.y);
        Point a = new Point(B.x - A.x, B.y - A.y);
        Point b = new Point(D.x - A.x, D.y - A.y);
        float h = W(v, b) / W(a, b);
        float k = W(a, v) / W(a, b);
        return 0 < h && h < 1 && 0 < k && k < 1;

    }

    public static void swapPoint(Point p1, Point p2) {
        float x = p1.x;
        float y = p1.y;
        p1.set(p2.x, p2.y);
        p2.set(x, y);
    }
}
