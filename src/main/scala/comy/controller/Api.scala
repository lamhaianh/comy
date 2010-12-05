package comy.controller

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.ByteMatrix
import com.google.zxing.qrcode.QRCodeWriter

import comy.Config
import comy.model.{DB, SaveUrlResult}

import org.scalatra._

object Api {
  val QR_CODE_WIDTH  = 150
  val QR_CODE_HEIGHT = 150
}

/** See ScalatraServlet vs. ScalatraFilter */
class Api extends ScalatraFilter {
  import Api._

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
      case SaveUrlResult.VALID     => 200
      case SaveUrlResult.INVALID   => 400
      case SaveUrlResult.DUPLICATE => 409
      case SaveUrlResult.ERROR     => 500
    }

    response.setStatus(status)
    if (status == 200) response.getWriter.print(resultString)
  }

  /** See: http://www.hascode.com/2010/05/playing-around-with-qr-codes/ */
  get("/api/qrcode") {  // ?url=xxx
    checkIpForShorten

    val url = params("url")

    val writer = new QRCodeWriter
    val mtx    = writer.encode(url, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT)
    invertImage(mtx)
    val image  = MatrixToImageWriter.toBufferedImage(mtx)

    val baos = new ByteArrayOutputStream
    ImageIO.write(image, "png", baos)

    contentType = "image/png"
    baos.toByteArray
  }

  //----------------------------------------------------------------------------

  protected def checkIpForShorten {
    val remoteIp = request.getRemoteAddr
    if (!Config.isApiAllowed(remoteIp)) halt(403)
  }

  private def invertImage(mtx: ByteMatrix) {
    for (w <- 0 until mtx.getWidth; h <- 0 until mtx.getHeight) {
      val inverted = if (mtx.get(w, h) == 0x00) 0xFF else 0x00
      mtx.set(w, h, inverted)
    }
  }
}
