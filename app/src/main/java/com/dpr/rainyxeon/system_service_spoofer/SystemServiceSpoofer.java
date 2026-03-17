package com.dpr.rainyxeon.system_service_spoofer;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class SystemServiceSpoofer implements IXposedHookZygoteInit {

    private static final String TAG = "[ServiceSpoofer] ";

    @Override
    public void initZygote(StartupParam startupParam) {
        XposedBridge.log(TAG + "Zygote init start");

        try {
            Class<?> smClass = XposedHelpers.findClass(
                "android.os.ServiceManager",
                null
            );

            XposedBridge.log(TAG + "ServiceManager class: " + smClass);

            hookListServices(smClass);
            hookGetService(smClass);
            hookCheckService(smClass);

        } catch (Throwable t) {
            XposedBridge.log(TAG + "Zygote ERROR:");
            XposedBridge.log(t);
        }
    }

    private void hookListServices(Class<?> smClass) {
        XposedBridge.hookAllMethods(smClass, "listServices", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                XposedBridge.log(TAG + "listServices() called");
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedBridge.log(TAG + "listServices() AFTER");

                String[] services = (String[]) param.getResult();

                if (services == null) {
                    XposedBridge.log(TAG + "services == null");
                    return;
                }

                XposedBridge.log(TAG + "Original size: " + services.length);

                List<String> filtered = new ArrayList<>();

                for (String s : services) {
                    XposedBridge.log(TAG + "Service: " + s);

                    if (s != null && !s.contains("lineage")) {
                        filtered.add(s);
                    } else {
                        XposedBridge.log(TAG + "Filtered OUT: " + s);
                    }
                }

                param.setResult(filtered.toArray(new String[0]));
                XposedBridge.log(TAG + "Filtered size: " + filtered.size());
            }
        });

        XposedBridge.log(TAG + "listServices hook installed");
    }

    private void hookGetService(Class<?> smClass) {
        XposedBridge.hookAllMethods(smClass, "getService", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String name = (String) param.args[0];
                XposedBridge.log(TAG + "getService(): " + name);

                if (name != null && (name.contains("lineage") || name.contains("profile"))) {
                    XposedBridge.log(TAG + "Blocking getService: " + name);
                    param.setResult(null);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                String name = (String) param.args[0];
                Object result = param.getResult();

                XposedBridge.log(TAG + "getService result [" + name + "]: " + result);
            }
        });

        XposedBridge.log(TAG + "getService hook installed");
    }

    private void hookCheckService(Class<?> smClass) {
        XposedBridge.hookAllMethods(smClass, "checkService", new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                String name = (String) param.args[0];
                XposedBridge.log(TAG + "checkService(): " + name);

                if (name != null && name.contains("lineage")) {
                    XposedBridge.log(TAG + "Blocking checkService: " + name);
                    param.setResult(null);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                String name = (String) param.args[0];
                Object result = param.getResult();

                XposedBridge.log(TAG + "checkService result [" + name + "]: " + result);
            }
        });

        XposedBridge.log(TAG + "checkService hook installed");
    }
}
