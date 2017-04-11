package otr.requests

import otr.Request

case class SendMessageRequest(message: Array[Byte], tlvs: List[Tlv]) extends Request

object SendMessageRequest {
  def apply(message: Array[Byte]): SendMessageRequest = new SendMessageRequest(message, List.empty)
}



