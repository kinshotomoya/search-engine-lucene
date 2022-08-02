import org.apache.lucene.store.MMapDirectory

import java.nio.file.Paths
import scala.io.BufferedSource

object IndexFromFile extends App {

  // TODO: ↓で作ったdirectoryにdocumentを格納する
  //  bulkで入れることはできる？
  val file: BufferedSource = scala.io.Source.fromInputStream(getClass.getClassLoader.getResourceAsStream("suggestion-keywords.csv"))
  file.getLines().foreach(line => {
    println(line)
  })
  file.close()


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

}
