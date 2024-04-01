package supersocksr.ppp.android.openppp2.i;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class PackageInformation {
    @SerializedName("packageName")
    @Expose(serialize = true, deserialize = true)
    public String packageName;

    @SerializedName("applicationName")
    @Expose(serialize = true, deserialize = true)
    public String applicationName;

    @SerializedName("applicationIcon")
    @Expose(serialize = true, deserialize = true)
    public Drawable applicationIcon;
}
