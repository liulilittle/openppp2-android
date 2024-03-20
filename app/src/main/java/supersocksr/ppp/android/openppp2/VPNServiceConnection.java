package supersocksr.ppp.android.openppp2;


import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import java.util.Objects;

public class VPNServiceConnection<T> implements ServiceConnection {
    private Class<T> clazz_ = null;
    private T service_ = null;

    public VPNServiceConnection(Class<T> clazz) {
        this.clazz_ = Objects.requireNonNull(clazz);
    }

    public T service() {
        return this.service_;
    }

    public Class<T> clazz() {
        return this.clazz_;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        synchronized (VPNServiceConnection.this) {
            this.service_ = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        synchronized (VPNServiceConnection.this) {
            if (service != null) {
                if (this.clazz_.isInstance(service)) {
                    this.service_ = (T) service;
                }
            }
        }
    }
}