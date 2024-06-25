package supersocksr.ppp.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import supersocksr.ppp.android.openppp2.IPAddressX;
import supersocksr.ppp.android.openppp2.Macro;
import supersocksr.ppp.android.openppp2.PackageX;
import supersocksr.ppp.android.openppp2.TextX;
import supersocksr.ppp.android.openppp2.VPNConfiguration;
import supersocksr.ppp.android.openppp2.VPNLinkConfiguration;
import supersocksr.ppp.android.openppp2.X;


public class MainActivity extends PppVpnActivity {
    private TextInputEditText server;
    private TextInputEditText static_server;
    private TextInputEditText tunip;
    private MaterialButton btn_start, btn_stop;
    private boolean blankTunIp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!is_reload_activity_by_launcher()) {
            setContentView(R.layout.activity_main);
            ViewGroup root_layout = findViewById(R.id.rootLayout);
            server = findViewById(R.id.serverAddress);
            static_server = findViewById(R.id.staticserver);
            TextInputEditText uuid = findViewById(R.id.guidNumber);
            tunip = findViewById(R.id.tunIP);
            btn_start = findViewById(R.id.btn_start);
            btn_stop = findViewById(R.id.btn_stop);
            btn_start.setOnClickListener(v -> {
                btn_start.setClickable(false);
                btn_stop.setClickable(true);
                cancelCurrentFocus(v);
                Toast.makeText(this, "Start openppp2.", Toast.LENGTH_SHORT).show();
                vpn_run();
            });
            btn_stop.setOnClickListener(v -> {
                btn_start.setClickable(true);
                btn_stop.setClickable(false);
                cancelCurrentFocus(v);
                Toast.makeText(this, "Stop openppp2.", Toast.LENGTH_SHORT).show();
                vpn_stop();
            });
            server.setOnFocusChangeListener(this::focusChangeAction);
            tunip.setOnFocusChangeListener(this::focusChangeAction);
            root_layout.setOnClickListener(this::cancelCurrentFocus);
            loadInput(server, "ppp://");
            loadInput(uuid, "");
            loadInput(tunip, "10.0.0.214");

            static_server = findViewById(R.id.staticserver);
            blankTunIp = X.is_empty(TextX.trim(TextX.to_string(static_server.getText())));

            tunip = findViewById(R.id.tunIP);
            findViewById(R.id.btn_start).setOnClickListener(v -> vpn_run());
            findViewById(R.id.btn_stop).setOnClickListener(v -> vpn_stop());
        }
    }

    //  save value for TextInputEditText
    private final void focusChangeAction(View v, boolean hasFocus) {
        if (!hasFocus && v instanceof TextInputEditText) {
            saveInput((TextInputEditText) v);
        }
    }

    private void loadInput(@NonNull TextInputEditText editText, String defaultValue) {
        // get SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("UserInput", MODE_PRIVATE);
        editText.setText(sharedPreferences.getString(getResources().getResourceEntryName(editText.getId()), defaultValue));
    }

    private void saveInput(@NonNull TextInputEditText editText) {
        // get SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("UserInput", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // save value
        String key = getResources().getResourceEntryName(editText.getId());
        editor.putString(key, TextX.trim(TextX.to_string(editText.getText())));
        editor.apply();
    }

    // cancel current focus view, if focus view is TextInputEditText, save value.
    private void cancelCurrentFocus(@NonNull View view) {
        View focusedView = getCurrentFocus();
        if (!(focusedView instanceof TextInputEditText)) {
            return;
        }
        try {
            focusChangeAction(focusedView, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        focusedView.clearFocus();
        // hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

    }

    private String readRawIpBypass() {
        String result = "";
        try {
            InputStream is = getResources().openRawResource(R.raw.ip);
            int length = is.available();

            byte[] buffer = new byte[length];
            is.read(buffer);

            result = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
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

            result = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected VPNLinkConfiguration vpn_load() {
        VPNLinkConfiguration config = new VPNLinkConfiguration();
        config.SubnetAddress = "255.255.255.0";
        config.IPAddress = TextX.trim(TextX.to_string(tunip.getText()));
        config.GatewayServer = IPAddressX.address_calc_first_address(config.IPAddress, config.SubnetAddress);

        config.VirtualSubnet = true;
        config.BlockQUIC = false;
        config.StaticMode = !blankTunIp;
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
        vpn.udp.static_.aggligator = 0;
        vpn.udp.static_.servers.add(TextX.trim(TextX.to_string(static_server.getText())));

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
        vpn.client.server = TextX.trim(TextX.to_string(server.getText()));
        vpn.client.bandwidth = 0;
        vpn.client.reconnections.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT;
        vpn.client.http_proxy.bind = "127.0.0.1";
        vpn.client.http_proxy.port = Macro.PPP_DEFAULT_HTTP_PROXY_PORT;

        return config;
    }
}
