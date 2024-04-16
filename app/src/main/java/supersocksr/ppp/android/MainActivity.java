package supersocksr.ppp.android;

import android.os.Bundle;

import supersocksr.ppp.android.openppp2.Macro;
import supersocksr.ppp.android.openppp2.PackageX;
import supersocksr.ppp.android.openppp2.PathX;
import supersocksr.ppp.android.openppp2.VPNConfiguration;
import supersocksr.ppp.android.openppp2.VPNLinkConfiguration;

public class MainActivity extends PppVpnActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!is_reload_activity_by_launcher()) {
            setContentView(R.layout.activity_main);

            findViewById(R.id.btn_start).setOnClickListener(v -> vpn_run());
            findViewById(R.id.btn_stop).setOnClickListener(v -> vpn_stop());
        }
    }

    @Override
    protected VPNLinkConfiguration vpn_load() {
        VPNLinkConfiguration config = new VPNLinkConfiguration();
        config.SubnetAddress = "255.255.255.0";
        config.GatewayServer = "10.0.0.1";
        config.IPAddress = "10.0.0.211";
        config.VirtualSubnet = true;
        config.BlockQUIC = true;
        config.StaticMode = true;
        config.FlashMode = false;
        config.AtomicHttpProxySet = true;
        config.DnsAddresses.add("8.8.8.8");
        config.DnsAddresses.add("8.8.4.4");

        config.BypassIpList = "223.5.5.5\r\n" + "223.6.6.6\r\n" + "120.197.153.0/24\r\n";
        config.BypassIpListFiles.add(PathX.path_get_cache_dir(MainActivity.this) + "/ip.txt");

        config.DNSRuleList = "baidu.com/1.2.4.8/nic\r\n" + "google.com/1.1.1.1/tun\r\n";
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
        vpn.udp.static_.keep_alived[0] = 1;
        vpn.udp.static_.keep_alived[1] = 5;
        vpn.udp.static_.servers.add("192.168.0.24:20000");

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

        vpn.client.guid = "{14261208-BB45-4DEB-B115-0FEA1D91B85B}";
        vpn.client.server = "ppp://192.168.0.24:20000/";
        vpn.client.bandwidth = 0;
        vpn.client.reconnections.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT;
        vpn.client.http_proxy.bind = "127.0.0.1";
        vpn.client.http_proxy.port = Macro.PPP_DEFAULT_HTTP_PROXY_PORT;

        return config;
    }
}