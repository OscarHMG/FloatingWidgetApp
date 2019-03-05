package com.rayzem.demowaze;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.rayzem.demowaze.model.PlaceBO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD = 1234;
    public static int OVERLAY_PERMISSION_REQ_CODE_CHATHEAD_MSG = 5678;

    public static final String WS_GOOGLE_MAPS_API = "AIzaSyBPzfauywyr4KCrm00pKYWTZvC54jEK8io";

    public Button btnStartWaze;
    private Toolbar mTopToolbar;
    private EditText et_destination;

    private PlaceAdapter originAdapter, destinationAdapter;
    private ArrayList<PlaceBO> al_origin_places, al_destination_places;
    private ListView lv_origin_places, lv_destination_places;

    private double originLat, originLng, destinationLat, destinationLng;


    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);


        et_destination= findViewById(R.id.et_destination);


        al_destination_places = new ArrayList<>();

        lv_destination_places = findViewById(R.id.lv_destination_places);

        destinationAdapter = new PlaceAdapter(this, al_destination_places);

        lv_destination_places.setAdapter(destinationAdapter);



        mTopToolbar = findViewById(R.id.toolbar);
        btnStartWaze = findViewById(R.id.btnStartWaze);

        btnStartWaze.setEnabled(false);


        setSupportActionBar(mTopToolbar);

        //Set listeners

        et_destination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                lv_destination_places.setVisibility(View.VISIBLE);
            }
        });



        et_destination.addTextChangedListener(new PlaceTextWatcher(et_destination, destinationAdapter));

        btnStartWaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Starting waze app ...", Snackbar.LENGTH_LONG).setAction("Action", null).show();

                if(canDrawOverlays(MainActivity.this)) {
                    startFloatingBubbleService();

                    openWazeApp();


                }else {
                    requestPermission(OVERLAY_PERMISSION_REQ_CODE_CHATHEAD);
                }
            }
        });


        lv_destination_places.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                requestGooglePlaceDetails(al_destination_places.get(position).getPlaceId(), false);
                et_destination.setText(al_destination_places.get(position).getDisplayName());
                et_destination.setSelection(al_destination_places.get(position).getDisplayName().length());
                InputMethodManager inputManager = (InputMethodManager) MainActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
    }


    private void startFloatingBubbleService() {
        startService(new Intent(MainActivity.this, FloatingBubbleService.class));
        finish();
    }

    private void requestPermission(int requestCode){
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, requestCode);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == OVERLAY_PERMISSION_REQ_CODE_CHATHEAD) {
            if (!canDrawOverlays(MainActivity.this)) {
                needPermissionDialog(requestCode);
            }else{
                startFloatingBubbleService();
            }

        }
    }

    private void needPermissionDialog(final int requestCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("You need to allow permission");
        builder.setPositiveButton("OK",
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        requestPermission(requestCode);
                    }
                });
        builder.setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        builder.setCancelable(false);
        builder.show();
    }



    public static boolean canDrawOverlays(Context context){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        else
            return Settings.canDrawOverlays(context);
    }


    public void openWazeApp() {
        //Format &navigate=yes
        //https://www.waze.com/livemap?ll=-2.9001285%2C-79.0058965&from=-2.168974%2C-79.8397207&at=now
        //String url = "waze://livemap?ll="+destinationLat+"%2C"+destinationLng+"&from="+originLat+"%2C"+originLng+"&at=now&navigate=yes";

        String url = "waze://?ll="+destinationLat+","+destinationLng+"&navigate=yes";
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.waze");

        if (intent != null) {
            // We found the activity now start the activity
            Intent intentWaze = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );

            startActivity(intentWaze);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }else{
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + "com.waze"));
            startActivity(intent);
        }

    }



    private void requestGooglePlaceDetails(String idPlace, final boolean isOrigin) {
        try {

            String path = String.format("placeid=%s&key=%s", idPlace, WS_GOOGLE_MAPS_API);
            String url = "https://maps.googleapis.com/maps/api/place/details/json?" + path;
            Log.i("URL", url);
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getJSONObject("result").length() > 0) {
                                    Double latitude = Double.valueOf(response.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lat").toString());
                                    Double longitude = Double.valueOf(response.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").get("lng").toString());


                                    if(isOrigin){
                                        originLat = latitude;
                                        originLng = longitude;
                                    }else{
                                        destinationLat = latitude;
                                        destinationLng = longitude;
                                    }
                                    btnStartWaze.setEnabled(true);
                                }
                            }
                            catch (Exception error) {

                            }
                        }
                    },
                    new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            );
            jsonObjectRequest.setTag("requestGooglePlaceDetails");
            //requestQueue.addToRequestQueue(jsonObjectRequest);
            requestQueue.add(jsonObjectRequest);
        }
        catch (Exception error) {

        }
    }

    private class PlaceTextWatcher implements TextWatcher {


        private EditText editText;
        private PlaceAdapter placeAdapter;



        private PlaceTextWatcher(EditText view, PlaceAdapter adapter) {
            this.editText = view;
            this.placeAdapter = adapter;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (editText.getText().toString().trim().isEmpty()) {
                placeAdapter.clear();
                findViewById(R.id.container_no_search).setVisibility(View.VISIBLE);
                btnStartWaze.setEnabled(false);
            }else {
                findViewById(R.id.container_no_search).setVisibility(View.GONE);

                requestQueue.cancelAll("");
                //VolleyManager.getInstance(getApplicationContext()).getRequestQueue().cancelAll("requestGooglePlace");
                requestAutoCompletePlaces();
            }

        }



        @Override
        public void afterTextChanged(Editable s) {

        }


        private void requestAutoCompletePlaces() {
            try {
                String input = URLEncoder.encode(editText.getText().toString().trim(), "UTF-8");
                String path = String.format("input=%s&key=%s&components=country:ec", input, WS_GOOGLE_MAPS_API);
                String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?" + path;
                Log.i("URL", url);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getJSONArray("predictions").length() > 0) {
                                placeAdapter.clear();
                                JSONArray predictions = response.getJSONArray("predictions");
                                for (int i = 0; i < predictions.length(); i++) {
                                    try {
                                        PlaceBO placeBO = new PlaceBO();
                                        placeBO.setPlaceId(predictions.getJSONObject(i).get("place_id").toString());
                                        placeBO.setDisplayName(predictions.getJSONObject(i).getString("description"));
                                        placeBO.setName(predictions.getJSONObject(i).getJSONObject("structured_formatting").get("main_text").toString());
                                        placeBO.setLocation(predictions.getJSONObject(i).getJSONObject("structured_formatting").get("secondary_text").toString());
                                        placeAdapter.add(placeBO);
                                    }
                                    catch (Exception error) {

                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
                requestQueue.add(jsonObjectRequest);

            }catch(Exception exception){

            }
        }
    }
}
