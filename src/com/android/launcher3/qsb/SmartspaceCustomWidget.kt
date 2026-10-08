/*
 * Copyright (C) 2026 The Android Open Source Project
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

package com.android.launcher3.qsb

import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import com.android.launcher3.dagger.LauncherComponentProvider.appComponent
import com.android.launcher3.widget.LauncherAppWidgetProviderInfo
import com.android.launcher3.widget.custom.CustomWidget

/** The in-process Smartspace widget that sits on the first row of the workspace. */
object SmartspaceCustomWidget : CustomWidget {

    override val id: String = "smartspace-widget"

    override fun updateWidgetInfo(context: Context, info: LauncherAppWidgetProviderInfo) {
        val idp = context.appComponent.idp
        info.widgetFeatures = AppWidgetProviderInfo.WIDGET_FEATURE_HIDE_FROM_PICKER
        info.resizeMode = AppWidgetProviderInfo.RESIZE_NONE
        info.spanX = idp.numColumns
        info.minSpanX = idp.numColumns
        info.maxSpanX = idp.numColumns

        // Taller grids leave less height per row, so give smartspace a second one to draw in.
        val rows = if (idp.numRows > 5) 2 else 1
        info.spanY = rows
        info.minSpanY = rows
        info.maxSpanY = rows
    }

    override fun createView(
        context: Context,
        info: LauncherAppWidgetProviderInfo,
    ): AppWidgetHostView = SmartspaceViewContainer(context)
}
