package comy.controller

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.ByteMatrix
import com.google.zxing.qrcode.QRCodeWriter

import org.scalatra._

object QrCode {
  val WIDTH  = 150
  val HEIGHT = 150
}

/** See ScalatraServlet vs. ScalatraFilter */
class QrCode extends ScalatraFilter {
  import QrCode._

  /** See: http://www.hascode.com/2010/05/playing-around-with-qr-codes/ */
  get("/user/qrcode") {  // ?url=xxx
    val url = params("url")

    val writer = new QRCodeWriter
    val mtx    = writer.encode(url, BarcodeFormat.QR_CODE, WIDTH, HEIGHT)
    invertImage(mtx)
    val image  = MatrixToImageWriter.toBufferedImage(mtx)

    val baos = new ByteArrayOutputStream
    ImageIO.write(image, "png", baos)

    contentType = "image/png"
    baos.toByteArray
  }

  //----------------------------------------------------------------------------

  private def invertImage(mtx: ByteMatrix) {
    for (w <- 0 until mtx.getWidth; h <- 0 until mtx.getHeight) {
      val inverted = if (mtx.get(w, h) == 0x00) 0xFF else 0x00
      mtx.set(w, h, inverted)
    }
  }
}
