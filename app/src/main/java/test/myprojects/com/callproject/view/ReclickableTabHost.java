package test.myprojects.com.callproject.view;

import android.content.Context;
import android.support.v4.app.FragmentTabHost;
import android.util.AttributeSet;

/**
 * Created by developer dtomic on 01/10/15.
 */
public class ReclickableTabHost extends FragmentTabHost {

    private VoicemailClickListener voicmailClicked;

    public ReclickableTabHost(Context context) {
        super(context);
    }

    public ReclickableTabHost(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentTab(int index) {
        if (index == 4) {
            // FIRE OFF NEW LISTENER
            if (voicmailClicked != null)
                voicmailClicked.onVoicemailClicked();
        } else {
            super.setCurrentTab(index);
        }
    }

    public void setOnVoicemailClickListener(VoicemailClickListener listener) {
        voicmailClicked = listener;
    }

    public interface VoicemailClickListener {
        void onVoicemailClicked();
    }
}
