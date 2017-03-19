package gui

import java.io.{InputStream, InputStreamReader}
import java.net.URL
import java.util.ResourceBundle.Control
import java.util.{Locale, PropertyResourceBundle, ResourceBundle}
import javafx.{scene => jfxs}

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.scene.Scene
import scalafxml.core.{DependenciesByType, FXMLLoader}


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


object Client extends JFXApp {
  println(getClass)
  println(getClass.getResource("/Login.fxml"))

  def load(fxml: URL) = {
    val loader = new FXMLLoader(fxml, new DependenciesByType(Map()))

    loader.setResources(ResourceBundle.getBundle("Translations", new Locale("cs", "CZ"), new UTF8Control))

    loader.load()
    loader.getRoot[jfxs.Parent]()
  }

  stage = new JFXApp.PrimaryStage {
    title.value = "Hello Stage"
    width = 600
    scene = new Scene(
      load(getClass.getResource("/Login.fxml"))
    )
  }
}
