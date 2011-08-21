package comy.action.api

import org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND

import xitrum.Action
import xitrum.annotation.{GET, Last}

import comy.model.DB

@Last
@GET("/:key")
class Lengthen extends Action {
  override def execute {
    val key = param("key")
    DB.getUrl(key) match {
      case Some(url) =>
        // Some KDDI AU mobiles display annoying dialog for 301
        redirectTo(url)

      case None =>
        response.setStatus(NOT_FOUND)
        renderText("Not Found")
    }
  }
}
