package matz.project.zur.matz;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;

/**
 * Created by user on 4/3/2017.
 */
public class MatzMapFragment extends Fragment {

    private MapFragment mapFragment;
    private boolean isInit =false;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v=  inflater.inflate(R.layout.map_fragment, container,
                false);

        FragmentManager manager = getChildFragmentManager();

        mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {

            FragmentTransaction fragmentTransaction = manager.beginTransaction();
            mapFragment = MapFragment.newInstance();
            fragmentTransaction.replace(R.id.map, mapFragment).commit();
        }

        if (mapFragment != null ) {

            mapFragment.getMapAsync((MatzMainActivity) getActivity());
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapFragment.onResume();


    }

    @Override
    public void onPause() {
        mapFragment.onPause();
        super.onPause();
    }

}
