package supersocksr.ppp.android.openppp2;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.VpnService;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import supersocksr.ppp.android.c.libopenppp2;

public final class NetworkListener {
    @SuppressLint("InlinedApi")
    private static final int[] TRANSPORT_PRIORITY = { // NetworkCapabilities.TRANSPORT_VPN
            NetworkCapabilities.TRANSPORT_ETHERNET, NetworkCapabilities.TRANSPORT_WIFI, NetworkCapabilities.TRANSPORT_BLUETOOTH, NetworkCapabilities.TRANSPORT_WIFI_AWARE, NetworkCapabilities.TRANSPORT_LOWPAN, NetworkCapabilities.TRANSPORT_USB, NetworkCapabilities.TRANSPORT_CELLULAR,};
    private boolean allow_no_activity_network_ = true;

    // Gets whether no active network is currently allowed, otherwise the VPN connection will be automatically disconnected when this happens.
    public boolean allow_no_activity_network() {
        return this.allow_no_activity_network_;
    }

    // Set whether no active network is currently allowed. Otherwise, the VPN connection will be automatically disconnected.
    public void allow_no_activity_network(boolean allow) {
        this.allow_no_activity_network_ = allow;
    }

    // Choose all available and online network card equipment.
    @Nullable
    private static NetworkCapabilities network_check_is_available_network(@NotNull ConnectivityManager connectivityManager, @NotNull Network network) {
        NetworkCapabilities networkCapabilities;
        try {
            networkCapabilities = connectivityManager.getNetworkCapabilities(network);
        } catch (Throwable ignored) {
            return null;
        }

        if (networkCapabilities == null) {
            return null;
        }

        try {
            if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                return null;
            }

            if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                return null;
            }

            if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return null;
            }

            if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                return null;
            }
        } catch (Throwable ignored) {
            return null;
        }
        return networkCapabilities;
    }

    // Choose all available and online network card equipment.
    @NotNull
    private static Map<Integer, Network> network_choose_all_available_network(@NotNull ConnectivityManager connectivityManager, @Nullable Network network, @NotNull Network[] networks) {
        Map<Integer, Network> chooses = new HashMap<Integer, Network>();
        if (network != null) {
            if (network_check_is_available_network(connectivityManager, network) == null) {
                network = null;
            }
        }

        for (Network now : networks) {
            if (network != null) {
                if (now.equals(network)) {
                    chooses.clear();
                    break;
                }
            }

            NetworkCapabilities networkCapabilities = network_check_is_available_network(connectivityManager, now);
            if (networkCapabilities == null) {
                continue;
            }

            for (int transport = 0; transport < TRANSPORT_PRIORITY.length; transport++) {
                if (transport == NetworkCapabilities.TRANSPORT_VPN) {
                    continue;
                }

                try {
                    if (!networkCapabilities.hasTransport(transport)) {
                        continue;
                    }
                } catch (Throwable ignored) {
                    continue;
                }

                if (!chooses.containsKey(transport)) {
                    chooses.put(transport, now);
                }
                break;
            }
        }
        return chooses;
    }

    // Gets the current preferred active physical network interface.
    public static Network network_get_active_network(Context context) {
        return network_choose_available_network(context, null);
    }

    // Select a physical network interface that is available and active.
    @androidx.annotation.Nullable
    private static Network network_choose_available_network(Context context, Network[] allNetworks) {
        ConnectivityManager connectivityManager = network_get_connectivity_manager(context);
        if (connectivityManager == null) {
            return null;
        }

        do {
            Network[][] networkss = network_get_all_networks_array(context, allNetworks, false, true);
            if (networkss == null || networkss.length < 2) {
                break;
            }

            if (allNetworks == null || allNetworks.length < 1) {
                allNetworks = networkss[1];
            }

            Network[] networks = networkss[0];
            if (networks != null && networks.length > 0) {
                return network_choose_available_network(networks[0], network_choose_all_available_network(connectivityManager, networks[0], networks));
            }

            networkss = network_get_all_networks_array(context, allNetworks, false, false);
            if (networkss == null || networkss.length < 2) {
                break;
            }

            networks = networkss[0];
            if (networks != null && networks.length > 0) {
                return network_choose_available_network(networks[0], network_choose_all_available_network(connectivityManager, networks[0], networks));
            }
        } while (false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return null;
        }

        try {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) {
                return null;
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            if (capabilities == null) {
                return null;
            }

            if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                return null;
            }

            if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                return null;
            }
            return network;
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Select a physical network interface that is available and active.
    @Nullable
    private static Network network_choose_available_network(Network network, Map<Integer, Network> networks) {
        if (networks == null || networks.size() < 1) {
            return network;
        }

        for (int transport : TRANSPORT_PRIORITY) {
            if (!networks.containsKey(transport)) {
                continue;
            }

            Network value = networks.get(transport);
            if (value != null) {
                return value;
            }
        }
        return network;
    }

    // Gets the current system network link manager object.
    @SuppressLint("WrongConstant")
    public static ConnectivityManager network_get_connectivity_manager(Context context) {
        if (context == null) {
            return null;
        }

        try {
            Object service = context.getSystemService("connectivity");
            if (service == null) {
                return null;
            } else {
                return (ConnectivityManager) service;
            }
        } catch (Throwable ignored) {
            return null;
        }
    }

    // Get a list of all online network card links.
    @androidx.annotation.Nullable
    @Nullable
    public static Network[] network_get_all_networks(Context context, boolean capabilitiesVPN, boolean validatedNetwork) {
        Network[][] networks_array = network_get_all_networks_array(context, capabilitiesVPN, validatedNetwork);
        if (networks_array == null || networks_array.length < 1) {
            return null;
        } else {
            return networks_array[0];
        }
    }

    // Get a list of all online network card links.
    @Nullable
    public static Network[][] network_get_all_networks_array(Context context, boolean capabilitiesVPN, boolean validatedNetwork) {
        return network_get_all_networks_array(context, null, capabilitiesVPN, validatedNetwork);
    }

    // List of all online network card links from the specified network interface array.
    @androidx.annotation.Nullable
    @Contract("null, _, _, _ -> fail")
    public static @Nullable Network[][] network_get_all_networks_array(Context context, Network[] networks, boolean capabilitiesVPN, boolean validatedNetwork) {
        if (context == null) {
            throw new InvalidParameterException("context");
        }

        ConnectivityManager connectivityManager = network_get_connectivity_manager(context);
        if (connectivityManager == null) {
            return null;
        }

        if (networks == null) {
            try {
                networks = connectivityManager.getAllNetworks();
            } catch (Throwable ignored) {
                return null;
            }
        }

        if (networks.length < 1) {
            return new Network[][]{networks, networks};
        }

        if (capabilitiesVPN && !validatedNetwork) {
            return new Network[][]{networks, networks};
        }

        ArrayList<Network> availableNetworks = new ArrayList<Network>();
        for (Network network : networks) {
            if (network == null) {
                continue;
            }

            NetworkCapabilities capabilities = null;
            try {
                capabilities = connectivityManager.getNetworkCapabilities(network);
            } catch (Throwable ignored) {
            }

            if (capabilities == null) {
                continue;
            }
            try {
                if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                    continue;
                }

                if (validatedNetwork) {
                    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                        continue;
                    }
                    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        continue;
                    }
                }

                if (!capabilitiesVPN) {
                    if (!capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                        continue;
                    }
                }
            } catch (Throwable ignored) {
                continue;
            }
            availableNetworks.add(network);
        }
        Network[] result = new Network[availableNetworks.size()];
        availableNetworks.toArray(result);
        return new Network[][]{result, networks};
    }

    // Set the default network Settings for the current app application to ignore the implementation of socket binding to the network.
    public static Network network_set_default_network(@NotNull Context context, Network network) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                ConnectivityManager cm = network_get_connectivity_manager(context);
                if (cm != null) {
                    cm.bindProcessToNetwork(network);
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            ConnectivityManager.setProcessDefaultNetwork(network);
        } catch (Throwable ignored) {
        }
        return network;
    }

    // Determine if airplane mode is enabled on the current android device.
    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean network_airplane_mode_is_turn_on(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    // Turn on or off the 2G/3G/4G/5G mobile GMS data network for android devices.
    public static int network_set_mobile_data_state(@NotNull Context context, boolean enabled) {
        try {
            TelephonyManager service = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (service == null) {
                return -1;
            }

            Method method = service.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
            method.invoke(service, enabled);
            return 0;
        } catch (Throwable ignored) {
            return 1;
        }
    }

    // Get whether the status of the 2G/3G/4G/5G mobile GMS data network for android devices is on or off.
    // return:
    // -1 err
    // 0  on
    // 1  off
    public static int network_get_mobile_data_state(@NotNull Context context) {
        try {
            TelephonyManager service = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (service == null) {
                return -1;
            }

            Method method = service.getClass().getDeclaredMethod("getDataEnabled");

            //noinspection DataFlowIssue
            return (Boolean) method.invoke(service) ? 0 : 1;
        } catch (Throwable ignored) {
            return -1;
        }
    }

    // The network that handles the current preferred physical activity.
    private boolean process(@NotNull VpnService service) {
        ConnectivityManager connectivityManager = network_get_connectivity_manager(service);
        if (connectivityManager == null) {
            return false;
        }

        Network[] networks = connectivityManager.getAllNetworks();
        if (networks.length < 1) {
            return false;
        } else if (network_is_lost_vpn_network(connectivityManager, networks)) {
            return false;
        } else if (network_airplane_mode_is_turn_on(service)) {
            return false;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Network[][] activeNetworks = network_get_all_networks_array(service, networks, false, false);
            if (activeNetworks != null) {
                service.setUnderlyingNetworks(activeNetworks[0]);
            } else {
                service.setUnderlyingNetworks(new Network[0]);
            }
        }

        /* network_set_default_network(service, network) */
        Network network = network_choose_available_network(service, networks);
        if (this.allow_no_activity_network_) {
            VPN.c.network(network);
        } else if (network != null) {      /* bindProcessToNetwork */
            VPN.c.network(network);        /* network_set_default_network(service, network) */
            return true;
        }

        return network_get_mobile_data_state(service) == 0;
    }

    // Determine if the current android vpn device has lost the vpn connection.
    // Some android distribution platforms have lost it, and the vpn app cannot be system notified of its status.
    private boolean network_is_lost_vpn_network(ConnectivityManager connectivityManager, Network[] networks) {
        if (networks == null || networks.length < 1 || connectivityManager == null) {
            return true;
        } else {
            int status = VPN.vpn_get_link_state();
            if (status != libopenppp2.LIBOPENPPP2_LINK_STATE_ESTABLISHED && status != libopenppp2.LIBOPENPPP2_LINK_STATE_CONNECTING && status != libopenppp2.LIBOPENPPP2_LINK_STATE_RECONNECTING) {
                return true;
            }
        }

        for (Network network : networks) {
            if (network == null) {
                continue;
            }

            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
            if (networkCapabilities == null) {
                continue;
            } else if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) || networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)) {
                continue;
            } else if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)) {
                continue;
            }
            return false;
        }
        return true;
    }

    // Updates the status of the current network listener.
    public boolean update() {
        VpnService service = VPN.c.service();
        if (service == null) {
            return false;
        } else {
            return process(service);
        }
    }

    // Releases the resources held by the current network interface listener object.
    public boolean release() {
        VpnService service = VPN.c.service();
        if (service == null) {
            return false;
        }

        Handler handler = VPN.c.handler();
        if (handler == null) {
            return false;
        }

        ConnectivityManager connectivityManager = network_get_connectivity_manager(service);
        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //noinspection DataFlowIssue
            connectivityManager.registerDefaultNetworkCallback(null, handler);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //noinspection DataFlowIssue
            connectivityManager.registerDefaultNetworkCallback(null);
        }

        return true;
    }

    // The finalizer function of the called object, which is usually called by gc.
    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        this.release();
    }

    // Running a network listener, this function can actually not be executed,
    // Do not need to be implemented, but I like to do some meaningless things.
    public boolean run() {
        VpnService service = VPN.c.service();
        if (service == null) {
            return false;
        }

        Handler handler = VPN.c.handler();
        if (handler == null) {
            return false;
        }

        NetworkCallback networkCallback = new NetworkCallback();
        NetworkRequest request = new NetworkRequest.Builder() {{
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
            addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED);
            if (Build.VERSION.SDK_INT == 23) {  // workarounds for OEM bugs.
                removeCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                removeCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL);
            }
        }}.build();

        ConnectivityManager connectivityManager = network_get_connectivity_manager(service);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback, handler);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
        return true;
    }

    private final class NetworkCallback extends ConnectivityManager.NetworkCallback {
        @Override
        public void onAvailable(@NonNull Network network) {
            super.onAvailable(network);
            NetworkListener.this.update();
        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            NetworkListener.this.update();
        }

        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            NetworkListener.this.update();
        }

        @Override
        public void onLosing(@NonNull Network network, int maxMsToLive) {
            super.onLosing(network, maxMsToLive);
            NetworkListener.this.update();
        }

        @Override
        public void onLinkPropertiesChanged(@NonNull Network network, @NonNull LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            NetworkListener.this.update();
        }

        @Override
        public void onBlockedStatusChanged(@NonNull Network network, boolean blocked) {
            super.onBlockedStatusChanged(network, blocked);
            NetworkListener.this.update();
        }

        @Override
        public void onUnavailable() {
            super.onUnavailable();
            VPN.c.stop();
        }
    }
}