package comy.controller

import comy.Config
import comy.model.{DB, SaveUrlResult}

import org.scalatra.ScalatraFilter

/** See ScalatraServlet vs. ScalatraFilter */
class Api extends ScalatraFilter {
  get("/:key") {
    val key = params("key")

    DB.getUrl(key) match {
      case Some(url) =>
        // Use 302 instead of 301 because:
        // * Some KDDI AU mobiles display annoying dialog for 301
        // * Not all browsers support HTTP/1.1
        response.setStatus(302)
        redirect(url)

      case None =>
        // Pass to other filters/servlets instead of just
        // response.setStatus(404)
        pass
    }
  }

  post("/api/shorten") {  // ?url=URL[&key=KEY]
    checkIpForShorten

    val url  = params("url")
    val keyo = params.get("key")

    val (resultCode, resultString) = DB.saveUrl(url, keyo)

    val status = resultCode match {
      case SaveUrlResult.VALID      => 200
      case SaveUrlResult.INVALID    => 400
      case SaveUrlResult.DUPLICATED => 409
      case SaveUrlResult.ERROR      => 500
    }

    response.setStatus(status)
    if (status == 200) response.getWriter.print(resultString)
  }

  //----------------------------------------------------------------------------

  protected def checkIpForShorten {
    val remoteIp = request.getRemoteAddr
    if (!Config.isApiAllowed(remoteIp)) halt(403)
  }
}
