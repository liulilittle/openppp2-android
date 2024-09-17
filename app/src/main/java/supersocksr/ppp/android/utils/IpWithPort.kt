package supersocksr.ppp.android.utils

import kotlinx.serialization.Serializable

@Serializable
class IpWithPort(val ip: String, val port: Int) {
  companion object {
    @Throws(IllegalArgumentException::class)
    fun fromString(string: String): IpWithPort {
      try {
        val split = string.split(":")
        return IpWithPort(split[0], split[1].toInt())
      } catch (e: IndexOutOfBoundsException) {
        throw IllegalArgumentException("ip and port should be separated by ':'")
      } catch (e: NumberFormatException) {
        throw IllegalArgumentException("port should be an integer")
      }
    }

    fun default(): IpWithPort {
      return IpWithPort("127.0.0.1", 1080)
    }
  }

  override fun toString(): String {
    return "$ip:$port"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false
    other as IpWithPort
    if (ip != other.ip) return false
    if (port != other.port) return false
    return true
  }

  override fun hashCode(): Int {
    var result = ip.hashCode()
    result = 31 * result + port
    return result
  }
}