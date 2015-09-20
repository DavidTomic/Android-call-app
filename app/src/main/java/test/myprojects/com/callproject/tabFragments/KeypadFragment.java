package test.myprojects.com.callproject.tabFragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import test.myprojects.com.callproject.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class KeypadFragment extends Fragment implements View.OnClickListener,
        View.OnLongClickListener {

    private View rootView;

    private EditText mPhoneNumberField;
    private RelativeLayout mOneButton;
    private RelativeLayout mTwoButton;
    private RelativeLayout mThreeButton;
    private RelativeLayout mFourButton;
    private RelativeLayout mFiveButton;
    private RelativeLayout mSixButton;
    private RelativeLayout mSevenButton;
    private RelativeLayout mEightButton;
    private RelativeLayout mNineButton;
    private RelativeLayout mZeroButton;
    private Button mStarButton;
    private Button mPoundButton;
    private ImageView mDialButton;
    private ImageButton mDeleteButton;


    public KeypadFragment() {
        // Required empty public constructor
    }

    private void initUI(View v) {
        initializeViews(v);
        addNumberFormatting();
        setClickListeners();
    }

    private void initializeViews(View v) {
        mPhoneNumberField = (EditText) v.findViewById(R.id.phone_number);
        mPhoneNumberField.setInputType(android.text.InputType.TYPE_NULL);

        mOneButton = (RelativeLayout) v.findViewById(R.id.one);
        mTwoButton = (RelativeLayout) v.findViewById(R.id.two);
        mThreeButton = (RelativeLayout) v.findViewById(R.id.three);
        mFourButton = (RelativeLayout) v.findViewById(R.id.four);
        mFiveButton = (RelativeLayout) v.findViewById(R.id.five);
        mSixButton = (RelativeLayout) v.findViewById(R.id.six);
        mSevenButton = (RelativeLayout) v.findViewById(R.id.seven);
        mEightButton = (RelativeLayout) v.findViewById(R.id.eight);
        mNineButton = (RelativeLayout) v.findViewById(R.id.nine);
        mZeroButton = (RelativeLayout) v.findViewById(R.id.zero);
        mStarButton = (Button) v.findViewById(R.id.asterisk);
        mPoundButton = (Button) v.findViewById(R.id.hash);
        mDialButton = (ImageView) v.findViewById(R.id.dialButton);
        mDeleteButton = (ImageButton) v.findViewById(R.id.deleteButton);
    }

    private void addNumberFormatting() {
        mPhoneNumberField.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
    }

    private void setClickListeners() {
        mZeroButton.setOnClickListener(this);
        mZeroButton.setOnLongClickListener(this);

        mOneButton.setOnClickListener(this);
        mTwoButton.setOnClickListener(this);
        mThreeButton.setOnClickListener(this);
        mFourButton.setOnClickListener(this);
        mFiveButton.setOnClickListener(this);
        mSixButton.setOnClickListener(this);
        mSevenButton.setOnClickListener(this);
        mEightButton.setOnClickListener(this);
        mNineButton.setOnClickListener(this);
        mStarButton.setOnClickListener(this);
        mPoundButton.setOnClickListener(this);
        mDialButton.setOnClickListener(this);

        mDeleteButton.setOnClickListener(this);
        mDeleteButton.setOnLongClickListener(this);
    }

    private void keyPressed(int keyCode) {
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mPhoneNumberField.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.one: {
                keyPressed(KeyEvent.KEYCODE_1);
                return;
            }
            case R.id.two: {
                keyPressed(KeyEvent.KEYCODE_2);
                return;
            }
            case R.id.three: {
                keyPressed(KeyEvent.KEYCODE_3);
                return;
            }
            case R.id.four: {
                keyPressed(KeyEvent.KEYCODE_4);
                return;
            }
            case R.id.five: {
                keyPressed(KeyEvent.KEYCODE_5);
                return;
            }
            case R.id.six: {
                keyPressed(KeyEvent.KEYCODE_6);
                return;
            }
            case R.id.seven: {
                keyPressed(KeyEvent.KEYCODE_7);
                return;
            }
            case R.id.eight: {
                keyPressed(KeyEvent.KEYCODE_8);
                return;
            }
            case R.id.nine: {
                keyPressed(KeyEvent.KEYCODE_9);
                return;
            }
            case R.id.zero: {
                keyPressed(KeyEvent.KEYCODE_0);
                return;
            }
            case R.id.hash: {
                keyPressed(KeyEvent.KEYCODE_POUND);
                return;
            }
            case R.id.asterisk: {
                keyPressed(KeyEvent.KEYCODE_STAR);
                return;
            }
            case R.id.deleteButton: {
                keyPressed(KeyEvent.KEYCODE_DEL);
                return;
            }
            case R.id.dialButton: {
                dialNumber();
                return;
            }

        }

    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.deleteButton: {
                Editable digits = mPhoneNumberField.getText();
                digits.clear();
                return true;
            }
            case R.id.zero: {
                keyPressed(KeyEvent.KEYCODE_PLUS);
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the native phone call activity
     */
    private void dialNumber() {
        String number = mPhoneNumberField.getText().toString();
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
            rootView = inflater.inflate(R.layout.fragment_keypad, container, false);

            // Initialise your layout here
            initUI(rootView);

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
