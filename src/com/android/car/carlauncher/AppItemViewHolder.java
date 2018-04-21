/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.car.carlauncher;

import android.annotation.Nullable;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * App item view holder that contains the app icon and name.
 */
public class AppItemViewHolder extends RecyclerView.ViewHolder {
    private final Context mContext;
    public View mAppItem;
    public ImageView mAppIconView;
    public TextView mAppNameView;

    public AppItemViewHolder(View view, Context context) {
        super(view);
        mContext = context;
        mAppItem = view.findViewById(R.id.app_item);
        mAppIconView = mAppItem.findViewById(R.id.app_icon);
        mAppNameView = mAppItem.findViewById(R.id.app_name);
    }

    /**
     * Binds the grid app item view with the app meta data.
     *
     * @param app Pass {@code null} will empty out the view.
     */
    public void bind(@Nullable AppMetaData app) {
        // Empty out the view
        mAppItem.setClickable(false);
        mAppItem.setOnClickListener(null);
        mAppIconView.setBackground(null);
        mAppNameView.setText(null);

        if (app == null) {
            return;
        }

        mAppItem.setOnClickListener(v -> AppLauncherUtils.launchApp(mContext, app));
        mAppIconView.setBackground(app.getIcon());
        mAppNameView.setText(app.getDisplayName());
    }
}
