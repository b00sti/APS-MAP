package com.example.joelwasserman.apsmap1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Created by b00sti on 03.04.2018
 */

public class MapView extends android.support.v7.widget.AppCompatImageView {

    private ArrayList<Beacon> beacons = new ArrayList<>();
    int lat = 111;
    int lng = 111;
    int oldLat = 111;
    int oldLng = 111;
    int tmpLat = 111;
    int tmpLng = 111;

    private Context mContext;
    int x = -1;
    int y = -1;
    private int xVelocity = 10;
    private int yVelocity = 5;

    private Handler h;

    private final int FRAME_RATE = 30;

    public void setLngLng(int newlat, int newlng) {
        tmpLat = lat;
        tmpLng = lng;
        oldLat = lat;
        oldLng = lng;
        this.lat = newlat;
        this.lng = newlng;
    }

    public MapView(Context context) {
        super(context);
        setBackgroundResource(R.mipmap.elmap);
        mContext = context;
        h = new Handler();
    }

    public MapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundResource(R.mipmap.elmap);
        mContext = context;
        h = new Handler();
    }

    public MapView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundResource(R.mipmap.elmap);
        mContext = context;
        h = new Handler();
    }

    private Runnable r = new Runnable() {

        @Override

        public void run() {

            invalidate();

        }

    };

    protected void onDraw(Canvas c) {

        Paint drawPaint = new Paint();
        drawPaint.setColor(Color.GREEN);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        for (Beacon beacon : beacons) {
            c.drawCircle(beacon.getLatOnMap(), beacon.getLngOnMap(), 10, drawPaint);
        }
        drawPaint.setColor(Color.RED);
        //c.drawCircle(lat, lng, 15, drawPaint);

        BitmapDrawable ball = (BitmapDrawable) mContext.getResources().getDrawable(R.mipmap.ic_launcher);

        if (x < 0 && y < 0) {

            x = this.getWidth() / 2;

            y = this.getHeight() / 2;

        } else {

            x += xVelocity;

            y += yVelocity;

            if ((x > this.getWidth() - ball.getBitmap().getWidth()) || (x < 0)) {

                xVelocity = xVelocity * -1;

            }

            if ((y > this.getHeight() - ball.getBitmap().getHeight()) || (y < 0)) {

                yVelocity = yVelocity * -1;

            }

        }

        if (lat > oldLat) {
            tmpLat += 2;
            if (tmpLat > lat) {
                tmpLat = lat;
            }
        } else {
            tmpLat -= 2;
            if (tmpLat < lat) {
                tmpLat = lat;
            }

        }

        if (lng > oldLng) {
            tmpLng += 2;
            if (tmpLng > lng) {
                tmpLng = lng;
            }
        } else {
            tmpLng -= 2;
            if (tmpLng < lng) {
                tmpLng = lng;
            }
        }

        c.drawCircle(tmpLat, tmpLng, 15, drawPaint);

        c.drawBitmap(ball.getBitmap(), x, y, null);

        h.postDelayed(r, FRAME_RATE);

    }

    public void setBeacons(ArrayList<Beacon> beacons) {
        this.beacons = beacons;
    }
}
