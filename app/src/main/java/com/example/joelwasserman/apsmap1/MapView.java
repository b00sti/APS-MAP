package com.example.joelwasserman.apsmap1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Created by b00sti on 03.04.2018
 */

public class MapView extends android.support.v7.widget.AppCompatImageView {

    private final int FRAME_RATE = 30;
    private ArrayList<Beacon> beacons = new ArrayList<>();
    private int lat = -100;
    private int lng = -100;
    private int oldLat = -100;
    private int oldLng = -100;
    private int tmpLat = -100;
    private int tmpLng = -100;
    private int velocity = 2;
    private Context mContext;
    private Handler h;
    private Runnable r = new Runnable() {

        @Override
        public void run() {
            invalidate();
        }

    };

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

    public void setLngLng(int newlat, int newlng) {
        tmpLat = lat;
        tmpLng = lng;
        oldLat = lat;
        oldLng = lng;
        this.lat = newlat;
        this.lng = newlng;
    }

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

        //BitmapDrawable ball = (BitmapDrawable) mContext.getResources().getDrawable(R.mipmap.ic_launcher);

        if (lat > oldLat) {
            tmpLat += velocity;
            if (tmpLat > lat) {
                tmpLat = lat;
            }
        } else {
            tmpLat -= velocity;
            if (tmpLat < lat) {
                tmpLat = lat;
            }
        }

        if (lng > oldLng) {
            tmpLng += velocity;
            if (tmpLng > lng) {
                tmpLng = lng;
            }
        } else {
            tmpLng -= velocity;
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
