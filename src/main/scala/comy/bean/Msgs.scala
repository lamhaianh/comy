package comy.bean

import java.text.MessageFormat
import java.util.{Locale, ResourceBundle}
import javax.faces.context.FacesContext

/** See LocaleHelper in the book JSF 2.0 Cookbook */
object Msgs {
  private val DEFAULT_BUNDLE = "msgs"

  def msgs(key: String, parameters: Any*): String = {
    val context = FacesContext.getCurrentInstance
    val bundle  = context.getApplication.getMessageBundle
    val bundle2 = if (bundle == null) DEFAULT_BUNDLE else bundle
    val locale  = context.getViewRoot.getLocale
    msgs(bundle2, locale, key, parameters: _*)
  }

  def msgs(bundle: String, locale: Locale, key: String, parameters: Any*): String = {
    val resourceBundle = ResourceBundle.getBundle(bundle, locale, getClassLoader(parameters))
    val message = try {
      resourceBundle.getString(key)
    } catch {
      case _ => ""
    }

    if (parameters.isEmpty) {
      message
    } else {
      val stringBuffer  = new StringBuffer
      val messageFormat = new MessageFormat(message, locale)
      messageFormat.format(parameters.toArray, stringBuffer, null).toString
    }
  }

  private def getClassLoader(defaultObject: AnyRef) = {
    val loader = Thread.currentThread.getContextClassLoader
    if (loader == null) defaultObject.getClass.getClassLoader else loader
  }
}
