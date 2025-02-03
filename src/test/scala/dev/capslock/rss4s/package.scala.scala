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
      feed shouldBe Right(
        Feed(
          "test feed",
          new URL("https://a.feed.example.com"),
          Some("This is test feed for RSS 2.0"),
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
      feed shouldBe Right(
        Feed(
          "another test feed",
          new URL("https://another.feed.example.com"),
          Some("This is another test feed for RSS 2.0"),
        ),
      )
    }
  }
}
