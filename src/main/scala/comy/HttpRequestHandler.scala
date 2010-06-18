package comy

import org.jboss.netty.handler.codec.http._
import HttpHeaders._
import HttpHeaders.Names._
import HttpResponseStatus._
import HttpVersion._
import HttpMethod._
import org.jboss.netty.buffer._
import org.jboss.netty.channel._
import org.jboss.netty.util.CharsetUtil

class HttpRequestHandler(config: Config, db: DB) extends SimpleChannelUpstreamHandler with Logger {
  override def messageReceived(ctx: ChannelHandlerContext, e: MessageEvent) {
    val request  = e.getMessage.asInstanceOf[HttpRequest]
    val response = new DefaultHttpResponse(HTTP_1_1, OK)

    val uri = request.getUri
    val qd = new QueryStringDecoder(uri)
    if (qd.getPath == "/api" && request.getMethod == POST) {
      if (!isApiAllowed(e)) {
        e.getChannel.close
        return
      }

      val url = qd.getParameters.get("url").get(0)
      if (url != null) {
        db.saveUrl(url) match {
          case Some(key) =>
            response.setContent(ChannelBuffers.copiedBuffer(key, CharsetUtil.UTF_8))
            response.setHeader(CONTENT_TYPE, "text/plain")
          case None =>
            response.setStatus(INTERNAL_SERVER_ERROR)
        }
      } else {
        response.setStatus(BAD_REQUEST)
      }
    } else {
      val key = uri.substring(1)  // Skip "/" prefix
      db.getUrl(key) match {
        case Some(url) =>
          response.setStatus(MOVED_PERMANENTLY)
          response.setHeader(LOCATION, url)
        case None =>
          response.setStatus(NOT_FOUND)
      }
    }

    val keepAlive = isKeepAlive(request)

    // Add 'Content-Length' header only for a keep-alive connection.
    // Close the non-keep-alive connection after the write operation is done.
    if (keepAlive) {
      response.setHeader(CONTENT_LENGTH, response.getContent.readableBytes)
    }
    val future = e.getChannel.write(response)
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE)
    }
  }

  override def exceptionCaught(ctx:ChannelHandlerContext, e:ExceptionEvent) {
    error(e.toString)
    e.getChannel.close
  }

  private def isApiAllowed(e: MessageEvent): Boolean = {
    val remoteAddress = e.getRemoteAddress().toString
    val ip = remoteAddress.substring(1, remoteAddress.indexOf(':'))
    config.isApiAllowed(ip)
  }
}
