package com.android.launcher3.qsb;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class QsbContainerView {
    public static final String SEARCH_ENGINE_SETTINGS_KEY = "selected_search_engine";

    @Nullable
    public static String getSearchWidgetPackageName(@NonNull Context context) {
        String providerPkg = Settings.Secure.getString(context.getContentResolver(),
                SEARCH_ENGINE_SETTINGS_KEY);
        if (providerPkg == null) {
            SearchManager searchManager = context.getSystemService(SearchManager.class);
            if (searchManager != null) {
                ComponentName componentName = searchManager.getGlobalSearchActivity();
                if (componentName != null) {
                    providerPkg = componentName.getPackageName();
                }
            }
        }
        return providerPkg;
    }
}
