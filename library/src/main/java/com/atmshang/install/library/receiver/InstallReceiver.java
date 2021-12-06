package com.atmshang.install.library.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInstaller;
import android.os.Process;

import com.atmshang.install.library.utils.InstallUtils;

import java.util.List;

public class InstallReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null) {
            return;
        }
        if (intent == null) {
            return;
        }
        final int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE);
        final String pkgName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME);
        InstallUtils.logInfo("installResult", "pkgName:" + pkgName);

        if (InstallUtils.needRelaunch(pkgName)) {
            try {
                LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                List<LauncherActivityInfo> launcherActivityInfoList = launcherApps.getActivityList(pkgName, Process.myUserHandle());
                for (LauncherActivityInfo launcherActivityInfo : launcherActivityInfoList) {
                    launcherApps.startMainActivity(launcherActivityInfo.getComponentName(), Process.myUserHandle(), null, null);
                }
                List<LauncherActivityInfo> launcherActivityInfoList2 = launcherApps.getActivityList("cn.com.techvision.managertest", Process.myUserHandle());
                for (LauncherActivityInfo launcherActivityInfo : launcherActivityInfoList2) {
                    launcherApps.startMainActivity(launcherActivityInfo.getComponentName(), Process.myUserHandle(), null, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (status == PackageInstaller.STATUS_SUCCESS) {
            InstallUtils.logInfo("installResult", "success");
            String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (!message.isEmpty()) {
                InstallUtils.logInfo("installResult", "message:" + message);
            }
        } else {
            InstallUtils.logInfo("installResult", "not success");

            String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            if (!message.isEmpty()) {
                InstallUtils.logInfo("installResult", "message:" + message);
            }
        }
    }
}
