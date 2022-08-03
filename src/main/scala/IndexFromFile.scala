import org.apache.lucene.document.{Document, Field, StringField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.MMapDirectory

import java.nio.file.Paths
import scala.io.BufferedSource

object IndexFromFile extends App {


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


  // データ作成時点でtokenizeなど済ましているので、特にanalyzerは設定しない
  val writerConfig: IndexWriterConfig = new IndexWriterConfig()
  val writer: IndexWriter = new IndexWriter(keywordIndex, writerConfig)


  // TODO: ↓で作ったdirectoryにdocumentを格納する
  //  bulkで入れることはできる？
  val file: BufferedSource = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("suggestion-keywords.csv"))
  file.getLines().foreach((line: String) => {
    val list = line.split(",")
    val text = list(0)
    val reading = list(1)
    addDoc(writer, text, reading)
  })

  file.close()
  writer.close()

  // TODO: query作成。custom analyzer指定
  //  analyzerSuggest使用（suggest機能として使えるのか試してみる）

  private def addDoc(writer: IndexWriter, text: String, reading: String): Unit = {
    val document = new Document()
    // suggest機能を作成するときは、データ作成時点でtokenizeしているので
    // index時にはtokenizeする必要はない
    document.add(new StringField("text", text, Field.Store.YES))
    // StringFieldはindex時にtokenizeされない
    document.add(new StringField("reading", reading, Field.Store.YES))

    writer.addDocument(document)
  }


}
