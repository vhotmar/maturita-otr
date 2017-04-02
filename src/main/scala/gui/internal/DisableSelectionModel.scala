package gui.internal

import javafx.collections.{FXCollections, ObservableList}
import javafx.scene.control.MultipleSelectionModel


class DisableSelectionModel[T] extends MultipleSelectionModel[T] {
  setSelectedIndex(-1)
  setSelectedItem(_)

  override def getSelectedItems: ObservableList[T] = {
    FXCollections.emptyObservableList()
  }

  override def getSelectedIndices: ObservableList[Integer] = {
    FXCollections.emptyObservableList()
  }

  override def selectFirst(): Unit = {}

  override def selectAll(): Unit = {}

  override def selectLast(): Unit = {}

  override def selectIndices(index: Int, indices: Int*): Unit = {}

  override def select(index: Int): Unit = {}

  override def select(obj: T): Unit = {}

  override def selectPrevious(): Unit = {}

  override def selectNext(): Unit = {}

  override def clearAndSelect(index: Int): Unit = {}

  override def clearSelection(index: Int): Unit = {}

  override def clearSelection(): Unit = {}

  override def isSelected(index: Int): Boolean = false

  override def isEmpty: Boolean = true
}
