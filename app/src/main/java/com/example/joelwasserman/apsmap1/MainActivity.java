package com.example.joelwasserman.apsmap1;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    android.bluetooth.BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;

    private AppCompatButton btnScanning;
    private AppCompatImageView ivMap;

    private boolean isScanningEnabled = false;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private long startTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    private ArrayList<Beacon> beacons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        readSDCard();
        initBle();

    }

    private void initViews() {
        ivMap = findViewById(R.id.ivMap);
        btnScanning = findViewById(R.id.StartScanButton);
        btnScanning.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (isScanningEnabled) {
                    stopScanning();
                    isScanningEnabled = false;
                    btnScanning.setText("SCAN");
                } else {
                    startScanning();
                    isScanningEnabled = true;
                    btnScanning.setText("STOP SCANNING");
                }
            }
        });
    }

    public void readSDCard() {

        ///Find the directory for the SD Card using the API
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);  //return the  Downloads folder directory in which your file will be created
        File file = new File(dir, "setup.txt");  //Creates a new file named MyFile1.txt in a folde "dir"

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            Toast.makeText(getApplicationContext(), "file.txt is OK",
                    Toast.LENGTH_LONG).show();
            int i = 0;
            //read line by line
            while ((line = br.readLine()) != null) {
                String[] arr;
                arr = line.split(",");
                beacons.add(new Beacon(arr[0], Integer.valueOf(arr[1]), Integer.valueOf(arr[2]), Integer.valueOf(arr[3])));
                i = i + 1;
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error in openning file.txt",
                    Toast.LENGTH_LONG).show();
        }

    }

    private void initBle() {
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();
        btScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }


    // Device scan callback.
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if (result.getDevice().getAddress() != null) {
                Log.d("", "onScanResult: " + result.getDevice().getAddress());

                for (Beacon beacon : beacons) {
                    if ((result.getDevice().getAddress().equals(beacon.getMacAddress())) && (result.getRssi() > -beacon.getRSSI())) {

                        //draw and approximate

                        startTime = SystemClock.uptimeMillis();
                        customHandler.postDelayed(updateTimerThread, 0);
                    }
                }
            }
        }
    };

    private void draw() {
        Paint drawPaint = new Paint();
        drawPaint.setColor(Color.GREEN);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        Bitmap tempBitmap2 = BitmapFactory.decodeResource(getApplication().getResources(),
                R.mipmap.elmap1);
        Bitmap tempBitmap = tempBitmap2.copy(Bitmap.Config.ARGB_8888, true);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(tempBitmap, 0, 0, null);
        tempCanvas.drawCircle(50, 50, 20, drawPaint);
        ivMap.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    System.out.println("coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            customHandler.postDelayed(this, 0);
            if (timeInMilliseconds > 3000) ivMap.setImageResource(R.mipmap.elmap);
        }
    };


    public void startScanning() {
        System.out.println("start scanning");
        btnScanning.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        btnScanning.setVisibility(View.VISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }
}
