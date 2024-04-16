package supersocksr.ppp.android.openppp2;

import android.app.PendingIntent;
import android.content.Intent;
import android.net.ProxyInfo;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.system.OsConstants;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import supersocksr.ppp.android.MainActivity;
import supersocksr.ppp.android.c.libopenppp2;
import supersocksr.ppp.android.openppp2.i.NetworkInterface;
import supersocksr.ppp.android.openppp2.i.NetworkStatistics;

public abstract class VPNConnection extends VpnService {
    public static final String ACTION_VPN_CTL = "supersocksr.ppp.android.openppp2.vpn.ctl";
    public static final String VPN_CTL_TAG = "CTL";
    public static final int VPN_CTL_START = 1;
    public static final int VPN_CTL_EXIT = 2;
    public static final int VPN_CTL_STATISTICS = 3;

    private final AtomicBoolean open_ = new AtomicBoolean(false);
    private ParcelFileDescriptor tun_ = null;
    private PowerManager.WakeLock power_wake_lock_ = null;

    // When the VPN service is revoke.
    @Override
    public void onRevoke() {
        stop();
        X.call_void(super::onRevoke);
    }

    // When the VPN service is destroy.
    @Override
    public void onDestroy() {
        stop();
        X.call_void(super::onDestroy);
    }

    // Make sure the power wake lock is on.
    private int ensure_power_wake_lock() {
        PowerManager.WakeLock power_wake_lock = this.power_wake_lock_;
        if (power_wake_lock != null) {
            return 0;
        }

        String app_name = ApplicationX.application_get_app_name(VPNConnection.this);
        if (TextUtils.isEmpty(app_name)) {
            return -1;
        }

        power_wake_lock = PowerX.power_open_wake_lock(VPNConnection.this, app_name, -1);
        if (power_wake_lock == null) {
            return -1;
        }

        this.power_wake_lock_ = power_wake_lock;
        return 1;
    }

    // Clear power wake lock.
    private PowerManager.WakeLock clear_power_wake_lock() {
        PowerManager.WakeLock power_wake_lock = this.power_wake_lock_;
        if (power_wake_lock != null) {
            this.power_wake_lock_ = null;
        }

        return power_wake_lock;
    }

    // Clear tun.
    private ParcelFileDescriptor clear_tun() {
        ParcelFileDescriptor tun = this.tun_;
        if (tun != null) {
            this.tun_ = null;
        }

        return tun;
    }

    // If the prefix value of mask overflows the range (16-30), correct and rewrite the mask.
    @Nullable
    private InetAddress prepare_rewrite_mask_if_out_of_range(VPNLinkConfiguration config, InetAddress mask) {
        int prefix = IPAddressX.netmask_to_prefix(mask);
        if (prefix < 16) {
            prefix = 16;
        } else if (prefix > 30) {
            prefix = 30;
        }

        String mask_string = IPAddressX.prefix_to_netmask_v4(prefix);
        mask = IPAddressX.string_to_address(mask_string);
        if (mask != null) {
            config.SubnetAddress = mask_string;
        }

        return mask;
    }

    // Calculate the current default gateway IP address by IP and MASK.
    private InetAddress prepare_calc_gw(@NonNull VPNLinkConfiguration config) {
        String gw_string = IPAddressX.address_calc_first_address(config.IPAddress, config.SubnetAddress);
        InetAddress gw = IPAddressX.string_to_address(gw_string);
        if (gw != null) {
            config.GatewayServer = gw_string;
        }

        return gw;
    }

    // Preparatory processing of vpn operation.
    private int prepare_x(VpnService service, NetworkListener network_listener) {
        VpnService.Builder builder = new VpnService.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(true);
        }

        // If the VPN link configuration fails to be obtained, return the command. Otherwise, prepare to configure the VPN instance.
        VPNLinkConfiguration config = load();
        if (config == null) {
            return Macro.RUN_VPN_LINK_CONFIGURATION_IS_NULL;
        } else {
            network_listener.allow_no_activity_network(config.AllowNoActivityNetwork);
        }

        // Check the VPN network interface configuration.
        InetAddress ip = IPAddressX.string_to_address(config.IPAddress);
        InetAddress gw = IPAddressX.string_to_address(config.GatewayServer);
        InetAddress mask = IPAddressX.string_to_address(config.SubnetAddress);
        if (!IPAddressX.address_is_v4_address(ip) || IPAddressX.address_is_loopback_address(ip)) {
            return Macro.RUN_VPN_NETWORK_INTERFACE_IP;
        } else if (!IPAddressX.address_is_v4_address(gw) || IPAddressX.address_is_loopback_address(gw)) {
            return Macro.RUN_VPN_NETWORK_INTERFACE_GW;
        } else if (!IPAddressX.address_is_v4_address(mask) || IPAddressX.address_is_loopback_address(mask)) {
            return Macro.RUN_VPN_NETWORK_INTERFACE_MASK;
        } else {
            // Prefix exceeds the expected range, the network mask needs to be rewritten.
            mask = prepare_rewrite_mask_if_out_of_range(config, mask);
            if (mask == null) {
                return Macro.RUN_VPN_NETWORK_INTERFACE_MASK;
            } else if (IPAddressX.address_if_same(ip, gw) || !IPAddressX.address_if_subnet(ip, gw, mask)) {
                gw = prepare_calc_gw(config);
                if (gw == null) {
                    return Macro.RUN_VPN_NETWORK_INTERFACE_SUBNET;
                }
            }
        }

        String cidr = IPAddressX.address_calc_cidr_address(config.IPAddress, config.SubnetAddress);
        if (X.is_empty(cidr)) {
            return Macro.RUN_VPN_NETWORK_INTERFACE_SUBNET;
        }

        // Attempts to set up the VPN app profile, and returns a specific error code if it fails.
        if (!VPN.vpn_set_app_configuration(config.VPNConfiguration)) {
            return Macro.RUN_VPN_SET_APP_CONFIGURATION_FAIL;
        }

        // Attempt to load the list of all IP routes to be bypassed by the VPN into the VPN underlying configuration.
        if (!VPN.vpn_set_bypass_ip_list(config.bypass_ip_list_do_load_all())) {
            return Macro.RUN_VPN_SET_BYPASS_IP_LIST_FAIL;
        } else {
            // Add all dns server list to the vpn service.
            Set<String> dns_addresses = config.DnsAddresses;
            if (!X.is_empty(dns_addresses)) {
                for (String dns_address : dns_addresses) {
                    InetAddress dns_ip = IPAddressX.string_to_address(dns_address);
                    if (dns_ip != null) {
                        builder.addDnsServer(dns_ip);
                    }
                }
            }
        }

        // Configure basic attributes of the network interface.
        int prefix = IPAddressX.netmask_to_prefix(config.SubnetAddress);
        builder.setMtu(Macro.MTU);
        builder.setBlocking(true);
        builder.allowFamily(OsConstants.AF_INET);
        builder.addAddress(config.IPAddress, prefix);
        VPN.vpn_set_dns_rules_list(config.DNSRuleList);

        // Set the VPN route table.
        builder.addRoute(cidr, prefix);
        builder.addRoute("0.0.0.0", 0);
        builder.addRoute("0.0.0.0", 1);
        builder.addRoute("128.0.0.0", 1);

        // Automatically set the Http Proxy option for current systems (only supported on >= Android 10).
        if (config.AtomicHttpProxySet) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ProxyInfo proxy_info = IPAddressX.proxy_info_build_direct_proxy("127.0.0.1", config.VPNConfiguration.client.http_proxy.port);
                if (proxy_info != null) {
                    builder.setHttpProxy(proxy_info);
                }
            }
        }

        // Set those web apps to go with or without the VPN network.
        PackageX.package_allowed_application_packages(service, builder, config.AllowedApplicationPackageNames, true);
        PackageX.package_allowed_application_packages(service, builder, config.DisallowedApplicationPackageNames, false);

        // Configure the Intent associated with the current VPN instance.
        PendingIntent pending_intent = PackageX.package_get_pending_intent(service, MainActivity.class);
        if (pending_intent == null) {
            return Macro.RUN_VPN_GET_PENDING_INTENT_FAIL;
        }

        builder.setConfigureIntent(pending_intent);
        builder.setSession(ApplicationX.application_get_app_name(service));

        // Create the tun/tap instance file descriptor handle for the VPN virtual NIC.
        ParcelFileDescriptor tun = X.call(builder::establish, null).item2;
        if (tun == null) {
            return Macro.RUN_VPN_ESTABLISH_VPN_BUILDER_FAIL;
        }

        // Check whether the tun file descriptor handle is valid.
        FileDescriptor tun_fd = tun.getFileDescriptor();
        if (tun_fd == null || !tun_fd.valid()) {
            X.close(tun);
            return Macro.RUN_UNKNOWN;
        }

        // New and build VPN interface configuration information.
        NetworkInterface network_interface = new NetworkInterface();
        network_interface.block_quic = config.BlockQUIC;
        network_interface.static_mode = config.StaticMode;
        network_interface.gw = config.GatewayServer;
        network_interface.ip = config.IPAddress;
        network_interface.mask = config.SubnetAddress;
        network_interface.vnet = config.VirtualSubnet;
        network_interface.tun = FileX.file_descriptor_to_int(tun_fd);
        if (!VPN.vpn_set_flash_mode(config.FlashMode)) {
            X.close(tun);
            return Macro.RUN_UNKNOWN;
        }

        // Set the network interface to the VPN loopback.
        int err = VPN.vpn_set_network_interface(network_interface);
        if (err == libopenppp2.LIBOPENPPP2_ERROR_NEW_NETWORKINTERFACE_FAIL) {
            X.close(tun);
            return Macro.RUN_ALLOCATED_MEMORY;
        }

        this.tun_ = tun;
        return Macro.RUN_OK;
    }

    // Release vpn connections.
    private boolean release_x() {
        synchronized (VPNConnection.this) {
            // The atom sets the current open Boolean flag and returns the atomic values between the Settings.
            boolean opened = this.open_.getAndSet(false);
            if (!opened) {
                return false;
            } else {
                // Cancel the VPN network start and statistics event callback function binding.
                VPN.c.NetworkStart = null;
                VPN.c.NetworkStatistics = null;
            }

            // Clear and return the reference to tun handle.
            ParcelFileDescriptor tun = clear_tun();

            // Clear and return the reference to power wake lock.
            PowerManager.WakeLock power_wake_lock = clear_power_wake_lock();

            // Forced to stop the current VPN services.
            VPN.c.stop();

            // Try to close the power wake lock.
            PowerX.power_close_wake_lock(power_wake_lock);

            // Clear the current VPN notification mounted in the notice bar.
            NotimicationX.notification_clear_to_notice_bar(VPNConnection.this, Macro.PPP_NOTIFICATION_CHANNEL_ID);

            // Try to close the VPN virtual network card instance handle that is currently open.
            X.close(tun);
            return true;
        }
    }

    // VPN run exit, but probably because the vpn run failure,
    // So you need to determine the cause of the exit.
    protected void exit(int reason) {
        // Forcibly stop VPN links.
        release_x();

        // Forced to stop the current VPN services.
        stop();

        // Broadcasts the current vpn exit event to receivers that subscribe to the broadcast.
        X.call_void(() -> sendBroadcast(new Intent(ACTION_VPN_CTL)
                .putExtra(VPN_CTL_TAG, VPN_CTL_EXIT)
                .putExtra("reason", reason)));
    }

    // Open and run vpn connections.
    protected int open() {
        synchronized (VPNConnection.this) {
            // If the VPN client is already open, a repeat error is returned.
            int err = Macro.RUN_OK;
            if (this.open_.get()) {
                return Macro.RUN_RERUN_VPN_CLIENT;
            }

            // Ensure that the power wake lock is currently enabled.
            int status = ensure_power_wake_lock();
            if (status < 0) {
                return Macro.RUN_ENSURE_POWER_WAKE_LOCK_ERROR;
            }

            // Try to turn on and run the VPN service.
            err = VPN.c.run(VPNConnection.this, this::prepare_x, this::exit);
            if (err == Macro.RUN_OK) {
                this.open_.set(true);
                VPN.c.NetworkStart = (ignored) -> this.ready();
                VPN.c.NetworkStatistics = (ignored, e) -> this.statistics(e);
            } else {
                // Clear and return the reference to tun handle.
                ParcelFileDescriptor tun = clear_tun();

                // Try to close the VPN virtual network card instance handle that is currently open.
                X.close(tun);

                // If power_wake lock is currently re-instantiated, clear is executed and its reference is returned.
                if (status > 0) {
                    // Clear and return the reference to power wake lock.
                    PowerManager.WakeLock power_wake_lock = clear_power_wake_lock();

                    // Try to close the power wake lock.
                    PowerX.power_close_wake_lock(power_wake_lock);
                }

                // This is the internal interface of the framework, and the three parties should not call it.
                libopenppp2.c.clear_configure();
            }

            return err;
        }
    }

    // Stop vpn connections.
    protected void stop() {
        VPN.c.stop();
    }

    // Statistics on network traffic reported per second by the VPN.
    protected void statistics(NetworkStatistics e) {
        X.call_void(() -> sendBroadcast(new Intent(ACTION_VPN_CTL)
                .putExtra(VPN_CTL_TAG, VPN_CTL_STATISTICS)
                .putExtra("IN", e.IN)
                .putExtra("OUT", e.OUT)
                .putExtra("RX", e.RX)
                .putExtra("TX", e.TX)));
    }

    // Get the actual status of the current VPN operations.
    // Values:
    //  LIBOPENPPP2_LINK_STATE_ESTABLISHED
    //  LIBOPENPPP2_LINK_STATE_UNKNOWN
    //  LIBOPENPPP2_LINK_STATE_CLIENT_UNINITIALIZED
    //  LIBOPENPPP2_LINK_STATE_EXCHANGE_UNINITIALIZED
    //  LIBOPENPPP2_LINK_STATE_RECONNECTING
    //  LIBOPENPPP2_LINK_STATE_CONNECTING
    //  LIBOPENPPP2_LINK_STATE_APPLICATIION_UNINITIALIZED
    protected int state() {
        return VPN.vpn_get_link_state();
    }

    // VPN client session has been opened, correctly and started and ready.
    protected void ready() {
        X.call_void(() -> sendBroadcast(new Intent(ACTION_VPN_CTL)
                .putExtra(VPN_CTL_TAG, VPN_CTL_START)));
    }

    // Get the current VPN service handler, which may be null.
    protected Handler handler() {
        return VPN.c.handler();
    }

    // Load a reference to the VPN link configurations.
    protected abstract VPNLinkConfiguration load();
}
