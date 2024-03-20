package supersocksr.ppp.android.openppp2.i;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class LinkOf {
    @SerializedName("server")
    @Expose(serialize = true, deserialize = true)
    public String server;

    @SerializedName("hostname")
    @Expose(serialize = true, deserialize = true)
    public String hostname;

    @SerializedName("address")
    @Expose(serialize = true, deserialize = true)
    public String address;

    @SerializedName("path")
    @Expose(serialize = true, deserialize = true)
    public String path;

    @SerializedName("url")
    @Expose(serialize = true, deserialize = true)
    public String url;

    @SerializedName("port")
    @Expose(serialize = true, deserialize = true)
    public int port;

    @SerializedName("proto")
    @Expose(serialize = true, deserialize = true)
    public String proto;

    @SerializedName("protocol")
    @Expose(serialize = true, deserialize = true)
    public String protocol;
}
