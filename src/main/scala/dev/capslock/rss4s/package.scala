package dev.capslock

import java.net.URL
import scala.xml.Node
import scala.xml.NodeSeq
import java.time.OffsetDateTime

package object rss4s:
  case class Feed(
      title: String,
      link: URL,
      description: Option[String] = None,
      items: List[Item] = List.empty,
  )
  case class Item(
      title: String,
      link: URL,
      description: Option[String] = None,
      publishedAt: Option[OffsetDateTime] = None,
  )

  enum FeedFormat:
    case Rss20
    case Atom
    case Unknown

  enum ParseFeedError:
    case InvalidXml
    case InvalidFeed
    case Unknown

  def parseFeed(feedString: String): Either[ParseFeedError, Feed] =
    import scala.xml.*
    import cats.syntax.traverse.{*, given} // toList.sequence
    import cats.syntax.option.{*, given}

    val feed = xml.parsing.XhtmlParser(scala.io.Source.fromString(feedString))

    detectFeedFormat(feed) match
      case FeedFormat.Rss20 => domain.RSS20.parse(feed)
      case FeedFormat.Atom  => domain.Atom.parse(feed)
      case FeedFormat.Unknown =>
        Left(ParseFeedError.InvalidFeed)
  end parseFeed

  private def detectFeedFormat(feed: NodeSeq): FeedFormat =
    if domain.RSS20.seemsLike(feed) then FeedFormat.Rss20
    else if domain.Atom.seemsLike(feed) then FeedFormat.Atom
    else FeedFormat.Unknown
end rss4s
