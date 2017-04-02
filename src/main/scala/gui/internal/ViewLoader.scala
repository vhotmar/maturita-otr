package gui.internal

import java.net.URL
import javafx.scene.Parent

trait ViewLoader {

  def load[T](s: String): (Parent, T)

  def load[T](fxml: URL): (Parent, T)
}
