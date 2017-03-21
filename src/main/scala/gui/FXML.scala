package gui

import java.net.URL
import java.util.{Locale, ResourceBundle}
import javafx.scene.Parent

import scalafxml.core.{DependenciesByType, FXMLLoader}

object FXML {
  def load(s: String): (Parent, Route) = load(getClass.getResource(s))

  def load(fxml: URL): (Parent, Route) = {
    val loader = new FXMLLoader(fxml, new DependenciesByType(Map()))

    loader.setResources(ResourceBundle.getBundle("Translations", new Locale("cs", "CZ"), new UTF8Control))

    loader.load()
    (loader.getRoot[javafx.scene.Parent](), loader.getController[Route]())
  }
}
