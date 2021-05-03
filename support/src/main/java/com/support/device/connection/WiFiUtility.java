package com.support.device.connection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WiFiUtility {
    public static String getIpAddress(Context context, WifiManager wifiManager) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
    }

    @SuppressLint("NewApi")
    public static String getDefaultIpAddresses(ConnectivityManager cm) {
        LinkProperties prop = cm.getLinkProperties(cm.getActiveNetwork());
        return formatIpAddresses(prop);
    }

    private static String formatIpAddresses(LinkProperties prop) {
        if (prop == null) return null;
        Iterator<InetAddress> iter = prop.getDnsServers().iterator();
        // If there are no entries, return null
        if (!iter.hasNext()) return null;
        // Concatenate all available addresses, comma separated
        StringBuilder addresses = new StringBuilder();
        while (iter.hasNext()) {
            addresses.append(iter.next().getHostAddress());
            if (iter.hasNext()) addresses.append("\n");
        }
        return addresses.toString();
    }

    public static String getHotspotAddress(WifiManager manager) {
        final DhcpInfo dhcp = manager.getDhcpInfo();
        int ipAddress = dhcp.gateway;
        ipAddress = (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) ?
                Integer.reverseBytes(ipAddress) : ipAddress;
        byte[] ipAddressByte = BigInteger.valueOf(ipAddress).toByteArray();
        try {
            InetAddress myAddr = InetAddress.getByAddress(ipAddressByte);
            return myAddr.getHostAddress();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            Log.e("Wifi Class", "Error getting Hotspot IP address ", e);
        }
        return "null";
    }

    @SuppressWarnings("unchecked")
    public static void setStaticIpConfiguration(WifiManager manager,
                                                WifiConfiguration config,
                                                InetAddress ipAddress,
                                                int prefixLength,
                                                InetAddress gateway,
                                                InetAddress[] dns) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException, InstantiationException {
        // First set up IpAssignment to STATIC.
        Object ipAssignment = getEnumValue("android.net.IpConfiguration$IpAssignment", "STATIC");
        callMethod(config, "setIpAssignment", new String[]{"android.net.IpConfiguration$IpAssignment"}, new Object[]{ipAssignment});

        // Then set properties in StaticIpConfiguration.
        Object staticIpConfig = newInstance("android.net.StaticIpConfiguration");
        Object linkAddress = newInstance("android.net.LinkAddress", new Class<?>[]{InetAddress.class, int.class}, new Object[]{ipAddress, prefixLength});

        setField(staticIpConfig, "ipAddress", linkAddress);
        setField(staticIpConfig, "gateway", gateway);
        getField(staticIpConfig, "dnsServers", ArrayList.class).clear();
        for (int i = 0; i < dns.length; i++)
            getField(staticIpConfig, "dnsServers", ArrayList.class).add(dns[i]);

        callMethod(config, "setStaticIpConfiguration", new String[]{"android.net.StaticIpConfiguration"}, new Object[]{staticIpConfig});

        int netId = manager.updateNetwork(config);
        boolean result = netId != -1;
        if (result) {
            boolean isDisconnected = manager.disconnect();
            boolean configSaved = manager.saveConfiguration();
            boolean isEnabled = manager.enableNetwork(config.networkId, true);
            boolean isReconnected = manager.reconnect();
        }
    }

    public static WifiConfiguration getCurrentWiFiConfiguration(Context context) {
        WifiConfiguration wifiConf = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                @SuppressLint("MissingPermission") List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
                if (configuredNetworks != null) {
                    for (WifiConfiguration conf : configuredNetworks) {
                        if (conf.networkId == connectionInfo.getNetworkId()) {
                            wifiConf = conf;
                            break;
                        }
                    }
                }
            }
        }
        return wifiConf;
    }

    private static Object newInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        return newInstance(className, new Class<?>[0], new Object[0]);
    }

    private static Object newInstance(String className, Class<?>[] parameterClasses, Object[] parameterValues) throws NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        Class<?> clz = Class.forName(className);
        Constructor<?> constructor = clz.getConstructor(parameterClasses);
        return constructor.newInstance(parameterValues);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object getEnumValue(String enumClassName, String enumValue) throws ClassNotFoundException {
        Class<Enum> enumClz = (Class<Enum>) Class.forName(enumClassName);
        return Enum.valueOf(enumClz, enumValue);
    }

    private static void setField(Object object, String fieldName, Object value) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.set(object, value);
    }

    private static <T> T getField(Object object, String fieldName, Class<T> type) throws IllegalAccessException, IllegalArgumentException, NoSuchFieldException {
        Field field = object.getClass().getDeclaredField(fieldName);
        return type.cast(field.get(object));
    }

    private static void callMethod(Object object, String methodName, String[] parameterTypes, Object[] parameterValues) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Class<?>[] parameterClasses = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++)
            parameterClasses[i] = Class.forName(parameterTypes[i]);

        Method method = object.getClass().getDeclaredMethod(methodName, parameterClasses);
        method.invoke(object, parameterValues);
    }

    public static void updateIpConfiguration(WifiManager wifiManager,WifiConfiguration wifiConf){
        try {
            setStaticIpConfiguration(wifiManager, wifiConf,
                    InetAddress.getByName("192.168.0.100"),
                    24,
                    InetAddress.getByName("10.0.0.2"),
                    new InetAddress[]{InetAddress.getByName("10.0.0.3"), InetAddress.getByName("10.0.0.4")});

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
