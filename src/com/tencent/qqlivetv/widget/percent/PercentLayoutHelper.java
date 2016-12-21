/**
 *****************************************************************************************************************************************************************************
 * 
 * @author :fengguangjing
 * @createTime:2016-12-21下午4:03:31
 * @version:4.2.4
 * @modifyTime:
 * @modifyAuthor:
 * @description:
 *****************************************************************************************************************************************************************************
 */
package com.tencent.qqlivetv.widget.percent;

/**
 *****************************************************************************************************************************************************************************
 * 
 * @author :fengguangjing
 * @createTime:2016-12-21下午4:03:31
 * @version:4.2.4
 * @modifyTime:
 * @modifyAuthor:
 * @description:
 *****************************************************************************************************************************************************************************
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.TextView;
import com.ktcp.utils.log.TVCommonLog;
import com.tencent.qqlivetv.R;
import com.tencent.qqlivetv.widget.percent.OpenPercentLayoutHelper.PercentLayoutInfo;
import com.tencent.qqlivetv.widget.percent.OpenPercentLayoutHelper.PercentLayoutParams;
import com.tencent.qqlivetv.widget.percent.PercentLayoutHelper.PercentLayoutInfo.PercentVal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PercentLayoutHelper {
	private static final String REGEX_PERCENT = "^(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)%([s]?[wh]?)$";
	private static final String TAG = "PercentLayout";
	private static int mHeightScreen;
	private static int mWidthScreen;
	private final ViewGroup mHost;

	public PercentLayoutHelper(ViewGroup paramViewGroup) {
		this.mHost = paramViewGroup;
		getScreenSize();
	}

	@NonNull
	private static PercentLayoutInfo checkForInfoExists(PercentLayoutInfo paramPercentLayoutInfo) {
		if (paramPercentLayoutInfo != null)
			return paramPercentLayoutInfo;
		return new PercentLayoutInfo();
	}

	public static void fetchWidthAndHeight(ViewGroup.LayoutParams paramLayoutParams, TypedArray paramTypedArray, int paramInt1, int paramInt2) {
		paramLayoutParams.width = paramTypedArray.getLayoutDimension(paramInt1, 0);
		paramLayoutParams.height = paramTypedArray.getLayoutDimension(paramInt2, 0);
	}

	private static int getBaseByModeAndVal(int widthHint, int heightHint, PercentLayoutInfo.BASEMODE basemode) {
		switch (basemode) {
		case BASE_HEIGHT:
			return heightHint;
		case BASE_WIDTH:
			return widthHint;
		case BASE_SCREEN_WIDTH:
			return mWidthScreen;
		case BASE_SCREEN_HEIGHT:
			return mHeightScreen;
		}
		return 0;
	}

	public static PercentLayoutInfo getPercentLayoutInfo(Context paramContext, AttributeSet paramAttributeSet) {
		TypedArray array = paramContext.obtainStyledAttributes(paramAttributeSet, R.styleable.PercentLayout_Layout);
		PercentLayoutInfo info = setPaddingRelatedVal(array, setMinMaxWidthHeightRelatedVal(array, setTextSizeSupportVal(array, setMarginRelatedVal(array, setWidthAndHeightVal(array, null)))));
		array.recycle();
		if (Log.isLoggable("PercentLayout", 3))
			TVCommonLog.d("PercentLayout", "constructed: " + paramAttributeSet);
		return info;
	}

	private static PercentLayoutInfo.PercentVal getPercentVal(TypedArray paramTypedArray, int paramInt, boolean paramBoolean) {
		return getPercentVal(paramTypedArray.getString(paramInt), paramBoolean);
	}

	private static PercentLayoutInfo.PercentVal getPercentVal(String paramString, boolean paramBoolean) {
		if (paramString == null)
			return null;
		Object localObject = Pattern.compile("^(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)%([s]?[wh]?)$").matcher(paramString);
		if (!(((Matcher) localObject).matches()))
			throw new RuntimeException("the value of layout_xxxPercent invalid! ==>" + paramString);
		int i = paramString.length();
		String floatVal = ((Matcher) localObject).group(1);
		String lastAlpha = paramString.substring(i - 1);
		
		float f = Float.parseFloat((String) floatVal) / 100.0F;
		
		PercentLayoutInfo.PercentVal percentVal = new PercentLayoutInfo.PercentVal();
		((PercentLayoutInfo.PercentVal) percentVal).percent = f;
		if (paramString.endsWith("sw")) {
			((PercentLayoutInfo.PercentVal) percentVal).basemode = PercentLayoutInfo.BASEMODE.BASE_SCREEN_WIDTH;
			return percentVal;
		}
		if (paramString.endsWith("sh")) {
			((PercentLayoutInfo.PercentVal) percentVal).basemode = PercentLayoutInfo.BASEMODE.BASE_SCREEN_HEIGHT;
			return percentVal;
		}
		if (paramString.endsWith("%")) {
			if (paramBoolean) {
				((PercentLayoutInfo.PercentVal) percentVal).basemode = PercentLayoutInfo.BASEMODE.BASE_WIDTH;
				return percentVal;
			}
			((PercentLayoutInfo.PercentVal) percentVal).basemode = PercentLayoutInfo.BASEMODE.BASE_HEIGHT;
			return percentVal;
		}
		if (paramString.endsWith("w")) {
			((PercentLayoutInfo.PercentVal) percentVal).basemode = PercentLayoutInfo.BASEMODE.BASE_WIDTH;
			return percentVal;
		}
		if (paramString.endsWith("h")) {
			((PercentLayoutInfo.PercentVal) percentVal).basemode = PercentLayoutInfo.BASEMODE.BASE_HEIGHT;
			return percentVal;
		}
		throw new IllegalArgumentException("the " + paramString + " must be endWith [%|w|h|sw|sh]");
	}

	private void getScreenSize() {
		WindowManager localWindowManager = (WindowManager) this.mHost.getContext().getSystemService("window");
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		localWindowManager.getDefaultDisplay().getMetrics(localDisplayMetrics);
		mWidthScreen = localDisplayMetrics.widthPixels;
		mHeightScreen = localDisplayMetrics.heightPixels;
	}

	private void invokeMethod(String paramString, int paramInt1, int paramInt2, View paramView, Class paramClass, PercentLayoutInfo.PercentVal paramPercentVal) {
		if (Log.isLoggable("PercentLayout", 3))
			TVCommonLog.d("PercentLayout", paramString + " ==> " + paramPercentVal);
		if (paramPercentVal != null) {
			 Method setMaxWidthMethod = null;
			try {
				setMaxWidthMethod = paramClass.getMethod(paramString, new Class[] { Integer.TYPE });
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			 try {
				 setMaxWidthMethod.setAccessible(true);
				setMaxWidthMethod.invoke(paramView, new Object[] { Integer.valueOf((int) (getBaseByModeAndVal(paramInt1, paramInt2, paramPercentVal.basemode) * paramPercentVal.percent)) });
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private static PercentLayoutInfo setMarginRelatedVal(TypedArray paramTypedArray, PercentLayoutInfo paramPercentLayoutInfo) {
		PercentLayoutInfo.PercentVal localPercentVal = getPercentVal(paramTypedArray, 2, true);
		Object localObject = paramPercentLayoutInfo;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				TVCommonLog.v("PercentLayout", "percent margin: " + localPercentVal.percent);
			localObject = checkForInfoExists(paramPercentLayoutInfo);
			((PercentLayoutInfo) localObject).leftMarginPercent = localPercentVal;
			((PercentLayoutInfo) localObject).topMarginPercent = localPercentVal;
			((PercentLayoutInfo) localObject).rightMarginPercent = localPercentVal;
			((PercentLayoutInfo) localObject).bottomMarginPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 3, true);
		paramPercentLayoutInfo = (PercentLayoutInfo) localObject;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				TVCommonLog.v("PercentLayout", "percent left margin: " + localPercentVal.percent);
			paramPercentLayoutInfo = checkForInfoExists((PercentLayoutInfo) localObject);
			paramPercentLayoutInfo.leftMarginPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 4, false);
		localObject = paramPercentLayoutInfo;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				TVCommonLog.d("PercentLayout", "percent top margin: " + localPercentVal.percent);
			localObject = checkForInfoExists(paramPercentLayoutInfo);
			((PercentLayoutInfo) localObject).topMarginPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 5, true);
		paramPercentLayoutInfo = (PercentLayoutInfo) localObject;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				Log.v("PercentLayout", "percent right margin: " + localPercentVal.percent);
			paramPercentLayoutInfo = checkForInfoExists((PercentLayoutInfo) localObject);
			paramPercentLayoutInfo.rightMarginPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 6, false);
		localObject = paramPercentLayoutInfo;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				Log.v("PercentLayout", "percent bottom margin: " + localPercentVal.percent);
			localObject = checkForInfoExists(paramPercentLayoutInfo);
			((PercentLayoutInfo) localObject).bottomMarginPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 7, true);
		paramPercentLayoutInfo = (PercentLayoutInfo) localObject;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				Log.v("PercentLayout", "percent start margin: " + localPercentVal.percent);
			paramPercentLayoutInfo = checkForInfoExists((PercentLayoutInfo) localObject);
			paramPercentLayoutInfo.startMarginPercent = localPercentVal;
		}
		localObject = getPercentVal(paramTypedArray, 8, true);
		
		PercentLayoutInfo percentVal = paramPercentLayoutInfo;
		if (localObject != null) {
			if (Log.isLoggable("PercentLayout", 2))
				Log.v("PercentLayout", "percent end margin: " + ((PercentLayoutInfo.PercentVal) localObject).percent);
			percentVal = checkForInfoExists(paramPercentLayoutInfo);
			percentVal.endMarginPercent = ((PercentLayoutInfo.PercentVal) localObject);
		}
		return ((PercentLayoutInfo) percentVal);
	}

	private static PercentLayoutInfo setMinMaxWidthHeightRelatedVal(TypedArray paramTypedArray, PercentLayoutInfo paramPercentLayoutInfo) {
		PercentLayoutInfo.PercentVal localPercentVal = getPercentVal(paramTypedArray, 10, true);
		if (localPercentVal != null) {
			checkForInfoExists(paramPercentLayoutInfo);
			paramPercentLayoutInfo.maxWidthPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 11, false);
		if (localPercentVal != null) {
			checkForInfoExists(paramPercentLayoutInfo);
			paramPercentLayoutInfo.maxHeightPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 12, true);
		if (localPercentVal != null) {
			checkForInfoExists(paramPercentLayoutInfo);
			paramPercentLayoutInfo.minWidthPercent = localPercentVal;
		}
		PercentVal percentVal = getPercentVal(paramTypedArray, 13, false);
		if (percentVal != null) {
			checkForInfoExists(paramPercentLayoutInfo);
			paramPercentLayoutInfo.minHeightPercent = percentVal;
		}
		return paramPercentLayoutInfo;
	}

	private static PercentLayoutInfo setPaddingRelatedVal(TypedArray paramTypedArray, PercentLayoutInfo paramPercentLayoutInfo) {
		PercentLayoutInfo.PercentVal localPercentVal = getPercentVal(paramTypedArray, 14, true);
		Object localObject = paramPercentLayoutInfo;
		if (localPercentVal != null) {
			localObject = checkForInfoExists(paramPercentLayoutInfo);
			((PercentLayoutInfo) localObject).paddingLeftPercent = localPercentVal;
			((PercentLayoutInfo) localObject).paddingRightPercent = localPercentVal;
			((PercentLayoutInfo) localObject).paddingBottomPercent = localPercentVal;
			((PercentLayoutInfo) localObject).paddingTopPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 17, true);
		paramPercentLayoutInfo = (PercentLayoutInfo) localObject;
		if (localPercentVal != null) {
			paramPercentLayoutInfo = checkForInfoExists((PercentLayoutInfo) localObject);
			paramPercentLayoutInfo.paddingLeftPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 18, true);
		localObject = paramPercentLayoutInfo;
		if (localPercentVal != null) {
			localObject = checkForInfoExists(paramPercentLayoutInfo);
			((PercentLayoutInfo) localObject).paddingRightPercent = localPercentVal;
		}
		localPercentVal = getPercentVal(paramTypedArray, 15, true);
		paramPercentLayoutInfo = (PercentLayoutInfo) localObject;
		if (localPercentVal != null) {
			paramPercentLayoutInfo = checkForInfoExists((PercentLayoutInfo) localObject);
			paramPercentLayoutInfo.paddingTopPercent = localPercentVal;
		}
		localObject = getPercentVal(paramTypedArray, 16, true);
		PercentLayoutInfo percentLayoutInfo = paramPercentLayoutInfo;
		if (localObject != null) {
			percentLayoutInfo = checkForInfoExists(paramPercentLayoutInfo);
			percentLayoutInfo.paddingBottomPercent = ((PercentLayoutInfo.PercentVal) localObject);
		}
		return ((PercentLayoutInfo) percentLayoutInfo);
	}

	private static PercentLayoutInfo setTextSizeSupportVal(TypedArray paramTypedArray, PercentLayoutInfo paramPercentLayoutInfo) {
		PercentLayoutInfo.PercentVal localPercentVal = getPercentVal(paramTypedArray, 9, false);
		PercentLayoutInfo percentLayoutInfo = paramPercentLayoutInfo;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				TVCommonLog.v("PercentLayout", "percent text size: " + localPercentVal.percent);
			percentLayoutInfo = checkForInfoExists(paramPercentLayoutInfo);
			percentLayoutInfo.textSizePercent = localPercentVal;
		}
		return percentLayoutInfo;
	}

	private static PercentLayoutInfo setWidthAndHeightVal(TypedArray paramTypedArray, PercentLayoutInfo paramPercentLayoutInfo) {
		PercentLayoutInfo.PercentVal localPercentVal = getPercentVal(paramTypedArray, 0, true);
		PercentLayoutInfo localPercentLayoutInfo = paramPercentLayoutInfo;
		if (localPercentVal != null) {
			if (Log.isLoggable("PercentLayout", 2))
				TVCommonLog.d("PercentLayout", "percent width: " + localPercentVal.percent);
			localPercentLayoutInfo = checkForInfoExists(paramPercentLayoutInfo);
			localPercentLayoutInfo.widthPercent = localPercentVal;
		}
		PercentLayoutInfo.PercentVal percentVal  = getPercentVal(paramTypedArray, 1, false);
		PercentLayoutInfo percentLayoutInfo = localPercentLayoutInfo;
		if (paramPercentLayoutInfo != null) {
			if (Log.isLoggable("PercentLayout", 2))
				TVCommonLog.v("PercentLayout", "percent height: " + percentVal.percent);
			percentLayoutInfo = checkForInfoExists(localPercentLayoutInfo);
			percentLayoutInfo.heightPercent = percentVal;
		}
		return percentLayoutInfo;
	}

//	private static boolean shouldHandleMeasuredHeightTooSmall(View paramView, PercentLayoutInfo paramPercentLayoutInfo)
//  {
//    int i = ViewCompat.getMeasuredHeightAndState(paramView);
//    if ((paramPercentLayoutInfo == null) || (paramPercentLayoutInfo.heightPercent == null));
//    do
//      return false;
//    while (((i & 0xFF000000) != 16777216) || (paramPercentLayoutInfo.heightPercent.percent < 0F) || (paramPercentLayoutInfo.mPreservedParams.height != -2);
//    return true;
//  }
//
//	private static boolean shouldHandleMeasuredWidthTooSmall(View paramView, PercentLayoutInfo paramPercentLayoutInfo)
//  {
//    int i = ViewCompat.getMeasuredWidthAndState(paramView);
//    if ((paramPercentLayoutInfo == null) || (paramPercentLayoutInfo.widthPercent == null));
//    do
//      return false;
//    while (((i & 0xFF000000) != 16777216) || (paramPercentLayoutInfo.widthPercent.percent < 0F) || (paramPercentLayoutInfo.mPreservedParams.width != -2);
//    return true;
//  }

	private static boolean shouldHandleMeasuredWidthTooSmall(View view, PercentLayoutInfo info)
    {
        int state = ViewCompat.getMeasuredWidthAndState(view) & ViewCompat.MEASURED_STATE_MASK;
        if (info == null || info.widthPercent == null)
        {
            return false;
        }
        return state == ViewCompat.MEASURED_STATE_TOO_SMALL && info.widthPercent.percent >= 0 &&
                info.mPreservedParams.width == ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    private static boolean shouldHandleMeasuredHeightTooSmall(View view, PercentLayoutInfo info)
    {
        int state = ViewCompat.getMeasuredHeightAndState(view) & ViewCompat.MEASURED_STATE_MASK;
        if (info == null || info.heightPercent == null)
        {
            return false;
        }
        return state == ViewCompat.MEASURED_STATE_TOO_SMALL && info.heightPercent.percent >= 0 &&
                info.mPreservedParams.height == ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    
	private void supportMinOrMaxDimesion(int paramInt1, int paramInt2, View paramView, PercentLayoutInfo paramPercentLayoutInfo) {
		Class localClass;
//		try {
			localClass = paramView.getClass();
			invokeMethod("setMaxWidth", paramInt1, paramInt2, paramView, localClass, paramPercentLayoutInfo.maxWidthPercent);
			invokeMethod("setMaxHeight", paramInt1, paramInt2, paramView, localClass, paramPercentLayoutInfo.maxHeightPercent);
			invokeMethod("setMinWidth", paramInt1, paramInt2, paramView, localClass, paramPercentLayoutInfo.minWidthPercent);
			invokeMethod("setMinHeight", paramInt1, paramInt2, paramView, localClass, paramPercentLayoutInfo.minHeightPercent);
//		} catch (NoSuchMethodException e) {
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		}
	}

	private void supportPadding(int paramInt1, int paramInt2, View paramView, PercentLayoutInfo paramPercentLayoutInfo) {
		int i = paramView.getPaddingLeft();
		int j = paramView.getPaddingRight();
		int k = paramView.getPaddingTop();
		int l = paramView.getPaddingBottom();
		PercentLayoutInfo.PercentVal localPercentVal = paramPercentLayoutInfo.paddingLeftPercent;
		if (localPercentVal != null)
			i = (int) (getBaseByModeAndVal(paramInt1, paramInt2, localPercentVal.basemode) * localPercentVal.percent);
		localPercentVal = paramPercentLayoutInfo.paddingRightPercent;
		if (localPercentVal != null)
			j = (int) (getBaseByModeAndVal(paramInt1, paramInt2, localPercentVal.basemode) * localPercentVal.percent);
		localPercentVal = paramPercentLayoutInfo.paddingTopPercent;
		if (localPercentVal != null)
			k = (int) (getBaseByModeAndVal(paramInt1, paramInt2, localPercentVal.basemode) * localPercentVal.percent);
		PercentLayoutInfo.PercentVal percentVal   = paramPercentLayoutInfo.paddingBottomPercent;
		if (percentVal != null)
			l = (int) (getBaseByModeAndVal(paramInt1, paramInt2, percentVal.basemode) * percentVal.percent);
		paramView.setPadding(i, k, j, l);
	}

//	private void supportTextSize(int paramInt1, int paramInt2, View paramView, PercentLayoutInfo paramPercentLayoutInfo)
//  {
//    float f;
//    PercentLayoutInfo.PercentVal percentVal    = paramPercentLayoutInfo.textSizePercent;
//    if (percentVal == null);
//    do
//    {
//      return;
//      f = getBaseByModeAndVal(paramInt1, paramInt2, percentVal.basemode);
//      f = (int)(percentVal.percent * f);
//    }
//    while (!(paramView instanceof TextView);
//    ((TextView)paramView).setTextSize(0, f);
//  }
    private void supportTextSize(int widthHint, int heightHint, View view, PercentLayoutInfo info)
    {
        //textsize percent support

        PercentLayoutInfo.PercentVal textSizePercent = info.textSizePercent;
        if (textSizePercent == null) return;

        int base = getBaseByModeAndVal(widthHint, heightHint, textSizePercent.basemode);
        float textSize = (int) (base * textSizePercent.percent);

        //Button 和 EditText 是TextView的子类
        if (view instanceof TextView)
        {
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
    }

	public void adjustChildren(int paramInt1, int paramInt2) {
		if (Log.isLoggable("PercentLayout", 3))
			TVCommonLog.d("PercentLayout",
					"adjustChildren: " + this.mHost + " widthMeasureSpec: " + View.MeasureSpec.toString(paramInt1) + " heightMeasureSpec: " + View.MeasureSpec.toString(paramInt2));
		int i = View.MeasureSpec.getSize(paramInt1);
		paramInt2 = View.MeasureSpec.getSize(paramInt2);
		if (Log.isLoggable("PercentLayout", 3))
			TVCommonLog.d("PercentLayout", "widthHint = " + i + " , heightHint = " + paramInt2);
		int j = this.mHost.getChildCount();
		paramInt1 = 0;
		if (paramInt1 < j) {
			PercentLayoutInfo localPercentLayoutInfo;
			View localView = this.mHost.getChildAt(paramInt1);
			ViewGroup.LayoutParams localLayoutParams = localView.getLayoutParams();
			if (Log.isLoggable("PercentLayout", 3))
				Log.d("PercentLayout", "should adjust " + localView + " " + localLayoutParams);
			if (localLayoutParams instanceof PercentLayoutParams) {
				localPercentLayoutInfo = ((PercentLayoutParams) localLayoutParams).getPercentLayoutInfo();
				if (Log.isLoggable("PercentLayout", 3))
					Log.d("PercentLayout", "using " + localPercentLayoutInfo);
				if (localPercentLayoutInfo != null) {
					supportTextSize(i, paramInt2, localView, localPercentLayoutInfo);
					supportPadding(i, paramInt2, localView, localPercentLayoutInfo);
					supportMinOrMaxDimesion(i, paramInt2, localView, localPercentLayoutInfo);
					if (!(localLayoutParams instanceof ViewGroup.MarginLayoutParams))
						localPercentLayoutInfo.fillLayoutParams(localLayoutParams, i, paramInt2);
//						break label318;
					localPercentLayoutInfo.fillMarginLayoutParams((ViewGroup.MarginLayoutParams) localLayoutParams, i, paramInt2);
				}
			}
			while (true) {
				while (true)
					paramInt1 += 1;
//				label318: localPercentLayoutInfo.fillLayoutParams(localLayoutParams, i, paramInt2);
			}
		}
	}
	  /**
     * Iterates over children and checks if any of them would like to get more space than it
     * received through the percentage dimension.
     * <p/>
     * If you are building a layout that supports percentage dimensions you are encouraged to take
     * advantage of this method. The developer should be able to specify that a child should be
     * remeasured by adding normal dimension attribute with {@code wrap_content} value. For example
     * he might specify child's attributes as {@code app:layout_widthPercent="60%p"} and
     * {@code android:layout_width="wrap_content"}. In this case if the child receives too little
     * space, it will be remeasured with width set to {@code WRAP_CONTENT}.
     *
     * @return True if the measure phase needs to be rerun because one of the children would like
     * to receive more space.
     */
    public boolean handleMeasuredStateTooSmall()
    {
        boolean needsSecondMeasure = false;
        for (int i = 0, N = mHost.getChildCount(); i < N; i++)
        {
            View view = mHost.getChildAt(i);
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (Log.isLoggable(TAG, Log.DEBUG))
            {
                Log.d(TAG, "should handle measured state too small " + view + " " + params);
            }
            if (params instanceof PercentLayoutParams)
            {
                PercentLayoutInfo info =
                        ((PercentLayoutParams) params).getPercentLayoutInfo();
                if (info != null)
                {
                    if (shouldHandleMeasuredWidthTooSmall(view, info))
                    {
                        needsSecondMeasure = true;
                        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                    if (shouldHandleMeasuredHeightTooSmall(view, info))
                    {
                        needsSecondMeasure = true;
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    }
                }
            }
        }
        if (Log.isLoggable(TAG, Log.DEBUG))
        {
            Log.d(TAG, "should trigger second measure pass: " + needsSecondMeasure);
        }
        return needsSecondMeasure;
    }

//
//	public boolean handleMeasuredStateTooSmall() {
//		boolean bool2 = false;
//		View localView = null;
//		ViewGroup.LayoutParams localLayoutParams = null;
//		PercentLayoutInfo localPercentLayoutInfo = null;
//		int j = this.mHost.getChildCount();
//		int i = 0;
//		boolean bool1 = false;
//		if (i < j) {
//			localView = this.mHost.getChildAt(i);
//			localLayoutParams = localView.getLayoutParams();
//			if (Log.isLoggable("PercentLayout", 3))
//				TVCommonLog.d("PercentLayout", "should handle measured state too small " + localView + " " + localLayoutParams);
//			bool2 = bool1;
//			if (localLayoutParams instanceof PercentLayoutParams) {
//				localPercentLayoutInfo = ((PercentLayoutParams) localLayoutParams).getPercentLayoutInfo();
//				bool2 = bool1;
//				if (localPercentLayoutInfo != null) {
//					if (!(shouldHandleMeasuredWidthTooSmall(localView, localPercentLayoutInfo)))
//						bool2 = bool1;
////						break label204;
//					localLayoutParams.width = -2;
//					bool1 = true;
//				}
//			}
//		}
//		while (true) {
//			if (shouldHandleMeasuredHeightTooSmall(localView, localPercentLayoutInfo)) {
//				localLayoutParams.height = -2;
//				bool2 = true;
//			}
//			while (true) {
//				while (true) {
//					i += 1;
//					bool1 = bool2;
//				}
////				if (Log.isLoggable("PercentLayout", 3))
////					TVCommonLog.d("PercentLayout", "should trigger second measure pass: " + bool1);
//				return bool1;
////				label204: bool2 = bool1;
//			}
//		}
//	}

	public void restoreOriginalParams() {
		int j = this.mHost.getChildCount();
		int i = 0;
		if (i < j) {
			Object localObject = this.mHost.getChildAt(i);
			ViewGroup.LayoutParams localLayoutParams = ((View) localObject).getLayoutParams();
			if (Log.isLoggable("PercentLayout", 3))
				TVCommonLog.d("PercentLayout", "should restore " + localObject + " " + localLayoutParams);
			if (localLayoutParams instanceof PercentLayoutParams) {
				localObject = ((PercentLayoutParams) localLayoutParams).getPercentLayoutInfo();
				if (Log.isLoggable("PercentLayout", 3))
					TVCommonLog.d("PercentLayout", "using " + localObject);
				if (localObject != null) {
					if (!(localLayoutParams instanceof ViewGroup.MarginLayoutParams))
						((PercentLayoutInfo) localObject).restoreLayoutParams(localLayoutParams);
//						break label159;
					((PercentLayoutInfo) localObject).restoreMarginLayoutParams((ViewGroup.MarginLayoutParams) localLayoutParams);
				}
			}
			while (true) {
				while (true)
					i += 1;
//				label159: ((PercentLayoutInfo) localObject).restoreLayoutParams(localLayoutParams);
			}
		}
	}

	/**
	 * Container for information about percentage dimensions and margins. It
	 * acts as an extension for {@code LayoutParams}.
	 */
	public static class PercentLayoutInfo {

		private enum BASEMODE {

			BASE_WIDTH, BASE_HEIGHT, BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT;

			/**
			 * width_parent
			 */
			public static final String PERCENT = "%";
			/**
			 * width_parent
			 */
			public static final String W = "w";
			/**
			 * height_parent
			 */
			public static final String H = "h";
			/**
			 * width_screen
			 */
			public static final String SW = "sw";
			/**
			 * height_screen
			 */
			public static final String SH = "sh";
		}

		public static class PercentVal {

			public float percent = -1;
			public BASEMODE basemode;

			public PercentVal() {
			}

			public PercentVal(float percent, BASEMODE baseMode) {
				this.percent = percent;
				this.basemode = baseMode;
			}

			@Override
			public String toString() {
				return "PercentVal{" + "percent=" + percent + ", basemode=" + basemode.name() + '}';
			}
		}

		public PercentVal widthPercent;
		public PercentVal heightPercent;

		public PercentVal leftMarginPercent;
		public PercentVal topMarginPercent;
		public PercentVal rightMarginPercent;
		public PercentVal bottomMarginPercent;
		public PercentVal startMarginPercent;
		public PercentVal endMarginPercent;

		public PercentVal textSizePercent;

		// 1.0.4 those attr for some views' setMax/min Height/Width method
		public PercentVal maxWidthPercent;
		public PercentVal maxHeightPercent;
		public PercentVal minWidthPercent;
		public PercentVal minHeightPercent;

		// 1.0.6 add padding supprot
		public PercentVal paddingLeftPercent;
		public PercentVal paddingRightPercent;
		public PercentVal paddingTopPercent;
		public PercentVal paddingBottomPercent;

		/* package */final ViewGroup.MarginLayoutParams mPreservedParams;

		public PercentLayoutInfo() {
			mPreservedParams = new ViewGroup.MarginLayoutParams(0, 0);
		}

		/**
		 * Fills {@code ViewGroup.LayoutParams} dimensions based on percentage
		 * values.
		 */
		public void fillLayoutParams(ViewGroup.LayoutParams params, int widthHint, int heightHint) {
			// Preserve the original layout params, so we can restore them after
			// the measure step.
			mPreservedParams.width = params.width;
			mPreservedParams.height = params.height;

			if (widthPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, widthPercent.basemode);
				params.width = (int) (base * widthPercent.percent);
			}
			if (heightPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, heightPercent.basemode);
				params.height = (int) (base * heightPercent.percent);
			}

			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "after fillLayoutParams: (" + params.width + ", " + params.height + ")");
			}
		}

		/**
		 * Fills {@code ViewGroup.MarginLayoutParams} dimensions and margins
		 * based on percentage values.
		 */
		public void fillMarginLayoutParams(ViewGroup.MarginLayoutParams params, int widthHint, int heightHint) {
			fillLayoutParams(params, widthHint, heightHint);

			// Preserver the original margins, so we can restore them after the
			// measure step.
			mPreservedParams.leftMargin = params.leftMargin;
			mPreservedParams.topMargin = params.topMargin;
			mPreservedParams.rightMargin = params.rightMargin;
			mPreservedParams.bottomMargin = params.bottomMargin;
			MarginLayoutParamsCompat.setMarginStart(mPreservedParams, MarginLayoutParamsCompat.getMarginStart(params));
			MarginLayoutParamsCompat.setMarginEnd(mPreservedParams, MarginLayoutParamsCompat.getMarginEnd(params));

			if (leftMarginPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, leftMarginPercent.basemode);
				params.leftMargin = (int) (base * leftMarginPercent.percent);
			}
			if (topMarginPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, topMarginPercent.basemode);
				params.topMargin = (int) (base * topMarginPercent.percent);
			}
			if (rightMarginPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, rightMarginPercent.basemode);
				params.rightMargin = (int) (base * rightMarginPercent.percent);
			}
			if (bottomMarginPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, bottomMarginPercent.basemode);
				params.bottomMargin = (int) (base * bottomMarginPercent.percent);
			}
			if (startMarginPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, startMarginPercent.basemode);
				MarginLayoutParamsCompat.setMarginStart(params, (int) (base * startMarginPercent.percent));
			}
			if (endMarginPercent != null) {
				int base = getBaseByModeAndVal(widthHint, heightHint, endMarginPercent.basemode);
				MarginLayoutParamsCompat.setMarginEnd(params, (int) (base * endMarginPercent.percent));
			}
			if (Log.isLoggable(TAG, Log.DEBUG)) {
				Log.d(TAG, "after fillMarginLayoutParams: (" + params.width + ", " + params.height + ")");
			}
		}

		@Override
		public String toString() {
			return "PercentLayoutInfo{" + "widthPercent=" + widthPercent + ", heightPercent=" + heightPercent + ", leftMarginPercent=" + leftMarginPercent + ", topMarginPercent=" + topMarginPercent
					+ ", rightMarginPercent=" + rightMarginPercent + ", bottomMarginPercent=" + bottomMarginPercent + ", startMarginPercent=" + startMarginPercent + ", endMarginPercent="
					+ endMarginPercent + ", textSizePercent=" + textSizePercent + ", maxWidthPercent=" + maxWidthPercent + ", maxHeightPercent=" + maxHeightPercent + ", minWidthPercent="
					+ minWidthPercent + ", minHeightPercent=" + minHeightPercent + ", paddingLeftPercent=" + paddingLeftPercent + ", paddingRightPercent=" + paddingRightPercent
					+ ", paddingTopPercent=" + paddingTopPercent + ", paddingBottomPercent=" + paddingBottomPercent + ", mPreservedParams=" + mPreservedParams + '}';
		}

		/**
		 * Restores original dimensions and margins after they were changed for
		 * percentage based values. Calling this method only makes sense if you
		 * previously called
		 * {@link OpenPercentLayoutHelper.PercentLayoutInfo#fillMarginLayoutParams}
		 * .
		 */
		public void restoreMarginLayoutParams(ViewGroup.MarginLayoutParams params) {
			restoreLayoutParams(params);
			params.leftMargin = mPreservedParams.leftMargin;
			params.topMargin = mPreservedParams.topMargin;
			params.rightMargin = mPreservedParams.rightMargin;
			params.bottomMargin = mPreservedParams.bottomMargin;
			MarginLayoutParamsCompat.setMarginStart(params, MarginLayoutParamsCompat.getMarginStart(mPreservedParams));
			MarginLayoutParamsCompat.setMarginEnd(params, MarginLayoutParamsCompat.getMarginEnd(mPreservedParams));
		}

		/**
		 * Restores original dimensions after they were changed for percentage
		 * based values. Calling this method only makes sense if you previously
		 * called
		 * {@link OpenPercentLayoutHelper.PercentLayoutInfo#fillLayoutParams}.
		 */
		public void restoreLayoutParams(ViewGroup.LayoutParams params) {
			params.width = mPreservedParams.width;
			params.height = mPreservedParams.height;
		}
	}

	/**
	 * If a layout wants to support percentage based dimensions and use this
	 * helper class, its {@code LayoutParams} subclass must implement this
	 * interface.
	 * <p/>
	 * Your {@code LayoutParams} subclass should contain an instance of
	 * {@code PercentLayoutInfo} and the implementation of this interface should
	 * be a simple accessor.
	 */
	public interface PercentLayoutParams {
		PercentLayoutInfo getPercentLayoutInfo();
	}
}