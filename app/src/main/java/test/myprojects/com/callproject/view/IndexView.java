package test.myprojects.com.callproject.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import test.myprojects.com.callproject.R;
import test.myprojects.com.callproject.Util.WindowSize;
import test.myprojects.com.callproject.model.Contact;

public class IndexView extends LinearLayout {
	private static final String TAG = "IndexView";

	public IndexView(Context context) {
		super(context);
	}

	public IndexView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public IndexView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void init(final PullToRefreshStickyList pullToRefreshStickyList) {
		setOrientation(VERTICAL);


		final char[] letters = {'A', '•', 'D', '•', 'F', '•', 'I', '•', 'K', '•', 'N', '•',
				'P', '•', 'S', '•', 'U', '•', 'X', '•', 'Z'};


	//	addTextView('#');
		for (int i = 0; i < letters.length; i++) {
			addTextView(String.valueOf(letters[i]));
		}

		final StickyListHeadersAdapter listAdapter;
		listAdapter = pullToRefreshStickyList.getRefreshableView().getAdapter();

		setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				Log.i(TAG, "onTouch");

				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN:
					case MotionEvent.ACTION_MOVE:
						Log.i(TAG, "ACTION_MOVE");
						int position = (int) (event.getY() * letters.length / v.getHeight());
						if (position >= 0 && position <= letters.length) {
							int localPosition;
							if (position == 0) {
								localPosition = 0;
							} else {
								char c = letters[position];
								if (c == '•' && (position + 1) <= letters.length) {
									c = letters[position + 1];
								}
								localPosition = searchPosition(listAdapter, c);
							}


							if (localPosition != -1) {
								Log.i(TAG, "setSelection localPosition " + localPosition);
								pullToRefreshStickyList.getRefreshableView().setSelection(localPosition);
							}

						} else {
							pullToRefreshStickyList.getRefreshableView().setSelection(0);
						}
						return true;
				}
				return false;

			}
		});
	}

	private void addTextView(String c) {
		TextView textView = new TextView(getContext());
		textView.setText(c);
		textView.setTextColor(getResources().getColor(R.color.blue_default));
		textView.setGravity(Gravity.CENTER);
		LayoutParams lllp = new LayoutParams(
				LayoutParams.MATCH_PARENT, 0);
		lllp.weight = 1;
		textView.setTextSize(WindowSize.convertPixelsToDp(textView.getTextSize()) - 2);
		addView(textView, lllp);

	//	Log.i(TAG, "size " + textView.getTextSize());

	}

	private int searchPosition(StickyListHeadersAdapter listAdapter, char c) {

		Log.i(TAG, "char " + String.valueOf(c));

		for (int i = 0; i < listAdapter.getCount(); i++) {
            Contact contact = (Contact) listAdapter.getItem(i);
			if (c == contact.getName().charAt(0)){
				Log.i(TAG, "return " + i);
				return i;
			}
		}

		Log.i(TAG, "return -1");
		return -1;
	}
}
