package supersocksr.ppp.android.utils

import Address
import kotlinx.serialization.Serializable

@Serializable
data class UserConfig(
  val name: String = "",
  val server: Address = Address.parse("ppp://127.0.0.1:1080"),
  val static_server: Address = Address.parse("192.168.0.24:20000"),
  val guid: String = "Random",
  val tun_address: Address? = null
) {

  fun validate() {
    if (!server.hasScheme()) {
      server.scheme = "ppp"
    }
    if (!server.hasPort()) {
      throw IllegalArgumentException("server port cannot be empty")
    }
    if (!static_server.hasPort()) {
      throw IllegalArgumentException("static_server port cannot be empty")
    }
  }
}
