import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.icu.ICUNormalizer2CharFilterFactory
import org.apache.lucene.analysis.ja.{JapaneseCompletionFilterFactory, JapaneseTokenizerFactory}
import org.apache.lucene.analysis.ja.dict.UserDictionary
import org.apache.lucene.analysis.ja.tokenattributes.ReadingAttribute
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import java.io.InputStreamReader
import java.util

object SimpleJapaneseCompletion extends App {
  val customAnalyzerBuilder = CustomAnalyzer.builder()
  val userDictionary = UserDictionary.open(new InputStreamReader(this.getClass.getResourceAsStream("user-dictionary.txt")))

  // tokenizer設定
  val tokenizerArgs: util.HashMap[String, String] = new util.HashMap[String, String]()
  customAnalyzerBuilder.withTokenizer(classOf[JapaneseTokenizerFactory], tokenizerArgs)

  // charFilterの設定
  // 半角仮名を全角仮名変換などノーマライズしている
//  customAnalyzerBuilder.addCharFilter(ICUNormalizer2CharFilterFactory.NAME)

  val japaneseCompletionFilterArgs: util.HashMap[String, String] = new util.HashMap[String, String]()
  customAnalyzerBuilder.addTokenFilter(classOf[JapaneseCompletionFilterFactory], japaneseCompletionFilterArgs)

  val customAnalyzer: CustomAnalyzer = customAnalyzerBuilder.build()

  // requestLogからのinput
  // :を入力すると前の文字を含んで読みを返してしまうのは、JapaneseCompletionFilterのバグではなさそう
  val inputKeyword = ";a"
  val tokenStream: TokenStream = customAnalyzer.tokenStream("", inputKeyword)
  tokenStream.reset()
  while (tokenStream.incrementToken()) {
    val readingAttribute = tokenStream.getAttribute(classOf[CharTermAttribute])
    println(readingAttribute.toString)
  }
}
