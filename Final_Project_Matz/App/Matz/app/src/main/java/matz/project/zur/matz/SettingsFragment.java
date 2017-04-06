package matz.project.zur.matz;


import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class SettingsFragment extends Fragment {

    private RequestQueue queue;
    public String user;
    private ArrayList<String> allPlaces = new ArrayList<>();

    public SettingsFragment(){

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        loadUserSetting(user,view);
        Spinner dropdown = (Spinner)view.findViewById(R.id.spinner);


        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                updateUser(user,allPlaces.get(position),null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            }
        });

        EditText radius = (EditText)view.findViewById(R.id.radius);
        radius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateUser(user,null,s.toString());
            }
        });

        return view;
    }


    public void updateUser(String user,String place,String radius){
        String url ="http://10.0.2.2:9000/user";
        JSONObject request= new JSONObject();
        try {
            request.put("uname",user);
            if(place!=null){
                request.put("selected",place);
            }
            if(radius!=null){
                request.put("radius",radius);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Request a string response from the provided URL.
        JsonObjectRequest settingsRequest = new JsonObjectRequest
                (Request.Method.PUT, url, request, new Response.Listener<JSONObject>() {

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
        queue.add(settingsRequest);
    }

    public void loadUserSetting(String user, final View view){

        String url ="http://10.0.2.2:9000/user";
        JSONObject request= new JSONObject();
        try {
            request.put("uname",user);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Request a string response from the provided URL.
        JsonObjectRequest settingsRequest = new JsonObjectRequest
                (Request.Method.POST, url, request, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        String userRadius="10";
                        try {
                            String selected = response.getString("selected");
                            userRadius = response.getString("radius");
                            allPlaces.add(selected);
                            JSONArray places=response.getJSONArray("places");
                            for(int i=0;i<places.length();i++){
                                String place = places.getString(i);
                                if(!place.equals(selected)){
                                    allPlaces.add(place);
                                }


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String[] arr = new String[allPlaces.size()];
                        for(int i=0;i<allPlaces.size();i++){
                            arr[i]=allPlaces.get(i);
                        }
                        ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arr);
                        mAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

                        Spinner dropdown = (Spinner)view.findViewById(R.id.spinner);
                        dropdown.setAdapter(mAdapter);
                        EditText radius = (EditText)view.findViewById(R.id.radius);
                        radius.setText(userRadius);

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });
        // Add the request to the RequestQueue.
        queue.add(settingsRequest);
    }

}
