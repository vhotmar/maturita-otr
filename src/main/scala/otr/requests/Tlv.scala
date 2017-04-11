package otr.requests

import otr.utils.SMP
import scodec.Codec
import scodec.bits.{BitVector, ByteVector}
import scodec.codecs._
import utils.Results.FResult
import utils.{EParsable, EParsableCompanion}

case class BigIntTlv(tlvType: Int, bigints: List[BigInt])

object BigIntTlv {
  def codec: Codec[List[BigInt]] = listOfN(int32, SMP.bigIntCodec).as[List[BigInt]]

  implicit def bigIntTlvToTlv(b: BigIntTlv): Tlv =
    Tlv(b.tlvType, BigIntTlv.codec.encode(b.bigints).require.toByteVector)
}

object UBigIntTlv {
  def unapply(arg: Tlv): Option[(Int, List[BigInt])] =
    BigIntTlv.codec.decode(arg.value.toBitVector).fold(x => None, (res) => Some(arg.tlvType -> res.value))
}

case class Tlv(tlvType: Int, value: ByteVector) extends EParsable {
  type E = Tlv

  def companion = Tlv
}

object Tlv extends EParsableCompanion[Tlv] {

  import otr.utils.AttemptConversions._
  import otr.utils.BitVectorConversions._

  def encodeList(list: List[Tlv]): FResult[BitVector] = listCodec.encode(list)

  def listCodec: Codec[List[Tlv]] = list(codec)

  def codec: Codec[Tlv] = {
    ("tlvType" | int16) ::
      ("value" | variableSizeBytes(int16, scodec.codecs.bytes))
  }.as[Tlv]

  def decodeList(bytes: Array[Byte]): FResult[List[Tlv]] = listCodec.decode(bytes)

}
