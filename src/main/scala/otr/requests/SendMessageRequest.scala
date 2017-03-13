package otr.requests

import otr.Request

case class SendMessageRequest(message: Array[Byte]) extends Request {

}
