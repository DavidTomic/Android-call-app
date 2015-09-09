package test.myprojects.com.callproject.tabFragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.AddContactsActivity;
import test.myprojects.com.callproject.EditNotificationsActivity;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.SetStatusActivity;
import test.myprojects.com.callproject.SettingsDetailActivity;
import test.myprojects.com.callproject.model.User;
import test.myprojects.com.callproject.DefaultTextActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {

    private View rootView;

    @Bind(R.id.tvLanguage)
    TextView tvLanguage;
    @Bind(R.id.tvPhone)
    TextView tvPhone;
    @Bind(R.id.tvName)
    TextView tvName;
    @Bind(R.id.tvEmail)
    TextView tvEmail;
    @Bind(R.id.tvDefaultText)
    TextView tvDefaultText;


    @OnClick(R.id.rlLanguage)
    public void languageClicked() {
        Intent intent = new Intent(getActivity(), SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_LANGUAGE);
        startActivity(intent);

    }

    @OnClick(R.id.rlPhone)
    public void phoneClicked() {
        Intent intent = new Intent(getActivity(), SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_PHONE);
        startActivity(intent);

    }

    @OnClick(R.id.rlPassword)
    public void passwordClicked() {
        Intent intent = new Intent(getActivity(), SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_PASSWORD);
        startActivity(intent);
    }

    @OnClick(R.id.rlName)
    public void nameClicked() {
        Intent intent = new Intent(getActivity(), SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_NAME);
        startActivity(intent);
    }

    @OnClick(R.id.rlEmail)
    public void emailClicked() {
        Intent intent = new Intent(getActivity(), SettingsDetailActivity.class);
        intent.putExtra("key", SettingsDetailActivity.EDIT_EMAIL);
        startActivity(intent);
    }

    @OnClick(R.id.rlDefaultText)
    public void defaultTextClicked() {
        startActivity(new Intent(getActivity(), DefaultTextActivity.class));
    }

    @OnClick(R.id.rlSetStatus)
    public void setStatusClicked() {
        startActivity(new Intent(getActivity(), SetStatusActivity.class));
    }

    @OnClick(R.id.rlManageContacts)
    public void manageContactClicked() {
        startActivity(new Intent(getActivity(), AddContactsActivity.class));
    }

    @OnClick(R.id.rlNotification)
    public void notificationClicked(){
        startActivity(new Intent(getActivity(), EditNotificationsActivity.class));
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        User user = User.getInstance(getActivity());

        switch (user.getLanguage()) {
            case ENGLISH:
                tvLanguage.setText(getString(R.string.english));
                break;
            case DANISH:
                tvLanguage.setText(getString(R.string.danish));
                break;
            default:
                tvLanguage.setText("");
                break;
        }

        tvPhone.setText(user.getPhoneNumber());
        tvName.setText(user.getName());
        tvEmail.setText(user.getEmail());
        tvDefaultText.setText(user.getStatusText());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {

            //       Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_settings, container, false);
            ButterKnife.bind(this, rootView);
        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null) {
                parent.removeView(rootView);
            }
            return rootView;
        }


        return rootView;
    }

}
