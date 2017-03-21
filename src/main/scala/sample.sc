case class Node[T](value: T) {
  var next: Option[T] = None
}

val s1 = Node("asdf")

val s2 = Node("qwer")

s1.next = Some(s2)