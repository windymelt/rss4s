package dev.capslock

import java.net.URL

package object rss4s {
  case class Feed(title: String, link: URL)
  enum ParseFeedError {
    case InvalidXml
    case InvalidFeed
    case Unknown
  }
  def parseFeed(feed: String): Either[ParseFeedError, Feed] = {
    import scala.xml._
    val x = xml.parsing.XhtmlParser(scala.io.Source.fromString(feed))
    println(x)
    val title = (x \ "channel" \ "title").text
    val link  = (x \ "channel" \ "link").text
    Right(Feed(title, new URL(link)))
  }
}
