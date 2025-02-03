package dev.capslock.rss4s

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import java.net.URL

class PackageSpec extends AnyFunSpec with Matchers {
  describe("rss4s") {
    it("should parse a RSS 2.0 feed (1)") {
      val feed = parseFeed("""<?xml version="1.0"?>
      <rss version="2.0">
      <channel>
      <title>test feed</title>
      <link>https://a.feed.example.com</link>
      <description>This is test feed for RSS 2.0</description>
      <item>
        <title>test item</title>
        <link>https://example.com/item</link>
        <description>This is test item for RSS 2.0</description>
      </item>
      </channel>
      </rss>
      """)
      val expectedItems = List(
        Item(
          "test item",
          new URL("https://example.com/item"),
          Some("This is test item for RSS 2.0"),
        ),
      )
      feed shouldBe Right(
        Feed(
          "test feed",
          new URL("https://a.feed.example.com"),
          Some("This is test feed for RSS 2.0"),
          expectedItems,
        ),
      )
    }

    it("should parse a RSS 2.0 feed (2)") {
      val feed = parseFeed("""<?xml version="1.0"?>
          <rss version="2.0">
          <channel>
          <title>another test feed</title>
          <link>https://another.feed.example.com</link>
          <description>This is another test feed for RSS 2.0</description>
          <item>
            <title>another test item</title>
            <link>https://example.com/item</link>
            <description>This is test item for RSS 2.0</description>
          </item>
          </channel>
          </rss>
          """)
      val expectedItems = List(
        Item(
          "another test item",
          new URL("https://example.com/item"),
          Some("This is test item for RSS 2.0"),
        ),
      )
      feed shouldBe Right(
        Feed(
          "another test feed",
          new URL("https://another.feed.example.com"),
          Some("This is another test feed for RSS 2.0"),
          expectedItems,
        ),
      )
    }

    it("should parse a RSS 2.0 feed without description") {
      val feed = parseFeed("""<?xml version="1.0"?>
          <rss version="2.0">
          <channel>
          <title>another test feed</title>
          <link>https://another.feed.example.com</link>
          <item>
            <title>another test item</title>
            <link>https://example.com/item</link>
            <description>This is test item for RSS 2.0</description>
          </item>
          </channel>
          </rss>
          """)
      val expectedItems = List(
        Item(
          "another test item",
          new URL("https://example.com/item"),
          Some("This is test item for RSS 2.0"),
        ),
      )
      feed shouldBe Right(
        Feed(
          "another test feed",
          new URL("https://another.feed.example.com"),
          None,
          expectedItems,
        ),
      )
    }

    it("should parse real RSS 2.0") {
      val feed = parseFeed("""<?xml version="1.0"?>
<rss version="2.0">
  <channel>
    <title>Lambdaカクテル</title>
    <link>https://blog.3qe.us/</link>
    <description>京都在住Webエンジニアの日記です</description>
    <lastBuildDate>Mon, 03 Feb 2025 23:20:29 +0900</lastBuildDate>
    <docs>http://blogs.law.harvard.edu/tech/rss</docs>
    <generator>Hatena::Blog</generator>
    
      
      
        
      
        <item>
          <title>なぜDBから引くときに1000件ずつchunkingするのか、説明できますか</title>
          <link>https://blog.3qe.us/entry/2024/09/02/223439</link>          <description>&lt;p&gt;MySQLやPostgreSQLといったRDBMSからデータを引いてくるとき、扱うデータの規模によっては、1000件ずつ&lt;code&gt;LIMIT&lt;/code&gt;をかけて順に引いていくということがある。&lt;/p&gt;

&lt;p&gt;以前slow queryが出たらよくやっていたのを思い出して、ふとこのあたりってどういう根拠があってやっているのだっけ、自分が知っている他に効能があったりするのかな、と思ってSlackに書き込んだところ、同僚の &lt;a href=&quot;http://blog.hatena.ne.jp/onk/&quot; class=&quot;hatena-id-icon&quot;&gt;&lt;img src=&quot;https://cdn.profile-image.st-hatena.com/users/onk/profile.png&quot; width=&quot;16&quot; height=&quot;16&quot; alt=&quot;&quot; class=&quot;hatena-id-icon&quot;&gt;id:onk&lt;/a&gt; に教えていただいた。その内容に加えて軽く調べた内容をまとめてみる。&lt;/p&gt;

&lt;p&gt;Web系の話です。みなさまの知見がありましたら教えてください。&lt;/p&gt;

&lt;h2 id=&quot;TLDR&quot;&gt;TL;DR&lt;/h2&gt;

&lt;ul&gt;
&lt;li&gt;刺さる&lt;a href=&quot;#f-ef2af921&quot; id=&quot;fn-ef2af921&quot; name=&quot;fn-ef2af921&quot; title=&quot;クエリなどが重すぎて応答が帰ってこなくなることをいうジャーゴン&quot;&gt;*1&lt;/a&gt;から&lt;/li&gt;
&lt;li&gt;刺さったら困るから

&lt;ul&gt;
&lt;li&gt;あたりまえ&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;/ul&gt;


&lt;h2 id=&quot;詳細&quot;&gt;詳細&lt;/h2&gt;

&lt;p&gt;もともとSlackに書いた原文は以下の通り(MySQL前提で書いているけどPostgresといった他のRDBMSにも適用できる話。):&lt;/p&gt;

&lt;blockquote&gt;&lt;p&gt;DB引くとき、Perl時代(？)によく1000件単位でchunkingしてたのって、I/Oでプロセスが刺さるのを回避したいからだったっけ&lt;/p&gt;&lt;/blockquote&gt;

&lt;p&gt;このときは、Perl固有の事情もあったっけ、というつもりで書いていた。これに対して、教えていただいた内容は以下の通り:&lt;/p&gt;

&lt;ul&gt;
&lt;li&gt;デッドロックの発生率が小さくなる

&lt;ul&gt;
&lt;li&gt;一発一発が小さくなり、すぐにロックが解放されるから&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;Indexが効きやすくなる

&lt;ul&gt;
&lt;li&gt;デカすぎるクエリを発射するとtable scanになることがあるらしい&lt;/li&gt;
&lt;li&gt;&lt;a href=&quot;https://dev.mysql.com/doc/refman/9.0/en/table-scan-avoidance.html&quot;&gt;MySQL :: MySQL 9.0 Reference Manual :: 10.2.1.23 Avoiding Full Table Scans&lt;/a&gt;&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;IO刺さりづらくもなると思う

&lt;ul&gt;
&lt;li&gt;(一発が小さくなるためI/O待ちの時間も小さくなる)&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;/ul&gt;


&lt;p&gt;というわけで、このやり方はおおむね現代でも通用しそう。以下は自分の感想。&lt;/p&gt;

&lt;ul&gt;
&lt;li&gt;デッドロックは発生するものなのでクエリを分割して確率を下げられるならそれでいい

&lt;ul&gt;
&lt;li&gt;名前がおどろおどろしいけどまともにトランザクション張ってるならクエリ殺すだけで大抵なんとかなる&lt;/li&gt;
&lt;li&gt;Web系ならこの感覚でいい&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;Indexについては、RDBMSの進化によってオプティマイザなどが賢くなっていくので、そのうち気にしなくてよくなるかもしれない？

&lt;ul&gt;
&lt;li&gt;構造的にどうしようもない場合もありそう&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;IOでスレッドが刺さるのは場合によりそう

&lt;ul&gt;
&lt;li&gt;Perlのようなシングルスレッド言語でStarmanといったpreforkするタイプのサーバを使っていたりすると問題となりうる

&lt;ul&gt;
&lt;li&gt;刺さるとそのプロセスを占領するから&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;現代のマルチスレッド/イベントループで駆動するシングルスレッド言語ではそれほど強い問題にはならないのではないか

&lt;ul&gt;
&lt;li&gt;それはそうと、クエリが刺さりまくるとマルチスレッド言語でも詰まる&lt;/li&gt;
&lt;li&gt;それはそうと、100000件引いた結果を普通に処理しているとシングルスレッド言語ではそこでCPU boundになって刺さる&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;そもそもデータがデカすぎてメモリに載らなかったりもする

&lt;ul&gt;
&lt;li&gt;バッチとかだったら当然考慮するべきだが、件数による&lt;/li&gt;
&lt;li&gt;オンラインの処理で1000件超えちゃうこともままある&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;それはそうと、引く件数が同じなら当然だいたい同じ時間がかかるので、時間を減らすための弾丸ではない

&lt;ul&gt;
&lt;li&gt;たまに走らすバッチジョブとかでとんでもない数のレコードが引かれてサービスを巻き添えにして詰まる、ということを回避するという保険の意味合いもある&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;/ul&gt;


&lt;h2 id=&quot;IN句のサイズを制約することもある&quot;&gt;IN句のサイズを制約することもある&lt;/h2&gt;

&lt;p&gt;IN句に渡すリストのサイズを制約して分割することもある。&lt;/p&gt;

&lt;ul&gt;
&lt;li&gt;Postgresは65535個以上のクエリパラメータを一度に扱うことができない

&lt;ul&gt;
&lt;li&gt;&lt;a href=&quot;https://www.postgresql.org/docs/current/limits.html&quot;&gt;https://www.postgresql.org/docs/current/limits.html&lt;/a&gt; による&lt;/li&gt;
&lt;li&gt;例えば巨大なIN句を実行してしまうと発生する&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;li&gt;Oracle DBではIN句の中身は1000個までという制限(ORA-01795)がある&lt;/li&gt;
&lt;li&gt;MySQLでは特段の制限はない

&lt;ul&gt;
&lt;li&gt;クライアントの実装にもよりそう&lt;/li&gt;
&lt;/ul&gt;
&lt;/li&gt;
&lt;/ul&gt;


&lt;p&gt;こういう本を読んで勉強したい・・・&lt;/p&gt;

&lt;p&gt;&lt;div class=&quot;hatena-asin-detail&quot;&gt;&lt;a href=&quot;https://www.amazon.co.jp/dp/4297104083?tag=windymelt02-22&amp;amp;linkCode=osi&amp;amp;th=1&amp;amp;psc=1&quot; class=&quot;hatena-asin-detail-image-link&quot; target=&quot;_blank&quot; rel=&quot;noopener&quot;&gt;&lt;img src=&quot;https://m.media-amazon.com/images/I/51B4ihagImL._SL500_.jpg&quot; class=&quot;hatena-asin-detail-image&quot; alt=&quot;失敗から学ぶRDBの正しい歩き方 (Software Design plus)&quot; title=&quot;失敗から学ぶRDBの正しい歩き方 (Software Design plus)&quot;&gt;&lt;/a&gt;&lt;div class=&quot;hatena-asin-detail-info&quot;&gt;&lt;p class=&quot;hatena-asin-detail-title&quot;&gt;&lt;a href=&quot;https://www.amazon.co.jp/dp/4297104083?tag=windymelt02-22&amp;amp;linkCode=osi&amp;amp;th=1&amp;amp;psc=1&quot; target=&quot;_blank&quot; rel=&quot;noopener&quot;&gt;失敗から学ぶRDBの正しい歩き方 (Software Design plus)&lt;/a&gt;&lt;/p&gt;&lt;ul class=&quot;hatena-asin-detail-meta&quot;&gt;&lt;li&gt;&lt;span class=&quot;hatena-asin-detail-label&quot;&gt;作者:&lt;/span&gt;&lt;a href=&quot;https://d.hatena.ne.jp/keyword/%C1%BE%BA%AC%20%C1%D4%C2%E7&quot; class=&quot;keyword&quot;&gt;曽根 壮大&lt;/a&gt;&lt;/li&gt;&lt;li&gt;技術評論社&lt;/li&gt;&lt;/ul&gt;&lt;a href=&quot;https://www.amazon.co.jp/dp/4297104083?tag=windymelt02-22&amp;amp;linkCode=osi&amp;amp;th=1&amp;amp;psc=1&quot; class=&quot;asin-detail-buy&quot; target=&quot;_blank&quot; rel=&quot;noopener&quot;&gt;Amazon&lt;/a&gt;&lt;/div&gt;&lt;/div&gt;&lt;/p&gt;
&lt;div class=&quot;footnote&quot;&gt;
&lt;p class=&quot;footnote&quot;&gt;&lt;a href=&quot;#fn-ef2af921&quot; id=&quot;f-ef2af921&quot; name=&quot;f-ef2af921&quot; class=&quot;footnote-number&quot;&gt;*1&lt;/a&gt;&lt;span class=&quot;footnote-delimiter&quot;&gt;:&lt;/span&gt;&lt;span class=&quot;footnote-text&quot;&gt;クエリなどが重すぎて応答が帰ってこなくなることをいうジャーゴン&lt;/span&gt;&lt;/p&gt;
&lt;/div&gt;</description>          <pubDate>Mon, 02 Sep 2024 22:34:39 +0900</pubDate>
          <guid isPermaLink="false">hatenablog://entry/6802340630903361364</guid>
          
            <category>技術</category>
          
            <category>db</category>
          
          <enclosure url="https://cdn.image.st-hatena.com/image/scale/c948858bd4619e368f9cbf9248614f5e7522b7c0/backend=imagemagick;version=1;width=1300/https%3A%2F%2Fm.media-amazon.com%2Fimages%2FI%2F51B4ihagImL._SL500_.jpg" type="image/jpeg" length="0" />
        </item>
      
    
      
      
        <item>
          <title>ヤモリくん</title>
          <link>https://blog.3qe.us/entry/2024/08/31/035015</link>          <description>&lt;p&gt;数日前、マンションの廊下に出た拍子にヤモリくんが家に入ってきてどこかに行ってしまった。&lt;/p&gt;

&lt;p&gt;暑かったのでホモサピエンスの巣で涼みたかったのかもしれないが、そのときは急いでいたのでそのままヤモリくんに留守番、まさに家守をさせていたが、とうとうその日は見失ってしまった。&lt;/p&gt;

&lt;p&gt;したらさっきヤモリくんが床をウロウロしてたので捕まえて外に出してやった（状況から推察するに『スタッフエンジニアの道』を読んでキャリアプランについて熟考していたようだ）。涼しくて快適だったかもしれないが、ヤモリは生き餌しか食べられないし、うちにいても食べるものがなくて死んでしまうだろう。それは可哀相なのでベランダで暮らしてもらう。これからも我が家のスタッフエンジニアとして家を守ってほしい。&lt;/p&gt;

&lt;p&gt;&lt;div class=&quot;hatena-asin-detail&quot;&gt;&lt;a href=&quot;https://www.amazon.co.jp/dp/4814400861?tag=windymelt02-22&amp;amp;linkCode=osi&amp;amp;th=1&amp;amp;psc=1&quot; class=&quot;hatena-asin-detail-image-link&quot; target=&quot;_blank&quot; rel=&quot;noopener&quot;&gt;&lt;img src=&quot;https://m.media-amazon.com/images/I/41zRbKQxu4L._SL500_.jpg&quot; class=&quot;hatena-asin-detail-image&quot; alt=&quot;スタッフエンジニアの道 ―優れた技術専門職になるためのガイド&quot; title=&quot;スタッフエンジニアの道 ―優れた技術専門職になるためのガイド&quot;&gt;&lt;/a&gt;&lt;div class=&quot;hatena-asin-detail-info&quot;&gt;&lt;p class=&quot;hatena-asin-detail-title&quot;&gt;&lt;a href=&quot;https://www.amazon.co.jp/dp/4814400861?tag=windymelt02-22&amp;amp;linkCode=osi&amp;amp;th=1&amp;amp;psc=1&quot; target=&quot;_blank&quot; rel=&quot;noopener&quot;&gt;スタッフエンジニアの道 ―優れた技術専門職になるためのガイド&lt;/a&gt;&lt;/p&gt;&lt;ul class=&quot;hatena-asin-detail-meta&quot;&gt;&lt;li&gt;&lt;span class=&quot;hatena-asin-detail-label&quot;&gt;作者:&lt;/span&gt;&lt;a href=&quot;https://d.hatena.ne.jp/keyword/Tanya%20Reilly&quot; class=&quot;keyword&quot;&gt;Tanya Reilly&lt;/a&gt;&lt;/li&gt;&lt;li&gt;オーム社&lt;/li&gt;&lt;/ul&gt;&lt;a href=&quot;https://www.amazon.co.jp/dp/4814400861?tag=windymelt02-22&amp;amp;linkCode=osi&amp;amp;th=1&amp;amp;psc=1&quot; class=&quot;asin-detail-buy&quot; target=&quot;_blank&quot; rel=&quot;noopener&quot;&gt;Amazon&lt;/a&gt;&lt;/div&gt;&lt;/div&gt;&lt;/p&gt;
</description>          <pubDate>Sat, 31 Aug 2024 03:50:15 +0900</pubDate>
          <guid isPermaLink="false">hatenablog://entry/6802340630902512919</guid>
          
            <category>暮らし</category>
          
          <enclosure url="https://cdn.image.st-hatena.com/image/scale/55772bc04969f452e40f83edd4c3f8f36e6421f9/backend=imagemagick;version=1;width=1300/https%3A%2F%2Fm.media-amazon.com%2Fimages%2FI%2F41zRbKQxu4L._SL500_.jpg" type="image/jpeg" length="0" />
        </item>
      
    
      
      
        <item>
          <title>Scala 3のsummonって何？</title>
          <link>https://blog.3qe.us/entry/2024/08/21/030430</link>          <description>&lt;p&gt;Scala 3には&lt;code&gt;summon&lt;/code&gt;という関数がある。普段はあまり使うことがないが、知っておくと&lt;code&gt;using&lt;/code&gt;まわりのデバッグにも使えるとても便利なやつだ。この記事では&lt;code&gt;summon&lt;/code&gt;の便利な利用法を紹介する。&lt;/p&gt;

&lt;p&gt;&lt;iframe src=&quot;https://hatenablog-parts.com/embed?url=https%3A%2F%2Fdocs.scala-lang.org%2Fscala3%2Freference%2Fcontextual%2Fusing-clauses.html&quot; title=&quot;Using Clauses&quot; class=&quot;embed-card embed-webcard&quot; scrolling=&quot;no&quot; frameborder=&quot;0&quot; style=&quot;display: block; width: 100%; height: 155px; max-width: 500px; margin: 10px 0px;&quot; loading=&quot;lazy&quot;&gt;&lt;/iframe&gt;&lt;cite class=&quot;hatena-citation&quot;&gt;&lt;a href=&quot;https://docs.scala-lang.org/scala3/reference/contextual/using-clauses.html&quot;&gt;docs.scala-lang.org&lt;/a&gt;&lt;/cite&gt;&lt;/p&gt;

&lt;h2 id=&quot;型の一致を確認する&quot;&gt;型の一致を確認する&lt;/h2&gt;

&lt;p&gt;&lt;strong&gt;&lt;code&gt;summon&lt;/code&gt;の典型的なユースケースは、2つの型が一致するかを確認すること&lt;/strong&gt;だ。&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;&lt;span class=&quot;synType&quot;&gt;type&lt;/span&gt; I = Int
summon[I =:= Int] &lt;span class=&quot;synComment&quot;&gt;// コンパイルが通る&lt;/span&gt;
&lt;/pre&gt;


&lt;p&gt;&lt;code&gt;summon[A =:= B]&lt;/code&gt;と書くと、型&lt;code&gt;A&lt;/code&gt;と&lt;code&gt;B&lt;/code&gt;が等しい場合にはコンパイルが通り、そうではない場合にはコンパイルに失敗する。サブタイプかどうかを見てほしいときは&lt;code&gt;&amp;lt;:&amp;lt;&lt;/code&gt;を使える。&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;summon[&lt;span class=&quot;synConstant&quot;&gt;String&lt;/span&gt; =:= Int]
&lt;/pre&gt;




&lt;pre class=&quot;code error&quot; data-lang=&quot;error&quot; data-unlink&gt;-- [E172] Type Error: ----------------------------------------------------------
1 |summon[String =:= Int]
  |                      ^
  |                      Cannot prove that String =:= Int.
1 error found&lt;/pre&gt;


&lt;p&gt;どういうときに2つの型が一致するか確認したいのかというと、例えば型レベル計算がうまくいっているかを知りたいときに便利だ。要するにこの型ってこの型に展開されるんだよね？というのがわかるのだ。&lt;/p&gt;

&lt;p&gt;例えば、型パラメータに渡したIntのぶん次元を持つベクトル&lt;code&gt;Vec&lt;/code&gt;型を次のように実装したとする(中身は難しいから今は知らなくていい)。&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;&lt;span class=&quot;synPreProc&quot;&gt;import&lt;/span&gt; scala.compiletime.ops.&lt;span class=&quot;synType&quot;&gt;int&lt;/span&gt;._
&lt;span class=&quot;synType&quot;&gt;type&lt;/span&gt; Vec[N &amp;lt;: Int] = N match {
  &lt;span class=&quot;synType&quot;&gt;case&lt;/span&gt; &lt;span class=&quot;synConstant&quot;&gt;0&lt;/span&gt; =&amp;gt; EmptyTuple
  &lt;span class=&quot;synType&quot;&gt;case&lt;/span&gt; _ =&amp;gt; Tuple.Append[Vec[N - &lt;span class=&quot;synConstant&quot;&gt;1&lt;/span&gt;], Double]
}
&lt;/pre&gt;


&lt;p&gt;面白いことにこうすると&lt;code&gt;Vec[4]&lt;/code&gt;と書くだけで&lt;code&gt;(Double, Double, Double, Double)&lt;/code&gt;が表現できる。でもこのコードを書いている間は本当にそうなっているか不安なので、&lt;code&gt;summon&lt;/code&gt;で確かめることができる:&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;summon[Vec[&lt;span class=&quot;synConstant&quot;&gt;4&lt;/span&gt;] =:= (Double, Double, Double, Double)] &lt;span class=&quot;synComment&quot;&gt;// コンパイルが通る&lt;/span&gt;
summon[Vec[&lt;span class=&quot;synConstant&quot;&gt;4&lt;/span&gt;] =:= (Double, Double)] &lt;span class=&quot;synComment&quot;&gt;// コンパイルが通らない&lt;/span&gt;
&lt;/pre&gt;


&lt;h2 id=&quot;usingできることを確認する&quot;&gt;&lt;code&gt;using&lt;/code&gt;できることを確認する&lt;/h2&gt;

&lt;p&gt;もう一つの典型的なユースケースは、CirceなどのJSONライブラリにおける&lt;code&gt;Encoder&lt;/code&gt;やDoobieなどのDBライブラリにおける&lt;code&gt;Meta&lt;/code&gt;といった、何かを変換するために必要なインスタンスを、&lt;code&gt;using&lt;/code&gt;で呼び出せるか確認することだ。&lt;/p&gt;

&lt;p&gt;つまり、どういうわけか&lt;code&gt;case class Foo(b: Bar)&lt;/code&gt;がJSONになってくれない、おかしいなあ、といったときに&lt;code&gt;Encoder[Foo]&lt;/code&gt;や&lt;code&gt;Encoder[Bar]&lt;/code&gt;があることをサッと確認する、といった流れで使う。&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;&lt;span class=&quot;synPreProc&quot;&gt;import&lt;/span&gt; io.circe._, io.circe.generic.auto._

&lt;span class=&quot;synType&quot;&gt;case&lt;/span&gt; &lt;span class=&quot;synType&quot;&gt;class&lt;/span&gt; Foo(n: Int)

&lt;span class=&quot;synComment&quot;&gt;// ...&lt;/span&gt;

summon[Encoder[Foo]] &lt;span class=&quot;synComment&quot;&gt;// Encoder[Foo]をusingできるならコンパイルが通る&lt;/span&gt;
&lt;/pre&gt;


&lt;p&gt;というか、&lt;code&gt;using&lt;/code&gt;できるはずのものをあえて明示的に&lt;strong&gt;召喚する&lt;/strong&gt;ことから&lt;strong&gt;summon&lt;/strong&gt;という名前がついているのだ。&lt;/p&gt;

&lt;p&gt;ちなみに&lt;code&gt;summon&lt;/code&gt;の定義はシンプルそのもの。&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;  &lt;span class=&quot;synComment&quot;&gt;/** Summon a given value of type `T`. Usually, the argument is not passed explicitly.&lt;/span&gt;
&lt;span class=&quot;synComment&quot;&gt;   *&lt;/span&gt;
&lt;span class=&quot;synComment&quot;&gt;   *  @tparam T the type of the value to be summoned&lt;/span&gt;
&lt;span class=&quot;synComment&quot;&gt;   *  @return the given value typed: the provided type parameter&lt;/span&gt;
&lt;span class=&quot;synComment&quot;&gt;   */&lt;/span&gt;
  transparent inline&lt;span class=&quot;synIdentifier&quot;&gt; def&lt;/span&gt; summon[T](using x: T): x.&lt;span class=&quot;synType&quot;&gt;type&lt;/span&gt; = x
&lt;/pre&gt;


&lt;h2 id=&quot;-の仕組み&quot;&gt;&lt;code&gt;=:=&lt;/code&gt; の仕組み&lt;/h2&gt;

&lt;p&gt;賢い読者はこう思ったに違いない。&lt;/p&gt;

&lt;p&gt;&lt;em&gt;&lt;code&gt;using&lt;/code&gt;できるものを明示的に呼び出しているのは分かったけど、じゃあ型が一致してるかどうかを&lt;code&gt;summon[A =:= B]&lt;/code&gt;で判定できるのはどうしてなんだよ？&lt;/em&gt;&lt;/p&gt;

&lt;p&gt;その種明かしをしよう。&lt;code&gt;=:=&lt;/code&gt;は、実は&lt;code&gt;=:=[A, B]&lt;/code&gt;という形で定義されたクラスで、&lt;code&gt;A&lt;/code&gt;と&lt;code&gt;B&lt;/code&gt;とが等しい場合にのみインスタンスが導出されるような巧妙な仕掛けになっている。かなり巧妙&lt;a href=&quot;#f-3971ceae&quot; id=&quot;fn-3971ceae&quot; name=&quot;fn-3971ceae&quot; title=&quot;これだけで1記事必要&quot;&gt;*1&lt;/a&gt;なのでいったんここでは踏み込まない。ちなみに&lt;code&gt;&amp;lt;:&amp;lt;&lt;/code&gt;も同様の仕組みになっている。&lt;/p&gt;

&lt;p&gt;まぁ今回は使い方を説明するということで、なんとか・・・&lt;/p&gt;
&lt;div class=&quot;footnote&quot;&gt;
&lt;p class=&quot;footnote&quot;&gt;&lt;a href=&quot;#fn-3971ceae&quot; id=&quot;f-3971ceae&quot; name=&quot;f-3971ceae&quot; class=&quot;footnote-number&quot;&gt;*1&lt;/a&gt;&lt;span class=&quot;footnote-delimiter&quot;&gt;:&lt;/span&gt;&lt;span class=&quot;footnote-text&quot;&gt;これだけで1記事必要&lt;/span&gt;&lt;/p&gt;
&lt;/div&gt;</description>          <pubDate>Wed, 21 Aug 2024 03:04:30 +0900</pubDate>
          <guid isPermaLink="false">hatenablog://entry/6802340630899985061</guid>
          
            <category>scala</category>
          
            <category>scala3</category>
          
          <enclosure url="https://ogimage.blog.st-hatena.com/12921228815713116358/6802340630899985061/1724177070" type="image/png" length="0" />
        </item>
  </channel>
</rss>""")

      feed.isRight shouldBe true
    }

    it("should parse Atom feed") {
      val feed = parseFeed("""<feed xmlns="http://www.w3.org/2005/Atom" xml:lang="ja">
  <title>Lambdaカクテル</title>
  
  <subtitle>京都在住Webエンジニアの日記です</subtitle>
  
  <link href="https://blog.3qe.us/"/>
  <updated>2025-02-03T23:20:29+09:00</updated>
  <author>
    <name>Windymelt</name>
  </author>
  <generator uri="https://blog.hatena.ne.jp/" version="55294c8e842f3b38121885e5a5179c">Hatena::Blog</generator>
  <id>hatenablog://blog/12921228815713116358</id>

  
    
    
    <entry>
        <title>Scalaで空文字列をNoneにしたいときはfilterすると良い</title>
        <link href="https://blog.3qe.us/entry/2025/02/03/232029"/>
        <id>hatenablog://entry/6802418398325722824</id>
        <published>2025-02-03T23:20:29+09:00</published>
        <updated>2025-02-03T23:20:29+09:00</updated>        <summary type="html">こんな文字列があるとする: val s1 = &quot;foo&quot; val s2 = &quot;&quot; これを、以下のような感じにしたいことはまあまあある: val s1a = Some(&quot;foo&quot;) val s2a = None つまり、以下のようなルールだ: 文字列sが空の場合、None。 それ以外の場合は、Some(s)。 素朴にmatchを使うこともできる: s1 match case &quot;&quot; =&gt; None case s =&gt; Some(s) しかしOptionにはfilterとfilterNotがあるので、先にOptionにしてからfilterすると素直に書ける: Some(s1).filter(_.no…</summary>
        <content type="html">&lt;p&gt;こんな文字列があるとする:&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;&lt;span class=&quot;synType&quot;&gt;val&lt;/span&gt; s1 = &lt;span class=&quot;synConstant&quot;&gt;&amp;quot;foo&amp;quot;&lt;/span&gt;
&lt;span class=&quot;synType&quot;&gt;val&lt;/span&gt; s2 = &lt;span class=&quot;synConstant&quot;&gt;&amp;quot;&amp;quot;&lt;/span&gt;
&lt;/pre&gt;


&lt;p&gt;これを、以下のような感じにしたいことはまあまあある:&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;&lt;span class=&quot;synType&quot;&gt;val&lt;/span&gt; s1a = Some(&lt;span class=&quot;synConstant&quot;&gt;&amp;quot;foo&amp;quot;&lt;/span&gt;)
&lt;span class=&quot;synType&quot;&gt;val&lt;/span&gt; s2a = None
&lt;/pre&gt;


&lt;p&gt;つまり、以下のようなルールだ:&lt;/p&gt;

&lt;ul&gt;
&lt;li&gt;文字列sが空の場合、&lt;code&gt;None&lt;/code&gt;。&lt;/li&gt;
&lt;li&gt;それ以外の場合は、&lt;code&gt;Some(s)&lt;/code&gt;。&lt;/li&gt;
&lt;/ul&gt;


&lt;p&gt;素朴に&lt;code&gt;match&lt;/code&gt;を使うこともできる:&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;s1 match
&lt;span class=&quot;synType&quot;&gt;case&lt;/span&gt; &lt;span class=&quot;synConstant&quot;&gt;&amp;quot;&amp;quot;&lt;/span&gt; =&amp;gt; None
&lt;span class=&quot;synType&quot;&gt;case&lt;/span&gt; s =&amp;gt; Some(s)
&lt;/pre&gt;


&lt;p&gt;しかし&lt;code&gt;Option&lt;/code&gt;には&lt;code&gt;filter&lt;/code&gt;と&lt;code&gt;filterNot&lt;/code&gt;があるので、先に&lt;code&gt;Option&lt;/code&gt;にしてから&lt;code&gt;filter&lt;/code&gt;すると素直に書ける:&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;Some(s1).filter(_.nonEmpty)
&lt;span class=&quot;synComment&quot;&gt;// equivalent&lt;/span&gt;
Some(s1).filterNot(_.isEmpty)
&lt;/pre&gt;


&lt;p&gt;Catsを使っている場合は、&lt;code&gt;.some&lt;/code&gt;を使える:&lt;/p&gt;

&lt;pre class=&quot;code lang-scala&quot; data-lang=&quot;scala&quot; data-unlink&gt;&lt;span class=&quot;synPreProc&quot;&gt;import&lt;/span&gt; cats.syntax.all.{*, given}
s1.some.filter(_.nonEmpty)
&lt;/pre&gt;


&lt;h2 id=&quot;あわせて読みたい&quot;&gt;あわせて読みたい&lt;/h2&gt;

&lt;p&gt;逆のパターンはCatsで素直に書くパターンがある。&lt;/p&gt;

&lt;p&gt;&lt;iframe src=&quot;https://hatenablog-parts.com/embed?url=https%3A%2F%2Fblog.3qe.us%2Fentry%2F2022%2F05%2F17%2F153645&quot; title=&quot;Noneは空文字に写したい、そんな君のためにcombineAllがある - Lambdaカクテル&quot; class=&quot;embed-card embed-blogcard&quot; scrolling=&quot;no&quot; frameborder=&quot;0&quot; style=&quot;display: block; width: 100%; height: 190px; max-width: 500px; margin: 10px 0px;&quot; loading=&quot;lazy&quot;&gt;&lt;/iframe&gt;&lt;cite class=&quot;hatena-citation&quot;&gt;&lt;a href=&quot;https://blog.3qe.us/entry/2022/05/17/153645&quot;&gt;blog.3qe.us&lt;/a&gt;&lt;/cite&gt;&lt;/p&gt;
</content>        
        <category term="技術" label="技術" />
        
        <category term="scala" label="scala" />
        
        <link rel="enclosure" href="https://ogimage.blog.st-hatena.com/12921228815713116358/6802418398325722824/1738592429" type="image/png" length="0" />

        <author>
            <name>Windymelt</name>
        </author>
    </entry>
    
    <entry>
        <title>文章は長ければ長いほど良いし、想いは言葉になっているほど良い</title>
        <link href="https://blog.3qe.us/entry/2025/02/03/033421"/>
        <id>hatenablog://entry/6802418398325467294</id>
        <published>2025-02-03T03:34:21+09:00</published>
        <updated>2025-02-03T03:34:21+09:00</updated>        <summary type="html">自分は批判的意見というものにある程度真面目に耳を傾けていたのだけれど、大好きな記事に、3秒で思い付いたようながらくたコメントがつけられているのを見て、すべての意見は傾聴に値する、というテーゼがゴツンとひび割れ、身銭を切って前に出たヤツを信じよう、という気持ちになってきた。文章は長ければ長いほど良いし、想いは言葉になっているほど良い</summary>
        <content type="html">&lt;p&gt;自分は批判的意見というものにある程度真面目に耳を傾けていたのだけれど、大好きな記事に、3秒で思い付いたようながらくたコメントがつけられているのを見て、すべての意見は傾聴に値する、というテーゼがゴツンとひび割れ、身銭を切って前に出たヤツを信じよう、という気持ちになってきた。文章は長ければ長いほど良いし、想いは言葉になっているほど良い&lt;/p&gt;
</content>        
        <category term="暮らし" label="暮らし" />
        
        <category term="日記" label="日記" />
        
        <link rel="enclosure" href="https://ogimage.blog.st-hatena.com/12921228815713116358/6802418398325467294/1738521261" type="image/png" length="0" />

        <author>
            <name>Windymelt</name>
        </author>
    </entry>
    
  
    
    
    <entry>
        <title>無印の黒文字の香りのルームフレグランスを買った</title>
        <link href="https://blog.3qe.us/entry/2025/02/02/024328"/>
        <id>hatenablog://entry/6802418398325165524</id>
        <published>2025-02-02T02:43:28+09:00</published>
        <updated>2025-02-02T02:43:28+09:00</updated>        <summary type="html">自分はどういうわけか自分が買ったものがすぐ廃盤になる呪い*1を受けていて、先日購入した無印のルームフレグランス「クリア」が既に廃盤になっていた。店頭にいたのはその生き残りだったことになる。 しょうがないので近所で見繕っていたのだが、良い香りにも色々あって、有名ブランドの「良い香り」はとにかく絵画のようにコンセプトが先行していて、これが俺が考えた良い香りだ！！！良い香りだろう！！！ムンムン！！！これでお前もこの世界観の一員だ！！！みたいな感じになりがちで、美術館とか家具屋さんとか車屋さんとかだったら良いかもしれないけど、ウチ、ワンルームのしがない独身会社員男性の家なんで・・・みたいな気持ちになる…</summary>
        <content type="html">&lt;p&gt;自分はどういうわけか自分が買ったものがすぐ廃盤になる呪い&lt;a href=&quot;#f-a8edf42f&quot; id=&quot;fn-a8edf42f&quot; name=&quot;fn-a8edf42f&quot; title=&quot;使っていたルラボのアイリスの香水も廃盤になった&quot;&gt;*1&lt;/a&gt;を受けていて、先日購入した無印のルームフレグランス「クリア」が既に廃盤になっていた。店頭にいたのはその生き残りだったことになる。&lt;/p&gt;

&lt;p&gt;しょうがないので近所で見繕っていたのだが、良い香りにも色々あって、有名ブランドの「良い香り」はとにかく絵画のようにコンセプトが先行していて、これが俺が考えた良い香りだ！！！良い香りだろう！！！ムンムン！！！これでお前もこの世界観の一員だ！！！みたいな感じになりがちで、美術館とか家具屋さんとか車屋さんとかだったら良いかもしれないけど、ウチ、ワンルームのしがない独身会社員男性の家なんで・・・みたいな気持ちになる。コルビュジェが建てた家ではないし、カッシーナの家具もない。一言で表すなら、ただ寝、食べ、仕事をする場所において主張が強すぎ、臭すぎるのだ。&lt;/p&gt;

&lt;p&gt;ディプティックのルームフレグランスはモノによるが抑えめの香りもある。しかし高い。そしてやはり主張が前に出る。イソップがリードディフューザーのフレグランスを出せばいいのに、と思う。&lt;/p&gt;

&lt;p&gt;で結局無印に戻ってきて、色々探したところ「クロモジ」に落ち着いた。楠のような木質系の香りに、どこか酸味のような上着をまとった香り。同様の系統に「ウッディ」があるが、あれはシダーなのでまさに杉系統の香りで、クロモジのような渋い酸味がしたたる感じはない。クロモジは「なんか柑橘っぽさ(柑橘ではない)」とウッディさのバランスがちょうど取れている。気にいったのでこれはかなり長いこと使いたい。&lt;/p&gt;

&lt;p&gt;&lt;iframe src=&quot;https://hatenablog-parts.com/embed?url=https%3A%2F%2Fwww.muji.com%2Fjp%2Fja%2Fstore%2Fcmdty%2Fdetail%2F%25E3%2582%25A4%25E3%2583%25B3%25E3%2583%2586%25E3%2583%25AA%25E3%2582%25A2%25E3%2583%2595%25E3%2583%25AC%25E3%2582%25B0%25E3%2583%25A9%25E3%2583%25B3%25E3%2582%25B9%25E3%2582%25AA%25E3%2582%25A4%25E3%2583%25AB%25E3%2580%2580%25E3%2582%25AF%25E3%2583%25AD%25E3%2583%25A2%25E3%2582%25B8%2F4550584359569%3Fsrsltid%3DAfmBOor-mt5mO-drWL4SC_Egbwnn1IGcBoGfG0Uhezs-sYtKatfa6kGO&quot; title=&quot;インテリアフレグランスオイル　クロモジ | 無印良品&quot; class=&quot;embed-card embed-webcard&quot; scrolling=&quot;no&quot; frameborder=&quot;0&quot; style=&quot;display: block; width: 100%; height: 155px; max-width: 500px; margin: 10px 0px;&quot; loading=&quot;lazy&quot;&gt;&lt;/iframe&gt;&lt;cite class=&quot;hatena-citation&quot;&gt;&lt;a href=&quot;https://www.muji.com/jp/ja/store/cmdty/detail/%E3%82%A4%E3%83%B3%E3%83%86%E3%83%AA%E3%82%A2%E3%83%95%E3%83%AC%E3%82%B0%E3%83%A9%E3%83%B3%E3%82%B9%E3%82%AA%E3%82%A4%E3%83%AB%E3%80%80%E3%82%AF%E3%83%AD%E3%83%A2%E3%82%B8/4550584359569?srsltid=AfmBOor-mt5mO-drWL4SC_Egbwnn1IGcBoGfG0Uhezs-sYtKatfa6kGO&quot;&gt;www.muji.com&lt;/a&gt;&lt;/cite&gt;&lt;/p&gt;

&lt;p&gt;さっき知ったのだが、これは季節限定らしい。またか。&lt;/p&gt;

&lt;p&gt;他に探すとしたら、北欧系のやつがいいと思う。ヘトキネンとかが良いのではないか。&lt;/p&gt;
&lt;div class=&quot;footnote&quot;&gt;
&lt;p class=&quot;footnote&quot;&gt;&lt;a href=&quot;#fn-a8edf42f&quot; id=&quot;f-a8edf42f&quot; name=&quot;f-a8edf42f&quot; class=&quot;footnote-number&quot;&gt;*1&lt;/a&gt;&lt;span class=&quot;footnote-delimiter&quot;&gt;:&lt;/span&gt;&lt;span class=&quot;footnote-text&quot;&gt;使っていたルラボのアイリスの香水も廃盤になった&lt;/span&gt;&lt;/p&gt;
&lt;/div&gt;</content>        
        <category term="暮らし" label="暮らし" />
        
        <category term="物欲" label="物欲" />
        
        <link rel="enclosure" href="https://ogimage.blog.st-hatena.com/12921228815713116358/6802418398325165524/1738431808" type="image/png" length="0" />

        <author>
            <name>Windymelt</name>
        </author>
    </entry>
    </feed>
    """)

      feed.isRight shouldBe true
    }
  }
}
