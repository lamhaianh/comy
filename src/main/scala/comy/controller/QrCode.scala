package comy.controller

import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.ByteMatrix
import com.google.zxing.qrcode.QRCodeWriter

import s3m._

object QrCode {
  val WIDTH  = 150
  val HEIGHT = 150
}

class QrCode extends Controller {
  import QrCode._

  /** See: http://www.hascode.com/2010/05/playing-around-with-qr-codes/ */
  @GET
  @Path("/user/qrcode")
  def qrcode {  // ?url=xxx
    val url = param("url")

    val writer = new QRCodeWriter
    val mtx    = writer.encode(url, BarcodeFormat.QR_CODE, WIDTH, HEIGHT)
    invertImage(mtx)
    val image  = MatrixToImageWriter.toBufferedImage(mtx)

    val baos = new ByteArrayOutputStream
    ImageIO.write(image, "png", baos)

    response.setContentType("image/png")
    renderBinary(baos.toByteArray)
  }

  //----------------------------------------------------------------------------

  private def invertImage(mtx: ByteMatrix) {
    for (w <- 0 until mtx.getWidth; h <- 0 until mtx.getHeight) {
      val inverted = if (mtx.get(w, h) == 0x00) 0xFF else 0x00
      mtx.set(w, h, inverted)
    }
  }
}
