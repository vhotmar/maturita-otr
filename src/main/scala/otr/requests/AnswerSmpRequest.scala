package otr.requests

import otr.Request

case class AnswerSmpRequest(secret: Array[Byte]) extends Request