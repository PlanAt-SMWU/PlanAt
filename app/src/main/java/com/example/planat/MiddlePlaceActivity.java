package com.example.planat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.LatLngBounds;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapSdk;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.CircleOverlay;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import static java.lang.Double.NaN;

public class MiddlePlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String CLIENT_ID = "mnu1q7kinz";
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Geocoder geocoder;
    private double latitude = 37.54647497980168;
    private double longitude = 126.96458430912304;
    private ImageButton add_button;
    private Button search_button, result_button, location_button;
    private EditText edit_text;
    private Vector<LatLng>markersPosition;
    private TextView tv_result;


    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.middle_place);

        locationSource = new FusedLocationSource(this,1000);


        NaverMapSdk.getInstance(this).setClient(
                new NaverMapSdk.NaverCloudPlatformClient(CLIENT_ID));

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        //????????? OnMapReadyCallback??? ?????? NaverMap ?????? ????????????
        mapFragment.getMapAsync(this);

        add_button = findViewById(R.id.add_button);
        search_button = findViewById(R.id.search_button);
        edit_text = findViewById(R.id.edit_text);
        result_button = findViewById(R.id.result_button); //???????????? ??????
        markersPosition = new Vector<LatLng>(); //?????????
        tv_result = findViewById(R.id.tv_result); //???????????? ?????? ?????? ??? ?????? ????????? ????????????
        location_button = findViewById(R.id.location_button); //???????????? ??????

    }
    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        //???????????? ????????? ????????? ?????????
        naverMap.setLayerGroupEnabled(NaverMap.LAYER_GROUP_TRANSIT, true);

        this.naverMap = naverMap;
        geocoder = new Geocoder(this);
        //locationSource??? set?????? ?????????????????? ????????????
        naverMap.setLocationSource(locationSource);
        //???????????? ?????? ?????? ?????? ??? ????????? ??????, ?????? ?????? ?????? ????????????
        naverMap.getUiSettings().setLocationButtonEnabled(true);
        LatLng initialPosition = new LatLng(latitude,longitude);
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialPosition);
        naverMap.moveCamera(cameraUpdate);

        // ?????? ?????????
        search_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //Focus????????? EditText??? ????????? ??????????????? ????????? ??? ????????? ????????????
                final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edit_text.getWindowToken(),0);

                String str = edit_text.getText().toString();
                edit_text.setText(null);
                List<Address> addressList = null;
                try {
                    // editText??? ????????? ?????????(??????, ??????, ?????? ???)??? ?????? ????????? ????????? ??????
                    addressList = geocoder.getFromLocationName(
                            str, // ??????
                            10); // ?????? ?????? ?????? ??????
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                if(addressList.size() == 0){
                    Toast.makeText(MiddlePlaceActivity.this, "?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d("??????",addressList.toString());
                // ????????? ???????????? split
                String []splitStr = addressList.get(0).toString().split(",");

                String lat = splitStr[10].substring(splitStr[10].indexOf("=") + 1); // ??????
                String lon = splitStr[12].substring(splitStr[12].indexOf("=") + 1); // ??????

                // ??????(??????, ??????) ??????
                LatLng point = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));

                add_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // ?????? ??????
                        Marker marker = new Marker();
                        marker.setPosition(point);
                        // ?????? ??????
                        marker.setMap(naverMap);

                        //?????? ???????????? ??????
                        markersPosition.add(marker.getPosition());

                    }
                });

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(point);
                naverMap.moveCamera(cameraUpdate);
            }
        });
        //???????????? ?????? ?????? ?????? ???
        result_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                double lat = 0,lon = 0;
                for(LatLng position : markersPosition){
                    lat += position.latitude;
                    lon += position.longitude;
                }
                LatLng point = new LatLng(lat/markersPosition.size(), lon/markersPosition.size());
                List<Address>address = null;
                try {
                    address = geocoder.getFromLocation(
                            lat/markersPosition.size(),
                            lon/markersPosition.size(),
                            // ??? ??????????????? ????????? ????????? ?????????.
                            1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                CameraUpdate cameraUpdate = CameraUpdate.scrollTo(point);
                naverMap.moveCamera(cameraUpdate);

                if(markersPosition.size() < 2){
                    Toast.makeText(MiddlePlaceActivity.this, "?????? ??? ??? ????????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();
                }
                else {
                    Marker marker = new Marker();
                    marker.setIcon(MarkerIcons.BLACK);
                    //?????? RED??? ???????????? ??????????????? ????????? ??????????????? ????????? ??????????????? ???????????????
                    marker.setIconTintColor(Color.RED);
                    marker.setPosition(point);
                    // ?????? ??????
                    marker.setMap(naverMap);

                    if(address.size() > 0) {
                        tv_result.setText(address.get(0).getAddressLine(0));
                    }else{
                        Toast.makeText(MiddlePlaceActivity.this, "??????????????? ????????????.", Toast.LENGTH_SHORT).show();
                    }
                    tv_result.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String url  = "nmap://route/public?dlat="+point.latitude+"&dlng="+point.longitude+"&dname="+tv_result.getText();
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            startActivity(intent);


//                            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                            if (list == null || list.isEmpty()) {
//                                Log.d("??????","?????????");
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nhn.android.nmap")));
//                            } else {
//                                startActivity(intent);
//                            }
                        }
                    });
                }
            }
        });
        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MiddlePlaceActivity.this,ScheduleActivity.class);
                intent.putExtra("location",tv_result.getText());
                setResult(RESULT_OK,intent);
                finish();
            }
        });
    }
}
