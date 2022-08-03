luceneで試したいことを実践する

## luke
indexの概要やdocumentがどのようにanalyzeされているのかなどGUIで確認できるツール

### 使い方
以下コマンドを実行するだけ
```shell
$ ./search-engine-lucene/viewer/luke/luke.sh
```

※ viewer/配下に、lucene-8.11.3のファイルをそのまま持ってきている
lukeはlucene8.3からluceneプロジェクトに吸収されたらしい


初期設定画面でDirectory指定で`./search-engine-lucene/keyword`を選択。
（MMapDirectoryを使っている場合）

https://github.com/DmitryKey/luke/


### 参考
https://www.lucenetutorial.com/lucene-in-5-minutes.html
