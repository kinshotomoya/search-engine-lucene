import org.apache.lucene.analysis.ja.JapaneseCompletionFilter
import org.apache.lucene.document.{Document, Field, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory

import java.nio.file.Paths
import scala.io.BufferedSource

object JapaneseCompletionFile extends App {
  // 便利すぎる。。。
  val indexAnalyzer = new org.apache.lucene.analysis.ja.JapaneseCompletionAnalyzer(null, JapaneseCompletionFilter.Mode.INDEX)
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


  //  val tokenStream = a.tokenStream("", "普通免許")
  //  tokenStream.reset()
  //  while (tokenStream.incrementToken()) {
  //    val attr = tokenStream.getAttribute(classOf[CharTermAttribute])
  //    println(attr.toString)
  //  }


  // NOTE:
  // indexにはMMapDirectoryを使う
  // memory-mapped-ioなので、マッピングされるファイルサイズと等しい、プロセス内の仮想アドレス空間を利用する
  //　仮想アドレス空間にioデバイスにアクセスするための領域を割り当てることで、CPUにとっては通常のメモリのように見せかけることができ、高速アクセス可能に
  // これ使うと、heapにindexを展開するByteBuffersDirectoryとかを使わなくて済む
  val keywordIndexFilePath = Paths.get("./keyword/")
  val locationIndexFilePath = Paths.get("./location/")

  // keywordとlocationでそれぞれMMapDirectoryを作る
  // これをプロセス内でもち回す
  val keywordIndex: MMapDirectory = new MMapDirectory(keywordIndexFilePath)
  val locationIndex: MMapDirectory = new MMapDirectory(locationIndexFilePath)


  //  val analyzer = IndexAnalyzer.customAnalyzer
  //  println(analyzer)

  // データ作成時点でtokenizeなど済ましているので、特にanalyzerは設定しない
  val writerConfig: IndexWriterConfig = new IndexWriterConfig(indexAnalyzer)
  val writer: IndexWriter = new IndexWriter(keywordIndex, writerConfig)


  // TODO: ↓で作ったdirectoryにdocumentを格納する
  //  bulkで入れることはできる？
  val file: BufferedSource = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("suggestion-keywords.csv"))
  file.getLines().foreach((line: String) => {
//    val list = line.split(",")
//    val text = list(0)
//    val reading = list(1)
    addDoc(writer, line)
  })

  file.close()
  writer.close()

  // TODO: query作成。custom analyzer指定
  //  analyzerSuggest使用（suggest機能として使えるのか試してみる）
  // ユーザーからのクエリ処理流れ
  // 1. 入力クエリをtokenizeする
  // 2. tokenFilterで
  //  val analyzer = CustomAnalyzer.builder().build()

  //  val parser = new QueryParser("reading", )


  private def addDoc(writer: IndexWriter, text: String): Unit = {
    val document = new Document()
    document.add(new TextField("text", text, Field.Store.YES))
    // StringFieldはindex時にtokenizeされない
//    document.add(new StringField("reading", reading, Field.Store.YES))

    // NOTE:
    // multi fieldがデフォルトでonになっているので、複数の値を同じフィールドに格納したい場合は
    // 同じフィールド名でそれぞれの値を登録しておけば良いらしい
    // 参考：https://kazuhira-r.hatenablog.com/entry/20140412/1397319047

    writer.addDocument(document)
  }


}
