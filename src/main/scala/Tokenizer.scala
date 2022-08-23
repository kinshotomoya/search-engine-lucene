import org.apache.lucene.analysis.ja.JapaneseTokenizer
import org.apache.lucene.analysis.ja.JapaneseTokenizer.Mode
import org.apache.lucene.analysis.ja.dict.UserDictionary
import org.apache.lucene.analysis.ja.tokenattributes.{BaseFormAttribute, InflectionAttribute, PartOfSpeechAttribute, ReadingAttribute}
import org.apache.lucene.analysis.tokenattributes.{BytesTermAttribute, CharTermAttribute}

import java.io.{InputStreamReader, StringReader}

object Tokenizer extends App {

  // ↓user辞書などkuromoji内で利用するものを設定できる
  // 参考：https://kazuhira-r.hatenablog.com/entry/20130616/1371390716
  val userDictionary = UserDictionary.open(new InputStreamReader(this.getClass.getResourceAsStream("user-dictionary.txt")))
  // punctuation = true => 句読点を除く
  val jaTokenizer: JapaneseTokenizer = new JapaneseTokenizer(userDictionary, true, Mode.NORMAL)

  // 解析文字列をtokenizerにset
  val text = "看護師のせi"
  val reader = new StringReader(text)
  jaTokenizer.setReader(reader)
  jaTokenizer.reset()

  // 取得したい属性を定義
  // ↓ 指定した属性を取得できる（品詞情報、読み、活用系などなど）
  // 参考：https://www.mlab.im.dendai.ac.jp/~yamada/ir/MorphologicalAnalyzer/Lucene_Kuromoji.html
  val readingAttribute = jaTokenizer.addAttribute(classOf[ReadingAttribute]) // 読み
  val partOfSpeech = jaTokenizer.addAttribute(classOf[PartOfSpeechAttribute]) // 品詞
  val charTerm = jaTokenizer.addAttribute(classOf[CharTermAttribute]) // もともと
  val baseForm = jaTokenizer.addAttribute(classOf[BaseFormAttribute]) // 元の形　ex) 探し -> 探す
  val byteTerm = jaTokenizer.addAttribute(classOf[BytesTermAttribute])
  val inflection = jaTokenizer.addAttribute(classOf[InflectionAttribute]) // 活用形

  while(jaTokenizer.incrementToken()) {
    println(s"$charTerm  ${partOfSpeech.getPartOfSpeech},${readingAttribute.getReading},${readingAttribute.getPronunciation},${inflection.getInflectionForm}, ${inflection.getInflectionType}, ${baseForm.getBaseForm}")
  }

}
