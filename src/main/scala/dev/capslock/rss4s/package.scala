package dev.capslock

import java.net.URL
import scala.xml.Node
import scala.xml.NodeSeq

package object rss4s {
  case class Feed(
      title: String,
      link: URL,
      description: Option[String] = None,
      items: List[Item] = List.empty,
  )
  case class Item(title: String, link: URL, description: Option[String] = None)

  enum FeedFormat {
    case Rss20
    case Atom
    case Unknown
  }

  enum ParseFeedError {
    case InvalidXml
    case InvalidFeed
    case Unknown
  }

  def parseFeed(feedString: String): Either[ParseFeedError, Feed] = {
    import scala.xml.*
    import cats.syntax.traverse.{*, given} // toList.sequence
    import cats.syntax.option.{*, given}

    val feed = xml.parsing.XhtmlParser(scala.io.Source.fromString(feedString))

    detectFeedFormat(feed) match
      case FeedFormat.Rss20 => parseRss20Feed(feed)
      case FeedFormat.Atom  => parseAtomFeed(feed)
      case FeedFormat.Unknown =>
        Left(ParseFeedError.InvalidFeed)
  }

  private def detectFeedFormat(feed: NodeSeq): FeedFormat = {
    val rss20 = (feed \\ "rss").nonEmpty
    val atom  = (feed \\ "feed").nonEmpty

    if rss20 then FeedFormat.Rss20
    else if atom then FeedFormat.Atom
    else FeedFormat.Unknown
  }

  private def parseRss20Feed(feed: NodeSeq): Either[ParseFeedError, Feed] = {
    import cats.syntax.traverse.{*, given} // toList.sequence
    import cats.syntax.option.{*, given}

    val title = (feed \ "channel" \ "title").text
    val link  = (feed \ "channel" \ "link").text

    val description =
      (feed \ "channel" \ "description").text.some.filter(_.nonEmpty)

    for {
      items <- (feed \ "channel" \ "item").map(parseRss20Item).toList.sequence
      feed  <- Right(Feed(title, new URL(link), description, items))
    } yield feed
  }

  private def parseAtomFeed(feed: NodeSeq): Either[ParseFeedError, Feed] = {
    import cats.syntax.traverse.{*, given} // toList.sequence
    import cats.syntax.option.{*, given}

    val title = (feed \ "title").text
    val link  = (feed \ "link").headOption.map(_.attribute("href").get.text)

    val description = (feed \ "subtitle").text.some.filter(_.nonEmpty)

    for {
      items <- (feed \ "entry").map(parseAtomItem).toList.sequence
      feed  <- Right(Feed(title, new URL(link.get), description, items))
    } yield feed
  }

  private def parseRss20Item(item: Node): Either[ParseFeedError, Item] = {
    import cats.syntax.option.{*, given}

    val title       = (item \ "title").text
    val link        = (item \ "link").text
    val description = (item \ "description").text.some.filter(_.nonEmpty)

    Right(Item(title, new URL(link), description))
  }

  private def parseAtomItem(item: Node): Either[ParseFeedError, Item] = {
    import cats.syntax.option.{*, given}

    val title = (item \ "title").text
    val link  = (item \ "link").headOption.map(_.attribute("href").get.text)
    val description = (item \ "summary").text.some.filter(_.nonEmpty)

    Right(Item(title, new URL(link.get), description))
  }
}
