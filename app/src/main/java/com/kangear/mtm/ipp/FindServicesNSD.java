
// FindServicesNSD.java

// Copyright (c) 2013 By Simon Lewis. All Rights Reserved.

package com.kangear.mtm.ipp;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;

final class FindServicesNSD
        implements
        DiscoveryListener,
        ResolveListener {

    //
    private NsdManager manager;
    private String serviceType;
    private Context mContext;
    private static final String TAG = "FindServicesNSD";

    // DiscoveryListener
    @Override
    public void onDiscoveryStarted(String theServiceType) {
        Log.d(TAG, "onDiscoveryStarted");
    }

    @Override
    public void onStartDiscoveryFailed(String theServiceType, int theErrorCode) {
        Log.d(TAG, "onStartDiscoveryFailed(" + theServiceType + ", " + theErrorCode);
    }

    @Override
    public void onDiscoveryStopped(String serviceType) {
        Log.d(TAG, "onDiscoveryStopped");
    }

    @Override
    public void onStopDiscoveryFailed(String theServiceType, int theErrorCode) {
        Log.d(TAG, "onStartDiscoveryFailed(" + theServiceType + ", " + theErrorCode);
    }

    @Override
    public void onServiceFound(NsdServiceInfo theServiceInfo) {
        Log.d(TAG, "onServiceFound(" + theServiceInfo + ")");
        Log.d(TAG, "name == " + theServiceInfo.getServiceName());
        Log.d(TAG, "type == " + theServiceInfo.getServiceType());
        serviceFound(theServiceInfo);
    }

    @Override
    public void onServiceLost(NsdServiceInfo theServiceInfo) {
        Log.d(TAG, "onServiceLost(" + theServiceInfo + ")");
    }

    // Resolve Listener
    @Override
    public void onServiceResolved(NsdServiceInfo theServiceInfo) {
        Log.d(TAG, "onServiceResolved(" + theServiceInfo + ")");
        Log.d(TAG, "name == " + theServiceInfo.getServiceName());
        Log.d(TAG, "type == " + theServiceInfo.getServiceType());
        Log.d(TAG, "host == " + theServiceInfo.getHost());
        Log.d(TAG, "port == " + theServiceInfo.getPort());
    }

    @Override
    public void onResolveFailed(NsdServiceInfo theServiceInfo, int theErrorCode) {
        Log.d(TAG, "onResolveFailed(" + theServiceInfo + ", " + theErrorCode);
    }

    //
    FindServicesNSD(Context context, NsdManager theManager, String theServiceType) {
        mContext = context;
        manager = theManager;
        serviceType = theServiceType;
    }

    void run() {
        manager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, this);
    }

    private void serviceFound(NsdServiceInfo theServiceInfo) {
        Log.d(TAG, "Service found: "+ theServiceInfo);
        if (theServiceInfo.getServiceType().equals(serviceType)){
            manager.resolveService(theServiceInfo, new ResolveListener() {
                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    Log.e(TAG, "Resolve Failed: " + serviceInfo);
                }
                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.i(TAG, "Service Resolved: " + serviceInfo);
                    // "ipp/printer"先写死，以后使用其他库才能实现动态的
                    String PRINTER = "http:/" + serviceInfo.getHost() + ":" + serviceInfo.getPort() + "/ipp/printer";
                    Log.e(TAG, "PRINTER_URL: " + PRINTER);

                    Intent i = new Intent("PRINTER");
                    i.putExtra("PRINTER", PRINTER);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(i);

                    try {
                        IppPrinter.getPrinter(new String[] {PRINTER});
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}