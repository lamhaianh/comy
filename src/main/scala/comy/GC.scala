package comy

import comy.model.DB

/**
 * This Gabage Collector should be run periodically to remove old (expired) URL
 * entries.
 */
object GC {
  def main(args: Array[String]) {
    println("GC started")
    DB.removeExpiredUrls
    println("GC started")
  }
}
