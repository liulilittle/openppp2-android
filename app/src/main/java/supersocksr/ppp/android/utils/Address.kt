package supersocksr.ppp.android.utils

import kotlinx.serialization.Serializable

@Serializable
data class Address(
  var scheme: String? = null,
  val host: String? = null,
  val additional_ip: String? = null,
  val port: Int? = null,
  val path: String? = null,
) {

  companion object {
    private val addressRegex = Regex(
      "^(?:([a-zA-Z][a-zA-Z0-9+.-]*)://)?" +  // Group 1: scheme
              "([^\\[\\]:/]+)?" +                 // Group 2: host
              "(?:\\[([^]]+)])?" +                    // Group 3: ip
              "(?::(\\d+))?" +                        // Group 4: port
              "(/.*)?"                                // Group 5: path
    )

    fun parse(input: String): Address? {
      return try {
        unsafeParse(input)
      } catch (e: Exception) {
        null
      }
    }

    fun unsafeParse(input: String): Address {
      val matchResult = addressRegex.matchEntire(input)
        ?: throw IllegalArgumentException("address not match regex.")
      val scheme = matchResult.groupValues.getOrNull(1)?.let { it.ifEmpty { null } }
      val host = matchResult.groupValues.getOrNull(2)?.let { it.ifEmpty { null } }
      val ip = matchResult.groupValues.getOrNull(3)?.let { it.ifEmpty { null } }
      val port = matchResult.groupValues.getOrNull(4)?.let { it.ifEmpty { null } }
      val path = matchResult.groupValues.getOrNull(5)?.let { it.ifEmpty { null } }
      return Address(scheme, host, ip, port?.toInt(), path)
    }
  }

  override fun toString(): String {
    val sb = StringBuilder()
    if (scheme != null) sb.append("$scheme://")
    if (host != null) sb.append(host)
    if (additional_ip != null) sb.append("[$additional_ip]")
    if (port != null) sb.append(":$port")
    if (path != null) sb.append(path)
    return sb.toString()
  }

  fun debug() {
    println("scheme: $scheme")
    println("host: $host")
    println("additional_ip: $additional_ip")
    println("port: $port")
    println("path: $path")
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Address

    return scheme == other.scheme && host == other.host && port == other.port && path == other.path && additional_ip == other.additional_ip
  }

  fun hasScheme(): Boolean {
    return scheme != null
  }

  fun hasPort(): Boolean {
    return port != null
  }

  fun hasAdditionalIp(): Boolean {
    return additional_ip != null
  }

  fun hasPath(): Boolean {
    return path != null
  }

  fun hasHost(): Boolean {
    return host != null
  }

  override fun hashCode(): Int {
    var result = scheme?.hashCode() ?: 0
    result = 31 * result + (host?.hashCode() ?: 0)
    result = 31 * result + (additional_ip?.hashCode() ?: 0)
    result = 31 * result + (port ?: 0)
    result = 31 * result + (path?.hashCode() ?: 0)
    return result
  }

}