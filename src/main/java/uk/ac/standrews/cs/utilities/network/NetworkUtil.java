/*
 * Copyright 2019 Systems Research Group, University of St Andrews:
 * <https://github.com/stacs-srg>
 *
 * This file is part of the module utilities.
 *
 * utilities is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * utilities is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with utilities. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package uk.ac.standrews.cs.utilities.network;

import uk.ac.standrews.cs.utilities.archive.Diagnostic;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Various network utilities.
 *
 * @author Stuart Norcross (stuart@cs.st-andrews.ac.uk)
 * @author Masih Hajiarabderkani (mh638@st-andrews.ac.uk)
 * @author Simone Conte (sic2@st-andrews.ac.uk)
 */
@SuppressWarnings("unused")
public final class NetworkUtil {

    /**
     * The undefined port.
     */
    @SuppressWarnings("WeakerAccess")
    public static final int UNDEFINED_PORT = -1;

    /**
     * Returns the first valid public point-to-point IPv4 address (Inet4Address) that can be found for an interface on the local host.
     * This method should be used in place of InetAddress.getLocalHost(), which may return an Inet6Address object
     * corresponding to the IPv6 address of a local interface. The bind operation is not supported by this
     * address family.
     *
     *
     * https://stackoverflow.com/a/9482369/2467938
     *
     - Any address in the range 127.xxx.xxx.xxx is a "loopback" address. It is only visible to "this" host.
     - Any address in the range 192.168.xxx.xxx is a private (aka site local) IP address. These are reserved for use within an organization.
     The same applies to 10.xxx.xxx.xxx addresses, and 172.16.xxx.xxx (see RFC 1918)
     - Addresses in the range 169.254.xxx.xxx are link local IP addresses. These are reserved for use on a single network segment.
     - Addresses in the range 224.xxx.xxx.xxx through 239.xxx.xxx.xxx are multicast addresses.
     - The address 255.255.255.255 is the broadcast address.
     - Anything else should be a valid public point-to-point IPv4 address.
     *
     * @return the first IPv4 address found
     * @throws UnknownHostException if no IPv4 address can be found
     */
    @SuppressWarnings("WeakerAccess")
    public static InetAddress getLocalIPv4Address() throws UnknownHostException {

        final InetAddress local_address = InetAddress.getLocalHost();

        if (isIPV4(local_address) && !isLoopback(local_address)) {
            return local_address;
        }

        // Otherwise, look for an IPv4 address among the other interfaces.
        InetAddress loopback_address = null;

        try {
            final Enumeration<NetworkInterface> interfaces_enumeration = NetworkInterface.getNetworkInterfaces();

            if (interfaces_enumeration != null) {
                while (interfaces_enumeration.hasMoreElements()) {

                    NetworkInterface network_interface = interfaces_enumeration.nextElement();
                    Enumeration<InetAddress> inet_addresses = network_interface.getInetAddresses();
                    while (inet_addresses.hasMoreElements()) {

                        InetAddress address = inet_addresses.nextElement();
                        if (isIPV4(address) && !isLoopback(address) && !isPrivate(address) &&
                                !isLinkLocal(address) && !isMulticast(address) && !isBroadcast(address)) {
                            return address;
                        }

                        loopback_address = address;
                    }
                }
            }

            // Haven't found any valid public IPv4 address, so return any other address if available.
            if (loopback_address != null) {
                return loopback_address;
            }

        } catch (SocketException e) {
            // Ignore.
        }

        throw new UnknownHostException("local host has no interface with an IPv4 address");
    }

    /**
     * Returns an InetSocketAddress corresponding to a local non-loopback IPv4 address.
     *
     * @param port the port
     * @return the corresponding local address
     * @throws UnknownHostException if no IPv4 address can be found
     */
    @SuppressWarnings("WeakerAccess")
    public static InetSocketAddress getLocalIPv4InetSocketAddress(final int port) throws UnknownHostException {

        return new InetSocketAddress(getLocalIPv4Address(), port);
    }

    /**
     * Outputs a diagnostic trace containing the address of a given host.
     *
     * @param prefix a trace prefix
     * @param host   the host
     */
    public static void reportHostAddress(final String prefix, final InetSocketAddress host) {

        Diagnostic.trace(prefix + ": " + formatHostAddress(host), Diagnostic.INIT);
    }

    /**
     * Extracts an InetSocketAddress from a string of the form "host:port". If the host part is empty, the local
     * loopback address is used. If the port part is empty, the specified default port is used.
     *
     * @param host_and_port a string of the form "host:port"
     * @param default_port  the default port to be used if the port is not specified
     * @return a corresponding InetSocketAddress
     * @throws UnknownHostException if the specified host cannot be resolved
     */
    public static InetSocketAddress extractInetSocketAddress(final String host_and_port, final int default_port) throws UnknownHostException {

        final String host_name = extractHostName(host_and_port);
        final int port = extractPortNumber(host_and_port);

        if (host_name.equals("")) {
            if (port == UNDEFINED_PORT) {
                return getLocalIPv4InetSocketAddress(default_port);
            }
            return getLocalIPv4InetSocketAddress(port);
        } else if (port == UNDEFINED_PORT) {
            return getInetSocketAddress(host_name, default_port);
        } else {
            return getInetSocketAddress(host_name, port);
        }
    }

    /**
     * Creates an InetSocketAddress for a given host and port.
     *
     * @param host the host
     * @param port the port
     * @return a corresponding InetSocketAddress
     * @throws UnknownHostException if the specified host cannot be resolved
     */
    @SuppressWarnings("WeakerAccess")
    public static InetSocketAddress getInetSocketAddress(final InetAddress host, final int port) throws UnknownHostException {

        return new InetSocketAddress(host, port);
    }

    /**
     * Creates an InetSocketAddress for a given host and port.
     *
     * @param host_name the host
     * @param port      the port
     * @return a corresponding InetSocketAddress
     * @throws UnknownHostException if the specified host cannot be resolved
     */
    @SuppressWarnings("WeakerAccess")
    public static InetSocketAddress getInetSocketAddress(final String host_name, final int port) throws UnknownHostException {

        return getInetSocketAddress(InetAddress.getByName(host_name), port);
    }

    /**
     * Extracts a host name from a string of the form "[host][:][port]". If the
     * host part is empty, the empty string is returned.
     *
     * @param host_and_port a string of the form "host:port", "host", "host:", ":port"
     * @return the host name
     */
    @SuppressWarnings("WeakerAccess")
    public static String extractHostName(final String host_and_port) {

        if (host_and_port == null) {
            return "";
        }

        final int separator_index = host_and_port.indexOf(":");

        if (separator_index != -1) {
            return host_and_port.substring(0, separator_index);
        }
        return host_and_port; // No port was specified.
    }

    /**
     * Extracts a port number as a string from a string of the form "[host][:][port]". If
     * the port part is empty, the string representation of {@link #UNDEFINED_PORT} is returned.
     *
     * @param host_and_port a string of the form "host:port", "host", "host:" or ":port"
     * @return the port number as a string
     */
    @SuppressWarnings("WeakerAccess")
    public static String extractPortNumberAsString(final String host_and_port) {

        if (host_and_port == null) {
            return String.valueOf(UNDEFINED_PORT);
        }

        final int separator_index = host_and_port.indexOf(":");

        // Check for "<host>", "<host>:" and ":"
        if (separator_index == -1 || separator_index == host_and_port.length() - 1) {
            return String.valueOf(UNDEFINED_PORT);
        }

        return host_and_port.substring(separator_index + 1);
    }

    /**
     * Extracts a port number from a string of the form "[host][:][port]". If
     * the port part is empty {@link #UNDEFINED_PORT} is returned.
     *
     * @param host_and_port a string of the form "host:port", "host", "host:" or ":port"
     * @return the port number
     */
    @SuppressWarnings("WeakerAccess")
    public static int extractPortNumber(final String host_and_port) {

        try {
            return Integer.parseInt(extractPortNumberAsString(host_and_port));
        } catch (final NumberFormatException e) {
            return UNDEFINED_PORT;
        }
    }

    /**
     * Tests whether a given address is a valid local address.
     *
     * @param address an address
     * @return true if the address is a valid local address
     */
    public static boolean isValidLocalAddress(final InetAddress address) {

        boolean local = address.isAnyLocalAddress() || address.isLoopbackAddress();
        if (!local) {
            try {
                local = NetworkInterface.getByInetAddress(address) != null;
            } catch (final SocketException e) {
                local = false;
            }
        }
        return local;
    }

    /**
     * Returns a description of a given host address.
     *
     * @param address an address
     * @return a description of the address
     */
    @SuppressWarnings("WeakerAccess")
    public static String formatHostAddress(final InetSocketAddress address) {

        if (address != null) {

            final String host = address.getAddress().getHostAddress();
            final int port = address.getPort();

            return formatHostAddress(host, port);
        }
        return null;
    }

    /**
     * Returns a description of a given host address.
     *
     * @param host an IP address
     * @param port a port
     * @return a description of the address
     */
    @SuppressWarnings("WeakerAccess")
    public static String formatHostAddress(final String host, final int port) {

        return host + ":" + port;
    }

    /**
     * Returns a description of a given socket.
     *
     * @param socket a socket
     * @return a description of the socket's address
     */
    public static String formatHostAddress(final Socket socket) {

        return formatHostAddress(socket.getInetAddress(), socket.getPort());
    }

    /**
     * Returns a description of a given host address.
     *
     * @param address an address
     * @param port    a port
     * @return a description of the address
     */
    @SuppressWarnings("WeakerAccess")
    public static String formatHostAddress(final InetAddress address, final int port) {

        return formatHostAddress(address.getHostAddress(), port);
    }

    /**
     * Alternative to {@link ServerSocket} constructors that calls {@link ServerSocket#setReuseAddress(boolean)} to enable reuse of the fixed
     * local port even when there is a previous connection to that port in the timeout state.
     *
     * @param local_port the local port
     * @return a socket bound to the given local port
     * @throws IOException if the new socket can't be connected to
     */
    @SuppressWarnings("WeakerAccess")
    public static ServerSocket makeReusableServerSocket(@SuppressWarnings("SameParameterValue") final int local_port) throws IOException {

        final ServerSocket socket = new ServerSocket();
        socket.setReuseAddress(true);

        socket.bind(new InetSocketAddress(local_port));

        return socket;
    }

    /**
     * Alternative to {@link ServerSocket} constructors that calls {@link ServerSocket#setReuseAddress(boolean)} to enable reuse of the fixed
     * local port even when there is a previous connection to that port in the timeout state.
     *
     * @param local_address the address to which the socket should be bound
     * @param local_port    the local port
     * @return a socket bound to the given local port
     * @throws IOException if the new socket can't be connected to
     */
    public static ServerSocket makeReusableServerSocket(final InetAddress local_address, final int local_port) throws IOException {

        final ServerSocket socket = new ServerSocket();

        socket.setReuseAddress(true);
        socket.bind(new InetSocketAddress(local_address, local_port));

        return socket;
    }

    /**
     * finds a free TCP port on the local machine.
     *
     * @return a free port on the local machine
     * @throws IOException if unable to check for free port
     */
    public static synchronized int findFreeLocalTCPPort() throws IOException {

        try (ServerSocket server_socket = makeReusableServerSocket(0)) {

            return server_socket.getLocalPort();
        }
    }

    /**
     * Constructs an {@link InetSocketAddress address} from an String representation of an address that is produced by {@link InetSocketAddress#toString()}.
     *
     * @param address_in_string the address in string
     * @return the address
     * @throws UnknownHostException if host is unknown
     * @see InetSocketAddress#toString()
     */
    public static InetSocketAddress getAddressFromString(final String address_in_string) throws UnknownHostException {

        if (address_in_string == null || address_in_string.equals("null")) {
            return null;
        }
        final String[] components = address_in_string.split(":", -1);
        final String host = components[0];
        final int port = Integer.parseInt(components[1]);
        final String name = getName(host);
        final byte[] address_bytes = getBytes(host);
        final InetAddress addr = name.equals("") ? InetAddress.getByAddress(address_bytes) : InetAddress.getByAddress(name, address_bytes);
        return new InetSocketAddress(addr, port);
    }

    //---------------------------------------------------------

    private static boolean isLoopback(InetAddress address) {

        return address.isLoopbackAddress();
    }

    private static boolean isPrivate(InetAddress address) {

        return address.isSiteLocalAddress();
    }

    private static boolean isLinkLocal(InetAddress address) {

        return address.isLinkLocalAddress();
    }

    private static boolean isMulticast(InetAddress address) {

        return address.isMulticastAddress();
    }

    private static boolean isBroadcast(InetAddress address) {

        return address.getHostAddress().equals("255.255.255.255");
    }

    private static boolean isIPV4(InetAddress address) {

        return address instanceof Inet4Address;
    }

    private static String getName(final String host) {

        final String[] name_address = host.split("/", -1);
        return name_address[0];
    }

    private static byte[] getBytes(final String host) {

        final String[] name_address = host.split("/", -1);
        final String[] byte_strings = name_address[1].split("\\.", -1);
        final byte[] bytes = new byte[byte_strings.length];

        for (int i = 0; i < byte_strings.length; i++) {
            final Integer j = Integer.valueOf(byte_strings[i]);
            bytes[i] = j.byteValue();
        }

        return bytes;
    }
}
