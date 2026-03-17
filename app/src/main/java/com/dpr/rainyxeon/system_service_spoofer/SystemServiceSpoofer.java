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
            hookBinder(lpparam);
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
                protected void beforeHookedMethod(MethodHookParam param) {
                    XposedBridge.log(TAG + "listServices() called");
                }
            });

            // getService
            XposedBridge.hookAllMethods(smClass, "getService", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String name = (String) param.args[0];
                    XposedBridge.log(TAG + "getService(): " + name);
                }
            });

            // checkService
            XposedBridge.hookAllMethods(smClass, "checkService", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    String name = (String) param.args[0];
                    XposedBridge.log(TAG + "checkService(): " + name);
                }
            });

        } catch (Throwable t) {
            XposedBridge.log(TAG + "ServiceManager hook failed in: " + lpparam.packageName);
        }
    }

    // Binder layer hook
    private void hookBinder(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(
                "android.os.BinderProxy",
                lpparam.classLoader,
                "transact",
                int.class,
                android.os.Parcel.class,
                android.os.Parcel.class,
                int.class,
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int code = (int) param.args[0];

                        XposedBridge.log(TAG + "Binder.transact code=" + code +
                                " | process=" + lpparam.packageName);
                    }
                }
            );

            XposedBridge.log(TAG + "Binder hook installed in: " + lpparam.packageName);

        } catch (Throwable t) {
            XposedBridge.log(TAG + "Binder hook failed in: " + lpparam.packageName);
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
