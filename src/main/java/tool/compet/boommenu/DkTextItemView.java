/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.TextView;

public class DkTextItemView extends DkItemView {
	ImageView ivIcon;
	TextView tvText;

	public DkTextItemView(Context context) {
		super(context);
	}

	public DkTextItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DkTextItemView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onFinishInflate() {
		ivIcon = findViewById(R.id.ivIcon);
		tvText = findViewById(R.id.tvText);

		tvText.setSelected(true);

		super.onFinishInflate();
	}
}
