package supersocksr.ppp.android.c;

import supersocksr.ppp.android.openppp2.VPN;

public final class libopenppp2 {
    public static final int LIBOPENPPP2_LINK_STATE_ESTABLISHED = 0;
    public static final int LIBOPENPPP2_LINK_STATE_UNKNOWN = 1;
    public static final int LIBOPENPPP2_LINK_STATE_CLIENT_UNINITIALIZED = 2;
    public static final int LIBOPENPPP2_LINK_STATE_EXCHANGE_UNINITIALIZED = 3;
    public static final int LIBOPENPPP2_LINK_STATE_RECONNECTING = 4;
    public static final int LIBOPENPPP2_LINK_STATE_CONNECTING = 5;
    public static final int LIBOPENPPP2_LINK_STATE_APPLICATIION_UNINITIALIZED = 6;
    // COMMON
    public static final int LIBOPENPPP2_ERROR_SUCCESS = 0;
    public static final int LIBOPENPPP2_ERROR_UNKNOWN = 1;
    public static final int LIBOPENPPP2_ERROR_ALLOCATED_MEMORY = 2;
    public static final int LIBOPENPPP2_ERROR_APPLICATIION_UNINITIALIZED = 3;
    // SET_APP_CONFIGURATION
    public static final int LIBOPENPPP2_ERROR_NEW_CONFIGURATION_FAIL = 101;
    public static final int LIBOPENPPP2_ERROR_ARG_CONFIGURATION_STRING_IS_NULL_OR_EMPTY = 102;
    public static final int LIBOPENPPP2_ERROR_ARG_CONFIGURATION_STRING_NOT_IS_JSON_OBJECT_STRING = 103;
    public static final int LIBOPENPPP2_ERROR_ARG_CONFIGURATION_STRING_CONFIGURE_ERROR = 104;
    // SET_NETWORK_INTERFACE
    public static final int LIBOPENPPP2_ERROR_NEW_NETWORKINTERFACE_FAIL = 201;
    public static final int LIBOPENPPP2_ERROR_ARG_TUN_IS_INVALID = 202;
    public static final int LIBOPENPPP2_ERROR_ARG_IP_IS_NULL_OR_EMPTY = 203;
    public static final int LIBOPENPPP2_ERROR_ARG_MASK_IS_NULL_OR_EMPTY = 204;
    public static final int LIBOPENPPP2_ERROR_ARG_IP_IS_NOT_AF_INET_FORMAT = 205;
    public static final int LIBOPENPPP2_ERROR_ARG_MASK_IS_NOT_AF_INET_FORMAT = 206;
    public static final int LIBOPENPPP2_ERROR_ARG_MASK_SUBNET_IP_RANGE_GREATER_65535 = 207;
    public static final int LIBOPENPPP2_ERROR_ARG_IP_IS_INVALID = 208;
    // RUN
    public static final int LIBOPENPPP2_ERROR_IT_IS_RUNING = 301;
    public static final int LIBOPENPPP2_ERROR_NETWORK_INTERFACE_NOT_CONFIGURED = 302;
    public static final int LIBOPENPPP2_ERROR_APP_CONFIGURATION_NOT_CONFIGURED = 303;
    public static final int LIBOPENPPP2_ERROR_OPEN_VETHERNET_FAIL = 304;
    public static final int LIBOPENPPP2_ERROR_OPEN_TUNTAP_FAIL = 305;
    public static final int LIBOPENPPP2_ERROR_VETHERNET_PPPD_THREAD_NOT_RUNING = 306;
    // STOP
    public static final int LIBOPENPPP2_ERROR_IT_IS_NOT_RUNING = 401;
    public static final libopenppp2 c = new libopenppp2(VPN.c.internal());

    static {
        System.loadLibrary("openppp2");
    }

    private final libopenppp2_internal internal_;

    private libopenppp2(libopenppp2_internal internal) {
        internal_ = internal;
    }

    public static boolean post_exec(int sequence) {
        return c.internal_.post_exec(sequence);
    }

    public static boolean start_exec(int key) {
        return c.internal_.start_exec(key);
    }

    public static void statistics(String json) {
        c.internal_.statistics(json);
    }

    public static boolean protect(int sockfd) {
        return c.internal_.protect(sockfd);
    }

    public native int stop();

    public native int run(int key);

    public native boolean post(int sequence);

    public native String get_network_interface();

    public native String get_bypass_ip_list();

    public native boolean set_bypass_ip_list(String iplist);

    public native boolean set_dns_rules_list(String rules);

    public native int set_network_interface(int tun, int mux, boolean vnet, boolean block_quic, boolean static_mode, String ip, String mask, String gw);

    public native int set_app_configuration(String configurations /* configurations is appsettings.json */);

    public native String get_app_configuration();

    public native long get_duration_time();

    public native int get_link_state();

    public native int get_aggligator_state();

    public native String get_default_ciphersuites();

    public native String bytes_to_address_string(byte[] address);

    public native byte[] string_to_address_bytes(String address);

    public native String link_of(String url);

    public native String get_ethernet_information(boolean default_);

    public native boolean ip_address_string_is_invalid(String address);

    public native boolean set_default_flash_type_of_service(boolean flash_mode);

    public native int is_default_flash_type_of_service();

    public native void clear_configure();

    public native String get_http_proxy_address_endpoint();

    public native String get_socks_proxy_address_endpoint();

    public native boolean if_subnet(String ip1_, String ip2_, String mask_);

    public native int netmask_to_prefix(byte[] address_);

    public native String prefix_to_netmask(boolean v4_or_v6, int prefix_);

    public native int socket_get_socket_type(int fd_);

    public interface libopenppp2_internal {
        boolean post_exec(int sequence);

        boolean protect(int sockfd);

        void statistics(String json);

        boolean start_exec(int key);
    }
}
