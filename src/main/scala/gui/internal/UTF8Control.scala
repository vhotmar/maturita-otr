package gui.internal

import java.io.{InputStream, InputStreamReader}
import java.util.ResourceBundle.Control
import java.util.{Locale, PropertyResourceBundle, ResourceBundle}

// Transformed into scala from:
// http://stackoverflow.com/questions/4659929/how-to-use-utf-8-in-resource-properties-with-resourcebundle
class UTF8Control extends Control {
  override def newBundle(baseName: String, locale: Locale, format: String, loader: ClassLoader, reload: Boolean): ResourceBundle = {
    val bundleName = toBundleName(baseName, locale)
    val resourceName = toResourceName(bundleName, "properties")

    var bundle: ResourceBundle = null
    var stream: InputStream = null

    if (reload) {
      val url = loader.getResource(resourceName)

      if (url != null) {
        val connection = url.openConnection()

        if (connection != null) {
          connection.setUseCaches(false)
          stream = connection.getInputStream
        }
      }
    } else {
      stream = loader.getResourceAsStream(resourceName)
    }

    if (stream != null) {
      try {
        bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"))
      } finally {
        stream.close()
      }
    }

    bundle
  }
}
