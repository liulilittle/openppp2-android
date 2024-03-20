package supersocksr.ppp.android.openppp2.i;

import supersocksr.ppp.android.openppp2.VPN;

public interface NetworkStatisticsEventHandler {
    void Handle(VPN sender, NetworkStatistics e);
}