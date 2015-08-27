package test.myprojects.com.callproject.tabFragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import test.myprojects.com.callproject.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {

    private View rootView;

    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        if (rootView == null) {

            //       Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

            // Initialise your layout here

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
            {
                parent.removeView(rootView);
            }

        }
        return  rootView;
    }


}
