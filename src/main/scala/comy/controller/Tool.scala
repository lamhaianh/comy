package comy.controller

import javax.faces.application.FacesMessage
import javax.faces.bean.ManagedBean
import javax.faces.context.FacesContext
import javax.servlet.http.HttpServletRequest
import scala.reflect.BeanInfo

import comy.model.{DB, SaveUrlResult}

@ManagedBean
@BeanInfo
class Tool {
  import Msgs._

  var url    = "http://mobion.jp/"
  var key    = ""
  var result = ""

  def shorten {
    key = key.trim
    val keyo = if (key.isEmpty) None else Some(key)

    val (resultCode, resultString) = DB.saveUrl(url, keyo)
    val message = resultCode match {
      case SaveUrlResult.VALID      => ""
      case SaveUrlResult.INVALID    => msgs("key_invalid")
      case SaveUrlResult.DUPLICATED => msgs("key_duplicated")
      case SaveUrlResult.ERROR      => msgs("server_error")
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
      // request.getContextPath returns "" or "/xxx"
      if (port == 80) {
        request.getScheme + "://" + request.getServerName + request.getContextPath + "/" + resultString
      } else {
        request.getScheme + "://" + request.getServerName + ":" + port + request.getContextPath + "/" + resultString
      }
    }
  }
}