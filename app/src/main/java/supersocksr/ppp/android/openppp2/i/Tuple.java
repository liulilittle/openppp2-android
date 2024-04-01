package supersocksr.ppp.android.openppp2.i;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class Tuple<T1, T2> {
    @SerializedName("item1")
    @Expose(serialize = true, deserialize = true)
    public final T1 item1;

    @SerializedName("item2")
    @Expose(serialize = true, deserialize = true)
    public final T2 item2;

    public Tuple(T1 item1, T2 item2) {
        this.item1 = item1;
        this.item2 = item2;
    }
}