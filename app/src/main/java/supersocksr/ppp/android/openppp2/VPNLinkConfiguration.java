package supersocksr.ppp.android.openppp2;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class VPNLinkConfiguration {
    @SerializedName("DnsAddresses")
    @Expose(serialize = true, deserialize = true)
    public final Set<String> DnsAddresses = new HashSet<String>();

    @SerializedName("AllowedApplicationPackageNames")
    @Expose(serialize = true, deserialize = true)
    public final Set<String> AllowedApplicationPackageNames = new HashSet<String>();

    @SerializedName("DisallowedApplicationPackageNames")
    @Expose(serialize = true, deserialize = true)
    public final Set<String> DisallowedApplicationPackageNames = new HashSet<String>();

    @SerializedName("BypassIpListFiles")
    @Expose(serialize = true, deserialize = true)
    public final Collection<String> BypassIpListFiles = new HashSet<String>();

    @SerializedName("VPNConfiguration")
    @Expose(serialize = true, deserialize = true)
    public final VPNConfiguration VPNConfiguration = new VPNConfiguration();

    @SerializedName("IPAddress")
    @Expose(serialize = true, deserialize = true)
    public String IPAddress;

    @SerializedName("GatewayServer")
    @Expose(serialize = true, deserialize = true)
    public String GatewayServer;

    @SerializedName("SubnetAddress")
    @Expose(serialize = true, deserialize = true)
    public String SubnetAddress;

    @SerializedName("BlockQUIC")
    @Expose(serialize = true, deserialize = true)
    public boolean BlockQUIC;

    @SerializedName("StaticMode")
    @Expose(serialize = true, deserialize = true)
    public boolean StaticMode;

    @SerializedName("FlashMode")
    @Expose(serialize = true, deserialize = true)
    public boolean FlashMode = true;

    @SerializedName("VirtualSubnet")
    @Expose(serialize = true, deserialize = true)
    public boolean VirtualSubnet;

    @SerializedName("AtomicHttpProxySet")
    @Expose(serialize = true, deserialize = true)
    public boolean AtomicHttpProxySet;

    @SerializedName("DNSRuleList")
    @Expose(serialize = true, deserialize = true)
    public String DNSRuleList;

    @SerializedName("BypassIpList")
    @Expose(serialize = true, deserialize = true)
    public String BypassIpList;

    @SerializedName("AllowNoActivityNetwork")
    @Expose(serialize = true, deserialize = true)
    public boolean AllowNoActivityNetwork = true;

    @NonNull
    public String bypass_ip_list_do_load_all() {
        StringBuilder result = new StringBuilder();
        String bypass_ip_list = this.BypassIpList;
        if (!X.is_empty(bypass_ip_list)) {
            result.append(bypass_ip_list).append("\r\n");
        }

        Collection<String> bypass_ip_list_files = this.BypassIpListFiles;
        if (!X.is_empty(bypass_ip_list_files)) {
            for (String bypass_ip_list_file : bypass_ip_list_files) {
                if (X.is_empty(bypass_ip_list_file)) {
                    continue;
                }

                bypass_ip_list = FileX.file_read_all_text(bypass_ip_list_file);
                if (X.is_empty(bypass_ip_list)) {
                    continue;
                }

                result.append(bypass_ip_list).append("\r\n");
            }
        }
        return result.toString();
    }
}
