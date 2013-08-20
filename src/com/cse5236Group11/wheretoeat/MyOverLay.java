package com.cse5236Group11.wheretoeat;

import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MyOverLay extends Overlay {

    private final int _pathColor;
    private final List<GeoPoint> _points;
    private boolean _drawStartEnd;

    public MyOverLay(List<GeoPoint> points) {
        this(points, Color.RED, true);
    }

    public MyOverLay(List<GeoPoint> points, int pathColor, boolean drawStartEnd) {
        this._points = points;
        this._pathColor = pathColor;
        this._drawStartEnd = drawStartEnd;
    }

    private void drawOval(Canvas canvas, Paint paint, Point point) {
        Paint ovalPaint = new Paint(paint);
        ovalPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        ovalPaint.setStrokeWidth(4);
        int _radius = 6;
        RectF oval = new RectF(point.x - _radius, point.y - _radius, point.x
                + _radius, point.y + _radius);
        canvas.drawOval(oval, ovalPaint);
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
            long when) {
        Projection projection = mapView.getProjection();
        if (shadow == false && this._points != null) {
            Point startPoint = null, endPoint = null;
            Path path = new Path();
            // We are creating the path
            for (int i = 0; i < this._points.size(); i++) {
                GeoPoint gPointA = this._points.get(i);
                Point pointA = new Point();
                projection.toPixels(gPointA, pointA);
                if (i == 0) { // This is the start point
                    startPoint = pointA;
                    path.moveTo(pointA.x, pointA.y);
                } else {
                    if (i == this._points.size() - 1) {
                        endPoint = pointA;
                    }
                    path.lineTo(pointA.x, pointA.y);
                }
            }

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(this._pathColor);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(8);
            paint.setAlpha(90);
            if (this.getDrawStartEnd()) {
                if (startPoint != null) {
                    this.drawOval(canvas, paint, startPoint);
                }
                if (endPoint != null) {
                    this.drawOval(canvas, paint, endPoint);
                }
            }
            if (!path.isEmpty()) {
                canvas.drawPath(path, paint);
            }
        }
        return super.draw(canvas, mapView, shadow, when);
    }

    public boolean getDrawStartEnd() {
        return this._drawStartEnd;
    }

    public void setDrawStartEnd(boolean markStartEnd) {
        this._drawStartEnd = markStartEnd;
    }
}