# OpenRegex

OpenRegex is written by Michael Schmitz at the Turing Center
<http://turing.cs.washington.edu/>.  It is licensed under the full GPL3.
Please see the LICENSE file for more details.


## Introduction

OpenRegex is an efficient and flexible token-based regular expression language
and engine.  Most regular expression implementations are closed to run only
over characters.  Although this is the the most common application for regular
expressions, OpenRegex does not have this restriction.  OpenRegex is open to
any sequences of user-defined objects.


## Applied to Natural Language

For example, OpenRegex is used in the R2A2 extension to ReVerb, an open-domain
information extractor, to determine argument boundaries.  In this case, tokens
are words in English sentences with additional information (the string of the
word, the part-of-speech tag, and the chunk tag).

    case class WordToken(string: String, postag: String, chunk: String)

Now that we have defined our token, we can build up a sentence (a NLP library
such as OpenNLP can help out here).  We will also need to define a way to
translate each token in the expression (text between <angled brackets>) into
an expression that can be applied to a word token.

    def compile(expr: String): RegularExpression[WordToken] = {
      // Translate an string "part=value" into a BaseExpression that
      // checks whether the part of a WordToken has value 'value'. */
      val factory = new ExpressionFactory<ChunkedSentenceToken>() {
        new Expression.BaseExpression[ChunkedSentenceToken](expression) {
          val Array(part, value) = expr.split("=")
          override def apply(entity: WordToken) = {
            part match {
              case "string" => entity.string == value
              case "postag" => entity.postag == value
              case "chunk" => entity.chunk == value
            }
          }
        }
      }

      RegularExpression.compile(expr, factory)
    }

Now we can compile a regular expression and apply it to a sentence.  Consider
the following pattern.  The first line defines a non-matching group that
matches a determiner ("a", "an", or "the").  The second line matches a sequence
of part-of-speech tags ("JJ" is adjective, "NNP" is proper noun, and "NN" is
common noun).

    (?:<string="a"> | <string="an"> | <string="the">)?
    <postag="JJ">* <postag='NNP'>+ <postag='NN'>+ <postag='NNP'>+

We can try applying it to a couple of sentences.

1.  The US president Barack Obama is travelling to Mexico.

    regex.find(sentence).groups.get(0) == "The US president Barack Obama"


2.  If all the ice melted from the frigid Earth continent Antarctica, sea
    levels would rise hundreds of feet.

    regex.find(sentence).groups.get(0) == "the frigid Earth continent Antarctica"


We may want to pull out the text from certain parts of our match.  We can do
this with either named or unnamed groups.  Consider the following new form of
the pattern and the sentence in example 2.

      (?:<string="a"> | <string="an"> | <string="the">)? <postag="JJ">*
      (<arg1>:<postag='NNP'>+) (<rel>:<postag='NN'>+) (<arg2>:<postag='NNP'>+)

      regex.find(sentence).groups.get(0) == "the frigid Earth continent Antarctica"
      regex.find(sentence).groups.get(1) == "Earth"
      regex.find(sentence).groups.get(2) == "continent"
      regex.find(sentence).groups.get(2) == "Antarctica"

      regex.find(sentence).group("arg1") == "Earth"
      regex.find(sentence).group("rel")  == "continent"
      regex.find(sentence).group("arg2") == "Antarctica"

## Supported Constructs

The regular expression library supports the following constructs.

    | alternation
    ? option
    * Kleene-star
    + plus
    ^ beginning
    $ end
    ()        matching groups
    (?:)      non-matching groups
    (<name>:) named groups

Most of these operators work the same as in java.util.regex.  Presently,
however, alternation binds to its immediate neighbors.  This means that `<a>
<b> | <c>` means `<a> (?:<b> | <c>)` whereas in Java it would mean `(?:<a> <b>)
| <c>`.  This may change in a future release so it is advised that the
alternation arguments be made explicit with non-matching groups.


## Simple Java Example

The NLP example is rather complex but it shows the power of OpenRegex.  For a
simpler example, look at RegularExpressions.word.  This is a static factory
method for a simple word-based regular expression where only the string is
considered.  This factory is used in the test cases.

You can also play around with RegularExpressions.word by running the main
method in RegularExpression and specifying an expression with arg1.

    mvn compile exec:java
        -Dexec.mainClass=edu.washington.cs.knowitall.regex.RegularExpression
        -Dexec.args="<the> <fat>* <cows> <are> <mooing> (?:<loudly>)?"


## Logic Expressions

Included is an engine for parsing and evaluating logic expressions.  For
example, you might want to extend the NLP regular expression language to be
able to check multiple fields in a single regular expression token.  If you
assumed each regular expression token to be a logic expression, you could
write patterns such as the following.

    <string="the" & postag="DT"> <postag="JJ"> <string="earth" | postag="NNP">

Extending the regular expression in this way is easy.  It only involves
rewriting the apply method in BaseExpression inside the compile method.
Most of the code below existed before--now it's just moved outside the
apply method.

    val logic = LogicExpression.compile(value,
    new Function[String, Tok.Arg[WordToken]]() {
      override def apply(expr: String) = {
        new Arg.Pred[String](expr) {
          val Array(part, value) = expr.split("=")
          override def apply(entity: WordToken) = part match {
            case "string" => entity.string == value
            case "postag" => entity.postag == value
            case "chunk" => entity.chunk == value
          }
        }
      }
    }

    override def apply(entity: WordToken) = {
      logic.apply(entity)
    }

Play around with logic expression by using the main method in LogicExpression.

  mvn exec:java -Dexec.mainClass=edu.washington.cs.knowitall.logic.LogicExpression


## Implementation

Regular expressions are evaluated using Thomson NFA, which is fast and not have
the pathological cases that most regular exprsesion libraries have.  For more
information about Thomson NFA in comparison to recurseive backtracking, read
http://swtch.com/~rsc/regexp/regexp1.html.  Future work may involve compiling
NFAs to DFAs.


## Future Work

1.  Compile to DFA.
2.  Use parser combinators for parsing regular expressions.
