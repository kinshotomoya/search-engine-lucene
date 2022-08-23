import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.custom.CustomAnalyzer
import org.apache.lucene.analysis.icu.ICUNormalizer2CharFilterFactory
import org.apache.lucene.analysis.ja.dict.UserDictionary
import org.apache.lucene.analysis.ja.{JapaneseCompletionAnalyzer, JapaneseTokenizerFactory}
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import java.io.InputStreamReader
import java.util
import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

// indexするファイル作成スクリプトはこれで良さそう
object JapaneseCompletion extends App {
  // 便利すぎる。。。
//  val a = new org.apache.lucene.analysis.ja.JapaneseCompletionAnalyzer(null, JapaneseCompletionFilter.Mode.INDEX)
  // INDEX
  // input = 反sy
  // ↓のようにtokenizerで分解されてそれぞれで出力（romanize）する
  //  反
  //  han
  //  hann
  //  sy

  // QUERY
  // input = 反sy
  // ↓のようにtokenizerされた文字列を結合して出力（romanize）する
  // searchの時にこのモードで使う
  //  反sy
  //  hansy
  //  hannsy

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
//  val partOfSpeechStopMap = new util.HashMap[String, String]()
//  partOfSpeechStopMap.put("tags", "stoptags.txt")
//  customAnalyzerBuilder.addTokenFilter(classOf[JapanesePartOfSpeechStopFilterFactory], partOfSpeechStopMap)

  // stop tagを設定しているので↓いらん
  // stopFilterの設定
  // 削除するワードを指定
//  val stopFilterMap = new util.HashMap[String, String]()
//  stopFilterMap.put("words", "stopwords.txt")
//  customAnalyzerBuilder.addTokenFilter(classOf[StopFilterFactory], stopFilterMap)

  val customAnalyzer: CustomAnalyzer = customAnalyzerBuilder.build()

  // requestLogからのinput
  val tokenList = scala.collection.mutable.ListBuffer.empty[String]
  val inputKeyword = "日本の正社員"
  val tokenStream: TokenStream = customAnalyzer.tokenStream("", inputKeyword)
  tokenStream.reset()
  while (tokenStream.incrementToken()) {
    val attr = tokenStream.getAttribute(classOf[CharTermAttribute])
    tokenList.addOne(attr.toString)
  }

  // ↓tokenizerによって、単語分解されたものにromaji読みをつけて結合している
  // ここもカスタムのJapaneseCompletionAnalyzerを作成する必要がある
  // 何故なら、「日本のせi」で「i」が消されてしまうデフォルトだと
  val listList = scala.collection.mutable.ListBuffer.empty[List[String]]
  val japaneseCompletionAnalyzer = new JapaneseCompletionAnalyzer()
  tokenList.foreach(t => {
    val list = ListBuffer.empty[String]
    val token = japaneseCompletionAnalyzer.tokenStream("", t)
    token.reset()
    var index = 0
    while (token.incrementToken()) {
      val attr: CharTermAttribute = token.getAttribute(classOf[CharTermAttribute])
      if(index != 0) list.addOne(attr.toString)
      index += 1
    }
    listList.addOne(list.toList)
    token.close()
  })
  println(listList)

  val result = combines(listList.toList)
  println(result)

  def combines(baseList: List[List[String]]): Seq[String] = {
    @tailrec
    def loop(first: Seq[String], base: List[List[String]]): Seq[String] = {
      val acc: Seq[String] = for {
        f <- first
        s <- base.head
      } yield f + s

      base match {
        case Nil => acc
        case _ :: Nil => acc
        case _ :: tail => loop(acc, tail)
      }
    }

    baseList match {
      case Nil => Seq.empty
      case head :: Nil => head
      case head :: tail => loop(head, tail)
    }
  }


//  val tokenStream = a.tokenStream("", "普通免許")
//  tokenStream.reset()
//  while (tokenStream.incrementToken()) {
//    val attr = tokenStream.getAttribute(classOf[CharTermAttribute])
//    println(attr.toString)
//  }


}
