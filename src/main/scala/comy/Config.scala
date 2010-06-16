package comy

import java.util.Properties
import java.io.FileInputStream

class Config(path: String) extends Logs {
  val properties = new Properties
  properties.load(new FileInputStream(path))

  val serverPort = properties.getProperty("SERVER_PORT").toInt
  val allowedIps = properties.getProperty("ALLOWED_IPS").split(",").map(ip => ip.trim)

  val dbHost           = properties.getProperty("DB_HOST")
  val dbPort           = properties.getProperty("DB_PORT").toInt
  val dbDB             = properties.getProperty("DB_DB")
  val dbExpirationDays = properties.getProperty("DB_EXPIRATION_DAYS").toInt

  val logPath             = properties.getProperty("LOG_PATH")
  setLogPath(logPath)

  def isAllowed(ip: String) = allowedIps.exists { ip2 =>
    (ip2 == "*") || (ip2 == ip)
  }
}
