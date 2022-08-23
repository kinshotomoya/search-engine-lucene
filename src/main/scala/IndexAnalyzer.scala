import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizerFactory
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import java.util

object IndexAnalyzer extends App {
  val customAnalyzerBuilder = CustomAnalyzer.builder()

  // tokenizer設定
  val tokenizerArgs: util.HashMap[String, String] = new util.HashMap[String, String]()
  tokenizerArgs.put("minGramSize", "1")
  tokenizerArgs.put("maxGramSize", "100")
  customAnalyzerBuilder.withTokenizer(classOf[EdgeNGramTokenizerFactory], tokenizerArgs)

  val customAnalyzer: CustomAnalyzer = customAnalyzerBuilder.build()

  // requestLogからのinput
  val inputKeyword = "エンジニア　正社員　新着"
  val tokenStream: TokenStream = customAnalyzer.tokenStream("", inputKeyword)
  tokenStream.reset()
  while (tokenStream.incrementToken()) {
    val attr = tokenStream.getAttribute(classOf[CharTermAttribute])
    println(attr.toString)
  }
}
