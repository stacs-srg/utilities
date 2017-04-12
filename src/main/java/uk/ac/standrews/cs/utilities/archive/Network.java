/*
 * Created on 26-Oct-2004
 */
package uk.ac.standrews.cs.utilities.archive;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author stuart, graham
 */
public class Network {

    public static int UNDEFINED_PORT = -1;

    public static InetSocketAddress defaultLocalHostAddress(int port) throws IllegalArgumentException, UnknownHostException {
        return new InetSocketAddress(InetAddress.getLocalHost(), port);
    }

    /**
     * Extracts an InetSocketAddress from a string of the form "host:port". If the host part is empty the local
     * loopback address is used. If the port part is empty the specified default port is used.
     *
     * @param host_and_port a string of the form "host:port"
     * @param default_port  the default port to be used if the port is not specified
     * @return a corresponding InetSocketAddress
     * @throws UnknownHostException if the specified host cannot be resolved
     */
    public static InetSocketAddress processHostPortParameter(String host_and_port, int default_port) throws IllegalArgumentException, UnknownHostException {

        if (host_and_port == null) return null;

        String host = extractHostName(host_and_port);
        int port = Integer.parseInt(extractPortNumber(host_and_port));

        InetAddress address = null;
        if (host != null) {
            //convert host string to InetAddress
            address = InetAddress.getByName(host);
        }

        if (port != -1) {
            if (address != null) return new InetSocketAddress(address, port);
            else return defaultLocalHostAddress(port);
        } else return new InetSocketAddress(address, default_port);
    }

    /**
     * Extracts an host name from a string of the form "[host][:][port]". If the
     * host part is empty null is returned.
     *
     * @param host_and_port a string of the form "host:port", "host", "host:", ":port"
     */
    public static String extractHostName(String host_and_port) {
        if (host_and_port == null) return null;
        int sepIndex = host_and_port.indexOf(":"); //where in the ":" character
        if (sepIndex != -1) return host_and_port.substring(0, sepIndex);
        else return host_and_port; //no port was specified
    }

    /**
     * Extracts an port number from a string of the form "[host][:][port]". If
     * the port part is empty the string representation of
     * Network.UNDEFINED_PORT is returned.
     *
     * @param host_and_port a string of the form "host:port", "host", "host:", ":port"
     */
    public static String extractPortNumber(String host_and_port) {
        if (host_and_port == null) return Integer.toString(UNDEFINED_PORT);
        int sepIndex = host_and_port.indexOf(":"); //where is the ":" character
        if (sepIndex == -1) return Integer.toString(UNDEFINED_PORT);
        else {
            //check that string is not "<host>:" or ":"
            if (sepIndex != host_and_port.length() - 1 && sepIndex != -1) {
                String portString = host_and_port.substring(sepIndex + 1, host_and_port.length());
                return portString;
            } else {
                return Integer.toString(UNDEFINED_PORT);
            }
        }
    }

    public static boolean isValidAddress(InetAddress a) {
        String localIP = a.getHostAddress();
        boolean res = false;
        try {
            ArrayList al = Collections.list(NetworkInterface.getNetworkInterfaces());
            Iterator ali = al.iterator();

            while (ali.hasNext() && !res) {
                NetworkInterface ni = (NetworkInterface) ali.next();
                ArrayList inet_al = Collections.list(ni.getInetAddresses());
                Iterator inet_ali = inet_al.iterator();

                while (inet_ali.hasNext() && !res) {
                    InetAddress currentInet = (InetAddress) inet_ali.next();
                    if (localIP.compareTo(currentInet.getHostAddress()) == 0) res = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
