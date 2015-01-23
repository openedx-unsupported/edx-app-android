package org.edx.mobile.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.edx.mobile.logger.OEXLogger;

public class NetworkUtil {

    private static final OEXLogger logger = new OEXLogger(NetworkUtil.class.getName());

    /**
     * Returns true if device is connected to wifi or mobile network, false
     * otherwise.
     * 
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager conMan = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo infoWifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (infoWifi != null) {
            State wifi = infoWifi.getState();
            if (wifi == NetworkInfo.State.CONNECTED) {
                logger.debug("Wifi is connected");
                return true;
            }
        }

        NetworkInfo infoMobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (infoMobile != null) {
            State mobile = infoMobile.getState();
            if (mobile == NetworkInfo.State.CONNECTED) {
                logger.debug("Mobile data is connected");
                return true;
            }
        }

        logger.debug("Network not available");
        return false;
    }
    
    /**
     * Check if there is any connectivity to a Wifi network
     * @param context
     * @return
     */
    public static boolean isConnectedWifi(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }
    
    /**
     * Check if there is any connectivity to a mobile network
     * @param context
     * @return
     */
    public static boolean isConnectedMobile(Context context){
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }
    
    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }
    
    public static String getIpAddress(Context context){
        String ipAddress = null;
        try {
            //Using WIFI Manger
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();

            String ipString = String.format(
                    "%d.%d.%d.%d",
                    (ip & 0xff),
                    (ip >> 8 & 0xff),
                    (ip >> 16 & 0xff),
                    (ip >> 24 & 0xff));
            ipAddress = ipString;


            //Using InetAddress
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        ipAddress = inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            logger.error(ex);
        }
        return ipAddress;
    }
    
}
