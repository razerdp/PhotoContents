package razerdp.github.com.demo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import java.lang.ref.WeakReference;

public class AppContext {
    private static final String TAG = "AppContext";
    public static Application sApplication;
    private static final InnerLifecycleHandler INNER_LIFECYCLE_HANDLER;

    static {
        Application app = null;
        try {
            app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            if (app == null)
                throw new IllegalStateException("Static initialization of Applications must be on main thread.");
        } catch (final Exception e) {
            Log.e(TAG, "Failed to get current application from AppGlobals." + e.getMessage());
            try {
                app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (final Exception ex) {
                Log.e(TAG, "Failed to get current application from ActivityThread." + e.getMessage());
            }
        } finally {
            sApplication = app;
        }
        INNER_LIFECYCLE_HANDLER = new InnerLifecycleHandler();
        if (sApplication != null) {
            sApplication.registerActivityLifecycleCallbacks(INNER_LIFECYCLE_HANDLER);
        }
    }

    public static boolean isAppVisable() {
        return INNER_LIFECYCLE_HANDLER != null && INNER_LIFECYCLE_HANDLER.started > INNER_LIFECYCLE_HANDLER.stopped;
    }

    public static boolean isAppBackground() {
        return INNER_LIFECYCLE_HANDLER != null && INNER_LIFECYCLE_HANDLER.resumed <= INNER_LIFECYCLE_HANDLER.stopped;
    }

    private static void checkAppContext() {
        if (sApplication == null) {
            reflectAppContext();
        }
        if (sApplication == null) {
            throw new IllegalStateException("app reference is null");
        }
    }

    private static void reflectAppContext() {
        Application app = null;
        try {
            app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            if (app == null)
                throw new IllegalStateException("Static initialization of Applications must be on main thread.");
        } catch (final Exception e) {
            Log.e(TAG, "Failed to get current application from AppGlobals." + e.getMessage());
            try {
                app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (final Exception ex) {
                Log.e(TAG, "Failed to get current application from ActivityThread." + e.getMessage());
            }
        } finally {
            sApplication = app;
        }
        if (sApplication != null && INNER_LIFECYCLE_HANDLER != null) {
            sApplication.registerActivityLifecycleCallbacks(INNER_LIFECYCLE_HANDLER);
        }
    }

    public static Application getAppInstance() {
        checkAppContext();
        return sApplication;
    }

    public static Context getAppContext() {
        checkAppContext();
        return sApplication.getApplicationContext();
    }

    public static Resources getResources() {
        checkAppContext();
        return sApplication.getResources();
    }

    public static Activity getTopActivity() {
        return INNER_LIFECYCLE_HANDLER.mTopActivity == null ? null : INNER_LIFECYCLE_HANDLER.mTopActivity.get();
    }

    private static class InnerLifecycleHandler implements Application.ActivityLifecycleCallbacks {
        private int created;
        private int resumed;
        private int paused;
        private int started;
        private int stopped;
        private WeakReference<Activity> mTopActivity;


        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            ++created;

        }

        @Override
        public void onActivityStarted(Activity activity) {
            ++started;

        }

        @Override
        public void onActivityResumed(Activity activity) {
            ++resumed;
            mTopActivity = new WeakReference<>(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            ++paused;

        }

        @Override
        public void onActivityStopped(Activity activity) {
            ++stopped;

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

}
