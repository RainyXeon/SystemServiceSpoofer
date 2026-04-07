package com.dpr.rainyxeon.system_service_spoofer;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SystemServiceSpoofer implements IXposedHookLoadPackage {

    private static final String TAG = "[ServiceSpoofer] ";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(TAG + "Loaded: " + lpparam.packageName);

        try {
            hookServiceManager(lpparam);
            hookRuntimeExec(lpparam);
        } catch (Throwable t) {
            XposedBridge.log(TAG + "ERROR:");
            XposedBridge.log(t);
        }
    }

    // ServiceManager hooks
    private void hookServiceManager(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> smClass = XposedHelpers.findClass(
                "android.os.ServiceManager",
                lpparam.classLoader
            );

            XposedBridge.log(TAG + "ServiceManager hooked in: " + lpparam.packageName);

            // listServices
            XposedBridge.hookAllMethods(smClass, "listServices", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Object result = param.getResult();

                    if (result instanceof String[]) {
                        String[] services = (String[]) result;

                        String[] filtered = Arrays.stream(services)
                            .filter(s -> s != null && !s.toLowerCase().contains("lineage"))
                            .toArray(String[]::new);

                        param.setResult(filtered);
                    }
                }
            });

            // getService
            XposedBridge.hookAllMethods(smClass, "getService", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String name = (String) param.args[0];

                    if (name != null && name.toLowerCase().contains("lineage")) {
                        XposedBridge.log(TAG + "Blocked getService(): " + name);
                        param.setResult(null);
                    }
                }
            });

            // checkService
            XposedBridge.hookAllMethods(smClass, "checkService", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String name = (String) param.args[0];

                    if (name != null && name.toLowerCase().contains("lineage")) {
                        XposedBridge.log(TAG + "Blocked checkService(): " + name);
                        param.setResult(null);
                    }
                }
            });

        } catch (Throwable t) {
            XposedBridge.log(TAG + "ServiceManager hook failed in: " + lpparam.packageName);
        }
    }

    // Shell detection hook
    private void hookRuntimeExec(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "java.lang.Runtime",
                lpparam.classLoader,
                "exec",
                String.class,
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String cmd = (String) param.args[0];

                        XposedBridge.log(TAG + "Runtime.exec(): " + cmd);

                        if (cmd != null && cmd.contains("service")) {
                            XposedBridge.log(TAG + "⚠️ Possible service detection via shell!");
                        }
                    }
                }
            );

        } catch (Throwable t) {
            XposedBridge.log(TAG + "Runtime.exec hook failed in: " + lpparam.packageName);
        }
    }
}
