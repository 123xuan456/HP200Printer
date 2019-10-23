package com.kangear.mtm.ipp;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfRenderer;
import android.net.nsd.NsdManager;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.hp.jipp.encoding.IppPacket;
import com.hp.jipp.model.Operation;
import com.hp.jipp.trans.IppClientTransport;
import com.hp.jipp.trans.IppPacketData;
import com.kangear.mtm.Jni;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

import static com.hp.jipp.encoding.AttributeGroup.groupOf;
import static com.hp.jipp.encoding.Tag.operationAttributes;
import static com.hp.jipp.model.Types.attributesCharset;
import static com.hp.jipp.model.Types.attributesNaturalLanguage;
import static com.hp.jipp.model.Types.documentFormat;
import static com.hp.jipp.model.Types.printerUri;
import static com.hp.jipp.model.Types.requestingUserName;

public class IppPrinter {
    private static final String TAG = "IppPrinter";
    private static Context mContext;

    /**
     * 单例模式: http://coolshell.cn/articles/265.html
     */
    private volatile static IppPrinter singleton = null;

    public IppPrinter(Context context) {
        mContext = context;
    }

    public static IppPrinter getInstance(Context context)   {
        if (singleton == null)  {
            synchronized (IppPrinter.class) {
                if (singleton== null)  {
                    singleton= new IppPrinter(context.getApplicationContext());
                }
            }
        }
        return singleton;
    }

    //
    public void findPrinter() {
        new FindServicesNSD(mContext, (NsdManager)mContext.getSystemService(Context.NSD_SERVICE), "_ipp._tcp.").run();
    }

    private int countPages(String pdfPath) {
        int totalpages = 0;
        try {
            File pdfFile = new File(pdfPath);
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            PdfRenderer pdfRenderer = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                pdfRenderer = new PdfRenderer(parcelFileDescriptor);
                totalpages = pdfRenderer.getPageCount();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return totalpages;
    }

    //
    public void doPrint(final String printerUrl, final String pdf) {
        final String raster = mContext.getCacheDir() + File.separator + "fuck.bin";

        final int totalpages = countPages(pdf);
        Log.e(TAG, "pdf页数：" + totalpages);
        if (totalpages == 0)
            return;

        // 将pcl3gui传递到打印机
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    for (int i=1; i<=totalpages; i++) {
                        // 将PDF转换成pcl3gui
                        Jni.getInstance(mContext).pdf2pcl3guiSinglePage(pdf, raster + i, i);

                        while(true) {
                            boolean ret = printDemo(new String[]{printerUrl, raster + i});
                            Thread.sleep(500);
                            if (ret) {
                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    // 如果忙返回失败
    public static boolean printDemo(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Arguments [PRINTER_PATH] [FILE] are required, received: " +
                    Arrays.asList(args));
        }
        URI uri = URI.create(args[0]);
        File inputFile = new File(args[1]);

        IppPacket printRequest = new IppPacket(Operation.printJob, 123,
                groupOf(operationAttributes,
                        attributesCharset.of("utf-8"),
                        attributesNaturalLanguage.of("en"),
                        printerUri.of(uri),
                        requestingUserName.of("jprint"),
                        documentFormat.of("application/vnd.hp-PCL")));

//        System.out.println("Sending " + printRequest.prettyPrint(1200, "  "));
        IppClientTransport transport = new HttpIppClientTransport();
        IppPacketData request = new IppPacketData(printRequest, new FileInputStream(inputFile));
        IppPacketData response = transport.sendData(uri, request);
        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));
        String msg = response.getPacket().prettyPrint(100, "  ");
        if (msg.contains("successful-ok")) {
            return true;
        } else {
            return false;
        }
    }

    public static void getPrinter(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Arguments [PRINTER_PATH] are required, received: " +
                    Arrays.asList(args));
        }
        URI uri = URI.create(args[0]);

        IppPacket printRequest = new IppPacket(Operation.getPrinterAttributes, 123,
                groupOf(operationAttributes,
                        attributesCharset.of("utf-8"),
                        attributesNaturalLanguage.of("en"),
                        printerUri.of(uri),
                        requestingUserName.of("jprint")));

//        System.out.println("Sending " + printRequest.prettyPrint(1200, "  "));
        IppClientTransport transport = new HttpIppClientTransport();
        IppPacketData request = new IppPacketData(printRequest);
        IppPacketData response = transport.sendData(uri, request);
//        System.out.println("Received: " + response.getPacket().prettyPrint(100, "  "));
        String lines[] = response.getPacket().prettyPrint(500, "  ").split("\\r?\\n");
        String line1 = null;
        for (String line: lines) {
            if (line.contains("printer-device-id")) {
                Log.e(TAG, line);
                line1 = line;
            }
        }

        if (line1 == null)
            return;

        lines = line1.split(" = ");

        if (lines.length == 2) {
            Log.e(TAG, lines[1]);
            line1 = lines[1];
        }

        if (line1 == null)
            return;

        lines = line1.split(";");
        String result = "";
        for (String line: lines) {
            if (line.contains("MDL") || line.contains("SN")) {
                Log.e(TAG, line);
                result += (" " + line);
            }
        }

        if (mContext == null)
            return;
        Intent i = new Intent("SN");
        i.putExtra("SN", result);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);

        Log.e(TAG, result);
    }
}
