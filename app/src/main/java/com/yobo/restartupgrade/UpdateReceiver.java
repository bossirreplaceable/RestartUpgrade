package com.yobo.restartupgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * 监测是否升级、安装、卸载成功
 */
public class UpdateReceiver extends BroadcastReceiver {


    private String TAG = "UpdateReceiver--------";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.PACKAGE_REPLACED")) {
            Toast.makeText(context, "升级了一个安装包，重新启动此程序", Toast.LENGTH_SHORT).show();
//            startApp(context);
        }

        // 接收安装广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            Log.e(TAG, "安装了:" + packageName + "包名的程序");
//            startApp(context);
        }

        //接收卸载广播
        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            Log.e(TAG, "卸载了:" + packageName + "包名的程序");
        }
    }

    /**
     * 监测到升级后执行app的启动 如果不是silenceInstall()可以注释掉
     */
    public void startApp(Context context) {
        // 打开自身 一般用于软件升级
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // 根据包名打开安装的apk 一般写你新安装的app包名
//        Intent resolveIntent = context.getPackageManager().
//                getLaunchIntentForPackage("com.yobo.restartupgrade");
//        context.startActivity(resolveIntent);
    }
}
