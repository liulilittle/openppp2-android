package supersocksr.ppp.android;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import supersocksr.ppp.android.c.libopenppp2;
import supersocksr.ppp.android.openppp2.ActivityX;
import supersocksr.ppp.android.openppp2.Macro;
import supersocksr.ppp.android.openppp2.VPN;
import supersocksr.ppp.android.openppp2.VPNLinkConfiguration;
import supersocksr.ppp.android.openppp2.VPNServiceConnection;
import supersocksr.ppp.android.openppp2.X;
import supersocksr.ppp.android.openppp2.i.NetworkStatistics;

public abstract class PppVpnActivity extends AppCompatActivity {
    public static final int REQUEST_VPN_SERVICE = 0;

    private final MyServiceConnection sc_ = new MyServiceConnection(PppVpnActivity.this, IPppVpnConnection.class);
    private final MyBroadcastReceiver br_ = new MyBroadcastReceiver(PppVpnActivity.this);
    private AtomicReference<MyServiceConnection> connection_ = null;
    private Handler handler_ = null;

    @Override
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.handler_ = new Handler();
        this.connection_ = new AtomicReference<MyServiceConnection>();
        X.call_void(() -> registerReceiver(this.br_, new IntentFilter(PppVpnConnection.ACTION_VPN_CTL)));

        // If the VPN network session link is in the status of reconnection, link has been established,
        // Or link is being connected, you need to manually connect to VPN service.
        int state = VPN.vpn_get_link_state();
        if (state == libopenppp2.LIBOPENPPP2_LINK_STATE_ESTABLISHED ||
                state == libopenppp2.LIBOPENPPP2_LINK_STATE_RECONNECTING ||
                state == libopenppp2.LIBOPENPPP2_LINK_STATE_CONNECTING) {
            this.bind();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        X.call_void(() -> unregisterReceiver(this.br_));
    }

    // Determines whether the specified activity is running repeatedly,
    // But must be started by tapping the app from the Android Home, and calls it when onCreate is created.
    protected boolean is_reload_activity_by_launcher() {
        boolean reload = ActivityX.activity_is_reload_activity_by_launcher(PppVpnActivity.this);
        if (reload) {
            X.call_void(this::finish);
        }
        return reload;
    }

    // Attempt to bind and connect a remote vpn service.
    private boolean bind() {
        Intent intent = new Intent(PppVpnActivity.this, PppVpnConnection.class);
        X.call_void(() -> this.startService(intent));

        int flags = BIND_AUTO_CREATE | BIND_WAIVE_PRIORITY;
        boolean ok = X.call(() -> this.bindService(intent, sc_, flags), false).item2;
        if (ok) {
            this.connection_.set(sc_);
        }

        return ok;
    }

    // Unbind the vpn service to remote.
    private void unbind() {
        ServiceConnection connection = this.connection_.getAndSet(null);
        if (connection != null) {
            X.call_void(() -> this.unbindService(connection));
        }
    }

    // Forcibly stop the remote vpn service if the vpn client session has been opened.
    private void stop() {
        IPppVpnConnection service = MyServiceConnection.get(PppVpnActivity.this);
        if (service != null) {
            service.stop();
        }

        PppVpnActivity.this.unbind();
    }

    // Call remote vpn services run the vpn client sessions.
    private int run() {
        IPppVpnConnection service = MyServiceConnection.get(PppVpnActivity.this);
        if (service == null) {
            return Macro.RUN_VPN_LINK_CONFIGURATION_IS_NULL;
        }

        VPNLinkConfiguration config = vpn_load();
        if (config == null) {
            return Macro.RUN_VPN_LINK_CONFIGURATION_IS_NULL;
        }

        return service.run(config);
    }

    // Load a reference to the VPN link configurations.
    protected abstract VPNLinkConfiguration vpn_load();

    // Get the processor of the current remote VPN service.
    public Handler handler() {
        return this.handler_;
    }

    // Get the interface of the current remote VPN service.
    public IPppVpnConnection service() {
        return MyServiceConnection.get(PppVpnActivity.this);
    }

    // After running the vpn session and exiting, you need to check the reason for exiting the vpn session.
    protected void vpn_on_exit(int reason) {
        if (reason != Macro.RUN_RERUN_VPN_CLIENT) {
            vpn_stop();
        }
    }

    // Try run a vpn client sessions.
    protected boolean vpn_run() {
        // Request the permission of the vpn service from the device administrator.
        Intent intent = null;
        try {
            intent = VpnService.prepare(PppVpnActivity.this);
        } catch (Throwable ignored) {
            return false;
        }

        return request(intent, REQUEST_VPN_SERVICE);
    }

    // Try to close vpn client sessions.
    protected void vpn_stop() {
        stop();
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
    protected int vpn_state() {
        IPppVpnConnection service = this.service();
        if (service != null) {
            return service.state();
        } else {
            return VPN.vpn_get_link_state();
        }
    }

    // VPN traffic statistics event triggered.
    protected void vpn_on_statistics(NetworkStatistics e) {

    }

    // VPN is being opened up and started running event is triggered.
    protected void vpn_on_start() {

    }

    // Attempt to open a remote VPN client sessions.
    private void open() {
        // When running the VPN is unsuccessful, the exit function is called immediately, passing in the problem reason.
        int reason = this.run();
        if (reason != Macro.RUN_OK) {
            this.vpn_on_exit(reason);
        }
    }

    // Dynamically request the permissions required for device administrator user permissions.
    protected boolean request(@Nullable Intent intent, int request_code) {
        if (intent != null) {
            //noinspection deprecation
            return X.call_void(() -> this.startActivityForResult(intent, request_code));
        } else {
            return X.call_void(() -> this.onActivityResult(request_code, RESULT_OK, null));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VPN_SERVICE) {
            if (resultCode != RESULT_OK) {
                this.stop();
            } else {
                IPppVpnConnection connection = this.service();
                if (connection == null) {
                    this.bind();
                } else {
                    this.open();
                }
            }
        }
    }

    private static class MyServiceConnection extends VPNServiceConnection<IPppVpnConnection> {
        private PppVpnActivity my_ = null;

        public MyServiceConnection(PppVpnActivity my, Class<IPppVpnConnection> clazz) {
            super(clazz);
            this.my_ = Objects.requireNonNull(my);
        }

        @Nullable
        public static IPppVpnConnection get(@NonNull PppVpnActivity my) {
            VPNServiceConnection<IPppVpnConnection> connection = my.connection_.get();
            if (connection == null) {
                return null;
            } else {
                return connection.service();
            }
        }

        public PppVpnActivity my() {
            return this.my_;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            super.onServiceConnected(name, service);

            // Try to open the vpn service, as this will usually
            // Only trigger this behavior when the vpn connection is open.
            this.my().open();
        }
    }

    private static class MyBroadcastReceiver extends BroadcastReceiver {
        private PppVpnActivity my_ = null;

        public MyBroadcastReceiver(PppVpnActivity my) {
            this.my_ = Objects.requireNonNull(my);
        }

        public PppVpnActivity my() {
            return this.my_;
        }

        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final int CTL = intent.getIntExtra(PppVpnConnection.VPN_CTL_TAG, 0);
            if (CTL == PppVpnConnection.VPN_CTL_START) {
                this.my().vpn_on_start();
            } else if (CTL == PppVpnConnection.VPN_CTL_EXIT) {
                int reason = intent.getIntExtra("reason", Macro.RUN_UNKNOWN);
                this.my().vpn_on_exit(reason);
            } else if (CTL == PppVpnConnection.VPN_CTL_STATISTICS) {
                NetworkStatistics e = new NetworkStatistics();
                e.IN = intent.getLongExtra("IN", 0);
                e.OUT = intent.getLongExtra("OUT", 0);
                e.RX = intent.getLongExtra("RX", 0);
                e.TX = intent.getLongExtra("TX", 0);
                this.my().vpn_on_statistics(e);
            }
        }
    }
}