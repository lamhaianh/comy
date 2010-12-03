package comy.controller

import scala.reflect.BeanProperty
import javax.faces.application.FacesMessage
import javax.faces.bean._
import javax.faces.context.FacesContext
import javax.servlet.http.HttpServletRequest

import comy.model.{DB, SaveUrlResult}

@ManagedBean
class Tool {
  @BeanProperty var url    = "http://mobion.jp/"
  @BeanProperty var key    = ""
  @BeanProperty var result = ""

  def shorten {
    key = key.trim
    val keyo = if (key.isEmpty) None else Some(key)

    val (resultCode, resultString) = DB.saveUrl(url, keyo)

    val message = resultCode match {
      case SaveUrlResult.VALID     => ""
      case SaveUrlResult.INVALID   => "Invalid key"
      case SaveUrlResult.DUPLICATE => "Duplicated key"
      case SaveUrlResult.ERROR     => "Server error"
    }

    val facesContext = FacesContext.getCurrentInstance
    if (!message.isEmpty)
      facesContext.addMessage(
        "form:key",
        new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null))

    result = if (resultCode != SaveUrlResult.VALID) {
      ""
    } else {
      val extContext = facesContext.getExternalContext
      val request    = extContext.getRequest.asInstanceOf[HttpServletRequest]
      val port = request.getServerPort
      if (port == 80) {
        request.getScheme + "://" + request.getServerName + "/" + request.getContextPath + resultString
      } else {
        request.getScheme + "://" + request.getServerName + ":" + port + "/" + request.getContextPath + resultString
      }
    }
  }
}
