package com.vivo.vivopackageinstallerbug;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.stream.Stream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class MainActivity extends AppCompatActivity {

    private static final String URL = "https://1b07f577fcc15b28d7950e7553c35ee6.dd.cdntips.com/download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";
    private static final String PATH = Environment.getExternalStorageDirectory() + "/1/system/custom";
    private static final String PATH_1_APK = Environment.getExternalStorageDirectory() + "/1/yyb.apk";
    private TextView textView;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
    }

    public void download(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                download();
            }
        }).start();
    }

    private void download() {
        File dir = new File(PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL)
                .get()
                .build();
        Call call = client.newCall(request);
        Response response = null;
        ResponseBody body = null;
        BufferedSink sink = null;
        try {
            response = call.execute();
            if (response.isSuccessful()) {
                display("请求成功，下载中");
                body = response.body();
                if (body != null) {
                    File file = new File(dir + "/yyb.apk");
                    sink = Okio.buffer(Okio.sink(file));
                    sink.writeAll(body.source());
                    sink.flush();
                    display("下载完成");
                }
            } else {
                display("请求失败：HTTP " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sink != null) {
                try {
                    sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (body != null) {
                body.close();
            }
            if (response != null) {
                response.close();
            }
        }
    }

    public void install(View view) {
        File file = new File(PATH + "/yyb.apk");
        if (!file.exists()) {
            display("文件不存在");
            return;
        }
        File toFile = new File(PATH_1_APK);
        if (!toFile.exists()) {
            copy(file, toFile);
        }
        installApk3(toFile);
    }

    public void hackInstall(View view) {
        File file = new File(PATH + "/yyb.apk");
        if (!file.exists()) {
            display("文件不存在");
            return;
        }
        installApk3(file);
    }

    private void display(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(text);
            }
        });
    }

    private void installApk3(File apkFile) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private void installApk2(File apkFile) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.fromFile(apkFile);
        intent.setDataAndType(uri, "application/vn.android.package-archive");
        startActivity(intent);
    }

    private void installApk(File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
        intent.setData(Uri.fromFile(file));
        startActivity(intent);
    }

    private void copy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(inStream);
            close(in);
            close(outStream);
            close(out);
        }
    }

    private void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void checkPermission(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (resultCode == RESULT_OK) {

            }
        }
    }
}