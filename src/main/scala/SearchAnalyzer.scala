import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.core.LowerCaseFilterFactory
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.ja.dict.UserDictionary
import org.apache.lucene.analysis.ja.{JapaneseReadingFormFilterFactory, JapaneseTokenizerFactory}
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import java.io.InputStreamReader
import java.util

// goal: ローマ字読みに変換する
// 手順
// 1. 入力文字列をカタカナ変換する（後に漢字 -> ローマ字変換できるようにカタカナにしておく）
// 2. 入力文字列をローマ字読みに変換する
//   2.1 仮名 -> ローマ字変換のchar filterを設定（キュウ -> kyuu変換、スキー -> sukiｰ、トウキョウ -> toukyou）
//   2.2 kuromoji tokenizer(romaji true)でトークン分割・ローマ字変換
//   2.3 各種フィルターを設定（lowercaseなど）
// 参考：https://qiita.com/navitime_tech/items/613c25967284c9452b73#%E3%83%AD%E3%83%BC%E3%83%9E%E5%AD%97%E5%85%A5%E5%8A%9B%E3%81%AB%E3%81%8A%E3%81%91%E3%82%8B%E8%AA%B2%E9%A1%8C


// 検索は以下のように行う
// 元々の入力文字列とromaji変換した文字列でprefix検索（& or検索）
// input keyword = 看護
//  reading_0: 看護
//  or
//  reading_0: kango
// NOTE:
// reading_0とreading_1...xはand検索を行う
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

  customAnalyzerBuilder.addTokenFilter(LowerCaseFilterFactory.NAME)
  customAnalyzerBuilder.addTokenFilter(ASCIIFoldingFilterFactory.NAME)

  // JapaneseReadingFormFilterの設定
  // romaji読みを取得
  val readingFormMap = new util.HashMap[String, String]()
//  readingFormMap.put("useRomaji", "true")
  customAnalyzerBuilder.addTokenFilter(classOf[JapaneseReadingFormFilterFactory], readingFormMap)

  val customAnalyzer: CustomAnalyzer = customAnalyzerBuilder.build()
  // 休jit => kyuujitu に変換したい
  val tokenStream: TokenStream = customAnalyzer.tokenStream("", "とうk")
  tokenStream.reset()
  while (tokenStream.incrementToken()) {
    val attr = tokenStream.getAttribute(classOf[CharTermAttribute])
    println(attr.toString)
  }

}
