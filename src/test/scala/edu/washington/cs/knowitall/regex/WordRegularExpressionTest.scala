package edu.washington.cs.knowitall.regex

import scala.collection.JavaConverters._

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import edu.washington.cs.knowitall.regex.Expression.BaseExpression

@RunWith(classOf[JUnitRunner])
class WordRegularExpressionTest extends Specification {
  case class WordToken(string: String, postag: String, chunk: String)

  def compile(string: String): RegularExpression[WordToken] = {
    // create a parser for regular expression language that have
    // the same token representation
    val parser =
      new RegularExpressionParser[WordToken]() {
        // Translate an string "part=value" into a BaseExpression that
        // checks whether the part of a WordToken has value 'value'.
        override def factory(string: String): BaseExpression[WordToken] = {
          new BaseExpression[WordToken](string) {
            val Array(part, quotedValue) = string.split("=")
            val value = quotedValue.drop(1).take(quotedValue.size - 2)
            override def apply(entity: WordToken) = {
              part match {
                case "string" => entity.string equalsIgnoreCase value
                case "postag" => entity.postag equalsIgnoreCase value
                case "chunk" => entity.chunk equalsIgnoreCase value
              }
            }
          }
        }
      }

    parser.parse(string)
  }

  "README regex example one" should {
    "work" in {
      val sentence = "The US president Barack Obama is travelling to Mexico."
      val tokens = Seq(
        WordToken("The", "DT", null),
        WordToken("US", "NNP", null),
        WordToken("president", "NN", null),
        WordToken("Barack", "NNP", null),
        WordToken("Obama", "NNP", null),
        WordToken("is", "VB", null),
        WordToken("travelling", "VB", null),
        WordToken("to", "TO", null),
        WordToken("Mexico", "NN", null),
        WordToken(".", ".", null))
      val regex = compile("""(?:<string='a'> | <string='an'> | <string='the'>)? <postag='JJ'>* <postag='NNP'>+ <postag='NN'>+ <postag='NNP'>+""")
      val found = Option(regex.find(tokens.asJava))
      found.size must_== 1
      found.get.groups.get(0).tokens.asScala.map(_.string).mkString(" ") must_== "The US president Barack Obama"
    }
  }
}
