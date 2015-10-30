package nordpol

import org.scalatest._

class ApduSpec extends WordSpec with Matchers {
  "Apdu.encodeHex" should {
    "encode one byte" in {
      Apdu.encodeHex(1.toByte) shouldEqual "01"
    }
    "encode FF" in {
      Apdu.encodeHex(0xFF.toByte) shouldEqual "FF"
    }
    "encode several bytes" in {
      Apdu.encodeHex(Array[Byte](0xFF.toByte, 0x00.toByte, 0x11.toByte)) shouldEqual "FF0011"
    }
  }
  "Apdu.decodeHex" should {
    "decode one byte" in {
      Apdu.decodeHex("01") shouldEqual Array[Byte](0x01.toByte)
    }
    "decode FF" in {
      Apdu.decodeHex("FF") shouldEqual Array[Byte](0xFF.toByte)
    }
    "decode ff" in {
      Apdu.decodeHex("ff") shouldEqual Array[Byte](0xFF.toByte)
    }
    "decode an array of bytes" in {
      Apdu.decodeHex("FF0011") shouldEqual Array[Byte](0xFF.toByte, 0x00.toByte, 0x11.toByte)
    }
    "refuse to decode uneven strings" in {
      intercept[IllegalArgumentException] {
        Apdu.decodeHex("123")
      }
    }
  }
  "Apdu.select" should {
    "generate command for A000000151000000" in {
      Apdu.select("A000000151000000") shouldEqual Apdu.decodeHex("00A4040008A000000151000000")
    }
    "generate command for appId 12345678 and suffix 01" in {
      Apdu.select("12345678", "01") shouldEqual Apdu.decodeHex("00A404000BA000000617001234567801")
    }
  }
  "Apdu.statusBytes" should {
    "return status from empty response" in {
      Apdu.statusBytes(Array[Byte](0x90.toByte, 0x00.toByte)) shouldEqual Array[Byte](0x90.toByte, 0x00.toByte)
    }
    "return status" in {
      Apdu.statusBytes(Array[Byte](0x01.toByte, 0x90.toByte, 0x00.toByte)) shouldEqual Array[Byte](0x90.toByte, 0x00.toByte)
    }
  }
  "Apdu.responseData" should {
    "return data from empty response" in {
      Apdu.responseData(Array[Byte](0x90.toByte, 0x00.toByte)) shouldEqual Array.empty[Byte]
    }
    "return data" in {
      Apdu.responseData(Array[Byte](0x01.toByte, 0x90.toByte, 0x00.toByte)) shouldEqual Array[Byte](0x01.toByte)
    }
  }
  "Apdu.hasStatus" should {
    "match a 0x9000 response" in {
      Apdu.hasStatus(Array[Byte](0x00, 0x00, 0x90.toByte, 0x00), Array[Byte](0x90.toByte, 0x00)) shouldEqual true
    }

    "don't match a 0x9090 response" in {
      Apdu.hasStatus(Array[Byte](0x00, 0x00, 0x90.toByte, 0x90.toByte), Array[Byte](0x90.toByte, 0x00)) shouldEqual false
    }

    "don't match failure with a 0x9000 response" in {
      Apdu.hasStatus(Array[Byte](0x00, 0x00, 0x6F.toByte, 0x00), Array[Byte](0x90.toByte, 0x00)) shouldEqual false
    }

    "match a 0x9000 response with a hex string" in {
      Apdu.hasStatus(Array[Byte](0x00, 0x00, 0x90.toByte, 0x00), "9000") shouldEqual true
    }

    "match a 9000 hex string response with a hex string" in {
      Apdu.hasStatus("00009000", "9000") shouldEqual true
    }
  }

}
