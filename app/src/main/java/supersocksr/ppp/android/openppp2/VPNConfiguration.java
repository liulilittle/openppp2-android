package supersocksr.ppp.android.openppp2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Warning: any attempt to provide VPN port mapping services for Android and IOS mobile platforms is not allowed,
// And the libopenppp2 implementation also refuses to enable these functions through app configuration on
// Android and IOS mobile platforms. Forcibly enabling these functions will cause loopback problems.
// The official will not provide this support and solution, if such requirements please use Windows, Linux, MacOS platform.
public final class VPNConfiguration {
    @SerializedName("key")
    @Expose(serialize = true, deserialize = true)
    public final KeyConfiguration key = new KeyConfiguration();

    @SerializedName("tcp")
    @Expose(serialize = true, deserialize = true)
    public final TcpConfiguration tcp = new TcpConfiguration();

    @SerializedName("udp")
    @Expose(serialize = true, deserialize = true)
    public final UdpConfiguration udp = new UdpConfiguration();

    @SerializedName("mux")
    @Expose(serialize = true, deserialize = true)
    public final MuxConfiguration mux = new MuxConfiguration();

    @SerializedName("websocket")
    @Expose(serialize = true, deserialize = true)
    public final WebsocketConfiguration websocket = new WebsocketConfiguration();

    @SerializedName("client")
    @Expose(serialize = true, deserialize = true)
    public final ClientConfiguration client = new ClientConfiguration();

    public VPNConfiguration() {
        clear();
    }

    public void clear() {
        VPNConfiguration config = this;
        config.key.kf = 154543927;
        config.key.kx = 128;
        config.key.kl = 10;
        config.key.kh = 12;
        config.key.protocol = "aes-128-cfb";
        config.key.protocol_key = "N6HMzdUs7IUnYHwq";
        config.key.transport = "aes-256-cfb";
        config.key.transport_key = "HWFweXu2g5RVMEpy";
        config.key.masked = true;
        config.key.plaintext = true;
        config.key.delta_encode = true;
        config.key.shuffle_data = true;

        config.tcp.inactive.timeout = Macro.PPP_TCP_INACTIVE_TIMEOUT;
        config.tcp.connect.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT;
        config.tcp.turbo = true;
        config.tcp.backlog = Macro.PPP_LISTEN_BACKLOG;
        config.tcp.fast_open = true;

        config.mux.inactive.timeout = Macro.PPP_MUX_INACTIVE_TIMEOUT;
        config.mux.connect.timeout = Macro.PPP_MUX_CONNECT_TIMEOUT;
        config.mux.keep_alived[0] = 0;
        config.mux.keep_alived[1] = Macro.PPP_MUX_CONNECT_TIMEOUT;

        config.udp.inactive.timeout = Macro.PPP_UDP_INACTIVE_TIMEOUT;
        config.udp.dns.timeout = Macro.PPP_DEFAULT_DNS_TIMEOUT;
        config.udp.static_.quic = true;
        config.udp.static_.dns = true;
        config.udp.static_.icmp = true;
        config.udp.static_.keep_alived[0] = 0;
        config.udp.static_.keep_alived[1] = 0;
        config.udp.static_.aggligator = 0;
        config.udp.static_.servers.clear();

        config.websocket.verify_peer = true;
        config.websocket.http.error = "Status Code: 404; Not Found";
        config.websocket.http.request.put("Cache-Control", "no-cache");
        config.websocket.http.request.put("Pragma", "no-cache");
        config.websocket.http.request.put("Accept-Encoding", "gzip, deflate");
        config.websocket.http.request.put("Accept-Language", "zh-CN,zh;q=0.9");
        config.websocket.http.request.put("Origin", "http://www.websocket-test.com");
        config.websocket.http.request.put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
        config.websocket.http.request.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0");
        config.websocket.http.response.put("Server", "Kestrel");

        config.client.guid = "{F4569208-BB45-4DEB-B115-0FEA1D91B85B}";
        config.client.server = "ppp://127.0.0.1:20000/";
        config.client.bandwidth = 10000;
        config.client.reconnections.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT;
        config.client.http_proxy.bind = "127.0.0.1";
        config.client.http_proxy.port = Macro.PPP_DEFAULT_HTTP_PROXY_PORT;
        config.client.socks_proxy.username = "";
        config.client.socks_proxy.password = "";
        config.client.socks_proxy.bind = "127.0.0.1";
        config.client.socks_proxy.port = Macro.PPP_DEFAULT_SOCKS_PROXY_PORT;
    }

    public static class KeyConfiguration {
        @SerializedName("kf")
        @Expose(serialize = true, deserialize = true)
        public int kf;

        @SerializedName("kx")
        @Expose(serialize = true, deserialize = true)
        public int kx;

        @SerializedName("kl")
        @Expose(serialize = true, deserialize = true)
        public int kl;

        @SerializedName("kh")
        @Expose(serialize = true, deserialize = true)
        public int kh;

        @SerializedName("protocol")
        @Expose(serialize = true, deserialize = true)
        public String protocol;

        @SerializedName("protocol-key")
        @Expose(serialize = true, deserialize = true)
        public String protocol_key;

        @SerializedName("transport")
        @Expose(serialize = true, deserialize = true)
        public String transport;

        @SerializedName("transport-key")
        @Expose(serialize = true, deserialize = true)
        public String transport_key;

        @SerializedName("masked")
        @Expose(serialize = true, deserialize = true)
        public boolean masked;

        @SerializedName("plaintext")
        @Expose(serialize = true, deserialize = true)
        public boolean plaintext;

        @SerializedName("delta-encode")
        @Expose(serialize = true, deserialize = true)
        public boolean delta_encode;

        @SerializedName("shuffle-data")
        @Expose(serialize = true, deserialize = true)
        public boolean shuffle_data;
    }

    public static class TimeoutConfiguration {
        @SerializedName("timeout")
        @Expose(serialize = true, deserialize = true)
        public int timeout;
    }

    public static class DnsConfiguration extends TimeoutConfiguration {
        @SerializedName("ttl")
        @Expose(serialize = true, deserialize = true)
        public int ttl;

        @SerializedName("cache")
        @Expose(serialize = true, deserialize = true)
        public boolean cache;
    }

    public static class WindowSizeConfiguration {
        @SerializedName("cwnd")
        @Expose(serialize = true, deserialize = true)
        public int cwnd;

        @SerializedName("rwnd")
        @Expose(serialize = true, deserialize = true)
        public int rwnd;
    }

    public static class TcpConfiguration extends WindowSizeConfiguration {
        @SerializedName("inactive")
        @Expose(serialize = true, deserialize = true)
        public final TimeoutConfiguration inactive = new TimeoutConfiguration();

        @SerializedName("connect")
        @Expose(serialize = true, deserialize = true)
        public final TimeoutConfiguration connect = new TimeoutConfiguration();

        @SerializedName("turbo")
        @Expose(serialize = true, deserialize = true)
        public boolean turbo;

        @SerializedName("backlog")
        @Expose(serialize = true, deserialize = true)
        public int backlog;

        @SerializedName("fast-open")
        @Expose(serialize = true, deserialize = true)
        public boolean fast_open;
    }

    public static class MuxConfiguration {
        @SerializedName("inactive")
        @Expose(serialize = true, deserialize = true)
        public final TimeoutConfiguration inactive = new TimeoutConfiguration();

        @SerializedName("connect")
        @Expose(serialize = true, deserialize = true)
        public final TimeoutConfiguration connect = new TimeoutConfiguration();

        @SerializedName("keep-alived")
        @Expose(serialize = true, deserialize = true)
        public final int[] keep_alived = new int[2];
    }

    public static class UdpConfiguration extends WindowSizeConfiguration {
        @SerializedName("inactive")
        @Expose(serialize = true, deserialize = true)
        public final TimeoutConfiguration inactive = new TimeoutConfiguration();

        @SerializedName("dns")
        @Expose(serialize = true, deserialize = true)
        public final DnsConfiguration dns = new DnsConfiguration();

        @SerializedName("static")
        @Expose(serialize = true, deserialize = true)
        public final StaticConfiguration static_ = new StaticConfiguration();

        public static class StaticConfiguration {
            @SerializedName("keep-alived")
            @Expose(serialize = true, deserialize = true)
            public final int[] keep_alived = new int[2];

            @SerializedName("servers")
            @Expose(serialize = true, deserialize = true)
            public final Set<String> servers = new HashSet<String>();

            @SerializedName("dns")
            @Expose(serialize = true, deserialize = true)
            public boolean dns;

            @SerializedName("quic")
            @Expose(serialize = true, deserialize = true)
            public boolean quic;

            @SerializedName("icmp")
            @Expose(serialize = true, deserialize = true)
            public boolean icmp;

            @SerializedName("aggligator")
            @Expose(serialize = true, deserialize = true)
            public int aggligator;
        }
    }

    public static class WebsocketConfiguration {
        @SerializedName("http")
        @Expose(serialize = true, deserialize = true)
        public final HttpConfiguration http = new HttpConfiguration();

        @SerializedName("verify-peer")
        @Expose(serialize = true, deserialize = true)
        public boolean verify_peer;

        public static class HttpConfiguration {
            @SerializedName("request")
            @Expose(serialize = true, deserialize = true)
            public final Map<String, String> request = new HashMap<>();

            @SerializedName("response")
            @Expose(serialize = true, deserialize = true)
            public final Map<String, String> response = new HashMap<>();

            @SerializedName("error")
            @Expose(serialize = true, deserialize = true)
            public String error;
        }
    }

    public static class ClientConfiguration {
        @SerializedName("reconnections")
        @Expose(serialize = true, deserialize = true)
        public final TimeoutConfiguration reconnections = new TimeoutConfiguration();

        @SerializedName("http-proxy")
        @Expose(serialize = true, deserialize = true)
        public final HttpProxyConfiguration http_proxy = new HttpProxyConfiguration();

        @SerializedName("socks-proxy")
        @Expose(serialize = true, deserialize = true)
        public final SocksProxyConfiguration socks_proxy = new SocksProxyConfiguration();

        @SerializedName("guid")
        @Expose(serialize = true, deserialize = true)
        public String guid;

        @SerializedName("server")
        @Expose(serialize = true, deserialize = true)
        public String server;

        @SerializedName("bandwidth")
        @Expose(serialize = true, deserialize = true)
        public long bandwidth;

        public static class HttpProxyConfiguration {
            @SerializedName("bind")
            @Expose(serialize = true, deserialize = true)
            public String bind;

            @SerializedName("port")
            @Expose(serialize = true, deserialize = true)
            public int port;
        }

        public static class SocksProxyConfiguration extends HttpProxyConfiguration {
            @SerializedName("username")
            @Expose(serialize = true, deserialize = true)
            public String username;

            @SerializedName("password")
            @Expose(serialize = true, deserialize = true)
            public String password;
        }
    }
}
