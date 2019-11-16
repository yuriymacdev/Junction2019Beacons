package com.example.chernobyl;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Output;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class MainActivity extends AppCompatActivity implements BeaconConsumer {

    static int  counter =0;

    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager;

    MediaPlayer mediaPlayer;

    private void playSample(int resid) {
        AssetFileDescriptor afd = this.getResources().openRawResourceFd(resid);

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setLooping(true);

            PlaybackParams params = new PlaybackParams();
            //params.setSpeed(10);
            //params.setPitch(0.25f);

            mediaPlayer.setPlaybackParams(params);
            afd.close();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "Unable to play audio queue do to exception: " + e.getMessage(), e);
        }
    }
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }



    private void requestData(String url, String jsonRequestString) {
        //Everything below is part of the Android Asynchronous HTTP Client

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("content-type", "application/json");
        RequestParams params = new RequestParams();
        params.add("JSON", jsonRequestString);



        client.post(url, params, new ResponseHandlerInterface() {
            @Override
            public void sendResponseMessage(HttpResponse response) throws IOException {
                Log.i("sendResponseMessage", response.toString());
                String s = convertStreamToString(response.getEntity().getContent());
                Log.i("", s);

            }

            @Override
            public void sendStartMessage() {

            }

            @Override
            public void sendFinishMessage() {

            }

            @Override
            public void sendProgressMessage(long bytesWritten, long bytesTotal) {

            }

            @Override
            public void sendCancelMessage() {

            }

            @Override
            public void sendSuccessMessage(int statusCode, Header[] headers, byte[] responseBody) {
                Log.i("HTTP REQUEST RESPONSE", responseBody.toString());
            }

            @Override
            public void sendFailureMessage(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.i("sendFailureMessage", ""+String.valueOf(statusCode));
            }

            @Override
            public void sendRetryMessage(int retryNo) {

            }

            @Override
            public URI getRequestURI() {
                return null;
            }

            @Override
            public void setRequestURI(URI requestURI) {

            }

            @Override
            public Header[] getRequestHeaders() {
                return new Header[0];
            }

            @Override
            public void setRequestHeaders(Header[] requestHeaders) {

            }

            @Override
            public boolean getUseSynchronousMode() {
                return false;
            }

            @Override
            public void setUseSynchronousMode(boolean useSynchronousMode) {

            }

            @Override
            public boolean getUsePoolThread() {
                return false;
            }

            @Override
            public void setUsePoolThread(boolean usePoolThread) {

            }

            @Override
            public void onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {

            }

            @Override
            public void onPostProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {

            }

            @Override
            public Object getTag() {
                return null;
            }

            @Override
            public void setTag(Object TAG) {

            }
        });

//        client.get(url, new JsonHttpResponseHandler() {
//            @Override
//            public void onSuccess(int statusCode, PreferenceActivity.Header[] headers,
//                                  JSONObject response) {
//                // called when response HTTP status is "200 OK"
//                Log.d("Bitcoin", "JSON: " + response.toString());
//
//                try {
//
//                    //The “ask” value below is a field in the JSON Object that was
//                    //retrieved from the BitcoinAverage API. It contains the current
//                    //bitcoin price
//
//                    String price = response.getString("ask");
//
//                    //Now we can use the value in the mPriceTextView
//
//
//                } catch (Exception e) {
//
//                    Log.e("Bitcoin", e.toString());
//
//                }
//
//            }
//
//            @Override
//            public void onFailure(int statusCode, Header[] headers, Throwable e,
//                                  JSONObject response) {
//
//                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
//
//                Log.d("Bitcoin", "Request fail! Status code: " + statusCode);
//                Log.d("Bitcoin", "Fail response: " + response);
//                Log.e("ERROR", e.toString());
//
//                Toast.makeText(MainActivity.this, "Request Failed",
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestData("http://10.84.113.85:8000", "{\"what\": \"reset\"}");

        playSample(R.raw.ic_geiger);



        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("NOT GRANTED", "ERROR");
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                    }
                });
                builder.show();
            }
        }

        setContentView(R.layout.activity_main);
        beaconManager = BeaconManager.getInstanceForApplication(this);
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
         beaconManager.getBeaconParsers().add(new BeaconParser().
                //setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
                        setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));

         beaconManager.setForegroundScanPeriod(100);

        beaconManager.bind(this);

        ImageView imageView=(ImageView) findViewById(R.id.imageView);
        imageView.setImageResource(R.drawable.ic_scale);





        new CountDownTimer(30000, 100) {
            public void onTick(long millisUntilFinished) {
                ImageView imageView=(ImageView) findViewById(R.id.imageView);
                imageView.setImageResource(R.drawable.ic_scale);


                Bitmap workingBitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                Bitmap bitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
                //Canvas canvas = new Canvas(mutableBitmap);

                //Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
                //Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                Canvas canvas = new Canvas(bitmap);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.BLACK);
                //canvas.drawCircle(50+(counter+=10), 50, 10, paint);
                paint.setStrokeWidth(15);
                paint.setStyle(Paint.Style.STROKE);

                float initAngle = (float)3.14*1.3f;
                float endAngle = initAngle+3.14f/2.3f;


                counter+=2;
                float angle = initAngle + (float)counter/100.0f;
                if(angle>=endAngle) {
                    angle = endAngle;
                }
                float newX = (float)cos(angle)*10;
                float newY = (float)sin(angle)*10;


                float pointerLength = bitmap.getWidth()/2.0f;
                float x0 = workingBitmap.getWidth()/2;
                float y0 = workingBitmap.getHeight()+300;
                float x1 = x0 + (float)cos(angle)*pointerLength;
                float y1 = y0 + (float)sin(angle)*pointerLength;


                canvas.drawLine(x0,y0,x1,y1, paint);


                imageView.setImageBitmap(bitmap);
            }

            public void onFinish() {

            }
        }.start();

        ImageView firstImage=(ImageView) findViewById(R.id.firstPlayerImageView);
        firstImage.setImageResource(R.drawable.ic_button1);

        ImageView secondImage=(ImageView) findViewById(R.id.secondPlayerImageView);
        secondImage.setImageResource(R.drawable.ic_button2);

        TextView firstText=(TextView) findViewById(R.id.firstPlayerTextView);
        firstText.setText("Mykola");

        TextView secondText=(TextView) findViewById(R.id.secondPlayerTextView);
        secondText.setText("Yuri");



    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    public void onBeaconServiceConnect() {
        Log.i("onBeaconServiceConnect", "onBeaconServiceConnect");
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    Beacon closestBeacon = null;
                    //Log.i("", "Found "+ beacons.size() + " beacons. Here they are:");
                    Iterator<Beacon> it = beacons.iterator();
                    for(int i=0; i<beacons.size(); i++) {
                        Beacon beacon = it.next();

                        //Log.i("BeaconFound", "Beacon detected: "+beacon.getDistance()+" with address: "+beacon.getBluetoothAddress());


                        if(beacon.getBluetoothAddress().toString().equals("D2:D7:7E:14:19:00")) {
                            TextView tv1 = (TextView)findViewById(R.id.infoTextView);
                            Log.i("", "Our beacon found: " + String.valueOf(beacon.getRssi()));


                            tv1.setText("Warning, radiation source detected with RRSI "+String.format("%d",  beacon.getRssi()));
                        }



                    }
                    //Log.i("","=========SCAN========");
                    //Log.i(TAG, "Beacon ranged with distance: "+beacons.iterator().next().getDistance()+", and id "+beacons.iterator());
                }
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }
    }
}
