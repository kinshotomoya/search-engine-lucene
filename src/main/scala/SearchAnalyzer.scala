import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.ja.dict.UserDictionary
import org.apache.lucene.analysis.ja.{JapaneseReadingFormFilterFactory, JapaneseTokenizerFactory}
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import java.io.InputStreamReader
import java.util

object SearchAnalyzer extends App {

  // TODO: 2
  // charfilterでひらがな・カタカナ -> romajiの自前マッピングを定義する
  // なぜなら、「きゃぷてん」と入力された文字列は、「ki」と「ぷてん」に形態素分解 + 読み追加されるのでおかしくなる
  // これを避けるために、char毎にひらがな・カタカナ -> ローマ字に変換できる charFilterを設定しておく
  // 参考：https://www.elastic.co/jp/blog/implementing-japanese-autocomplete-suggestions-in-elasticsearch


  // TODO: 3
  //　searchAnalyzerで他のFilterは必要か確かめる


  val customAnalyzerBuilder = CustomAnalyzer.builder()
  // 参考：https://kazuhira-r.hatenablog.com/entry/20130616/1371390716
  val userDictionary = UserDictionary.open(new InputStreamReader(this.getClass.getResourceAsStream("user-dictionary.txt")))
  // punctuation = true => 句読点を除

  // tokenizer設定
  val tokenizerArgs: util.HashMap[String, String] = new util.HashMap[String, String]()
  tokenizerArgs.put("userDictionary", "user-dictionary.txt")
  tokenizerArgs.put("mode", "NORMAL")
  customAnalyzerBuilder.withTokenizer(classOf[JapaneseTokenizerFactory], tokenizerArgs)


  // tokenFilterの設定
  // 形態素解析後の単語に「ひらがな -> カタカナ」変換する
  // indexデータ作成時にはいらん
  // search analyzerで使う（きゃぷてん -> キャプテン　問題のため）
  //  val hiraganaMap = new util.HashMap[String, String]()
  //  hiraganaMap.put("id", "Hiragana-Katakana")
  //  customAnalyzerBuilder.addTokenFilter(classOf[ICUTransformFilterFactory], hiraganaMap)

  // JapaneseReadingFormFilterの設定
  // romaji読みを取得
  val readingFormMap = new util.HashMap[String, String]()
  readingFormMap.put("useRomaji", "true")
  customAnalyzerBuilder.addTokenFilter(classOf[JapaneseReadingFormFilterFactory], readingFormMap)

  val customAnalyzer: CustomAnalyzer = customAnalyzerBuilder.build()
  val tokenStream: TokenStream = customAnalyzer.tokenStream("", "看護")
  tokenStream.reset()
  while (tokenStream.incrementToken()) {
    val attr = tokenStream.getAttribute(classOf[CharTermAttribute])
    println(attr.toString)
  }

}
