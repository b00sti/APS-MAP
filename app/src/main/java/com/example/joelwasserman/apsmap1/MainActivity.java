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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;


public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_MEDIA = 10001;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    android.bluetooth.BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner btScanner;
    long timeInMilliseconds = 0L;
    int i = 50;
    int j = 50;
    boolean isBack = false;
    private AppCompatButton btnScanning;
    private MapView ivMap;
    private boolean isScanningEnabled = false;
    private long startTime = 0L;
    private Handler customHandler = new Handler();
    private ArrayList<Beacon> beacons = new ArrayList<>();
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            customHandler.postDelayed(this, 0);
            if (timeInMilliseconds > 3000) ivMap.setImageResource(R.mipmap.elmap);
        }
    };
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_MEDIA);
        } else {
            readSDCard();
        }

        initBle();
        draw(i, j);

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
                beacons.add(prepareBeacon(arr));
                i = i + 1;
            }
            ivMap.setBeacons(beacons);
            ivMap.invalidate();
            Toast.makeText(this, "Beacons: " + beacons.size(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error in openning file.txt",
                    Toast.LENGTH_LONG).show();
        }

    }

    private Beacon prepareBeacon(String[] arr) {
        BitmapDrawable mapImage = (BitmapDrawable) mContext.getResources().getDrawable(R.mipmap.ic_launcher);

        int photoSizeHeight = mapImage;
        int photoSizeWigth = mapImage;
        int mapSizeHeight = ivMap.getHeight();
        int mapSizeWidth = ivMap.getWidth();
        int lat = Integer.valueOf(arr[2]);
        int lng = Integer.valueOf(arr[3]);
        new Beacon(arr[0], Integer.valueOf(arr[1]),
                lat, lng,
                lat * mapSizeHeight / photoSizeHeight,
                lng * mapSizeWidth / photoSizeWigth,
                )
    }

    private void initBle() {
        Observable
                .interval(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        draw(i, j);
                        if (i > 500) {
                            isBack = true;
                        }
                        if (isBack) {
                            i -= 50;
                            j -= 50;
                        } else {
                            i += 50;
                            j += 50;
                        }
                        Log.d("", "accept: draw");
                    }
                });

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

    private void draw(int lat, int lng) {
        ivMap.setLngLng(lat, lng);
/*        Paint drawPaint = new Paint();
        drawPaint.setColor(Color.GREEN);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(5);
        drawPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        Bitmap tempBitmap2 = BitmapFactory.decodeResource(getApplication().getResources(),
                R.mipmap.elmap);
        Bitmap tempBitmap = tempBitmap2.copy(Bitmap.Config.ARGB_8888, true);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(tempBitmap, 0, 0, null);
        for (Beacon beacon : beacons) {
            tempCanvas.drawCircle(beacon.getLat(), beacon.getLng(), 10, drawPaint);
        }
        drawPaint.setColor(Color.RED);
        tempCanvas.drawCircle(lat, lng, 15, drawPaint);
        Log.d("", "draw: " + tempCanvas.getWidth() + " / " + tempCanvas.getHeight());
        Toast.makeText(this, "" + "draw: " + tempCanvas.getWidth() + " / " + tempCanvas.getHeight(), Toast.LENGTH_LONG).show();
        ivMap.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));*/
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_MEDIA:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    readSDCard();
                }
                break;
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

    public void startScanning() {
        System.out.println("start scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }
}
