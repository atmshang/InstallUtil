package com.atmshang.install.library.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

import com.atmshang.install.library.receiver.InstallReceiver;
import com.blankj.utilcode.util.CloseUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InstallUtils {

    private static final List<String> autoRestartList = new CopyOnWriteArrayList<String>() {{
        add("com.tencent.qqmusic");
    }};

    public static boolean needRelaunch(String pkgName) {
        return autoRestartList.contains(pkgName);
    }

    public static void logInfo(String what, String message) {
        Log.i("install_background", what + ":" + message);
    }


    public static int createSession(Context ctx, File apkFile, boolean replace) {
        logInfo("createSession", "apkFile:" + apkFile.getAbsolutePath());
        logInfo("createSession", "apkSize:" + apkFile.length());
        logInfo("createSession", "replace:" + replace);

        PackageInstaller installer = ctx.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                replace ?
                        PackageInstaller.SessionParams.MODE_FULL_INSTALL :
                        PackageInstaller.SessionParams.MODE_INHERIT_EXISTING);
        params.setSize(apkFile.length());

        int sessionId = -1;
        try {
            sessionId = installer.createSession(params);
        } catch (IOException e) {
            e.printStackTrace();
        }

        logInfo("createSession", "sessionId:" + sessionId);
        return sessionId;
    }

    public static boolean copyInstallFile(Context ctx, File apkFile, int sessionId) {
        logInfo("copyInstallFile", "apkFile:" + apkFile.getAbsolutePath());
        logInfo("copyInstallFile", "apkSize:" + apkFile.length());

        boolean success = false;
        InputStream in = null;
        OutputStream out = null;
        PackageInstaller.Session session = null;
        PackageInstaller installer = ctx.getPackageManager().getPackageInstaller();

        try {
            session = installer.openSession(sessionId);
            out = session.openWrite("temp.apk", 0, apkFile.length());
            in = new FileInputStream(apkFile);
            byte[] buffer = new byte[2048];
            while (true) {
                int length = in.read(buffer);
                if (length <= 0) {
                    break;
                }
                out.write(buffer, 0, length);
            }
            session.fsync(out);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(in);
            CloseUtils.closeIOQuietly(out);
            CloseUtils.closeIOQuietly(session);
        }
        logInfo("copyInstallFile", "result:" + success);
        return success;
    }

    public static void execInstall(Context ctx, int sessionId) {
        logInfo("execInstall", "sessionId:" + sessionId);


        PackageInstaller installer = ctx.getPackageManager().getPackageInstaller();
        PackageInstaller.Session session = null;

        try {
            session = installer.openSession(sessionId);
            Intent intent = new Intent(ctx, InstallReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            session.commit(pendingIntent.getIntentSender());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIOQuietly(session);
        }
        logInfo("execInstall", "commissioned");
    }
}
