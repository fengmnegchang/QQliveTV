/**
 *****************************************************************************************************************************************************************************
 * 
 * @author :fengguangjing
 * @createTime:2016-12-21下午2:56:55
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
 * @createTime:2016-12-21下午2:56:55
 * @version:4.2.4
 * @modifyTime:
 * @modifyAuthor:
 * @description:
 *****************************************************************************************************************************************************************************
 */

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class PercentLinearLayout extends LinearLayout {
	private static final String TAG = "PercentLinearLayout";
	private OpenPercentLayoutHelper mPercentLayoutHelper = new OpenPercentLayoutHelper(this);

	public PercentLinearLayout(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	private int getScreenHeight() {
		WindowManager localWindowManager = (WindowManager) getContext().getSystemService("window");
		DisplayMetrics localDisplayMetrics = new DisplayMetrics();
		localWindowManager.getDefaultDisplay().getMetrics(localDisplayMetrics);
		return localDisplayMetrics.heightPixels;
	}

	public LayoutParams generateLayoutParams(AttributeSet paramAttributeSet) {
		return new LayoutParams(getContext(), paramAttributeSet);
	}

	protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
		this.mPercentLayoutHelper.restoreOriginalParams();
	}

	protected void onMeasure(int paramInt1, int paramInt2) {
		int i = View.MeasureSpec.getSize(paramInt2);
		int k = View.MeasureSpec.getMode(paramInt2);
		int j = View.MeasureSpec.makeMeasureSpec(i, k);
		int l = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(paramInt1), View.MeasureSpec.getMode(paramInt1));
		i = j;
		if (k == 0) {
			i = j;
			if (getParent() != null) {
				i = j;
				if (getParent() instanceof ScrollView) {
					Context localContext = getContext();
					if (!(localContext instanceof Activity)){
						i = getScreenHeight();
					}
					i = ((Activity) localContext).findViewById(16908290).getMeasuredHeight();
				}
			}
		}
		while (true) {
			i = View.MeasureSpec.makeMeasureSpec(i, k);
			this.mPercentLayoutHelper.adjustChildren(l, i);
			super.onMeasure(paramInt1, paramInt2);
			if (this.mPercentLayoutHelper.handleMeasuredStateTooSmall())
				super.onMeasure(paramInt1, paramInt2);
			return;
		}
	}
}