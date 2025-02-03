package dev.capslock

import java.net.URL
import scala.xml.Node

package object rss4s {
  case class Feed(
      title: String,
      link: URL,
      description: Option[String] = None,
      items: List[Item] = List.empty,
  )
  case class Item(title: String, link: URL, description: Option[String] = None)

  enum ParseFeedError {
    case InvalidXml
    case InvalidFeed
    case Unknown
  }

  def parseFeed(feed: String): Either[ParseFeedError, Feed] = {
    import scala.xml.*
    import cats.syntax.traverse.{*, given} // toList.sequence
    import cats.syntax.option.{*, given}

    val x     = xml.parsing.XhtmlParser(scala.io.Source.fromString(feed))
    val title = (x \ "channel" \ "title").text
    val link  = (x \ "channel" \ "link").text

    val description =
      (x \ "channel" \ "description").text.some.filter(_.nonEmpty)

    for {
      items <- (x \ "channel" \ "item").map(parseItem).toList.sequence
      feed  <- Right(Feed(title, new URL(link), description, items))
    } yield feed
  }

  private def parseItem(item: Node): Either[ParseFeedError, Item] = {
    import cats.syntax.option.{*, given}

    val title       = (item \ "title").text
    val link        = (item \ "link").text
    val description = (item \ "description").text.some.filter(_.nonEmpty)

    Right(Item(title, new URL(link), description))
  }
}
