package edu.scalanus.compiler

import edu.scalanus.compiler.LiteralParser._
import edu.scalanus.errors.ScalanusException
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class LiteralParserSpec extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  "LiteralParser.parse* functions" should "correctly parse valid literals" in {
    val data = Table(
      ("function", "literal", "value"),

      (parseBool _, "True", true),
      (parseBool _, "False", false),

      (parseInt _, "123", 123),
      (parseInt _, "0", 0),
      (parseInt _, "00000", 0),
      (parseInt _, "-1", -1),
      (parseInt _, "-0", 0),
      (parseInt _, "12_34", 1234),
      (parseInt _, "12_34_", 1234),
      (parseInt _, "12__34", 1234),
      (parseInt _, "001234", 1234),
      (parseInt _, "0b0101", 5),
      (parseInt _, "0b0000101", 5),
      (parseInt _, "0b00_00_10_1", 5),
      (parseInt _, "0b_00_00_10_1", 5),
      (parseInt _, "0o777", 511),
      (parseInt _, "0o00777", 511),
      (parseInt _, "0o0077__7", 511),
      (parseInt _, "0o_0077__7", 511),
      (parseInt _, "0xbeef", 0xbeef),
      (parseInt _, "0x_beef_", 0xbeef),

      (parseFloat _, "1.0", 1.0),
      (parseFloat _, "1", 1.0),
      (parseFloat _, ".1", .1),
      (parseFloat _, "1_00.1_2", 100.12),
      (parseFloat _, "1.0e-6", 1.0e-6),
      (parseFloat _, "1.0E-6", 1.0e-6),
      (parseFloat _, "1.0e+6", 1.0e+6),
      (parseFloat _, "1.0E+6", 1.0e+6),
      (parseFloat _, "1.0E+_6_", 1.0e+6),
      (parseFloat _, "1e-6", 1e-6),

      (parseString _, "\"abcd\"", "abcd"),
      (parseString _, "\"\"", ""),
      (parseString _, "\"\\\"\"", "\""),

      (parseChar _, "'a'", 'a'),
      (parseChar _, "'\\''", '\'')
    )

    forEvery(data) { (f, literal, value) =>
      f(literal) shouldBe value
    }
  }

  "LiteralParser.unescape" should "correctly unescape valid strings" in {
    val data = Table(
      ("raw", "unescaped"),
      ("aaa", "aaa"),
      ("a\\na", "a\na"),
      ("a\\ra", "a\ra"),
      ("a\\ta", "a\ta"),
      ("a\\0a", "a\u0000a"),
      ("a\\'a", "a'a"),
      ("a\\\"a", "a\"a"),
      ("a\\\\a", "a\\a"),
      ("a\\x20a", "a a"),
      ("a\\x0aa", "a\na"),
      ("a\\x0Aa", "a\na"),
      ("foo\\r\\nbar", "foo\r\nbar"),
      ("\\u{0119}dw\\u{0105}rd", "\u0119dw\u0105rd"),
      ("\\u{0}", "\u0000"),
      ("\\u{00}", "\u0000"),
      ("\\u{000}", "\u0000"),
      ("\\u{0000}", "\u0000"),
      ("\\u{00000}", "\u0000"),
      ("\\u{000000}", "\u0000"),
      ("\\xff", "\u00ff")
    )

    forEvery(data) { (raw, unescaped) =>
      LiteralParser.unescape(raw) shouldBe unescaped
    }
  }

  it should "raise ScalanusException for invalid strings" in {
    val data = Table(
      "raw",
      "\\u{0000000}",
      "\\u{00000000}",
      "\\u{}",
      "\\u{",
      "\\u",
      "\\u}",
      "\\u{zzzz}",
      "\\a",
      "\\x",
      "\\x0",
      "foo\\\n    bar"
    )

    forEvery(data) { raw =>
      an[ScalanusException] should be thrownBy LiteralParser.unescape(raw)
    }
  }

}
