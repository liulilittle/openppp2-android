package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.net.Network;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import supersocksr.ppp.android.c.libopenppp2;
import supersocksr.ppp.android.openppp2.i.EthernetInformation;
import supersocksr.ppp.android.openppp2.i.IFunc_2;
import supersocksr.ppp.android.openppp2.i.LinkOf;
import supersocksr.ppp.android.openppp2.i.NetworkInterface;
import supersocksr.ppp.android.openppp2.i.NetworkStartEventHandler;
import supersocksr.ppp.android.openppp2.i.NetworkStatisticsEventHandler;
import supersocksr.ppp.android.openppp2.i.RunAsynchronousCallback;

// The user can call the interface provided by the VPN engine to realize the specific function.
public final class VPN {
    // For the current instance, the VPN engine includes the libopenppp2 native interface mapping library.
    // Multiple instances cannot be created. Instances must be constructed statically to reduce unnecessary memory fragmentation.
    @SuppressLint("StaticFieldLeak")
    public static final VPN c = new VPN();
    // VPN engine member class private variables, the three parties must not maliciously modify,
    // Intrusive programming, change the value of the internal member.
    private final HashMap<Integer, Runnable> posts_ = new HashMap<>();
    private final AtomicInteger aid_ = new AtomicInteger(0);
    private final LIBOPENPPP2_INTERNAL internal_ = new LIBOPENPPP2_INTERNAL();
    // VPN transmission layer traffic statistics event processor.
    public NetworkStatisticsEventHandler NetworkStatistics;
    // VPN start event processor.
    public NetworkStartEventHandler NetworkStart;
    private int session_id_ = 0;
    private VpnService service_ = null;
    private Handler handler_ = null;
    private Network network_ = null;
    private NetworkListener network_listener_ = null;

    // Get a list of currently recommended default ssl and tls protocol cipher suites.
    public static String get_default_cipher_suites() {
        return libopenppp2.c.get_default_ciphersuites();
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
    public static int vpn_get_link_state() {
        return libopenppp2.c.get_link_state();
    }

    // Get the status of the current VPN aggligator.
    // Values:
    // LIBOPENPPP2_AGGLIGATOR_STATE_NONE
    // LIBOPENPPP2_AGGLIGATOR_STATE_UNKNOWN
    // LIBOPENPPP2_AGGLIGATOR_STATE_ESTABLISHED
    // LIBOPENPPP2_AGGLIGATOR_STATE_RECONNECTING
    // LIBOPENPPP2_AGGLIGATOR_STATE_CONNECTING
    public static int vpn_get_aggligator_state() {
        return libopenppp2.c.get_aggligator_state();
    }

    // Set the VPN to flash mode, return < 0 Fails，= 0 Normal，> 0 Flash.
    public static boolean vpn_set_flash_mode(boolean value) {
        return libopenppp2.c.set_default_flash_type_of_service(value);
    }

    // Get the VPN to flash mode.
    public static int vpn_get_flash_mode() {
        return libopenppp2.c.is_default_flash_type_of_service();
    }

    // Set the VPN to tun safe queue mode.
    public static boolean vpn_set_tun_safe_queue(boolean value) {
        return libopenppp2.c.set_tun_safe_queue(value);
    }

    // Get the VPN to tun safe queue mode, return < 0 Fails，= 0 Unsafe，> 0 Safety.
    public static int vpn_get_tun_safe_queue() {
        return libopenppp2.c.is_tun_safe_queue();
    }

    // Get the ip route list that the current VPN configuration bypasses.
    public static String vpn_get_bypass_ip_list() {
        return libopenppp2.c.get_bypass_ip_list();
    }

    // Gets the duration of the current VPN operations.
    public static long vpn_get_duration_time() {
        return libopenppp2.c.get_duration_time();
    }

    // Set up dns domain name network card route rules.
    public static boolean vpn_set_dns_rules_list(String rules) {
        return libopenppp2.c.set_dns_rules_list(rules);
    }

    // Set the ip route list that the vpn bypasses.
    public static boolean vpn_set_bypass_ip_list(String bypass_ip_list) {
        return libopenppp2.c.set_bypass_ip_list(bypass_ip_list);
    }

    // Set the ip route list file that the vpn bypasses.
    public static boolean vpn_set_bypass_ip_list_file(String bypass_ip_list_file_path) {
        String bypass_ip_list = FileX.file_read_all_text(bypass_ip_list_file_path).trim();
        return vpn_set_bypass_ip_list(bypass_ip_list);
    }

    // Gets the local http-proxy server address.
    @Nullable
    public static InetSocketAddress vpn_get_http_proxy_address_endpoint() {
        String address_string = libopenppp2.c.get_http_proxy_address_endpoint();
        return IPAddressX.address_string_to_inet_socket_address(address_string);
    }

    // Gets the local socks-proxy server address.
    @Nullable
    public static InetSocketAddress vpn_get_socks_proxy_address_endpoint() {
        String address_string = libopenppp2.c.get_socks_proxy_address_endpoint();
        return IPAddressX.address_string_to_inet_socket_address(address_string);
    }

    // Gets the network interface settings for the current VPN configurations.
    @Nullable
    public static NetworkInterface vpn_get_network_interface() {
        String json = libopenppp2.c.get_network_interface();
        return JsonX.json_deserialize(json, NetworkInterface.class);
    }

    // Set the VPN virtual network interface information.
    public static int vpn_set_network_interface(int tun, boolean vnet, boolean block_quic, boolean static_mode, String ip, String mask, String gw) {
        return libopenppp2.c.set_network_interface(tun, vnet, block_quic, static_mode, ip, mask, gw);
    }

    // Set the VPN virtual network interface information.
    public static int vpn_set_network_interface(@NonNull NetworkInterface network_interface) {
        return libopenppp2.c.set_network_interface(network_interface.tun, network_interface.vnet, network_interface.block_quic, network_interface.static_mode, network_interface.ip, network_interface.mask, network_interface.gw);
    }

    // Get the Ethernet remote configuration information of the current VPN.
    public static EthernetInformation vpn_get_ethernet_information() {
        String json = libopenppp2.c.get_ethernet_information(false);
        return JsonX.json_deserialize(json, EthernetInformation.class);
    }

    // Enter the url and return the vpn's absolute link processing.
    @Nullable
    public static LinkOf vpn_link_of(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        String json = libopenppp2.c.link_of(url);
        return JsonX.json_deserialize(json, LinkOf.class);
    }

    // Get the app configuration currently being used by the VPN.
    @Nullable
    public static VPNConfiguration vpn_get_app_configuration() {
        String json = libopenppp2.c.get_app_configuration();
        return JsonX.json_deserialize(json, VPNConfiguration.class);
    }

    // Set the app configuration to vpn.
    public static boolean vpn_set_app_configuration(VPNConfiguration configuration) {
        if (configuration == null) {
            return false;
        }

        String json = JsonX.json_serialize(configuration);
        if (TextUtils.isEmpty(json)) {
            return false;
        }

        int err = libopenppp2.c.set_app_configuration(json);
        return err == libopenppp2.LIBOPENPPP2_ERROR_SUCCESS;
    }

    // This function is called when the VPN service triggers the onCreate function.
    public boolean onCreate(VpnService service) {
        if (service == null) {
            return false;
        }

        this.service_ = service;
        this.handler_ = new Handler();
        return true;
    }

    // Get the current VPN service object, which may be null.
    @Nullable
    public VpnService service() {
        return this.service_;
    }

    // Get the current VPN service handler, which may be null.
    @Nullable
    public Handler handler() {
        return this.handler_;
    }

    // Get the current VPN network object, which may be null.
    @Nullable
    public Network network() {
        return this.network_;
    }

    // Set the current VPN network object, which may be null.
    public void network(Network network) {
        this.network_ = network;
    }

    // Gets the current best active network listener.
    @Nullable
    public NetworkListener network_listener() {
        return this.network_listener_;
    }

    // Generates a new 32-bit ID digit.
    private int generate_id() {
        for (; ; ) {
            int sequence = aid_.incrementAndGet();
            if (sequence < 1) {
                aid_.set(0);
            } else {
                return sequence;
            }
        }
    }

    // Post function calls to VPN network protector background thread.
    public boolean post(Runnable runnable) {
        if (runnable == null) {
            return false;
        }

        synchronized (VPN.this) {
            VpnService service = this.service_;
            if (service == null) {
                return false;
            }

            Handler handler = this.handler_;
            if (handler == null) {
                return false;
            }

            int sequence = 0;
            for (; ; ) {
                sequence = generate_id();

                boolean contains = posts_.containsKey(sequence);
                if (!contains) {
                    break;
                }
            }

            boolean posted = libopenppp2.c.post(sequence);
            if (!posted) {
                return false;
            } else {
                posts_.put(sequence, runnable);
            }
        }
        return true;
    }

    // Get the internal implementation interface reference, third party secondary development users do not call the interface.
    public libopenppp2.libopenppp2_internal internal() {
        return internal_;
    }

    // The link of the VPN is stopped.
    public void stop() {
        NetworkListener network_listener = null;
        synchronized (VPN.this) {
            this.service_ = null;
            this.handler_ = null;
            this.network_ = null;
            this.posts_.clear();

            // Captures the network listener and resets the network listener to NULL.
            network_listener = this.network_listener_;
            if (network_listener != null) {
                this.network_listener_ = null;
            }
        }

        // If the network listener is not empty, try to call its release function to release the data it holds.
        if (network_listener != null) {
            network_listener.release();
        }

        // The openppp2 service is forcibly stopped.
        libopenppp2.c.stop();
    }

    // Try to run this VPN client process service.
    public int run(VpnService service, IFunc_2<VpnService, NetworkListener, Integer> prepare, RunAsynchronousCallback runnable) {
        // Check the validity of parameters passed in by the three parties running the VPN service.
        if (runnable == null) {
            return Macro.RUN_ARG_VPN_RUNNABLE_IS_NULL;
        } else if (service == null) {
            return Macro.RUN_ARG_VPN_SERVICE_IS_NULL;
        } else if (prepare == null) {
            return Macro.RUN_ARG_PREPARE_IS_NULL;
        }

        synchronized (VPN.this) {
            // First, determine whether the current VPN running state can try to run this VPN service.
            int status = VPN.vpn_get_link_state();
            if (status == libopenppp2.LIBOPENPPP2_LINK_STATE_APPLICATIION_UNINITIALIZED) {
                return Macro.RUN_VPN_UNABLE_TO_INITIALIZED;
            } else if (status == libopenppp2.LIBOPENPPP2_LINK_STATE_UNKNOWN) {
                return Macro.RUN_UNKNOWN;
            } else if (status != libopenppp2.LIBOPENPPP2_LINK_STATE_CLIENT_UNINITIALIZED) {
                return Macro.RUN_RERUN_VPN_CLIENT;
            } else if (this.service_ != null) {
                return Macro.RUN_RERUN_VPN_CLIENT;
            }

            // Set the current active network and available physical network to the VPN service.
            Network network = NetworkListener.network_get_active_network(service);
            if (network == null) {
                return Macro.RUN_NOT_ANY_ACTIVE_NETWORK;
            } else {
                Network[] networks = NetworkListener.network_get_all_networks(service, false, false);
                if (networks == null || networks.length < 1) {
                    return Macro.RUN_NOT_ANY_ACTIVE_NETWORK;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    service.setUnderlyingNetworks(networks);
                }
            }

            // Preparing the VPN environment deployment process.
            final NetworkListener network_listener = new NetworkListener();
            Integer E = prepare.handle(service, network_listener);
            if (E != Macro.RUN_OK) {
                return E;
            }

            // Deploy the logic control data that needs to be temporarily stored for the current VPN operation.
            final Handler handler = new Handler();
            final int session_id = vpn_generate_session_id();
            this.network_ = network;
            this.service_ = service;
            this.handler_ = handler;
            this.network_listener_ = network_listener;
            this.network_listener_.run();
            this.session_id_ = session_id;

            // Running a VPN network to protect background JVM threads is a basic requirement for embedding the openppp2 framework.
            final Awaitable awaitable = new Awaitable();
            new Thread(() -> {
                int err = Macro.RUN_INTERRUPT;
                if (awaitable.processed()) {
                    // Run the per-second tick update logical processing and invoke the openppp2 interface to run the network protection service.
                    int reason = libopenppp2.c.run(session_id);
                    stop();

                    // Convert the error code returned by openppp2 running the VPN client service to the simple error code of the JAR layer framework.
                    switch (reason) {
                        case libopenppp2.LIBOPENPPP2_ERROR_SUCCESS:
                            err = Macro.RUN_OK;
                            break;
                        case libopenppp2.LIBOPENPPP2_ERROR_IT_IS_RUNING:
                            err = Macro.RUN_RERUN_VPN_CLIENT;
                            break;
                        case libopenppp2.LIBOPENPPP2_ERROR_NETWORK_INTERFACE_NOT_CONFIGURED:
                            err = Macro.RUN_NETWORK_INTERFACE_NOT_CONFIGURED;
                            break;
                        case libopenppp2.LIBOPENPPP2_ERROR_APP_CONFIGURATION_NOT_CONFIGURED:
                            err = Macro.RUN_APP_CONFIGURATION_NOT_CONFIGURED;
                            break;
                        case libopenppp2.LIBOPENPPP2_ERROR_OPEN_TUNTAP_FAIL:
                            err = Macro.RUN_VPN_OPEN_TUNTAP_FAIL;
                            break;
                        case libopenppp2.LIBOPENPPP2_ERROR_OPEN_VETHERNET_FAIL:
                            err = Macro.RUN_VPN_OPEN_VETHERNET_FAIL;
                            break;
                        case libopenppp2.LIBOPENPPP2_ERROR_ALLOCATED_MEMORY:
                            err = Macro.RUN_ALLOCATED_MEMORY;
                            break;
                        default:
                            err = Macro.RUN_UNKNOWN;
                            break;
                    }
                }

                // Post the current vpn exit event to the main thread.
                vpn_post_on_exit(handler, session_id, err, runnable);
            }).start();

            boolean await_ok = awaitable.await();
            if (await_ok) {
                X.call_void(() -> handler.post(this::next));
                return Macro.RUN_OK;
            } else {
                return Macro.RUN_INTERRUPT;
            }
        }
    }

    // Generate a new session ID for the VPN link, but ensure that it is different from the session ID in use.
    private int vpn_generate_session_id() {
        for (; ; ) {
            int session_id = generate_id();
            if (session_id != this.session_id_) {
                return session_id;
            }
        }
    }

    // Post the current vpn exit event to the main thread.
    private void vpn_post_on_exit(Handler handler, int session_id, int reason, RunAsynchronousCallback runnable) {
        X.call_void(() -> handler.post(() -> {
            boolean callable = false;
            synchronized (VPN.this) {
                callable = this.session_id_ == session_id;
                if (callable) {
                    this.session_id_ = 0;
                }
            }

            if (callable) {
                runnable.handle(reason);
            }
        }));
    }

    // update framework processing is iterated once per second.
    private boolean next() {
        synchronized (VPN.this) {
            VpnService service = this.service_;
            if (service == null) {
                return false;
            }

            Handler handler = this.handler_;
            if (handler == null) {
                return false;
            }

            NetworkListener network_listener = this.network_listener_;
            if (network_listener == null) {
                return false;
            }

            return X.call_void(() -> handler.postDelayed(() -> {
                // If the current operation has been canceled.
                NetworkListener nl = VPN.this.network_listener_;
                if (nl == null || nl != network_listener) {
                    return;
                } else {
                    Handler h = VPN.this.handler_;
                    if (h != handler) {
                        return;
                    }
                }

                // Call the network listener and check the best active network at the moment!
                nl.update();
                next();
            }, 1000));
        }
    }

    private final class LIBOPENPPP2_INTERNAL implements libopenppp2.libopenppp2_internal {
        // Post to Network Protector background thread to call request sequence execution function internally.
        @Override
        public boolean post_exec(int sequence) {
            Runnable runnable = null;
            synchronized (VPN.this) {
                if (posts_.containsKey(sequence)) {
                    runnable = posts_.get(sequence);
                    posts_.remove(sequence);
                }
            }

            if (runnable == null) {
                return false;
            } else {
                runnable.run();
                return true;
            }
        }

        // Protect these socket file descriptors from VPN routes, resulting in loopback network problems.
        @Override
        public boolean protect(int fd) {
            if (fd == -1) {
                return false;
            }

            Network network = VPN.this.network_;
            if (network == null) {
                return false;
            }

            int err = bind_socket_to_network(network, fd);
            return err == 0;
        }

        // Statistics processing of network traffic from VPN.
        @Override
        public void statistics(String json) {
            // The following deserializes the relevant business logic only if the user subscribing to the network Statistics event handler.
            NetworkStatisticsEventHandler handler = VPN.this.NetworkStatistics;
            if (handler != null) {
                // Deserialization passes the network statistics json to JAVA objects from libopenppp2 via JNI reversecall Java functions.
                supersocksr.ppp.android.openppp2.i.NetworkStatistics statistics = JsonX.json_deserialize(json, supersocksr.ppp.android.openppp2.i.NetworkStatistics.class);
                if (statistics != null) {
                    handler.Handle(VPN.this, statistics);
                }
            }
        }

        // VPN session network began to succeed and ready to run.
        @Override
        public boolean start_exec(int key) {
            // Determine if the current start key is the same or if this is the transaction that was run in the last vpn session.
            synchronized (VPN.this) {
                if (VPN.this.session_id_ != key) {
                    return false;
                }
            }

            // Triggers the event that the VPN network is ready to run.
            NetworkStartEventHandler handler = VPN.this.NetworkStart;
            if (handler != null) {
                handler.Handle(VPN.this);
            }
            return true;
        }

        // Bind socket file descriptor instances to specific networks, such as 3G/4G/5G, WIFI, Bluetooth networks, etc.
        private int bind_socket_to_network(Network network, int fd) {
            if (network == null) {
                return -1;
            }

            // Gets the file descriptor fd handle of the current socket
            // And checks if it is already linked to the remote.
            if (FileX.file_get_socket_type(fd) == OsConstants.SOCK_STREAM) {
                FileDescriptor fileDescriptor = FileX.int_to_file_descriptor(fd);
                try {
                    InetAddress inetAddress = ((InetSocketAddress) Os.getpeername(fileDescriptor)).getAddress();
                    if (!inetAddress.isAnyLocalAddress()) {
                        return 1;
                    }
                } catch (ErrnoException e) {
                    if (e.errno != OsConstants.ENOTCONN) {
                        return e.errno;
                    }
                }

                // In Android M (6.0) and later operating systems, specific network-bound sockets are called.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        network.bindSocket(fileDescriptor);
                        return 0;
                    } catch (IOException e) {
                        Throwable cause = e.getCause();
                        if (cause != null) {
                            if (cause instanceof ErrnoException) {
                                ErrnoException ee = (ErrnoException) e.getCause();
                                if (ee.errno == Macro.ENONET || ee.errno == OsConstants.EACCES || ee.errno == OsConstants.EPERM) {
                                    return ee.errno;
                                }
                            }
                        }
                    }
                }
            }

            // If the socket fails to be bound to a specific network, the protect function provided by the vpn service is invoked to avoid network loopback problems.
            return X.call(() -> {
                VpnService service = VPN.this.service();
                if (service == null) {
                    return -1;
                } else {
                    service.protect(fd);
                    return +0;
                }
            }, -1).item2;
        }
    }
}
