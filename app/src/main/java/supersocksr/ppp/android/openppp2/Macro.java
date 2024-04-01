package supersocksr.ppp.android.openppp2;

import android.os.Build;
import android.system.OsConstants;

public final class Macro {
    public static final int MTU = 1500;
    public static final int ENONET = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? OsConstants.ENONET : 64;
    public static final int TENTHOUSAND = 10000;

    public static final int PPP_LISTEN_BACKLOG = 511;
    public static final int PPP_TCP_CONNECT_TIMEOUT = 5;
    public static final int PPP_TCP_INACTIVE_TIMEOUT = 300;
    public static final int PPP_UDP_INACTIVE_TIMEOUT = 72;
    public static final int PPP_DNS_SYS_PORT = 53;
    public static final int PPP_UDP_TIMER_INTERVAL = 10;
    public static final int PPP_DEFAULT_DNS_TIMEOUT = 4;
    public static final int PPP_DEFAULT_HTTP_PROXY_PORT = 3128; // android: 3128, no 8080.
    public static final int PPP_NOTIFICATION_CHANNEL_ID = 1;

    public static final int RUN_OK = 0;
    public static final int RUN_ARG_VPN_SERVICE_IS_NULL = 1;
    public static final int RUN_VPN_UNABLE_TO_INITIALIZED = 2;
    public static final int RUN_UNKNOWN = 3;
    public static final int RUN_INTERRUPT = 4;
    public static final int RUN_RERUN_VPN_CLIENT = 5;
    public static final int RUN_NETWORK_INTERFACE_NOT_CONFIGURED = 6;
    public static final int RUN_APP_CONFIGURATION_NOT_CONFIGURED = 7;
    public static final int RUN_VPN_OPEN_TUNTAP_FAIL = 8;
    public static final int RUN_VPN_OPEN_VETHERNET_FAIL = 9;
    public static final int RUN_ALLOCATED_MEMORY = 10;
    public static final int RUN_ARG_VPN_RUNNABLE_IS_NULL = 11;
    public static final int RUN_NOT_ANY_ACTIVE_NETWORK = 12;
    public static final int RUN_ARG_PREPARE_IS_NULL = 13;
    public static final int RUN_ENSURE_POWER_WAKE_LOCK_ERROR = 14;
    public static final int RUN_VPN_LINK_CONFIGURATION_IS_NULL = 15;
    public static final int RUN_VPN_SET_APP_CONFIGURATION_FAIL = 16;
    public static final int RUN_VPN_SET_BYPASS_IP_LIST_FAIL = 17;
    public static final int RUN_VPN_GET_PENDING_INTENT_FAIL = 18;
    public static final int RUN_VPN_ESTABLISH_VPN_BUILDER_FAIL = 19;
    public static final int RUN_VPN_NETWORK_INTERFACE_IP = 20;
    public static final int RUN_VPN_NETWORK_INTERFACE_GW = 21;
    public static final int RUN_VPN_NETWORK_INTERFACE_MASK = 22;
    public static final int RUN_VPN_NETWORK_INTERFACE_SUBNET = 23;
}
