package dev.capslock

import java.net.URL

package object rss4s {
  case class Feed(title: String, link: URL, description: Option[String] = None)
  enum ParseFeedError {
    case InvalidXml
    case InvalidFeed
    case Unknown
  }
  def parseFeed(feed: String): Either[ParseFeedError, Feed] = {
    import scala.xml._
    val x           = xml.parsing.XhtmlParser(scala.io.Source.fromString(feed))
    val title       = (x \ "channel" \ "title").text
    val link        = (x \ "channel" \ "link").text
    val description = Option((x \ "channel" \ "description").text)
    Right(Feed(title, new URL(link), description))
  }
}
