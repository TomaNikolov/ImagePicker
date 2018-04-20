package org.apache.cordova;

import android.util.Log;
import android.content.Intent;
import java.lang.reflect.Method;
import org.json.JSONArray;

public class PluginManager {
	private CallbackContext pluginManagerCallback;
	private Object instance;

	public PluginManager(CallbackContext pluginManagerCallback) {
		this.pluginManagerCallback = pluginManagerCallback;
		Log.w("PluginManager", "Instantiated PluginManager");
	}

	public void exec(String serviceName, String actionName, JSONArray actionArgs) throws Exception {
		Log.w("PluginManager", "PluginManager exec called");
		Class<?> c = Class.forName("com.synconset." + serviceName);
		Object instance = c.newInstance();
		this.instance = instance;
		Method method = instance.getClass().getMethod("execute", String.class, JSONArray.class, CallbackContext.class);
		method.invoke(instance, actionName, actionArgs, this.pluginManagerCallback);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) throws Exception {
		Method method = this.instance.getClass().getMethod("onActivityResult", Integer.class, Integer.class, Intent.class);
		method.invoke(instance, requestCode, resultCode, data);
	}
}