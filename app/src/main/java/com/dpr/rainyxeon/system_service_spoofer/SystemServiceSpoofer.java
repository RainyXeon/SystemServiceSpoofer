package com.dpr.rainyxeon.system_service_spoofer;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.util.List;
import java.util.ArrayList;

public class SystemServiceSpoofer implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!"android".equals(lpparam.packageName)) return;

        try {
            hookListService();
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

    private void hookListService() {
        Class<?> smClass = XposedHelpers.findClass(
            "android.os.ServiceManager",
            ClassLoader.getSystemClassLoader()
        );
        XposedBridge.hookAllMethods(smClass, "listServices", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                String[] services = (String[]) param.getResult();
                if (services == null) return;
                List<String> filtered = new ArrayList<>();
                for (String s : services) {
                    if (s != null && !s.contains("lineage")) {
                        XposedBridge.log("[ServiceSpoofer] Service filtered: " + s);
                        filtered.add(s);
                    }
                }
                param.setResult(filtered.toArray(new String[0]));
                XposedBridge.log("[ServiceSpoofer] Total services filtered: " + filtered.size());
            }
        });
    }
}
