package com.example.ex10_googlemap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MyMapFrrr {

    MainActivity activity;
    GoogleMap googleMap;

    boolean firstupdateMapMe = false;

    Marker me;

    MyMapFrrr(MainActivity activity, GoogleMap googleMap){
        this.activity = activity;
        this.googleMap = googleMap;


        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setMapToolbarEnabled(false);
        uiSettings.setIndoorLevelPickerEnabled(false);
        uiSettings.setZoomControlsEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setScrollGesturesEnabled(false);

        me = createMarker(Color.GREEN);
    }


    //마커 만들기
    Marker createMarker(int color){
        MarkerOptions markerOptions = new MarkerOptions()
                .position(new LatLng(0.0,0.0) )
                .draggable(false)
                .anchor(0.5f,0.5f)
                .flat(true)
                .visible(false)
                .icon(BitmapDescriptorFactory.fromBitmap( colorBitmap(color) ))
                ;

        return googleMap.addMarker(markerOptions);
    }

    //비트맵 가져와서 색깔 칠하기
    Bitmap colorBitmap(int color){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;

        Bitmap res = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_navigation_white_48dp, options);

        Paint pp = new Paint();
        pp.setColorFilter(new LightingColorFilter(color,1));
        Canvas canvas = new Canvas(res);
        canvas.drawBitmap(res, 0f,0f, pp);

        return res;
    }

    void updateMapMe(double latitude, double longitude, double heading){
        LatLng position = new LatLng(latitude,longitude);

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                me.setVisible(true);
                me.setPosition(position);
                me.setRotation((float)heading);

                CameraPosition.Builder caBuilder = null;

                if(!firstupdateMapMe){
                    firstupdateMapMe = true;
                    caBuilder = new CameraPosition.Builder().zoom(20f).target(position);
                }else{
                    caBuilder = new CameraPosition.Builder().zoom(googleMap.getCameraPosition().zoom).target(position);
                }


                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition( caBuilder.build() ));

            }
        });
    }
}
