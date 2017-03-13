package otr.messages

import otr.Types
import otr.messages.data.DataT
import otr.messages.types.Mac
import otr.utils.{Message, MessageCompanion}
import scodec.Codec
import scodec.bits.{ByteVector, HexStringSyntax}
import scodec.codecs._

// TODO: Need to add old mac keys - forgeability
case class Data(
  flags: Byte,
  dataT: types.Data[DataT],
  mac: Mac
) extends Message {
  type E = Data

  def companion = Data
}

object Data extends MessageCompanion[Data] {
  def codec(version: Int): Codec[Data] = {
    ("flags" | byte) ::
      ("dataT" | Types.bData(DataT.codec)) ::
      ("mac" | Types.mac(20))
  }.as[Data]

  def command: ByteVector = hex"0x03"
}


