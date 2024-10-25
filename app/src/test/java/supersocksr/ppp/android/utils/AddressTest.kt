package supersocksr.ppp.android.utils

import Address
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class AddressTest {

  @Test
  fun testToString() {
    assertEquals("89.0.142.86", Address(null, "89.0.142.86", null).toString())
    assertEquals("https://89.0.142.86", Address("https", "89.0.142.86", null).toString())
    assertEquals("ppp://89.0.142.86:20000", Address("ppp", "89.0.142.86", 20000).toString())
  }

  @Test
  fun testParse() {
    assertEquals(Address(null, "89.0.142.86", null), Address.parse("89.0.142.86"))
    assertEquals(Address("https", "89.0.142.86", null), Address.parse("https://89.0.142.86"))
    assertEquals(Address("ppp", "89.0.142.86", 20000), Address.parse("ppp://89.0.142.86:20000"))
  }
}