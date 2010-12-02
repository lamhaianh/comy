package comy.controller

import java.io.ByteArrayOutputStream
import java.net.URI

import javax.imageio.ImageIO
import javax.ws.rs.{GET, POST, Path, PathParam, QueryParam, Produces}
import javax.ws.rs.core.Response

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.ByteMatrix
import com.google.zxing.qrcode.QRCodeWriter

import comy.Config
import comy.model.{DB, SaveUrlResult}

object Api {
  val QR_CODE_WIDTH  = 150
  val QR_CODE_HEIGHT = 150
}

class Api {
  import Api._

  //beforeFilter("checkIpForShorten", Except("lengthen"))

  @POST
  @Path("/api/shorten")  // ?url=URL[&key=KEY]
  def shorten(@PathParam("url") url: String, @QueryParam("key") key: String) = {
    val keyo = if (key != null) Some(key) else None
    val (resultCode, resultString) = DB.saveUrl(url, keyo)

    val status = resultCode match {
      case SaveUrlResult.VALID     => 200
      case SaveUrlResult.INVALID   => 400
      case SaveUrlResult.DUPLICATE => 409
      case SaveUrlResult.ERROR     => 500
    }

    if (status != 200) Response.status(status).build else resultString
  }

  /** See: http://www.hascode.com/2010/05/playing-around-with-qr-codes/ */
  @Path("/api/qrcode")  // ?url=xxx
  @Produces(Array("image/png"))
  def qrcode(@QueryParam("key") url: String) = {
    val writer = new QRCodeWriter
    val mtx    = writer.encode(url, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT)
    invertImage(mtx)
    val image  = MatrixToImageWriter.toBufferedImage(mtx)

    val baos = new ByteArrayOutputStream
    ImageIO.write(image, "png", baos)
    baos.toByteArray
  }

  @Path("/{key}")
  def lengthen(@PathParam("key") key: String) {
    DB.getUrl(key) match {
      case Some(url) =>
        // Use 302 instead of 301 because:
        // * Some KDDI AU mobiles display annoying dialog for 301
        // * Not all browsers support HTTP/1.1
        Response.seeOther(new URI(url)).build

      case None =>
        Response.status(404).build
    }
  }

  //----------------------------------------------------------------------------

  protected def checkIpForShorten = {
/*    if (Config.isApiAllowed(remoteIp)) {
      true
    } else {
      response.setStatus(FORBIDDEN)
      false
    }
*/
    true
  }

  private def invertImage(mtx: ByteMatrix) {
    for (w <- 0 until mtx.getWidth; h <- 0 until mtx.getHeight) {
      val inverted = if (mtx.get(w, h) == 0x00) 0xFF else 0x00
      mtx.set(w, h, inverted)
    }
  }
}
