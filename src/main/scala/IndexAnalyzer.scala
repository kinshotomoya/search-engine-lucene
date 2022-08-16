import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.core.{LowerCaseFilterFactory, StopFilterFactory}
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.icu.ICUNormalizer2CharFilterFactory
import org.apache.lucene.analysis.ja.dict.UserDictionary
import org.apache.lucene.analysis.ja.{JapanesePartOfSpeechStopFilterFactory, JapaneseReadingFormFilterFactory, JapaneseTokenizerFactory}
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory
import org.apache.lucene.analysis.shingle.ShingleFilterFactory
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import java.io.InputStreamReader
import java.util


// 手順
// 1. カタカナ変換する
// 2. ↑をさらにローマ字変換する（長音符変換(tokyo -> toukyou, スキー -> sukiｰ)、shu・syu対応）
// なので2回にわたってanalyzerで処理する必要がある（何故なら一気にローマ字読みまで処置すると、カタカナに対してcharFilterを適応するなどできないから（2.に記載しているshu, syu対応など））
// 参考：https://qiita.com/navitime_tech/items/613c25967284c9452b73#%E3%83%AD%E3%83%BC%E3%83%9E%E5%AD%97%E5%85%A5%E5%8A%9B%E3%81%AB%E3%81%8A%E3%81%91%E3%82%8B%E8%AA%B2%E9%A1%8C
object IndexAnalyzer extends App {

  val customAnalyzerBuilder = CustomAnalyzer.builder()
  // 参考：https://kazuhira-r.hatenablog.com/entry/20130616/1371390716
  val userDictionary = UserDictionary.open(new InputStreamReader(this.getClass.getResourceAsStream("user-dictionary.txt")))
  // punctuation = true => 句読点を除く

  // tokenizer設定
  val tokenizerArgs: util.HashMap[String, String] = new util.HashMap[String, String]()
  tokenizerArgs.put("userDictionary", "user-dictionary.txt")
  tokenizerArgs.put("mode", "NORMAL")
  customAnalyzerBuilder.withTokenizer(classOf[JapaneseTokenizerFactory], tokenizerArgs)

  // charFilterの設定
  // 半角仮名を全角仮名変換などノーマライズしている
  customAnalyzerBuilder.addCharFilter(ICUNormalizer2CharFilterFactory.NAME)

  // partOFSpeechStopFilterの設定
  // stoptags.txtに指定している
  val partOfSpeechStopMap = new util.HashMap[String, String]()
  partOfSpeechStopMap.put("tags", "stoptags.txt")
  customAnalyzerBuilder.addTokenFilter(classOf[JapanesePartOfSpeechStopFilterFactory], partOfSpeechStopMap)

  // stopFilterの設定
  // 削除するワードを指定
  val stopFilterMap = new util.HashMap[String, String]()
  stopFilterMap.put("words", "stopwords.txt")
  customAnalyzerBuilder.addTokenFilter(classOf[StopFilterFactory], stopFilterMap)

  // JapaneseReadingFormFilterの設定
  // romaji読みを取得
  val readingFormMap = new util.HashMap[String, String]()
  readingFormMap.put("useRomaji", "true")
  customAnalyzerBuilder.addTokenFilter(classOf[JapaneseReadingFormFilterFactory], readingFormMap)

  // Q: 0
  // ローマ字読みの同音を追加する
  // ex) 「しょ」は「sho」と「syo」が存在するので、それぞれの読みを追加する必要がある
  // suggest-apiではどうやっていたのか調べる
  // A: 0
  // => application側で定義している
  // ↓こんな感じで
  // map.put("シュ", Array[String]("syu", "shu"))

  // Q: 0.1
  // 「トレーラー」=> 「toreｰraｰ」のローマ字を取得したいが、どうするのか？
  // デフォルトのreading_formだと、「torera」になってしまう
  // もしかして、stoptagsで「ー」を消しているかも？
  // A: 0.1
  // => まあ別に、indexとsearchで「ー」の処理を合わせておけば問題はない

  // ShingleFilterの設定
  // 形態素解析で分解されたワードを結合する
  // Q: 1
  // 以下のように結合前の単語も表示されるので、結合後単語のみ表示されるようにする
  //  tokyotawa
  //  tawa
  //  kango
  //  kangoshi
  //  shi
  //  shigoto
  // A: 1
  // => RequestLogAnalyzerで不要な単語（助詞や動詞）は削除されているので、ここは名詞だけなら正常に動くので問題ない

  val singleFilterMap = new util.HashMap[String, String]()
  singleFilterMap.put("minShingleSize", "2")
  singleFilterMap.put("maxShingleSize", "10")
  singleFilterMap.put("outputUnigrams", "false")
  singleFilterMap.put("tokenSeparator", "")
  singleFilterMap.put("outputUnigramsIfNoShingles", "true")
  singleFilterMap.put("fillerToken", "")
  customAnalyzerBuilder.addTokenFilter(classOf[ShingleFilterFactory], singleFilterMap)

  // LowerCaseFilterの設定
  // 大文字を小文字に変換する
  customAnalyzerBuilder.addTokenFilter(LowerCaseFilterFactory.NAME)

  // ASCIIFoldingFilterの設定
  // readingFormFilterでは、一部の文字で長音符付きで読みが追加されることがある
  // ex) とうきょう -> tōkyō
  // これを長音符なしで読みをつけるための設定
  // ex) とうきょう -> tokyo
  customAnalyzerBuilder.addTokenFilter(ASCIIFoldingFilterFactory.NAME)


  // edge ngram filterを使ってtoken分割している
  // prefix searchはパフォーマンスが悪いので、edge ngramで分割して検索する
  val edgeEgramArgs: util.HashMap[String, String] = new util.HashMap[String, String]()
  edgeEgramArgs.put("minGramSize", "1")
  edgeEgramArgs.put("maxGramSize", "100")
  customAnalyzerBuilder.addTokenFilter(classOf[EdgeNGramFilterFactory], edgeEgramArgs)


  val customAnalyzer: CustomAnalyzer = customAnalyzerBuilder.build()

  // requestLogからのinput
  val inputKeyword = "エンジニア"
  val tokenStream: TokenStream = customAnalyzer.tokenStream("", inputKeyword)
  tokenStream.reset()
  while (tokenStream.incrementToken()) {
    val attr = tokenStream.getAttribute(classOf[CharTermAttribute])
    println(attr.toString)
  }

  // NOTE: カタカナは不要
  // expected index document
  //  {
  //    "text": "看護師 仕事",
  //    "reading_0": ["看護師", "kanngoshi", "kangosi"],
  //    "reading_1": ["仕事", "shigoto", "sigoto"],
  //  }

  // 参考
  //  https://techblog.zozo.com/entry/solr-suggester
  // https://qiita.com/navitime_tech/items/613c25967284c9452b73
}
