package comy.controller

import comy.Config
import comy.model.{DB, SaveUrlResult}

import s3m._
import s3m.exception._

/** See ScalatraServlet vs. ScalatraFilter */
class Api extends Controller {
  @GET
  @Path("/:key")
  def lengthen {
    val key = param("key")

    DB.getUrl(key) match {
      case Some(url) =>
        // Use 302 instead of 301 because:
        // * Some KDDI AU mobiles display annoying dialog for 301
        // * Not all browsers support HTTP/1.1
        response.setStatus(302)
        response.sendRedirect(url)
        complete

      case None =>
        // Pass to other filters/servlets instead of just
        // response.setStatus(404)
        throw new Pass
    }
  }

  @POST
  @Path("/api/shorten")
  def shorten {  // ?url=URL[&key=KEY]
    checkIpForShorten

    val url  = param("url")
    val keyo = paramo("key")

    val (resultCode, resultString) = DB.saveUrl(url, keyo)

    val status = resultCode match {
      case SaveUrlResult.VALID      => 200
      case SaveUrlResult.INVALID    => 400
      case SaveUrlResult.DUPLICATED => 409
      case SaveUrlResult.ERROR      => 500
    }

    response.setStatus(status)
    if (status == 200) renderText(resultString) else complete
  }

  //----------------------------------------------------------------------------

  protected def checkIpForShorten {
    val remoteIp = request.getRemoteAddr
    if (!Config.isApiAllowed(remoteIp)) {
      response.sendError(403)
      throw new Halt
    }
  }
}
