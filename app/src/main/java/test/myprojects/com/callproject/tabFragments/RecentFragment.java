package test.myprojects.com.callproject.tabFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import test.myprojects.com.callproject.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment {


    private static final String TAG = "RecentFragment";
    private View rootView;

    public RecentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  //      Log.i(TAG, "onCreate");
    }

    @Override
    public void onResume() {
        super.onResume();
 //       Log.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
  //      Log.i(TAG, "onPause");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {

       //     Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_recent, container, false);

            // Initialise your layout here

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
            {
                parent.removeView(rootView);
            }
            return  rootView;
        }

        return rootView;
    }


}
