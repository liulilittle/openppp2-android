package supersocksr.ppp.android.openppp2.i;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public final class PackageInformation2 {
    @SerializedName("packageName")
    @Expose(serialize = true, deserialize = true)
    public String packageName;

    @SerializedName("applicationName")
    @Expose(serialize = true, deserialize = true)
    public String applicationName;

    @SerializedName("iconImageUrl")
    @Expose(serialize = true, deserialize = true)
    public String iconImageUrl;

    @NonNull
    @Override
    public String toString() {
        String package_name = this.packageName;
        if (package_name == null) {
            return "";
        } else {
            return package_name;
        }
    }
}
