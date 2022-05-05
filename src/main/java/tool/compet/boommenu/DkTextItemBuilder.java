/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.graphics.Color;

/**
 * This item contains ONE icon and ONE text.
 * Call `setStyle()` to customize position between text and icon.
 */
public class DkTextItemBuilder extends DkItemBuilder<DkTextItemBuilder> {
	public static final int STYLE_TEXT_INSIDE_ICON = 1; // text at bottom and inside of icon
	public static final int STYLE_TEXT_LEFT_OUTSIDE_ICON = 2; // text at left and outside of icon
	public static final int STYLE_TEXT_RIGHT_OUTSIDE_ICON = 3; // text at right and outside of icon
	public static final int STYLE_TEXT_TOP_OUTSIDE_ICON = 4; // text at top and outside of icon
	public static final int STYLE_TEXT_BOTTOM_OUTSIDE_ICON = 5; // text at bottom and outside of icon

	protected int style = STYLE_TEXT_RIGHT_OUTSIDE_ICON;

	protected int iconRes;
	protected int iconBorderColor = Color.TRANSPARENT;
	protected int iconBorderThickness = Integer.MIN_VALUE;
	protected int textRes;

	@Override
	protected DkItemView getView(Context context) {
		DkTextItemView itemView;
		int layoutRes;

		if (style == STYLE_TEXT_INSIDE_ICON) {
			layoutRes = R.layout.dk_boommenu_item_text_inside;
		}
		else if (style == STYLE_TEXT_LEFT_OUTSIDE_ICON) {
			layoutRes = R.layout.dk_boommenu_item_text_left;
		}
		else if (style == STYLE_TEXT_RIGHT_OUTSIDE_ICON) {
			layoutRes = R.layout.dk_boommenu_item_text_right;
		}
		else if (style == STYLE_TEXT_TOP_OUTSIDE_ICON) {
			layoutRes = R.layout.dk_boommenu_item_text_top;
		}
		else if (style == STYLE_TEXT_BOTTOM_OUTSIDE_ICON) {
			layoutRes = R.layout.dk_boommenu_item_text_bottom;
		}
		else {
			throw new RuntimeException("Must provide valid style");
		}

		itemView = super.initView(context, layoutRes);

		if (iconRes > 0) {
			itemView.ivIcon.setImageResource(iconRes);
		}
		if (textRes > 0) {
			itemView.tvText.setText(textRes);
		}

		return itemView;
	}

	public DkTextItemBuilder setStyle(int style) {
		this.style = style;
		return this;
	}

	public DkTextItemBuilder setIcon(int iconRes) {
		this.iconRes = iconRes;
		return this;
	}

	public DkTextItemBuilder setText(int textRes) {
		this.textRes = textRes;
		return this;
	}
}
