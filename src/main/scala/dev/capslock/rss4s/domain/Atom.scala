package dev.capslock.rss4s
package domain

import scala.xml.NodeSeq
import java.net.URL
import java.time.OffsetDateTime
import scala.xml.Node

object Atom:
  def seemsLike(tree: NodeSeq): Boolean = (tree \\ "feed").nonEmpty

  def parse(tree: NodeSeq): Either[ParseFeedError, Feed] =
    import cats.syntax.traverse.{*, given} // toList.sequence
    import cats.syntax.option.{*, given}

    val title = (tree \ "title").text
    val link  = (tree \ "link").headOption.map(_.attribute("href").get.text)

    val description = (tree \ "subtitle").text.some.filter(_.nonEmpty)

    for
      items <- (tree \ "entry").map(parseAtomItem).toList.sequence
      feed  <- Right(Feed(title, new URL(link.get), description, items))
    yield feed
  end parse

  private def parseAtomItem(item: Node): Either[ParseFeedError, Item] =
    import cats.syntax.option.{*, given}

    val title = (item \ "title").text
    val link  = (item \ "link").headOption.map(_.attribute("href").get.text)
    val description       = (item \ "summary").text.some.filter(_.nonEmpty)
    val publishedAt       = (item \ "published").text.some.filter(_.nonEmpty)
    val parsedPublishedAt = publishedAt.flatMap(parseAtomDateTime)

    Right(
      Item(
        title,
        new URL(link.get),
        description,
        parsedPublishedAt,
      ),
    )
  end parseAtomItem

  private def parseAtomDateTime(s: String): Option[OffsetDateTime] =
    import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
    import scala.util.control.Exception.allCatch

    // https://www.ietf.org/rfc/rfc4287.txt
    // 3.3. Date Constructs
    // > A Date construct is an element whose content MUST conform to the
    // > "date-time" production in [RFC3339].
    // ISO 8601 is a profile of RFC 3339.
    // Hence we sometimes fail to parse RFC 3339 dates with this implementation.

    allCatch.opt(
      OffsetDateTime.parse(s, ISO_OFFSET_DATE_TIME),
    )
  end parseAtomDateTime
end Atom
