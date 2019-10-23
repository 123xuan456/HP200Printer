package com.kangear.mtm;

import android.content.Context;

public class Jni {
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * 单例模式: http://coolshell.cn/articles/265.html
     */
    private volatile static Jni singleton = null;
    public static Jni getInstance(Context context)   {
        if (singleton == null)  {
            synchronized (Jni.class) {
                if (singleton== null)  {
                    singleton= new Jni(context.getApplicationContext());
                }
            }
        }
        return singleton;
    }

    private Jni(Context context) {
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String pdf2ps(String pdfPath, String psPath);

    public native String pdf2pcl3guiSinglePage(String pdfPath, String psPath, int pageNumber);
    public native String pdf2pcl3gui(String pdfPath, String psPath);
}
