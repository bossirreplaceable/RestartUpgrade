package com.yobo.restartupgrade;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main--------";
    private Context mContext = MainActivity.this;
    //    private String updateUrl = "http://192.168.1.24/test/testapk.apk";
//    private String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String tempPath = "system/app-debug.apk";
    private List<ApplicationInfo> mListAppcations;
    private PackageManager mPackageManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPackageManager = getPackageManager();
        mListAppcations = mPackageManager.getInstalledApplications
                (PackageManager.GET_UNINSTALLED_PACKAGES);

        // 判断是否有Root权限
        if (RootUtils.isRooted()) {
            Log.e(TAG, "系统有Root权限");
            // 根据包名判断软件是否安装
            if (isInstall("com.yobo.restartupgrade")) {
                // 有安装
                Log.i(TAG, "有安装");
            } else {
                // 没有安装
                Log.i(TAG, "没有安装");
                // 从assets目录中获取并安装
//                installFromAssets();
            }
            // 本地版本号如果小于服务器端版本号 此部分逻辑参考
            // 从网络中下载并安装
//            installFromNet();

        }
    }


    public void clickMe(View v ) {
        silenceInstall();
    }

//    /**
//     * 从assets目录中获取并安装
//     */
//    public void installFromAssets(){
//        // 传统安装 并启动
////        normalInstall(mContext);
//
//        // 静默升级 并启动
//      silenceInstall();
//    }

//    /**
//     * 从网络中下载并安装
//     */
//    public void installFromNet(){
//        // 判断包名是否相同  再判断版本号，如果本地版本号小于服务器端软件版本号就升级
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                // 从Url中下载  下载至 Environment.getExternalStorageDirectory() 目录的update.apk文件 并安装
//                install();
//            }
//        }).start();
//    }

    /**
     * 从Url中下载  下载至 Environment.getExternalStorageDirectory() 目录的update.apk文件 并安装
     */
//    private void install() {
//        // 传统安装 并启动
////        normalInstall(mContext);
//
//        // 静默安装 并启动
//        silenceInstall();
//    }


//    /**
//     * 传统安装
//     *
//     * @param context
//     */
//    public void normalInstall(Context context) {
//        // 进行资源的转移 将assets下的文件转移到可读写文件目录下
//        // installFromAssets() 时取消注释
////        createFile();
////        File file = new File(tempPath);
//
//        // installFromNet() 时取消注释
//        File file = downLoadFile(updateUrl, rootPath);
//        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        intent.setAction(Intent.ACTION_VIEW);
//        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
//        context.startActivity(intent);
//        startApp(context);
//    }

    /**
     * 安装后自启动apk
     *
     * @param context
     */
    private static void startApp(Context context) {
        execRootShellCmd("am start -S  " + context.getPackageName() + "/"
                + MainActivity.class.getCanonicalName() + " \n");
    }

    /**
     * 执行shell命令
     *
     * @param cmds
     * @return
     */
    private static boolean execRootShellCmd(String... cmds) {
        if (cmds == null || cmds.length == 0) {
            return false;
        }
        DataOutputStream dos = null;
        InputStream dis = null;
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");// 经过Root处理的android系统即有su命令
            dos = new DataOutputStream(p.getOutputStream());

            for (int i = 0; i < cmds.length; i++) {
                dos.writeBytes(cmds[i] + " \n");
            }
            dos.writeBytes("exit \n");

            int code = p.waitFor();

            return code == 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (dos != null) {
                    dos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            try {
                if (p != null) {
                    p.destroy();
                    p = null;
                }
            } catch (Exception e3) {
                e3.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 静默安装 并启动
     *
     * @return
     */
    public boolean silenceInstall() {
        // 进行资源的转移 将assets下的文件转移到可读写文件目录下
        // installFromAssets() 时取消注释
//        createFile();
//        File file = new File(tempPath);

        // installFromNet() 时取消注释
        File file = new File(tempPath);
        boolean result = false;
        Process process = null;
        OutputStream out = null;
        Log.i(TAG, "file.getPath()：" + file.getPath());
        if (file.exists()) {
            System.out.println(file.getPath() + "==");
            try {
                process = Runtime.getRuntime().exec("su");
                out = process.getOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(out);
                // 获取文件所有权限
                dataOutputStream.writeBytes("chmod 777 " + file.getPath()
                        + "\n");
                // 进行静默安装命令
                dataOutputStream
                        .writeBytes("LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
                                + file.getPath());
                dataOutputStream.flush();
                // 关闭流操作
                dataOutputStream.close();
                out.close();
                int value = process.waitFor();

                // 代表成功
                if (value == 0) {
                    Log.i(TAG, "安装成功！");
                    result = true;
                    // 失败
                } else if (value == 1) {
                    Log.i(TAG, "安装失败！");
                    result = false;
                    // 未知情况
                } else {
                    Log.i(TAG, "未知情况！");
                    result = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {

//                startApp(this);
            }
            if (!result) {
                Log.i(TAG, "root权限获取失败，将进行普通安装");
//                normalInstall(mContext);
                result = true;
            }
        }
        return result;
    }


    /**
     * 判断软件是否安装
     *
     * @return
     */
    public boolean isInstall(String name) {
        for (ApplicationInfo info : mListAppcations) {
            Log.e(TAG, "getPackagename: ---" + info.packageName);
            // 匹配QQ的包名 如果手机中安装了QQ
            if (info.packageName.equals(name)) {
                return true;
            }
        }
        return false;
    }
}


//    /**
//     * 进行资源的转移 将assets下的文件转移到可读写文件目录下
//     */
//    public void createFile() {
//        InputStream is = null;
//        FileOutputStream fos = null;
//        try {
//            // 从assets文件夹中获取testapk.apk文件
//            is = getAssets().open("testapk.apk");
//            File file = new File(tempPath);
//            file.createNewFile();
//            fos = new FileOutputStream(file);
//            byte[] temp = new byte[1024];
//            int i = 0;
//            while ((i = is.read(temp)) > 0) {
//                fos.write(temp, 0, i);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


//
//    /**
//     * 下载至 Environment.getExternalStorageDirectory().getPath() + "/update.apk"
//     *
//     * @param httpUrl
//     * @return
//     */
//    private File downLoadFile(String httpUrl , String filePath) {
//        if (TextUtils.isEmpty(httpUrl)) throw new IllegalArgumentException();
//        File file = new File(filePath);
//        if (!file.exists()) file.mkdirs();
//        file = new File(filePath + File.separator + "update.apk");
//        InputStream inputStream = null;
//        FileOutputStream outputStream = null;
//        HttpURLConnection connection = null;
//
//        try {
//            URL url = new URL(httpUrl);
//            connection = (HttpURLConnection) url.openConnection();
//            connection.setConnectTimeout(10 * 1000);
//            connection.setReadTimeout(10 * 1000);
//            connection.connect();
//            inputStream = connection.getInputStream();
//            outputStream = new FileOutputStream(file);
//            byte[] buffer = new byte[1024];
//            int len = 0;
//            while ((len = inputStream.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, len);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (inputStream != null)
//                    inputStream.close();
//                if (outputStream != null)
//                    outputStream.close();
//                if (connection != null)
//                    connection.disconnect();
//            } catch (IOException e) {
//                inputStream = null;
//                outputStream = null;
//            }
//        }
//        return file;
//    }