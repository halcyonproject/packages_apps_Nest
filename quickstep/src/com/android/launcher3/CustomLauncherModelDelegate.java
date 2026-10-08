package com.android.launcher3;

import android.app.smartspace.SmartspaceConfig;
import android.app.smartspace.SmartspaceManager;
import android.app.smartspace.SmartspaceSession;
import android.app.smartspace.SmartspaceTarget;
import android.app.smartspace.SmartspaceTargetEvent;
import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.launcher3.LauncherSettings.Favorites;
import com.android.launcher3.R;
import com.android.launcher3.dagger.ApplicationContext;
import com.android.launcher3.model.AllAppsList;
import com.android.launcher3.model.BgDataModel;
import com.android.launcher3.model.ModelTaskController;
import com.android.launcher3.model.PredictedItemFactory;
import com.android.launcher3.model.QuickstepModelDelegate;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.pm.UserCache;
import com.android.launcher3.util.Executors;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

public class CustomLauncherModelDelegate extends QuickstepModelDelegate
    implements SmartspaceSession.OnTargetsAvailableListener {

    public static final String TAG = "CustomLauncherModelDelegate";

    /** Below {@link Favorites#EXTENDED_CONTAINERS}; -110 is CONTAINER_PRIVATESPACE upstream now. */
    public static final int CONTAINER_SMARTSPACE = Favorites.EXTENDED_CONTAINERS - 1;

    private static final int MAX_RETAINED_TARGET_SETS = 5;

    public final Context mContext;
    public final Deque mSmartspaceTargets = new LinkedList<List>();

    public SmartspaceSession mSmartspaceSession;

    @Inject
    public CustomLauncherModelDelegate(@ApplicationContext Context context,
            InvariantDeviceProfile idp,
            UserCache userCache,
            PredictedItemFactory.Factory itemParserFactory,
            @Nullable @Named("ICONS_DB") String dbFileName) {
        super(context, idp, userCache, itemParserFactory, dbFileName);
        mContext = context;
    }

    @Override
    public void destroy() {
        super.destroy();
        destroySmartspaceSession();
    }

    public final void destroySmartspaceSession() {
        if (mSmartspaceSession != null) {
            mSmartspaceSession.close();
            mSmartspaceSession = null;
        }
    }

    public void onTargetsAvailable(List<SmartspaceTarget> targets) {
        List<SmartspaceTarget> list = targets.stream()
                                             // Holiday alarms are shown by the clock instead of here.
                                             .filter(t -> t.getFeatureType()
                                                     != SmartspaceTarget.FEATURE_HOLIDAY_ALARM)
                                             .collect(Collectors.toList());
        mSmartspaceTargets.offerLast(list);
        if (mSmartspaceTargets.size() > MAX_RETAINED_TARGET_SETS) {
            mSmartspaceTargets.pollFirst();
        }
        mModel.enqueueModelUpdateTask((taskController, dataModel, apps) -> {
            List<ItemInfo> items = new ArrayList<>(mSmartspaceTargets.size());
            for (SmartspaceTarget target : list) {
                SmartspaceItem item = new SmartspaceItem();
                item.setSmartspaceTarget(target);
                item.container = CONTAINER_SMARTSPACE;
                item.itemType = Favorites.ITEM_TYPE_QSB;
                items.add(item);
            }
            BgDataModel.FixedContainerItems container =
                    new BgDataModel.FixedContainerItems(CONTAINER_SMARTSPACE, items);
            taskController.bindExtraContainerItems(container);
        });
    }

    public void notifySmartspaceEvent(SmartspaceTargetEvent event) {
        mModel.enqueueModelUpdateTask((taskController, dataModel, apps) -> {
            if (mSmartspaceSession != null) {
                mSmartspaceSession.notifySmartspaceEvent(event);
            }
        });
    }

    @Override
    public void validateData() {
        super.validateData();
        if (mSmartspaceSession != null) {
            mSmartspaceSession.requestSmartspaceUpdate();
        }
    }

    @Override
    public void workspaceLoadComplete() {
        super.workspaceLoadComplete();
        destroySmartspaceSession();
        if (!mActive) {
            return;
        }
        Log.d(TAG, "Starting smartspace session for home");

        SmartspaceManager smartspaceManager = (SmartspaceManager) mContext.getSystemService(SmartspaceManager.class);
        if (smartspaceManager != null) {
            mSmartspaceSession = smartspaceManager.createSmartspaceSession(new SmartspaceConfig.Builder(mContext, "home").build());
            mSmartspaceSession.addOnTargetsAvailableListener(Executors.MODEL_EXECUTOR, this);
            mSmartspaceSession.requestSmartspaceUpdate();
        } else {
            Log.e(TAG, "SmartspaceManager is null, cannot create SmartspaceSession");
        }
    }

    public class SmartspaceItem extends ItemInfo {
        public SmartspaceTarget mSmartspaceTarget;

        public SmartspaceTarget getSmartspaceTarget() {
            return mSmartspaceTarget;
        }

        public void setSmartspaceTarget(SmartspaceTarget target) {
            mSmartspaceTarget = target;
        }
    }
}
