package test.myprojects.com.callproject.tabFragments;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.Util.Prefs;

/**
 * Created by developer dtomic on 16/09/15.
 */
public class AnswerMachineFragment extends Fragment {

    private View rootView;

    @Bind(R.id.etVoiceMail)
    EditText etVoiceMail;

    @OnClick(R.id.ivDialButton)
    public void dialClicked(){
        String number = etVoiceMail.getText().toString();
        if (number.length() > 0) {
            startActivity(new Intent(Intent.ACTION_CALL,
                    Uri.parse("tel:" + number)));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootView == null) {

            //       Log.i(TAG, "onCreateView inflate");
            rootView = inflater.inflate(R.layout.fragment_answer_machine, container, false);
            ButterKnife.bind(this, rootView);
            // Initialise your layout here

            etVoiceMail.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                            || (actionId == EditorInfo.IME_ACTION_DONE)) {

                        String number = etVoiceMail.getText().toString();

                        if (number.length()>0){
                            Prefs.setVoiceMailNumber(getActivity(), number);
                        }
                    }
                    return false;
                }
            });

        } else {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
            {
                parent.removeView(rootView);
            }

        }
        return  rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    private void refreshUI(){
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        String voiceMailNumber = Prefs.getVoiceMailNumber(getActivity());

        if (!voiceMailNumber.contentEquals("")){
            etVoiceMail.setText(voiceMailNumber);

        }else {
            voiceMailNumber = tm.getVoiceMailNumber();

            if (voiceMailNumber != null && voiceMailNumber.length()>0){
                etVoiceMail.setText(voiceMailNumber);
            }
        }
    }

}
