package otr.messages.types

import scodec.bits.ByteVector

case class Data[B](bytes: ByteVector, value: B)
