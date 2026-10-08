package com.android.launcher3.qsb;

import static com.android.launcher3.util.Executors.UI_HELPER_EXECUTOR;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

import com.android.launcher3.celllayout.CellLayoutLayoutParams;

import com.android.launcher3.HalcyonLauncher;
import com.android.launcher3.CustomLauncherModelDelegate;
import com.android.launcher3.R;
import com.android.launcher3.icons.GraphicsUtils;
import com.android.launcher3.util.PluginManagerWrapper;
import com.android.launcher3.views.ActivityContext;
import com.android.launcher3.widget.LauncherAppWidgetHostView;

import com.android.systemui.plugins.BcSmartspaceDataPlugin;
import com.android.systemui.plugins.PluginListener;

import com.google.android.systemui.smartspace.BcSmartspaceDataProvider;
import com.google.android.systemui.smartspace.BcSmartspaceView;

/** Hosts the Smartspace view on the workspace, as a {@link SmartspaceCustomWidget}. */
public class SmartspaceViewContainer extends LauncherAppWidgetHostView
        implements PluginListener<BcSmartspaceDataPlugin> {

    public BcSmartspaceView mView;
    private final BcSmartspaceDataProvider mPlugin;

    public SmartspaceViewContainer(Context context) {
        super(context);

        mView = (BcSmartspaceView) inflate(context, R.layout.smartspace_enhanced2, null);
        // Attaching makes binder calls the view refuses to run on the main thread.
        mView.setBgHandler(UI_HELPER_EXECUTOR.getHandler());
        // onFinishInflate only sets this on the adapter it uses to pre-inflate a card.
        mView.setUiSurface(BcSmartspaceDataPlugin.UI_SURFACE_HOME_SCREEN);
        mView.setPrimaryTextColor(GraphicsUtils.getAttrColor(context, R.attr.workspaceTextColor));
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.setMarginStart(getResources()
                .getDimensionPixelSize(R.dimen.enhanced_smartspace_margin_start_launcher));
        addView(mView, layoutParams);

        // The preview renderer inflates the workspace outside of Launcher, so there is nothing to
        // feed targets or the unlock animation; the view still draws its default date card.
        Context activityContext = ActivityContext.lookupContext(context);
        if (activityContext instanceof HalcyonLauncher launcher) {
            mPlugin = launcher.getSmartspacePlugin();
            launcher.getLauncherUnlockAnimationController().setSmartspaceView(mView);

            CustomLauncherModelDelegate delegate =
                    (CustomLauncherModelDelegate) launcher.getModel().getModelDelegate();
            mPlugin.setEventDispatcher(event -> delegate.notifySmartspaceEvent(event));
        } else {
            mPlugin = new BcSmartspaceDataProvider();
        }
        mView.registerDataProvider(mPlugin);
}

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && mView.mAdapter.getItemCount() > 1) {
            // The host only auto-claims scrollable widgets, and that check knows AdapterView alone.
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onLongClick(View view) {
        // Fixed part of the home screen, not a widget the user placed; the switch hides it instead.
        return false;
    }

    @Override
    public void onPluginConnected(BcSmartspaceDataPlugin plugin, Context context) {
        mView.registerDataProvider(plugin);
    }

    @Override
    public void onPluginDisconnected(BcSmartspaceDataPlugin plugin) {
        mView.registerDataProvider(mPlugin);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (params instanceof CellLayoutLayoutParams lp) {
            lp.canReorder = false;
        }
        super.setLayoutParams(params);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (getLayoutParams() instanceof CellLayoutLayoutParams lp) {
            lp.canReorder = false;
        }
        PluginManagerWrapper.INSTANCE.get(getContext())
                .addPluginListener(this, BcSmartspaceDataPlugin.class);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        PluginManagerWrapper.INSTANCE.get(getContext()).removePluginListener(this);
    }
}
