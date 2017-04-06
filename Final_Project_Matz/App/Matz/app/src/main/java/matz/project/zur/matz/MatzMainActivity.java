package matz.project.zur.matz;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.android.gms.location.LocationRequest.create;

public class MatzMainActivity extends FragmentActivity implements OnMapReadyCallback {
    private RequestQueue queue;
    private GoogleMap mMap;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    private Context ctx;
    LocationListener locationListener;
    private LatLng lastCameraPosition;
    private FragmentManager manager = getFragmentManager();
    private SQLiteDatabase database;
    private DBHelper dbHelper;
    private String[] allColumns = { DBHelper.COLUMN_USERNAME,DBHelper.COLUMN_PASSWORD};
    private List<Map<String, String>> alertData = new ArrayList<Map<String, String>>();
    private String authUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matz_main);
        ctx = this;
        final DrawerLayout drawer = ((DrawerLayout) findViewById(R.id.drawer_layout));
        Toolbar toolbar = (Toolbar) findViewById(R.id.mainToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        queue = Volley.newRequestQueue(this);
        MatzMapFragment mapFragment = new MatzMapFragment();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.Fragment_container, mapFragment, "MapFragment");
        transaction.commit();
        manager.executePendingTransactions();

        mDrawerList = (ListView) findViewById(R.id.navList);
        //ImageView header = new ImageView(this);
        //header.setImageDrawable(Uri.);
        //mDrawerList.addHeaderView(header);
        addDrawerItems();
        //mapFragment.getMapAsync(this);
    }

    private void addDrawerItems() {
        final String[] actionArray = {"Map","Settings", "Logout"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, actionArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (actionArray[position]){
                    case "Map":{

                        MatzMapFragment mapFragment = new MatzMapFragment();
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.Fragment_container, mapFragment, "MapFragment");
                        transaction.commit();
                        manager.executePendingTransactions();
                        break;

                    }
                    case "Settings":{
                        SettingsFragment settingsFragment = new SettingsFragment();
                        settingsFragment.user = authUser;
                        FragmentTransaction transaction = manager.beginTransaction();
                        transaction.replace(R.id.Fragment_container,settingsFragment,"SettingsFragment");
                        transaction.commit();
                        manager.executePendingTransactions();
                        break;
                    }
                    case "Logout":{
                        dbHelper = new DBHelper(ctx);
                        database = dbHelper.getWritableDatabase();
                        database.delete(DBHelper.TABLE_USERS,null,null);
                        database.close();
                        dbHelper.close();
                        Intent i = new Intent(ctx,LoginActivity.class);
                        ctx.startActivity(i);
                        finish();
                        break;
                    }
                }

                ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            loadMarkers();
            dbHelper = new DBHelper(ctx);
            database = dbHelper.getWritableDatabase();
            Cursor cursor = database.query(DBHelper.TABLE_USERS,allColumns,null,null,null,null,null);
            cursor.moveToFirst();
            if(!cursor.isAfterLast()){
                authUser = cursor.getString(0);
                loadAlerts(authUser);
            }
            cursor.close();
            database.close();
            dbHelper.close();
            if(lastCameraPosition == null) {
                LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            666);


                } else {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            lastCameraPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCameraPosition, 15));
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    }, null);

                }
            }
            else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastCameraPosition, 15));
            }

            FloatingActionButton add = (FloatingActionButton) findViewById(R.id.addReport);
            add.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    //Intent myIntent = new Intent(view.getContext(), agones.class);
                    //startActivityForResult(myIntent, 0);
                    final EditText input = new EditText(ctx);
                    float dpi = ctx.getResources().getDisplayMetrics().density;
                    input.setInputType(InputType.TYPE_CLASS_TEXT);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setTitle("Add new matz sighting")
                            .setMessage("add more info on the sighting")
                            .setPositiveButton("Report", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String desc = input.getText().toString();
                                    LatLng location = mMap.getCameraPosition().target;
                                    mMap.addMarker(new MarkerOptions().position(location).title(desc));
                                    addMarker(location,desc);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    ///
                                }
                            });

                    AlertDialog alertDialog = builder.create(); //Read Update
                    alertDialog.setView(input, (int) (16 * dpi), 0, (int) (16 * dpi), 0);
                    alertDialog.show();  //<-- See This!
                }

            });


    }


    public void addMarker(LatLng location,String description){
        String url ="http://10.0.2.2:9000/Markers";

        JSONObject request= new JSONObject();
        try {
            request.put("Lat",location.latitude);
            request.put("Long",location.longitude);
            request.put("desc",description);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Request a string response from the provided URL.
        JsonObjectRequest markerRequest = new JsonObjectRequest
                (Request.Method.POST, url, request, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        // Add the request to the RequestQueue.
        queue.add(markerRequest);
    }

    public void loadMarkers(){

        String url ="http://10.0.2.2:9000/Markers";

        // Request a string response from the provided URL.
        JsonObjectRequest markersRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        //mTxtDisplay.setText("Response: " + response.toString());
                        try {
                            JSONArray markers=response.getJSONArray("markers");
                            for(int i=0;i<markers.length();i++){
                                JSONObject marker = markers.getJSONObject(i);
                                LatLng location = new LatLng(marker.getDouble("Lat"), marker.getDouble("Long"));
                                String desc = marker.getString("desc");
                                mMap.addMarker(new MarkerOptions().position(location).title(desc));

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        // Add the request to the RequestQueue.
        queue.add(markersRequest);
    }

    public void loadAlerts(String user){

        String url ="http://10.0.2.2:9000/Alerts";
        JSONObject request= new JSONObject();
        try {
            request.put("uname",user);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Request a string response from the provided URL.
        JsonObjectRequest alertRequest = new JsonObjectRequest
                (Request.Method.POST, url, request, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        //mTxtDisplay.setText("Response: " + response.toString());
                        try {
                            JSONArray alerts=response.getJSONArray("alerts");


                            // init alert data
                            for(int i=0;i<alerts.length();i++){
                                JSONObject alert = alerts.getJSONObject(i);
                                String desc = alert.getString("desc");
                                String location = alert.getString("location");
                                Map<String,String> formattedAlert= new HashMap<String, String>();
                                formattedAlert.put("desc",desc);
                                formattedAlert.put("location","near "+location);
                                alertData.add(formattedAlert);

                            }
                            ImageButton alertBtn = (ImageButton) findViewById(R.id.alertButton);
                            alertBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                    builder.setTitle("Alerts");

                                    ListView alertList = new ListView(ctx);
                                    SimpleAdapter adapter = new SimpleAdapter(ctx, alertData,
                                            android.R.layout.simple_list_item_2,
                                            new String[] {"desc", "location" },
                                            new int[] {android.R.id.text1, android.R.id.text2 });
                                    alertList.setAdapter(adapter);

                                    builder.setView(alertList);
                                    builder.setPositiveButton("dismiss", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            alertData.clear();
                                        }
                                    });
                                    AlertDialog dialog = builder.create();

                                    dialog.show();
                                }
                            });



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        // Add the request to the RequestQueue.
        queue.add(alertRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 666: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                    try {
                        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                LatLng currLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currLocation, 15));
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {

                            }

                            @Override
                            public void onProviderEnabled(String provider) {

                            }

                            @Override
                            public void onProviderDisabled(String provider) {

                            }
                        },null);
                    }
                    catch (SecurityException ex){
                        // log error
                    }
                }
                else{
                    // show error view
                }

            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
