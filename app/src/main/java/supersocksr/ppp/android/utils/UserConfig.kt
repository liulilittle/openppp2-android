package supersocksr.ppp.android.utils

import kotlinx.serialization.Serializable

@Serializable
data class UserConfig(
  val name: String = "",
  val server: IpWithPort = IpWithPort.default(),
  val static_server: String = "192.168.0.24:20000",
  val guid: String = "Random",
  val tun_address: String = "10.0.0.214"
)