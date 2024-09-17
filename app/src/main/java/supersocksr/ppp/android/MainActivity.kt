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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import supersocksr.ppp.android.c.libopenppp2.LIBOPENPPP2_LINK_STATE_CLIENT_UNINITIALIZED
import supersocksr.ppp.android.openppp2.IPAddressX
import supersocksr.ppp.android.openppp2.Macro
import supersocksr.ppp.android.openppp2.VPNLinkConfiguration
import supersocksr.ppp.android.ui.theme.Openppp2Theme
import supersocksr.ppp.android.ui.theme.Pink500
import supersocksr.ppp.android.utils.IpWithPort
import supersocksr.ppp.android.utils.RawReader
import supersocksr.ppp.android.utils.UserConfig
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID


const val TAG = "MainActivity"
const val ALL_CONFIGS_KEY = "all_configs"

class MainActivity : PppVpnActivity() {
  val userConfigListSerializer = ListSerializer(UserConfig.serializer())
  lateinit var configPreferences: SharedPreferences
  lateinit var settingsPreferences: SharedPreferences
  lateinit var settings: Settings
  val selectedUserConfig: MutableState<UserConfig?> = mutableStateOf(null)

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
    Log.i(TAG, "load ${selectedUserConfig.value!!}")
    val rawReader = RawReader(resources)
    val config = VPNLinkConfiguration().apply {
      SubnetAddress = "255.255.255.0"
      IPAddress = selectedUserConfig.value!!.tun_address
      Log.d(TAG, "IPAddress: $IPAddress")
      GatewayServer = IPAddressX.address_calc_first_address(IPAddress, SubnetAddress)

      VirtualSubnet = true
      BlockQUIC = false
      StaticMode = selectedUserConfig.value!!.tun_address.isEmpty()
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
            servers.add(selectedUserConfig.value!!.static_server)
            Log.d(TAG, "udp static servers: ${servers}")
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
          server = "ppp://" + selectedUserConfig.value!!.server.toString()
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
  fun hideIME(view: View? = null) {
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
    val selectedTab = remember { mutableStateOf(0) } // 当前选中的按钮索引

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
      NavItem("Home", Icons.Default.Home),
      NavItem("Settings", Icons.Default.Settings),
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
    val originalAllConfig = configPreferences.getString(ALL_CONFIGS_KEY, "[]")!!
      .let { Json.decodeFromString(userConfigListSerializer, it) }.toMutableList()
    val configListState = remember { mutableStateListOf(*originalAllConfig.toTypedArray()) }
    var vpnRunning by remember { mutableStateOf(vpn_state() != LIBOPENPPP2_LINK_STATE_CLIENT_UNINITIALIZED) }

    val itemModifier = Modifier
      .aspectRatio(1.85f, true)
      .padding(20.dp)

    // functions
    val select = { index: Int? ->
      selectedConfig = index
      selectedUserConfig.value = index?.let { configListState[it] }
    }
    val saveConfig = {
      configPreferences.edit()
        .putString(
          ALL_CONFIGS_KEY,
          Json.encodeToString(userConfigListSerializer, configListState)
        ).apply()
    }

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
                } else {
                  select(index)
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = config.name.ifEmpty { config.server.ip },
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
                saveConfig()
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
        var testText = remember { mutableStateOf("Test") }

        Button(
          onClick = {
            if (selectedConfig == null) {
              Toast.makeText(this@MainActivity, "Please select a config first", Toast.LENGTH_SHORT)
                .show()
              return@Button
            }
            vpn_run()
            vpnRunning = true
            testText.value = "Test"
          },
          enabled = vpnRunning.not()
        ) {
          Text(getString(R.string.vpn_start))
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

        Button(onClick = {
          vpn_stop()
          vpnRunning = false
        }) {
          Text(getString(R.string.vpn_stop))
        }
      }

      if (isEditing && selectedConfig != null) {
        EditConfigDialog(
          config = configListState[selectedConfig!!],
          onDismiss = { isEditing = false },
          onSave = { newConfigValue ->
            configListState[selectedConfig!!] = newConfigValue
            saveConfig()
            isEditing = false
          },
          onDelete = {
            configListState.removeAt(selectedConfig!!)
            saveConfig()
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
    var static_server by remember { mutableStateOf(TextFieldValue(config.static_server)) }
    var tun_address by remember { mutableStateOf(TextFieldValue(config.tun_address)) }

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
            text = "Edit Configuration",
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
              label = { Text("Config Name") },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = KeyboardActions(onDone = { hideIME() })
            )
          }
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = server,
              onValueChange = { server = it },
              label = { Text("Server IP and Port") },
              leadingIcon = {
                Text("ppp://")
              },
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
              label = { Text("GUID") },
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
              label = { Text("Static server IP and Port") },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
          item {
            OutlinedTextField(
              modifier = dialogTextFieldModifier,
              value = tun_address,
              onValueChange = { tun_address = it },
              label = { Text("Tun Address") },
              keyboardOptions = dialogKeyboardOptions,
              keyboardActions = dialogKeyboardActions
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            try {
              onSave(
                UserConfig(
                  name = name.text.trim(),
                  server = IpWithPort.fromString(server.text.trim()),
                  static_server = static_server.text.trim(),
                  guid = guid.text.trim(),
                  tun_address = tun_address.text.trim(),
                )
              )
            } catch (e: Exception) {
              e.printStackTrace()
              Toast.makeText(this, "Invalid Server IP and Port: ${e.message}", Toast.LENGTH_LONG)
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
  @OptIn(DelicateCoroutinesApi::class)
  fun testConnection(state: MutableState<String>) {
    // 启动异步任务
    GlobalScope.launch(Dispatchers.IO) {
      val url = URL(settingsPreferences.getString(TEST_LINK_KEY, TEST_LINK_DEFAULT)!!)
      val connection = url.openConnection() as HttpURLConnection
      val beginTime = System.currentTimeMillis()

      try {
        // 设置连接超时时间
        val timeout = settingsPreferences.getInt(TIMEOUT_KEY, TIMEOUT_DEFAULT)
        connection.connectTimeout = timeout
        connection.readTimeout = timeout

        // 发起请求并读取响应
        connection.requestMethod = "GET"
        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpURLConnection.HTTP_OK) {
          withContext(Dispatchers.Main) {
            state.value = (System.currentTimeMillis() - beginTime).toString() + "ms"
          }
        } else {
          Log.d(TAG, "Response code: $responseCode")
          withContext(Dispatchers.Main) {
            state.value = "-1 ms"
          }
        }
      } catch (e: ConnectException) {
        val cause: String = e.cause.toString()
        Log.w(TAG, "$cause: ${e.message}")
        withContext(Dispatchers.Main) {
          state.value = cause
        }
      } catch (e: Exception) {
        Log.w(TAG, "${e.cause}: ${e.message}")
        withContext(Dispatchers.Main) {
          state.value = "Error"
          Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
        }
      } finally {
        connection.disconnect()
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
