package supersocksr.ppp.android.utils

import android.content.res.Resources
import java.nio.charset.StandardCharsets

class RawReader(private val resources: Resources) {
  fun readRawResource(resId: Int): String {
    return try {
      (resources.openRawResource(resId).use { inputStream ->
        inputStream.readBytes().toString(StandardCharsets.UTF_8)
      })
    } catch (e: Exception) {
      e.printStackTrace()
      ""
    }
  }
}