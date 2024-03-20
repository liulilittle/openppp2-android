package supersocksr.ppp.android.openppp2.i;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class NetworkStatistics {
    @SerializedName("tx")
    @Expose(serialize = true, deserialize = true)
    public long TX;

    @SerializedName("rx")
    @Expose(serialize = true, deserialize = true)
    public long RX;

    @SerializedName("in")
    @Expose(serialize = true, deserialize = true)
    public long IN;

    @SerializedName("out")
    @Expose(serialize = true, deserialize = true)
    public long OUT;
}