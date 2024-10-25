import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class Address(var scheme: String?, val host: String, val port: Int?) {

  companion object {
    fun parse(address: String): Address {
      val uri =
        URI.create(if (address.contains("://")) address else "//$address")  // 确保 scheme 存在，否则 URI 解析会失败
      return Address(
        scheme = uri.scheme,
        host = uri.host ?: throw IllegalArgumentException("Invalid host"),
        port = if (uri.port == -1) null else uri.port
      )
    }

    fun tryParse(address: String): Address? {
      return try {
        parse(address)
      } catch (e: IllegalArgumentException) {
        null
      }
    }
  }

  override fun toString(): String {
    return "${scheme?.let { "$it://" } ?: ""}$host${port?.let { ":$it" } ?: ""}"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Address

    if (scheme != other.scheme) return false
    if (host != other.host) return false
    if (port != other.port) return false

    return true
  }

  override fun hashCode(): Int {
    var result = scheme?.hashCode() ?: 0
    result = 31 * result + host.hashCode()
    result = 31 * result + (port ?: 0)
    return result
  }

  fun hasScheme(): Boolean {
    return scheme != null
  }

  fun hasPort(): Boolean {
    return port != null
  }

}