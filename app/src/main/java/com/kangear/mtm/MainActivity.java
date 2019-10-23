package com.kangear.mtm;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kangear.mtm.ipp.IppPrinter;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import flipagram.assetcopylib.AssetCopier;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_PICK_FILE = 1;
    RxPermissions rxPermissions = null;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    String pdf = "";
    static String printerurl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
//        tv.setText(Jni.getInstance(this).stringFromJNI());

        rxPermissions = new RxPermissions(this); // where this is an Activity or Fragment instance

        // Must be done during an initialization phase like onCreate
        rxPermissions.request(Manifest.permission_group.STORAGE);
        Printer.getInstance(this).init();

        verifyStoragePermissions(this);

        int count = -1;
        File destDir = this.getApplicationContext().getCacheDir();
        try {
            count = new AssetCopier(this.getApplicationContext())
                    .withFileScanning()
                    .copy("testpage", destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        IntentFilter iif = new IntentFilter("SN");
        iif.addAction("PRINTER");

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, iif);
        pdf = destDir + File.separator + "HelloWorld.hpdj1000";
    }

    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                int count = -1;
                File destDir = this.getApplicationContext().getCacheDir();
                try {
                    count = new AssetCopier(this.getApplicationContext())
                            .withFileScanning()
                            .copy("testpage", destDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String pdf = destDir + File.separator + "abc.pdf";
//                String raster = destDir + File.separator + "fuck.bin";
//                try {
//                    Printer.getInstance(this).printPdf(raster, pdf);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                IppPrinter.getInstance(this).doPrint(printerurl, pdf);
                break;

            case R.id.find_printer:
                IppPrinter.getInstance(this).findPrinter();
                break;
            case R.id.select_pdf:
                // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
                // browser.
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

                // Filter to only show results that can be "opened", such as a
                // file (as opposed to a list of contacts or timezones)
                intent.addCategory(Intent.CATEGORY_OPENABLE);

                // PKCS#8 MIME types aren't widely supported, so we'll try */* fro now.
//                intent.setType("application/pdf");
                intent.setType("*/*");
                try {
                    startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
                } catch (ActivityNotFoundException e) {
                }
                break;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_PICK_FILE) {
                Uri uri = data.getData();
                Toast.makeText(this, "文件路径："+uri.getPath().toString(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "文件路径：" + uri.getPath().toString());

                // 将文件拷贝到cache目录中打印
                InputStream is = null;
                OutputStream outStream = null;
                try {
                    is = getContentResolver().openInputStream(uri);
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);

                    File targetFile = new File(getCacheDir() + File.separator + "heihei.pdf");
                    Log.i(TAG, targetFile.getAbsolutePath());
                    pdf = targetFile.getAbsolutePath();
                    outStream = new FileOutputStream(targetFile);
                    outStream.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (outStream != null) {
                            outStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


                IppPrinter.getInstance(this).doPrint(printerurl, pdf);
            }
        }
    }

    /**
     * 字符流读写复制文件
     *
     * @param srcFile 源文件
     * @param out 目标文件
     */
    public static void FileReaderFileWriter(File srcFile, String out) {
        FileWriter fileWriter = null;
        FileReader fileReader = null;
        try {
            //创建一个可以往文件中写入字符数据的字符输出流对象。
            /*
             * 既然是往一个文件中写入文字数据，那么在创建对象时，就必须明确该文件(用于存储数据的目的
             * 地)。
             *
             * 如果文件不存在，则会自动创建。
             * 如果文件存在，则会被覆盖。
             *
             * 如果构造函数中加入true，可以实现对文件进行续写！
             */
            fileWriter = new FileWriter(out);
            //1,创建读取字符数据的流对象。
            /*
             * 在创建读取流对象时，必须要明确被读取的文件。一定要确定该文件是存在的。
             *
             * 用一个读取流关联一个已存在文件。
             */
            fileReader = new FileReader(srcFile);

            //创建一个临时容器，用于缓存读取到的字符。
            char[] chars = new char[1024];
            //定义一个变量记录读取到的字符数，(其实就是往数组里装的字符个数 )
            int num = 0;
            while ((num = fileReader.read(chars)) != -1) {
                fileWriter.write(chars, 0, num);
                fileWriter.flush();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null)
                    fileWriter.close();
                if (fileReader != null)
                    fileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if (act == null)
                return;

            switch (intent.getAction()) {
                case "PRINTER":
                    printerurl = intent.getStringExtra("PRINTER");
                    Log.i(TAG, "printerurl: " + printerurl);
                    break;

                case "SN":
                    String sn = intent.getStringExtra("SN");
                    EditText messageView = (EditText) findViewById(R.id.chatInput);
                    messageView.setText("" + sn);
                    break;
            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }
}
