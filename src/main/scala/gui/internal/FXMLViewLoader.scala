package gui.internal

import java.net.URL
import java.util.ResourceBundle
import javafx.scene.Parent

import scalafxml.core.{ControllerDependencyResolver, FXMLLoader}

class FXMLViewLoader(resourceBundle: ResourceBundle, dependencyResolver: ControllerDependencyResolver) extends ViewLoader {
  def load[T](s: String): (Parent, T) = load[T](getClass.getResource(s))

  def load[T](fxml: URL): (Parent, T) = {
    val loader = new FXMLLoader(fxml, dependencyResolver)

    loader.setResources(resourceBundle)

    loader.load()

    (loader.getRoot[javafx.scene.Parent](), loader.getController[T]())
  }
}
