package supersocksr.ppp.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import supersocksr.ppp.android.c.libopenppp2.LIBOPENPPP2_LINK_STATE_CLIENT_UNINITIALIZED
import supersocksr.ppp.android.openppp2.IPAddressX
import supersocksr.ppp.android.openppp2.Macro
import supersocksr.ppp.android.openppp2.VPN
import supersocksr.ppp.android.openppp2.VPNLinkConfiguration
import supersocksr.ppp.android.openppp2.i.LinkOf
import supersocksr.ppp.android.ui.theme.Openppp2Theme
import supersocksr.ppp.android.ui.theme.Pink500
import supersocksr.ppp.android.utils.Address
import supersocksr.ppp.android.utils.RawReader
import supersocksr.ppp.android.utils.UserConfig
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.UUID
import java.util.concurrent.TimeUnit

const val TAG = "MainActivity"
const val ALL_CONFIGS_KEY = "all_configs"

class MainActivity : PppVpnActivity() {
  private val userConfigListSerializer = ListSerializer(UserConfig.serializer())
  private lateinit var configPreferences: SharedPreferences
  private lateinit var settingsPreferences: SharedPreferences
  private lateinit var settings: Settings
  private val selectedUserConfig: MutableState<UserConfig?> = mutableStateOf(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    configPreferences = getSharedPreferences("config_list", Context.MODE_PRIVATE)
    settingsPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE)
    settings = Settings(this, settingsPreferences)
    setContent {
      Openppp2Theme { App() }
    }
  }

  override fun vpn_load(): VPNLinkConfiguration? {
    if (selectedUserConfig.value == null) {
      return null
    }
    Log.i(TAG, "running using config: ${selectedUserConfig.value!!}")
    val rawReader = RawReader(resources)
    val config = VPNLinkConfiguration().apply {
      SubnetAddress = "255.255.255.0"
      IPAddress = selectedUserConfig.value!!.tun_address.toString()
      Log.d(TAG, "IPAddress: $IPAddress")
      GatewayServer = IPAddressX.address_calc_first_address(IPAddress, SubnetAddress)

      VirtualSubnet = true
      BlockQUIC = false
      StaticMode = selectedUserConfig.value!!.tun_address != null
      Log.d(TAG, "StaticMode: $StaticMode")
      FlashMode = false
      AtomicHttpProxySet = false
      DnsAddresses.apply {
        add("8.8.8.8")
        add("8.8.4.4")
      }

      BypassIpList = rawReader.readRawResource(R.raw.ip)
      DNSRuleList = rawReader.readRawResource(R.raw.domain)
      AllowedApplicationPackageNames.add(packageName)
      DisallowedApplicationPackageNames.add(packageName)

      VPNConfiguration.apply {
        key.apply {
          kf = 154543927
          kx = 128
          kl = 10
          kh = 12
          protocol = "aes-128-cfb"
          protocol_key = "N6HMzdUs7IUnYHwq"
          transport = "aes-256-cfb"
          transport_key = "HWFweXu2g5RVMEpy"
          masked = false
          plaintext = false
          delta_encode = false
          shuffle_data = false
        }

        tcp.apply {
          inactive.timeout = Macro.PPP_TCP_INACTIVE_TIMEOUT
          connect.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT
          turbo = true
          backlog = Macro.PPP_LISTEN_BACKLOG
          fast_open = true
        }

        udp.apply {
          inactive.timeout = Macro.PPP_UDP_INACTIVE_TIMEOUT
          dns.timeout = Macro.PPP_DEFAULT_DNS_TIMEOUT
          static_.apply {
            quic = true
            dns = true
            icmp = true
            keep_alived[0] = 0
            keep_alived[1] = 0
            aggligator = 0
            servers.add(selectedUserConfig.value!!.static_server.toString())
            Log.d(TAG, "udp static servers: $servers")
          }
        }

        websocket.apply {
          verify_peer = true
          http.apply {
            error = "Status Code: 404; Not Found"
            request.apply {
              put("Cache-Control", "no-cache")
              put("Pragma", "no-cache")
              put("Accept-Encoding", "gzip, deflate")
              put("Accept-Language", "zh-CN,zh;q=0.9")
              put("Origin", "http://www.websocket-test.com")
              put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits")
              put(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36 Edg/121.0.0.0"
              )
            }
            response["Server"] = "Kestrel"
          }
        }

        client.apply {
          guid = UUID.randomUUID().toString()
          Log.d(TAG, "client guid: $guid")
          server = VPN.vpn_link_of(selectedUserConfig.value!!.server.toString())!!.url
          Log.d(TAG, "client server: $server")
          bandwidth = 0
          reconnections.timeout = Macro.PPP_TCP_CONNECT_TIMEOUT

          http_proxy.apply {
            bind = "127.0.0.1"
            port = Macro.PPP_DEFAULT_HTTP_PROXY_PORT
          }

          socks_proxy.apply {
            bind = "127.0.0.1"
            port = Macro.PPP_DEFAULT_SOCKS_PROXY_PORT
          }
        }
      }

    }
    return config
  }

  // FIXME: this function actually cannot hide ime.
  private fun hideIME(view: View? = null) {
    val focusedView = currentFocus
    if (focusedView !is TextInputEditText) {
      return
    }
    focusedView.clearFocus()
    // hide keyboard
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow((view ?: focusedView).windowToken, 0)
  }

  // 全应用，底部导航栏
  @Composable
  fun App() {
    val navController = rememberNavController()
    val selectedTab = remember { mutableIntStateOf(0) } // 当前选中的按钮索引

    Scaffold(
      bottomBar = {
        BottomNavigationBar(
          navController = navController,
          selectedTab = selectedTab
        )
      }
    ) { innerPadding ->
      NavHost(
        navController = navController,
        startDestination = "home",
        Modifier.padding(innerPadding)
      ) {
        composable("Home") { ConfigSelectionScreen(navController) }
        composable("Settings") { settings.SettingsScreen() }
      }
    }
  }

  // 底部导航栏
  @Composable
  fun BottomNavigationBar(navController: NavHostController, selectedTab: MutableState<Int>) {
    data class NavItem(val label: String, val icon: ImageVector)

    val items = listOf(
      NavItem(getString(R.string.nav_home), Icons.Default.Home),
      NavItem(getString(R.string.nav_settings), Icons.Default.Settings),
    )

    NavigationBar(
      tonalElevation = 8.dp
    ) {
      items.forEachIndexed { index, item ->
        val isSelected = index == selectedTab.value

        NavigationBarItem(
          icon = {
            Icon(
              imageVector = item.icon,
              contentDescription = item.label
            )
          },
          label = { Text(item.label) },
          selected = isSelected,
          onClick = {
            selectedTab.value = index
            navController.navigate(item.label)
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = Color.White,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            unselectedIconColor = Color.Gray,
            unselectedTextColor = Color.Gray,
            indicatorColor = MaterialTheme.colorScheme.primary
          ),
          alwaysShowLabel = true
        )
      }
    }
  }

  // 主界面，配置列表
  @Composable
  fun ConfigSelectionScreen(navController: NavHostController) {
    var selectedConfig by remember { mutableStateOf<Int?>(null) }
    var isEditing by remember { mutableStateOf(false) }

    // 拿到所有配置，如果反序列化失败（升级 openppp2 后数据格式不兼容）就清空所有当前配置。
    val originalAllConfig = configPreferences.getString(ALL_CONFIGS_KEY, "[]")!!
      .let {
        try {
          Json.decodeFromString(userConfigListSerializer, it)
        } catch (e: Exception) {
          Toast.makeText(
            applicationContext,
            getString(R.string.warn_deserialize),
            Toast.LENGTH_LONG
          ).show()
          emptyList()
        }
      }.toMutableList()
    val configListState = remember { mutableStateListOf(*originalAllConfig.toTypedArray()) }
    var vpnRunning by remember { mutableStateOf(vpn_state() != LIBOPENPPP2_LINK_STATE_CLIENT_UNINITIALIZED) }

    val itemModifier = Modifier
      .aspectRatio(1.85f, true)
      .padding(20.dp)

    // functions
    val select = { index: Int? ->
      Log.d(TAG, "selected config index: $index")
      selectedConfig = index
      selectedUserConfig.value = index?.let { configListState[it] }
    }
    val persistAllConfig = {
      Log.d(TAG, "persistAllConfig")
      configPreferences.edit()
        .putString(
          ALL_CONFIGS_KEY,
          Json.encodeToString(userConfigListSerializer, configListState)
        ).apply()
    }

    // 主屏 UI
    Column {
      LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        modifier = Modifier
          .weight(1f)
          // 点击空白处取消选中
          .clickable(interactionSource = null, indication = null) {
            if (isEditing) {
              isEditing = false
            } else {
              select(null)
            }
          },
        contentPadding = PaddingValues(8.dp)
      ) {
        itemsIndexed(configListState) { index, config ->
          val isSelected = index == selectedConfig
          val borderWidth by animateDpAsState(
            targetValue = if (isSelected) 4.dp else 0.dp,
            animationSpec = tween(durationMillis = 200)
          )
          Box(
            modifier = itemModifier
              .background(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp)
              )
              .border(
                BorderStroke(
                  borderWidth,
                  if (isSelected) Pink500 else MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(8.dp)
              )
              .clickable {
                if (isSelected) {
                  isEditing = true
                  Log.d(TAG, "Edit config: $index")
                } else {
                  select(index)
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = config.name.ifEmpty { config.server.host.orEmpty() },
              fontSize = 14.sp,
              color = MaterialTheme.colorScheme.onPrimary,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        item {
          Box(
            modifier = itemModifier
              .background(Color.Gray, RoundedCornerShape(8.dp))
              .clickable {
                Log.i(TAG, "Add a new config")
                configListState.add(UserConfig())
                persistAllConfig()
              },
            contentAlignment = Alignment.Center
          ) {
            Text(text = "+", fontSize = 36.sp, color = Color.White)
          }
        }
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
      ) {
        val testText = remember { mutableStateOf("Test") }
        var startText by remember { mutableStateOf(getString(R.string.vpn_start)) }

        // 开始按钮
        Button(
          onClick = {
            if (selectedConfig == null) {
              Toast.makeText(
                this@MainActivity,
                getString(R.string.warn_config_not_select),
                Toast.LENGTH_SHORT
              )
                .show()
              return@Button
            }
            vpn_run()
            vpnRunning = true
            testText.value = "Test"
            selectedUserConfig.value?.name?.let { startText = it }
          },
          enabled = vpnRunning.not()
        ) {
          Text(startText)
        }
        if (vpnRunning) {
          Button(onClick = {
            if (testText.value != "Testing...") {
              testConnection(testText)
            }
            testText.value = "Testing..."
          }) {
            Text(testText.value)
          }
        }

        // 停止按钮
        Button(onClick = {
          vpn_stop()
          vpnRunning = false
          startText = getString(R.string.vpn_start)
        }) {
          Text(getString(R.string.vpn_stop))
        }
      }

      if (isEditing && selectedConfig != null) {
        EditConfigDialog(
          config = configListState[selectedConfig!!],
          onDismiss = { isEditing = false },
          onSave = { newConfigValue ->
            // 点击保存按钮后，更新当前选中配置 + 持久化，关闭窗口
            Log.d(TAG, "Save config: $newConfigValue")
            configListState[selectedConfig!!] = newConfigValue
            selectedUserConfig.value = newConfigValue
            persistAllConfig()
            isEditing = false
          },
          onDelete = {
            configListState.removeAt(selectedConfig!!)
            persistAllConfig()
            isEditing = false
            select(null)
          }
        )
      }
    }
  }

  // 编辑配置框
  @Composable
  fun EditConfigDialog(
    config: UserConfig,
    onDismiss: () -> Unit,
    onSave: (UserConfig) -> Unit,
    onDelete: () -> Unit
  ) {
    var name by remember { mutableStateOf(TextFieldValue(config.name)) }
    var server by remember {
      mutableStateOf(
        TextFieldValue(
          config.server.toString()
        )
      )
    }
    var guid by remember { mutableStateOf(TextFieldValue(config.guid)) }
    var static_server by remember { mutableStateOf(TextFieldValue(config.static_server.toString())) }
    // null 则填入空字符串
    var tun_address by remember {
      mutableStateOf(
        TextFieldValue(
          config.tun_address?.toString() ?: ""
        )
      )
    }

    val lazyListState = rememberLazyListState()
    val dialogKeyboardActions = KeyboardActions(onDone = {
      hideIME()
    })
    val dialogKeyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done)
    val dialogTextFieldModifier = Modifier
      .fillMaxWidth()
      .padding(8.dp)

    AlertDialog(
      onDismissRequest = onDismiss,
      title = {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = getString(R.string.config_title),
            modifier = Modifier
              .weight(1f)
              .padding(end = 8.dp),
            fontSize = 22.sp
          )
          DeleteButton { onDelete() }
        }
      },
      modifier = Modifier.clickable(interactionSource = null, indication = null) { hideIME() },
      text = {
        LazyColumn(state = lazyListState, contentPadding = PaddingValues(4.dp)) {
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = name,
              onValueChange = { name = it },
              label = { Text(getString(R.string.config_name)) },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = KeyboardActions(onDone = { hideIME() })
            )
          }
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = server,
              onValueChange = { server = it },
              label = { Text(getString(R.string.config_server)) },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = guid,
              readOnly = true,
              onValueChange = { guid = it },
              label = { Text(getString(R.string.config_guid)) },
              placeholder = { Text("Random") },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = static_server,
              onValueChange = { static_server = it },
              label = { Text(getString(R.string.config_static)) },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = tun_address,
              onValueChange = { tun_address = it },
              label = { Text(getString(R.string.config_tun)) },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            // 保存配置
            try {
              val cfg = UserConfig(
                name = name.text.trim(),
                server = Address.unsafeParse(server.text.trim()),
                static_server = Address.unsafeParse(static_server.text.trim()),
                guid = guid.text.trim(),
                tun_address = tun_address.text.trim()
                  .let { if (it.isBlank()) null else Address.parse(it) },
              )
              cfg.validate()
              onSave(cfg)
            } catch (e: Exception) {
              e.printStackTrace()
              Toast.makeText(
                this,
                "Invalid supersocksr.ppp.android.utils.Address: ${e.message}",
                Toast.LENGTH_LONG
              )
                .show()
            }
          }
        ) {
          Text(getString(R.string.save))
        }
      },
      dismissButton = {
        Button(
          onClick = onDismiss
        ) {
          Text(getString(R.string.cancel))
        }
      }
    )
  }

  // 测试连接
  private fun testConnection(state: MutableState<String>) {
    Log.i(TAG, "testConnection starting..")
    lifecycleScope.launch(Dispatchers.IO) {
      val url = URL(settingsPreferences.getString(TEST_LINK_KEY, TEST_LINK_DEFAULT)!!)
      val timeout = settingsPreferences.getLong(TIMEOUT_KEY, TIMEOUT_DEFAULT)
      val client = OkHttpClient.Builder()
        .connectTimeout(timeout, TimeUnit.MILLISECONDS)
        .readTimeout(timeout, TimeUnit.MILLISECONDS)
        .callTimeout(timeout, TimeUnit.MILLISECONDS)
        .build()

      val request = Request.Builder()
        .url(url)
        .get()
        .build()

      val beginTime = System.currentTimeMillis()
      Log.d(TAG, "beginTime: $beginTime")
      try {
        client.newCall(request).execute().use { response ->
          val result = if (response.isSuccessful) {
            (System.currentTimeMillis() - beginTime).toString() + "ms"
          } else {
            "-1 ms"
          }
          withContext(Dispatchers.Main) {
            state.value = result
          }
        }
      } catch (e: Exception) {
        Log.e(TAG, "${e.cause}: ${e.message}")
        val tx = when (e.cause) {
          is ConnectException -> {
            "No Connection"
          }

          is UnknownHostException -> {
            "Unknown Host"
          }

          is SocketTimeoutException -> {
            "Timeout"
          }

          else -> {
            "Error"
          }
        }
        withContext(Dispatchers.Main) {
          state.value = tx
          Toast.makeText(this@MainActivity, "${e.cause}: ${e.message}", Toast.LENGTH_LONG).show()
        }
      }
    }
  }
}


@Composable
fun DeleteButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
  IconButton(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.Delete, // 使用 Material Design 的删除图标
      contentDescription = "Delete",
      tint = MaterialTheme.colorScheme.primary // 设置图标颜色
    )
  }
}
