package supersocksr.ppp.android;

import supersocksr.ppp.android.openppp2.VPNLinkConfiguration;

public interface IPppVpnConnection {
    // Attempt to run the VPN client service.
    int run(VPNLinkConfiguration config);

    // Stop the VPN client service.
    void stop();

    // Get the link state of the VPN client service.
    int state();

    // Get the VPN link configuration files.
    VPNLinkConfiguration load();

    // Post is called to the VPN client protector.
    boolean post_to_protector(Runnable runnable);

    // Post is called to the VPN client service.
    boolean post_to_service(Runnable runnable, int milliseconds);
}
