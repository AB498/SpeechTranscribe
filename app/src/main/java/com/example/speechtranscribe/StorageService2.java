package com.example.speechtranscribe;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.vosk.Model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StorageService2 {
    protected static final String TAG = org.vosk.android.StorageService.class.getSimpleName();

    public StorageService2() {
    }

    public static void unpack(Context context, String path, final org.vosk.android.StorageService.Callback<Model> completeCallback, final org.vosk.android.StorageService.Callback<IOException> errorCallback) {
        Executor executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                //String outputPath = sync(context, sourcePath, targetPath);
                Model model = new Model(path);
                handler.post(() -> {
                    completeCallback.onComplete(model);
                });
            } catch (Exception var8) {

            }

        });
    }

    public static String sync(Context context, String sourcePath, String targetPath) throws IOException {
        AssetManager assetManager = context.getAssets();
        File externalFilesDir = context.getExternalFilesDir((String)null);
        if (externalFilesDir == null) {
            throw new IOException("cannot get external files dir, external storage state is " + Environment.getExternalStorageState());
        } else {
            File targetDir = new File(externalFilesDir, targetPath);
            String resultPath = (new File(targetDir, sourcePath)).getAbsolutePath();
            String sourceUUID = readLine(assetManager.open(sourcePath + "/uuid"));

            try {
                String targetUUID = readLine(new FileInputStream(new File(targetDir, sourcePath + "/uuid")));
                if (targetUUID.equals(sourceUUID)) {
                    return resultPath;
                }
            } catch (FileNotFoundException var9) {
            }

            deleteContents(targetDir);
            copyAssets(assetManager, sourcePath, targetDir);
            copyFile(assetManager, sourcePath + "/uuid", targetDir);
            return resultPath;
        }
    }

    private static String readLine(InputStream is) throws IOException {
        return (new BufferedReader(new InputStreamReader(is))).readLine();
    }

    private static boolean deleteContents(File dir) {
        File[] files = dir.listFiles();
        boolean success = true;
        if (files != null) {
            File[] var3 = files;
            int var4 = files.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                File file = var3[var5];
                if (file.isDirectory()) {
                    success &= deleteContents(file);
                }

                if (!file.delete()) {
                    success = false;
                }
            }
        }

        return success;
    }

    private static void copyAssets(AssetManager assetManager, String path, File outPath) throws IOException {
        String[] assets = assetManager.list(path);
        if (assets != null) {
            if (assets.length == 0) {
                if (!path.endsWith("uuid")) {
                    copyFile(assetManager, path, outPath);
                }
            } else {
                File dir = new File(outPath, path);
                if (!dir.exists()) {
                    Log.v(TAG, "Making directory " + dir.getAbsolutePath());
                    if (!dir.mkdirs()) {
                        Log.v(TAG, "Failed to create directory " + dir.getAbsolutePath());
                    }
                }

                String[] var5 = assets;
                int var6 = assets.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    String asset = var5[var7];
                    copyAssets(assetManager, path + "/" + asset, outPath);
                }
            }

        }
    }

    private static void copyFile(AssetManager assetManager, String fileName, File outPath) throws IOException {
        Log.v(TAG, "Copy " + fileName + " to " + outPath);
        InputStream in = assetManager.open(fileName);
        OutputStream out = new FileOutputStream(outPath + "/" + fileName);
        byte[] buffer = new byte[4000];

        int read;
        while((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        in.close();
        out.close();
    }

    public interface Callback<R> {
        void onComplete(R result);
    }
}
