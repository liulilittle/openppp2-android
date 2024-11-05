package supersocksr.ppp.android.utils

import Address
import kotlinx.serialization.Serializable

@Serializable
data class UserConfig(
  val name: String = "",
  val server: Address = Address.unsafeParse("ppp://127.0.0.1:1080"),
  val static_server: Address = Address.unsafeParse("192.168.0.24:20000"),
  val guid: String = "Random",
  val tun_address: Address? = Address.unsafeParse("10.0.0.214"),
) {
  fun validate() {
    // supplement
    if (!server.hasScheme()) {
      server.scheme = "ppp"
    }
    // check
    if (server.scheme == "ppp" && !server.hasPort()) {
      throw IllegalArgumentException("ppp server port cannot be empty")
    }
    if (!static_server.hasPort()) {
      throw IllegalArgumentException("static_server port cannot be empty")
    }
  }
}
