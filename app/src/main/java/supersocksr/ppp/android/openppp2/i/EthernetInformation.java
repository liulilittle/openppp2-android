package supersocksr.ppp.android.openppp2.i;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class EthernetInformation {
    @SerializedName("BandwidthQoS")
    @Expose(serialize = true, deserialize = true)
    public long BandwidthQoS; // Maximum Quality of Service (QoS) bandwidth throughput speed per second, 0 for unlimited, 1 for 1 Kbps.

    @SerializedName("ExpiredTime")
    @Expose(serialize = true, deserialize = true)
    public long ExpiredTime; // The remaining network traffic allowance that can be allowed for incoming clients, 0 is unlimited.

    @SerializedName("IncomingTraffic")
    @Expose(serialize = true, deserialize = true)
    public long IncomingTraffic; // The remaining network traffic allowance that can be allowed for outgoing clients, 0 is unlimited.

    @SerializedName("OutgoingTraffic")
    @Expose(serialize = true, deserialize = true)
    public long OutgoingTraffic; // The time duration during which clients are expired time from using PPP (Point-to-Point Protocol) VPN services, 0 for no restrictions, measured in seconds.
}