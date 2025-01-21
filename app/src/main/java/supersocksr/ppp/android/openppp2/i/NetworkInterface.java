package supersocksr.ppp.android.openppp2.i;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NetworkInterface {
    @SerializedName("block-quic")
    @Expose(serialize = true, deserialize = true)
    public boolean block_quic;

    @SerializedName("static")
    @Expose(serialize = true, deserialize = true)
    public boolean static_mode;

    @SerializedName("tun")
    @Expose(serialize = true, deserialize = true)
    public int tun;

    @SerializedName("vnet")
    @Expose(serialize = true, deserialize = true)
    public boolean vnet;

    @SerializedName("mux")
    @Expose(serialize = true, deserialize = true)
    public int mux;

    @SerializedName("gw")
    @Expose(serialize = true, deserialize = true)
    public String gw;

    @SerializedName("ip")
    @Expose(serialize = true, deserialize = true)
    public String ip;

    @SerializedName("mask")
    @Expose(serialize = true, deserialize = true)
    public String mask;
}
