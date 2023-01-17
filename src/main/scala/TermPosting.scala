import Main.{args, runMain}
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, TextField}
import org.apache.lucene.index.{IndexReader, IndexWriter, IndexWriterConfig, NoMergePolicy, SegmentReader, TieredMergePolicy}
import org.apache.lucene.store.FSDirectory

import java.nio.file.Paths

object TermPosting extends App {
  def runMain(args: List[String]): Unit = {
    val directory = FSDirectory.open(Paths.get("./data/index"))
    val analyzer = new StandardAnalyzer()
    val indexConfig = new IndexWriterConfig(analyzer)
    val conf1 = indexConfig.setMaxFullFlushMergeWaitMillis(10)
    val conf2 = conf1.setRAMBufferSizeMB(1)
    val mergePolicy = new TieredMergePolicy()
//    val mergePolicy = NoMergePolicy.INSTANCE
//    mergePolicy.setMaxCFSSegmentSizeMB(100)
    val p2 = mergePolicy.setMaxMergedSegmentMB(5)
    val conf3 = conf2.setMergePolicy(p2)

    val writer = new IndexWriter(directory, conf3)
    for(index <- 1 to 1000000) {
      val document = new Document()
      document.add(new TextField("title", s"load${index} of the ring-${index}", Field.Store.YES))
      document.add(new TextField("content", s"ロードオブザリングです。面白いですね。", Field.Store.YES))
      writer.addDocument(document)
    }
    writer.flush()
    writer.commit()

  }

  runMain(args.toList)

}
