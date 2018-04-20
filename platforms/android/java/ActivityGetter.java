package org.apache.cordova;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.content.pm.PackageManager;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityGetter implements CordovaInterface {
	protected ExecutorService threadPool;
	protected CallbackMap permissionResultCallbacks;

	public ActivityGetter() {
		this.threadPool = Executors.newCachedThreadPool();
		this.permissionResultCallbacks = new CallbackMap();
	}

	public Activity getActivity() {
		try {
			Class activityThreadClass = Class.forName("android.app.ActivityThread");
			Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
			Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
			activitiesField.setAccessible(true);

			Map<Object, Object> activities = (Map<Object, Object>) activitiesField.get(activityThread);
			if (activities == null)
				return null;

			for (Object activityRecord : activities.values()) {
				Class activityRecordClass = activityRecord.getClass();
				Field pausedField = activityRecordClass.getDeclaredField("paused");
				pausedField.setAccessible(true);
				if (!pausedField.getBoolean(activityRecord)) {
					Field activityField = activityRecordClass.getDeclaredField("activity");
					activityField.setAccessible(true);
					Activity activity = (Activity) activityField.get(activityRecord);
					return activity;
				}
			}
		} catch (Exception e) {

		}

		return null;
	}

	@Override
	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public void requestPermissions(CordovaPlugin plugin, int requestCode, String [] permissions) {
		int mappedRequestCode = permissionResultCallbacks.registerCallback(plugin, requestCode);
		getActivity().requestPermissions(permissions, mappedRequestCode);
	}

	public void startActivityForResult(Object command, Intent intent, int requestCode) {
		try {
			this.getActivity().startActivityForResult(intent, requestCode);
		} catch (Exception e) {
		}
	}

	public boolean hasPermission(String permission)
	{
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
		{
			int result = this.getActivity().checkSelfPermission(permission);
			return PackageManager.PERMISSION_GRANTED == result;
		}
		else
		{
			return true;
		}
	}
}