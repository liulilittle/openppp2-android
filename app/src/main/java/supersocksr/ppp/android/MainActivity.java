package supersocksr.ppp.android;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import supersocksr.ppp.android.openppp2.Macro;
import supersocksr.ppp.android.openppp2.PackageX;
import supersocksr.ppp.android.openppp2.VPNConfiguration;
import supersocksr.ppp.android.openppp2.VPNLinkConfiguration;




public class MainActivity extends PppVpnActivity {
    private EditText server;
    private EditText static_server;
    private EditText uuid;
    private EditText tunip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!is_reload_activity_by_launcher()) {
            setContentView(R.layout.activity_main);
            server=findViewById(R.id.serverAddress);
            static_server=findViewById(R.id.staticserver);
            uuid=findViewById(R.id.guidNumber);
            tunip=findViewById(R.id.tunIP);
            findViewById(R.id.btn_start).setOnClickListener(v -> vpn_run());
            findViewById(R.id.btn_stop).setOnClickListener(v -> vpn_stop());
        }
    }

private String readRawIpBypass() {
        String result = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.ip);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            result = new String(buffer, "utf8");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
}

private String readRawDomainBypass() {
        String result = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.domain);
            int length = is.available();
            byte[] buffer = new byte[length];
            is.read(buffer);
            result = new String(buffer, "utf8");
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected VPNLinkConfiguration vpn_load() {
        VPNLinkConfiguration config = new VPNLinkConfiguration();
        config.SubnetAddress = "255.255.255.0";
        String ip_addr = tunip.getText().toString().trim();
        String[] temp;
        String delimiter = "\\.";
        temp=ip_addr.split(delimiter);
        config.GatewayServer = temp[0]+"."+temp[1]+"."+temp[2]+"."+"1";
        config.IPAddress = ip_addr;
        config.VirtualSubnet = true;
        config.BlockQUIC = false;
        config.StaticMode = true;
        config.FlashMode = false;
        config.AtomicHttpProxySet = false;
        config.DnsAddresses.add("8.8.8.8");
        config.DnsAddresses.add("8.8.4.4");

        config.BypassIpList = readRawIpBypass();

        config.DNSRuleList = readRawDomainBypass();
        config.AllowedApplicationPackageNames.add(PackageX.package_get_package_name(MainActivity.this));
        config.DisallowedApplicationPackageNames.add(PackageX.package_get_package_name(MainActivity.this));

        VPNConfiguration vpn = config.VPNConfiguration;
        vpn.key.kf = 154543927;
        vpn.key.kx = 128;
        vpn.key.kl = 10;
        vpn.key.kh = 12;
        vpn.key.protocol = "aes-128-cfb";
        vpn.key.protocol_key = "N6HMzdUs7IUnYHwq";
        vpn.key.transport = "aes-256-cfb";
        vpn.key.transport_key = "HWFweXu2g5RVMEpy";
        vpn.key.masked = false;
        vpn.key.plaintext = false;
        vpn.key.delta_encode = false;
        vpn.key.shuffle_data = false;

        vpn.tcp.inactive.timeout = Macro.PPP_TCP_INACTIVE_TIMEOUT;
        vpn.tcp.connect.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT;
        vpn.tcp.turbo = true;
        vpn.tcp.backlog = Macro.PPP_LISTEN_BACKLOG;
        vpn.tcp.fast_open = true;

        vpn.udp.inactive.timeout = Macro.PPP_UDP_INACTIVE_TIMEOUT;
        vpn.udp.dns.timeout = Macro.PPP_DEFAULT_DNS_TIMEOUT;
        vpn.udp.static_.quic = true;
        vpn.udp.static_.dns = true;
        vpn.udp.static_.icmp = true;
        vpn.udp.static_.keep_alived[0] = 0;
        vpn.udp.static_.keep_alived[1] = 0;
        vpn.udp.static_.servers.add(static_server.getText().toString().trim());

        vpn.websocket.verify_peer = true;
        vpn.websocket.http.error = "Status Code: 404; Not Found";
        vpn.websocket.http.request.put("Cache-Control", "no-cache");
        vpn.websocket.http.request.put("Pragma", "no-cache");
        vpn.websocket.http.request.put("Accept-Encoding", "gzip, deflate");
        vpn.websocket.http.request.put("Accept-Language", "zh-CN,zh;q=0.9");
        vpn.websocket.http.request.put("Origin", "http://www.websocket-test.com");
        vpn.websocket.http.request.put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
        vpn.websocket.http.request.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
        vpn.websocket.http.response.put("Server", "Kestrel");

        vpn.client.guid = java.util.UUID.randomUUID().toString();
        vpn.client.server = server.getText().toString().trim();
        vpn.client.bandwidth = 0;
        vpn.client.reconnections.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT;

        return config;
    }
}
