package otr.requests

import otr.Request

case class InitSmpRequest(secret: Array[Byte], question: Option[Array[Byte]]) extends Request
