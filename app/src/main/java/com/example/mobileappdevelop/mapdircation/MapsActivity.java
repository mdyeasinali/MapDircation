package com.example.mobileappdevelop.mapdircation;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.google.android.gms.maps.model.JointType.ROUND;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GoogleMapOptions options;
    private DirctionService service;
    private String origin = "23.750774,90.392941";
    private String destination = "23.763636,90.348569";
    private String[] instructionarrey;
    private Button btnIns;
    private Button btnnext;

    private PolylineOptions polylineOptions, blackPolylineOptions;
    private Polyline blackPolyline, greyPolyLine;
    private DirectionRecponse directionRecponse;
    private List<LatLng> polyLineList;
    private Marker marker;
    private int index, next;
    private float v;
    private Handler handler;
    private LatLng startPosition, endPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        service = ApiClient.getRetrofit().create(DirctionService.class);
        btnIns = (Button) findViewById(R.id.btnshow);
        btnnext = (Button) findViewById(R.id.nextroot);
        polyLineList = new ArrayList<>();

        options = new GoogleMapOptions().zoomControlsEnabled(true);
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(options);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment);
        transaction.commit();
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getDirection();
    }

    private void getDirection() {
        String apikey = getString(R.string.google_maps_deriction);
        String urlString = String.format("directions/json?origin=%s&destination=%s&key=%s", origin, destination, apikey);

        Call<DirectionRecponse> call = service.getDirection(urlString);
        call.enqueue(new Callback<DirectionRecponse>() {
            @Override
            public void onResponse(Call<DirectionRecponse> call, Response<DirectionRecponse> response) {
                if (response.code() == 200) {
                    btnIns.setEnabled(true);
                    btnnext.setEnabled(true);
                    directionRecponse = response.body();

                    final LatLng latLng = new LatLng(
                            directionRecponse.getRoutes().get(0).getLegs().get(0).getStartLocation().getLat(),
                            directionRecponse.getRoutes().get(0).getLegs().get(0).getStartLocation().getLng()

                    );


                    List<DirectionRecponse.Step> steps = directionRecponse.getRoutes().get(0).getLegs().get(0).getSteps();
                    instructionarrey = new String[steps.size()];
                    for (int i = 0; i < steps.size(); i++) {

                        //Overview Polyline
                        String polylinec =  directionRecponse.getRoutes().get(0).getOverviewPolyline().getPoints();
                        polyLineList = decodePoly(polylinec);

                        double startLat = steps.get(i).getStartLocation().getLat();
                        double startLng = steps.get(i).getStartLocation().getLng();
                        double endlat = steps.get(i).getEndLocation().getLat();
                        double endlng = steps.get(i).getEndLocation().getLng();
                        LatLng start = new LatLng(startLat, startLng);
                        LatLng end = new LatLng(endlat, endlng);
                        String instructions = String.valueOf(Html.fromHtml(steps.get(i).getHtmlInstructions()));
                        instructionarrey[i] = instructions;
                      //  Polyline polyline = mMap.addPolyline(new PolylineOptions().add(start).add(end).clickable(true));

                        // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,11));

                        polylineOptions = new PolylineOptions();
                        polylineOptions.add(start)
                                .add(end).clickable(true)
                                .geodesic(true).width(5)
                                .color(Color.CYAN).startCap(new SquareCap())
                                .endCap(new SquareCap())
                                .jointType(ROUND);
                        greyPolyLine = mMap.addPolyline(polylineOptions);




//                        blackPolylineOptions = new PolylineOptions();
//                        blackPolylineOptions.width(5);
//                        blackPolylineOptions.color(Color.BLACK);
//                        blackPolylineOptions.startCap(new SquareCap());
//                        blackPolylineOptions.endCap(new SquareCap());
//                        blackPolylineOptions.jointType(ROUND);
//                        blackPolyline = mMap.addPolyline(blackPolylineOptions);


                    }

                    //Update latlo
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (LatLng latLngs : polyLineList) {
                        builder.include(latLngs);
                    }


                    ValueAnimator polylineAnimator = ValueAnimator.ofInt(0, 100);
                    polylineAnimator.setDuration(2000);
                    polylineAnimator.setInterpolator(new LinearInterpolator());
                    polylineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                            List<LatLng> points = greyPolyLine.getPoints();
                            int percentValue = (int) valueAnimator.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int) (size * (percentValue / 100.0f));
                            List<LatLng> p = points.subList(0, newPoints);
                            greyPolyLine.setPoints(p);
                        }
                    });
                    polylineAnimator.start();


                    //animateCamera
                    LatLngBounds bounds = builder.build();
                    CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 12);
                    mMap.animateCamera(mCameraUpdate);


                    marker = mMap.addMarker(new MarkerOptions().position(latLng)
                            .flat(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

//


//                    marker = mMap.addMarker(new MarkerOptions().position(latLng)
//                            .flat(true)
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
//                            .target(mMap.getCameraPosition().target)
//                            .zoom(17)
//                            .bearing(30)
//                            .tilt(45)
//                            .build()));

//                    marker = mMap.addMarker(new MarkerOptions().position(latLng)
//                            .flat(true)
//                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car)));


                }
            }

            @Override
            public void onFailure(Call<DirectionRecponse> call, Throwable t) {

            }
        });


    }


    public void showInstruc(View view) {
        AlertDialog dialog = new AlertDialog.Builder(this).setItems(instructionarrey, null).show();


    }

    public void nextroot(View view) {

    }




    private float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
