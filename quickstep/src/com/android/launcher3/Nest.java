package com.android.launcher3;

import android.app.smartspace.SmartspaceTarget;
import android.os.Bundle;

import com.android.launcher3.NestModelDelegate.SmartspaceItem;

import com.android.launcher3.model.BgDataModel;
import com.android.launcher3.qsb.LauncherUnlockAnimationController;
import com.android.launcher3.uioverrides.QuickstepLauncher;
import com.android.quickstep.SystemUiProxy;

import com.google.android.systemui.smartspace.BcSmartspaceDataProvider;

import java.util.List;
import java.util.stream.Collectors;

public class Nest extends QuickstepLauncher {
    private BcSmartspaceDataProvider mSmartspacePlugin = new BcSmartspaceDataProvider();
    private LauncherUnlockAnimationController mUnlockAnimationController =
            new LauncherUnlockAnimationController(this);

    public BcSmartspaceDataProvider getSmartspacePlugin() {
        return mSmartspacePlugin;
    }

    public LauncherUnlockAnimationController getLauncherUnlockAnimationController() {
        return mUnlockAnimationController;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        SystemUiProxy.INSTANCE.get(this).setLauncherUnlockAnimationController(this.getClass().getSimpleName(), mUnlockAnimationController);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SystemUiProxy.INSTANCE.get(this).setLauncherUnlockAnimationController("null", null);
    }

    @Override
    public void onOverlayVisibilityChanged(boolean visible) {
        super.onOverlayVisibilityChanged(visible);
        mUnlockAnimationController.updateSmartspaceState();
    }

    @Override
    public void onPageEndTransition() {
        super.onPageEndTransition();
        mUnlockAnimationController.updateSmartspaceState();
    }

    @Override
    public void bindExtraContainerItems(BgDataModel.FixedContainerItems container) {
        if (container.containerId == -110) {
            List<SmartspaceTarget> targets = container.items.stream()
                                                            .map(item -> ((SmartspaceItem) item).getSmartspaceTarget())
                                                            .collect(Collectors.toList());
            mSmartspacePlugin.onTargetsAvailable(targets);
        }
        super.bindExtraContainerItems(container);
    }
}
