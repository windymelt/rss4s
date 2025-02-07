package dev.capslock.rss4s
package domain

import scala.xml.NodeSeq
import java.net.URL
import java.time.OffsetDateTime
import scala.xml.Node

object RSS20 {
  def seemsLike(tree: NodeSeq): Boolean = (tree \\ "rss").nonEmpty

  def parse(tree: NodeSeq): Either[ParseFeedError, Feed] = {
    import cats.syntax.traverse.{*, given} // toList.sequence
    import cats.syntax.option.{*, given}

    val title = (tree \ "channel" \ "title").text
    val link  = (tree \ "channel" \ "link").text

    val description =
      (tree \ "channel" \ "description").text.some.filter(_.nonEmpty)

    for {
      items <- (tree \ "channel" \ "item").map(parseRss20Item).toList.sequence
      feed  <- Right(Feed(title, new URL(link), description, items))
    } yield feed
  }

  private def parseRss20Item(item: Node): Either[ParseFeedError, Item] = {
    import cats.syntax.option.{*, given}

    val title       = (item \ "title").text
    val link        = (item \ "link").text
    val description = (item \ "description").text.some.filter(_.nonEmpty)
    val publishedAt = (item \ "pubDate").text.some.filter(_.nonEmpty)

    val parsedPublishedAt = publishedAt.flatMap(parseRss20DateTime)

    Right(
      Item(
        title,
        new URL(link),
        description,
        parsedPublishedAt,
      ),
    )
  }

  private def parseRss20DateTime(s: String): Option[OffsetDateTime] = {
    import java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
    import scala.util.control.Exception.allCatch

    // https://www.rssboard.org/rss-draft-1#data-types-datetime
    // > All date-time values MUST conform to the RFC 822 Date and Time Specification with the exception that a four-digit year is permitted and RECOMMENDED over a two-digit year.
    // ISO 1123 is a subset of RFC 822; it's the same format but with a four-digit year.
    // This implementation cannot handle two-digit years now.

    allCatch.opt(
      OffsetDateTime.parse(s, RFC_1123_DATE_TIME),
    )
  }
}
