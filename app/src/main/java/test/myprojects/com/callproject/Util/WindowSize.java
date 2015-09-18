package test.myprojects.com.callproject.Util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class WindowSize {

	public static int windowHeight = 0, windowWidth = 0, densityDpi = 0;

	public static void initialize(Context context) {
		final DisplayMetrics displaymetrics = new DisplayMetrics();

		WindowManager windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay()
				.getMetrics(displaymetrics);
		if (windowHeight == 0)
			windowHeight = displaymetrics.heightPixels;
		if (windowWidth == 0)
			windowWidth = displaymetrics.widthPixels;
		if (densityDpi == 0)
			densityDpi = displaymetrics.densityDpi;
	}

	public static int convertDpToPixel(float dp) {

		float px = dp * (densityDpi / 160f);
		return (int) px;
	}

	public static float convertPixelsToDp(float px) {

		float dp = px / (densityDpi / 160f);
		return dp;

	}
}
