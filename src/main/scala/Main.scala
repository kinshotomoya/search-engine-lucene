import org.apache.lucene.analysis.ja.JapaneseAnalyzer
import org.apache.lucene.document.{Document, Field, StringField, TextField}
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, Query, TopDocs}
import org.apache.lucene.store.ByteBuffersDirectory

// in memoryなindexを作成する
object Main extends App {
  def runMain(args: List[String]): Unit = {
    // analyzer
    // StandardAnalyzerは以下3つを含んでいる
    // StandardTokenizer、LowerCaseFilter、StopFilter
    // 日本語対応のkuromojiなどを使いたかったら、ここのanalyzerの設定でkuromoji指定すればいいのかな
    // そもそもkuromojiをdependencyに追加する必要もある
    // TODO:
    //  kuromoji入れる
    //  カスタムFilter入れる（不要ワードを取り除くなど）
    //  カスタム辞書周りはどう追加する？（synonym,ngword,stopword等）

    val analyzer = new JapaneseAnalyzer()

//    val analyzer = new StandardAnalyzer

    // heapにindexを格納するためのバッファ
    val index = new ByteBuffersDirectory

    // 設定を保持
    val config = new IndexWriterConfig()

    val writer = new IndexWriter(index, config)

    // ドキュメントをindexに追加していく
    addDoc(writer, "Lucene in Action", "193398817")
    addDoc(writer, "Lucene for Dummies", "55320055Z")
    addDoc(writer, "Managing Gigabytes", "55063554A")
    addDoc(writer, "The Art of Computer Science", "9900333X")

    // TODO: どのようにindexが構成されているかみる（tokenizerで文字がどのように分割されているのか・データ構造）
    // => lukeを使えばGUI上でindex内容が見れるっぽい
    // lucene8.3からluceneの一部になっている
    //
    writer.close()


    // TODO: documentが格納されているindexを読み込むにはどうする？
    //  登録するデータを格納したファイル（hoge.txt）からindexを作成するのはどうする？
    //  実際にsuggest-apiでは、検索ログからデータを作成してs3にデータを置いておく
    //  そのs3にあるデータをindexに登録する
//    DirectoryReader.open(Directory)

    // 検索クエリを作成する
    // 検索クエリに対してanalyzeするのに、analyzerの指定が必要
    val queryParser = new QueryParser("title", analyzer)
    val queryKeyword = args.mkString(" ")
    val query: Query = queryParser.parse(queryKeyword)
    println(query)

    // 実際に検索する
    // TODO: prefix searchはどうやって実現する？
    // 参考：autocompleteの例。luceneのモジュールにAnalyzingSuggesterというsuggestを良しなにやってくれるモジュールがある！？
    // https://medium.com/@ekaterinamihailova/in-memory-search-and-autocomplete-with-lucene-8-5-f2df1bc71c36
    val reader: DirectoryReader = DirectoryReader.open(index)
    // indexにが変更しない場合はこのsearcherを使い回す（パフォーマンスの観点から）
    val searcher = new IndexSearcher(reader)
    val res: TopDocs = searcher.search(query, 10)
    val hits = res.scoreDocs

    //　検索結果を表示する
    hits.foreach(scoreDoc => {
      val docId = scoreDoc.doc
      val doc = searcher.doc(docId)
      println(s"isbn: ${doc.get("isbn")}. title: ${doc.get("title")}")
    })
  }

  // 実行例：sbt "runMain Main lucene"
  runMain(args.toList)

  private def addDoc(writer: IndexWriter, text: String, isbn: String): Unit = {
    val document = new Document()
    // TextFieldはindex時にtokenizeされる
    // Stored = trueにすることで、検索結果としてこのフィールドが含まれる

    // suggest機能を作成するときは、データ作成時点でtokenizeしているので
    // index時にはtokenizeする必要はない
    document.add(new TextField("title", text, Field.Store.YES))
    // StringFieldはindex時にtokenizeされない
    document.add(new StringField("isbn", isbn, Field.Store.YES))

    writer.addDocument(document)

  }

}
