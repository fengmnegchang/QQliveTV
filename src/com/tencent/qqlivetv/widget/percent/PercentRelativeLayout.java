/**
 *****************************************************************************************************************************************************************************
 * 
 * @author :fengguangjing
 * @createTime:2016-12-21下午3:01:15
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
 * @createTime:2016-12-21下午3:01:15
 * @version:4.2.4
 * @modifyTime:
 * @modifyAuthor:
 * @description:
 *****************************************************************************************************************************************************************************
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class PercentRelativeLayout extends RelativeLayout {
	private final OpenPercentLayoutHelper mHelper = new OpenPercentLayoutHelper(this);

	public PercentRelativeLayout(Context paramContext) {
		super(paramContext);
	}

	public PercentRelativeLayout(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
	}

	public PercentRelativeLayout(Context paramContext, AttributeSet paramAttributeSet, int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
	}

	public LayoutParams generateLayoutParams(AttributeSet paramAttributeSet) {
		return new LayoutParams(getContext(), paramAttributeSet);
	}

	protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
		super.onLayout(paramBoolean, paramInt1, paramInt2, paramInt3, paramInt4);
		this.mHelper.restoreOriginalParams();
	}

	protected void onMeasure(int paramInt1, int paramInt2) {
		this.mHelper.adjustChildren(paramInt1, paramInt2);
		super.onMeasure(paramInt1, paramInt2);
		if (this.mHelper.handleMeasuredStateTooSmall())
			super.onMeasure(paramInt1, paramInt2);
	}
}