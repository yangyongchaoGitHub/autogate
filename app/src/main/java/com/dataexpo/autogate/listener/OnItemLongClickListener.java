package com.dataexpo.autogate.listener;

import android.view.View;

@FunctionalInterface
public interface OnItemLongClickListener {
    void onLongItemClick(View view, int position);
}
