/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

public class DkDualTextItemView extends DkItemView {
	ImageView ivIcon;
	TextView tvTitle;
	TextView tvSummary;

	public DkDualTextItemView(Context context) {
		super(context);
	}

	public DkDualTextItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DkDualTextItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		ivIcon = findViewById(R.id.ivIcon);
		tvTitle = findViewById(R.id.tvText);
		tvSummary = findViewById(R.id.tvSubText);

		// Run marquee for long text
		tvTitle.setSelected(true);
		tvSummary.setSelected(true);

		super.onFinishInflate();
	}
}
