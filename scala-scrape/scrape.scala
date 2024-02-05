//> using jvm "adopt:11"
//> using dep org.jsoup:jsoup:1.17.2
//> using dep "com.softwaremill.sttp.client4::core:4.0.0-M8"
//> using dep com.lihaoyi::upickle:3.1.4

import org.jsoup._
import scala.jdk.CollectionConverters._
import scala.collection.mutable.ListMap
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path
import java.nio.file.FileAlreadyExistsException
import sttp.client4.quick._
import sttp.client4.Response
import upickle.default._
import upickle.implicits.key
import java.util.NoSuchElementException
import java.net.SocketTimeoutException

object JsoupScraper {

  def main(args: Array[String]): Unit = {
    val scrapeRootUrl = args(0)
    val outputDir = args(1)
    println(scrapeRootUrl)
    println(outputDir)
    val doc = Jsoup.connect(scrapeRootUrl).get()
    println(doc.title)
    // Get all the links to chapters
    val links = doc.select("article").select("a[href]")

    var chapterLinks = ListMap[String, String]()
    val localLinkPattern = scrapeRootUrl.split("/")(1)
    //val localLinkPattern = "exiledrebelsscanlations.com"
    for (l <- links.asScala) {
      val ref = l.attr("abs:href")
      if (ref.contains(localLinkPattern)){
        println("%s --> %s".format(ref, l.text))
        chapterLinks.addOne(ref, l.text)
      }
    }
    println("I have this many chapters to scrape: %d".format(chapterLinks.knownSize))
    // create output directory if it doesn't exist:
    try {
      println("Creating 'output' directory: %s".format(outputDir))
      val path = Paths.get(outputDir)
      Files.createDirectory(path)
    } catch {
      case fae: FileAlreadyExistsException => println("Directory already exists") 
    }
    // Begin scraping
    for (chapter <- chapterLinks) {
      try {
        val chapterLink = chapter._1.trim
        val chapterFileName = "%s/%s.html".format(outputDir, chapterLink.split("/").last)
        println("Lookup link: %s".format(chapterLink))
        println("Writing to: %s".format(chapterFileName))
        // need to use wayback API as most of these have been taken down
        // https://archive.org/help/wayback_api.php
        val waybackRequest = "http://archive.org/wayback/available?url=%s&timestamp=20190101".format(chapterLink)
        println("Wayback link: %s".format(waybackRequest))
        val response = quickRequest.get(uri"$waybackRequest").send()
        val body = response.body
        val json = ujson.read(body)
        val closestLink = json("archived_snapshots").obj("closest").obj("url").str
        val p = Paths.get(chapterFileName)
        Files.createFile(p)
        val chapterDoc = Jsoup.connect(closestLink).timeout(20000).get()
        val chapterText = chapterDoc.select("article")
        val writer = Files.newBufferedWriter(p)
        // TODO: add meta charset tag
        writer.write(chapterText.outerHtml)
        // don't hammer
        Thread.sleep(300)
      } catch {
        case fae: FileAlreadyExistsException => println("[WARN] File already exists, skipping, check") 
        case nsee: NoSuchElementException => println("[ERROR] No closest wayback to 2019, skipping")
        case ste: SocketTimeoutException => println("[ERROR] Socket timeout, do this later")
      }
    }
  }
}
