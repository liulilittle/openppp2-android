package supersocksr.ppp.android.openppp2;

import android.net.ProxyInfo;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Objects;

import supersocksr.ppp.android.c.libopenppp2;

public final class IPAddressX {
    public static final InetAddress ADDRESS_ANY = address_of(new byte[]{0, 0, 0, 0});
    public static final InetAddress ADDRESS_NONE = address_of(new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255});
    public static final InetAddress ADDRESS_LOOPBACK = address_of("localhost", new byte[]{127, 0, 0, 1});
    public static final InetAddress ADDRESS_BROADCAST = ADDRESS_NONE;
    public static final int MIN_PORT = 0;
    public static final int MAX_PORT = 65535;

    // Converts a byte array to an IP address.
    @Nullable
    public static InetAddress address_of(byte[] bytes, int offset) {
        if (bytes == null || offset < 0) {
            return null;
        }

        try {
            byte[] new_bytes = bytes;
            int len = bytes.length - offset;
            if (len >= 16) {
                if (offset != 0) {
                    new_bytes = new byte[16];
                    for (int i = 0, l = offset + 16; offset < l; i++, offset++) {
                        new_bytes[i] = bytes[offset];
                    }
                }
                return Inet6Address.getByAddress(new_bytes);
            } else if (len >= 4) {
                if (offset != 0) {
                    new_bytes = new byte[4];
                    for (int i = 0, l = offset + 4; offset < l; i++, offset++) {
                        new_bytes[i] = bytes[offset];
                    }
                }
                return Inet4Address.getByAddress(new_bytes);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    // Converts a byte array to an IP address.
    @Nullable
    public static InetAddress address_of(byte[] address) {
        return address_of(null, address);
    }

    // Converts a byte array to an IP address.
    @Nullable
    public static InetAddress address_of(String host, byte[] address) {
        if ((host == null || host.length() < 1) && (address == null)) {
            return null;
        }

        try {
            if (address.length >= 16) {
                return Inet6Address.getByAddress(host, address);
            } else if (address.length >= 4) {
                return Inet4Address.getByAddress(host, address);
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    // Convert a string to an IP address.
    @Nullable
    public static InetAddress string_to_address(String address) {
        try {
            byte[] V = libopenppp2.c.string_to_address_bytes(address);
            if (V == null) {
                return null;
            } else {
                return address_of(V);
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Check whether the IP address string is in a invalid format.
    public static boolean address_is_invalid(String host) {
        if (X.is_empty(host)) {
            return true;
        } else {
            return libopenppp2.c.ip_address_string_is_invalid(host);
        }
    }

    // Check whether it is an IPV4 or IPV6 loopback address.
    public static boolean address_is_loopback_address(InetAddress host) {
        return address_is_loopback_address(address_to_string(host));
    }

    // Check whether it is an IPV4 or IPV6 loopback address.
    public static boolean address_is_loopback_address(String host) {
        byte[] bytes = libopenppp2.c.string_to_address_bytes(host);
        if (bytes == null || bytes.length < 4) {
            return false;
        } else if (bytes.length == 4) {
            return (bytes[0] & 0xff) == 127;
        } else if (bytes.length == 16) {
            for (int i = 0; i < 15; i++) {
                int h = bytes[i] & 0xff;
                if (h != 0) {
                    return false;
                }
            }

            return bytes[15] == 1;
        } else {
            return false;
        }
    }

    // Builds an instance of direct proxy information, returning a managed reference if successful or NULL otherwise.
    @Nullable
    public static ProxyInfo proxy_info_build_direct_proxy(String host, int port) {
        if (address_is_invalid(host)) {
            return null;
        } else if (port <= MIN_PORT || port > MAX_PORT) {
            return null;
        } else {
            return X.call(() -> ProxyInfo.buildDirectProxy(host, port), null).item2;
        }
    }

    // Convert a bytes to an IP address string.
    public static String bytes_to_address_string(byte[] address) {
        return libopenppp2.c.bytes_to_address_string(address);
    }

    // Test whether the tcp port is bound.
    public static boolean test_is_tcp_port_bound(String host, int port) {
        try (ServerSocket socket = new ServerSocket()) {
            socket.bind(new InetSocketAddress(string_to_address(host), port));
            return false;
        } catch (Throwable ignored) {
            return true;
        }
    }

    // Test whether the tcp port is bound.
    public static boolean test_is_tcp_port_bound(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false;
        } catch (Throwable ignored) {
            return true;
        }
    }

    // Test whether the udp port is bound.
    public static boolean test_is_udp_port_bound(String host, int port) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.bind(new InetSocketAddress(string_to_address(host), port));
            return false;
        } catch (Throwable ignored) {
            return true;
        }
    }

    // Test whether the udp port is bound.
    public static boolean test_is_udp_port_bound(int port) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            return false;
        } catch (Throwable ignored) {
            return true;
        }
    }

    // Conversion between string and ipep address format.
    public static String inet_socket_address_to_address_string(InetSocketAddress address) {
        if (address == null) {
            return null;
        }

        InetAddress host = address.getAddress();
        if (host == null) {
            return null;
        }

        int port = address.getPort();
        String rets = bytes_to_address_string(host.getAddress());
        if (port > MIN_PORT && port <= MAX_PORT) {
            rets += ":";
            rets += String.valueOf(port);
        }
        return rets;
    }

    // Conversion between string and ipep address format.
    @Nullable
    public static InetSocketAddress address_string_to_inet_socket_address(String address_string) {
        if (X.is_empty(address_string)) {
            return null;
        }

        InetAddress address = null;
        int port = MIN_PORT;

        int index = address_string.indexOf(':');
        if (index < 0) {
            address = string_to_address(address_string);
        } else {
            String host = address_string.substring(0, index);
            if (TextUtils.isEmpty(host)) {
                return null;
            }

            address = string_to_address(host);
            port = X.call(() -> Integer.valueOf(address_string.substring(index + 1)), 0).item2;
        }

        if (address == null) {
            return null;
        }

        return new InetSocketAddress(address, port);
    }

    // Gets the binary form of the InetAddress.
    public static byte[] address_of(InetAddress address) {
        if (address == null) {
            return null;
        } else {
            return address.getAddress();
        }
    }

    // Network byte order to host byte order.
    public static InetAddress network_to_host_order(InetAddress address) {
        if (address == null) {
            return null;
        } else if (X.IS_LITTLE_ENDIAN) {
            byte[] buffer = X.reserve(address_of(address));
            if (buffer == null) {
                return null;
            } else {
                return address_of(buffer);
            }
        } else {
            return address;
        }
    }

    // Host byte order to network byte order.
    public static InetAddress host_to_network_order(InetAddress address) {
        return network_to_host_order(address);
    }

    // Network byte order to host byte order.
    public static int network_to_host_order(int address) {
        if (X.IS_LITTLE_ENDIAN) {
            return Integer.reverseBytes(address);
        } else {
            return address;
        }
    }

    // Host byte order to network byte order.
    public static int host_to_network_order(int address) {
        return network_to_host_order(address);
    }

    // Whether it is a V4 address.
    public static boolean address_is_v4_address(InetAddress address) {
        return address instanceof Inet4Address;
    }

    // Whether it is a V6 address.
    public static boolean address_is_v6_address(InetAddress address) {
        return address instanceof Inet6Address;
    }

    // Calculate the cidr address.
    @Nullable
    public static String address_calc_cidr_address(String address, String mask) {
        InetAddress r = address_calc_ip_address(address, mask, 0);
        if (r == null) {
            return null;
        }

        return bytes_to_address_string(r.getAddress());
    }

    // Calculate the first address.
    @Nullable
    public static String address_calc_first_address(String address, String mask) {
        InetAddress r = address_calc_ip_address(address, mask, 1);
        if (r == null) {
            return null;
        }

        return bytes_to_address_string(r.getAddress());
    }

    // Calculate the last address.
    @Nullable
    public static String address_calc_last_address(String address, String mask) {
        InetAddress r = address_calc_ip_address(address, mask, 2);
        if (r == null) {
            return null;
        }

        return bytes_to_address_string(r.getAddress());
    }

    // Calculate the broadcast address.
    @Nullable
    public static String address_calc_broadcast_address(String address, String mask) {
        InetAddress r = address_calc_ip_address(address, mask, 3);
        if (r == null) {
            return null;
        }

        return bytes_to_address_string(r.getAddress());
    }

    // Calculate the ip address.
    @Nullable
    private static InetAddress address_calc_ip_address(String address, String mask, int return_type) {
        return address_calc_ip_address(string_to_address(address), string_to_address(mask), return_type);
    }

    // Calculate the ip address.
    @Nullable
    private static InetAddress address_calc_ip_address(InetAddress address, InetAddress mask, int return_type) {
        if (!address_is_v4_address(address) || !address_is_v4_address(mask)) {
            return null;
        }

        Inet4Address address_v4 = (Inet4Address) address;
        Inet4Address mask_v4 = (Inet4Address) mask;

        int address_int = X.bytes_to_int(address_v4.getAddress());
        int mask_int = X.bytes_to_int(mask_v4.getAddress());

        address_int = IPAddressX.network_to_host_order(address_int);
        mask_int = IPAddressX.network_to_host_order(mask_int);

        int network_ip = address_int & mask_int;
        if (return_type == 1) { // first etc.
            network_ip++;
        } else if (return_type == 2) { // last etc.
            int __broadcast_ip = network_ip | (~network_ip & 0xff);
            network_ip = __broadcast_ip - 1;
        } else if (return_type == 3) { // board cast etc.
            network_ip = network_ip | (~network_ip & 0xff);
        }

        network_ip = IPAddressX.host_to_network_order(network_ip);

        byte[] network_ip_bytes = X.int_to_bytes(network_ip);
        return address_of(network_ip_bytes);
    }

    // Convert prefix to netmask v4.
    public static String prefix_to_netmask_v4(int prefix) {
        return libopenppp2.c.prefix_to_netmask(true, prefix);
    }

    // Convert prefix to netmask v6.
    public static String prefix_to_netmask_v6(int prefix) {
        return libopenppp2.c.prefix_to_netmask(false, prefix);
    }

    // Convert netmask to prefix.
    public static int netmask_to_prefix(String address) {
        return netmask_to_prefix(string_to_address(address));
    }

    // Convert netmask to prefix.
    public static int netmask_to_prefix(InetAddress address) {
        if (address == null) {
            return -1;
        }

        if (address_is_v4_address(address)) {
            Inet4Address p = (Inet4Address) address;
            int r = libopenppp2.c.netmask_to_prefix(p.getAddress());
            if (r < 0) {
                r = 32;
            }
            return r;
        } else if (address_is_v6_address(address)) {
            Inet6Address p = (Inet6Address) address;
            int r = libopenppp2.c.netmask_to_prefix(p.getAddress());
            if (r < 0) {
                r = 128;
            }
            return r;
        } else {
            return -1;
        }
    }

    // Convert address to string.
    public static String address_to_string(InetAddress address) {
        if (address == null) {
            return null;
        } else {
            return bytes_to_address_string(address.getAddress());
        }
    }

    // If same subnet.
    public static boolean address_if_subnet(String ip_x, String ip_y, String mask) {
        return libopenppp2.c.if_subnet(ip_x, ip_y, mask);
    }

    // If same subnet.
    public static boolean address_if_subnet(InetAddress ip_x, InetAddress ip_y, InetAddress mask) {
        return address_if_subnet(address_to_string(ip_x), address_to_string(ip_y), address_to_string(mask));
    }

    // If same address.
    public static boolean address_if_same(InetAddress x, InetAddress y) {
        if (x == y) {
            return true;
        }

        String xs = address_to_string(x);
        String ys = address_to_string(y);
        return Objects.equals(xs, ys);
    }
}
