package supersocksr.ppp.android.utils

import Address
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class AddressTest {

  @Test
  fun testToString() {
    assertEquals("89.0.142.86", Address(null, "89.0.142.86").toString())
    assertEquals("https://89.0.142.86", Address("https", "89.0.142.86", null).toString())
    assertEquals("ppp://89.0.142.86:20000", Address("ppp", "89.0.142.86", port = 20000).toString())
    assertEquals(
      "ws://www.baidu.com[127.0.0.1]:899/tun",
      Address("ws", "www.baidu.com", "127.0.0.1", 899, "/tun").toString()
    )
    assertEquals(
      "ws://www.baidu.com[127.0.0.1]/tun",
      Address("ws", "www.baidu.com", "127.0.0.1", null, "/tun").toString()
    )
  }

  @Test
  fun testParse() {
    assertEquals(Address(host = "89.0.142.86"), Address.unsafeParse("89.0.142.86"))
    assertEquals(Address("https", "89.0.142.86", null), Address.unsafeParse("https://89.0.142.86"))
    assertEquals(
      Address("ppp", "89.0.142.86", port = 20000),
      Address.unsafeParse("ppp://89.0.142.86:20000")
    )
    assertEquals(
      Address("ws", "www.baidu.com", "127.0.0.1", 899, "/tun"),
      Address.unsafeParse("ws://www.baidu.com[127.0.0.1]:899/tun")
    )
    assertEquals(
      Address("ws", "www.baidu.com", "127.0.0.1", null, "/tun"),
      Address.unsafeParse("ws://www.baidu.com[127.0.0.1]/tun")
    )
  }
}