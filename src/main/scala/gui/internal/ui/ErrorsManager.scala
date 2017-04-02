package gui.internal.ui

import scalafx.collections.ObservableMap
import scalafx.scene.control.TextField

case class ErrorsManager(errors: ObservableMap[String, String], config: Map[String, TextField]) {
  val validations: Map[String, ValidationControl] = config.mapValues(field => new ValidationControl(field))

  errors.onChange((map, change) => {
    validations.foreach(_._2.hide())

    map
      .filter(x => !x._2.isEmpty)
      .filter(x => validations.contains(x._1))
      .foreach(x => validations(x._1).show(x._2))
  })
}
