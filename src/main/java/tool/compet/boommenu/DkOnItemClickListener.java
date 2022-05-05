/*
 * Copyright (c) 2017-2021 DarkCompet. All rights reserved.
 */

package tool.compet.boommenu;

import android.view.View;

public interface DkOnItemClickListener {
	/**
	 * Called when an item was clicked.
	 *
	 * @param itemView The item view.
	 * @param index    The position of the item in item list.
	 */
	void onClick(View itemView, int index);
}
