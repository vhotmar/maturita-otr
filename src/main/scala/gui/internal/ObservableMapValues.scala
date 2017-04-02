package gui.internal

import scalafx.collections.ObservableMap.{Add, Remove, Replace}
import scalafx.collections.{ObservableBuffer, ObservableMap}

class ObservableMapValues[K, V](map: ObservableMap[K, V]) {
  val internal: ObservableBuffer[V] = ObservableBuffer.empty[V]

  map.onChange((map, change) => {
    change match {
      case Replace(_, toAdd, toRemove) =>
        internal -= toRemove
        internal += toAdd

      case Add(_, toAdd) =>
        internal += toAdd

      case Remove(_, toRemove) =>
        internal -= toRemove
    }
  })
}

object ObservableMapValues {
  def apply[K, V](map: ObservableMap[K, V]): ObservableBuffer[V] =
    new ObservableMapValues(map).internal
}
