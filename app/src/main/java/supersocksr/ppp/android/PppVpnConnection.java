package supersocksr.ppp.android;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Objects;

import supersocksr.ppp.android.openppp2.Macro;
import supersocksr.ppp.android.openppp2.VPN;
import supersocksr.ppp.android.openppp2.VPNConnection;
import supersocksr.ppp.android.openppp2.VPNLinkConfiguration;
import supersocksr.ppp.android.openppp2.X;

public final class PppVpnConnection extends VPNConnection {
    private VPNLinkConfiguration config_ = null;
    private Binder binder_ = null;

    // When the VPN service is created.
    @Override
    public void onCreate() {
        super.onCreate();
        this.binder_ = new VpnConnectionBinder(PppVpnConnection.this);
    }

    // When the VPN service is bind.
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Binder binder = this.binder_;
        if (binder != null) {
            return binder;
        } else {
            return X.call(() -> super.onBind(intent), null).item2;
        }
    }

    // VPN run exit, but probably because the vpn run failure,
    // So you need to determine the cause of the exit.
    @Override
    protected void exit(int reason) {
        super.exit(reason);
    }

    // Load a reference to the VPN link configurations.
    @Override
    protected VPNLinkConfiguration load() {
        return this.config_;
    }

    // Stop vpn connections.
    @Override
    protected void stop() {
        super.stop();
        this.config_ = null;
    }

    private static class VpnConnectionBinder extends Binder implements IPppVpnConnection {
        private PppVpnConnection my_ = null;

        public VpnConnectionBinder(PppVpnConnection my) {
            this.my_ = Objects.requireNonNull(my);
        }

        @Override
        public int run(VPNLinkConfiguration config) {
            if (config == null) {
                return Macro.RUN_VPN_LINK_CONFIGURATION_IS_NULL;
            } else {
                this.my_.config_ = config;
                return this.my_.open();
            }
        }

        @Override
        public void stop() {
            this.my_.stop();
        }

        @Override
        public int state() {
            return this.my_.state();
        }

        @Override
        public VPNLinkConfiguration load() {
            return this.my_.load();
        }

        @Override
        public boolean post_to_protector(Runnable runnable) {
            return VPN.c.post(runnable);
        }

        @Override
        public boolean post_to_service(Runnable runnable, int milliseconds) {
            if (runnable == null) {
                return false;
            } else {
                milliseconds = Math.max(0, milliseconds);
            }

            Handler handler = this.my_.handler();
            if (handler == null) {
                return false;
            } else {
                return handler.postDelayed(runnable, milliseconds);
            }
        }
    }
}
