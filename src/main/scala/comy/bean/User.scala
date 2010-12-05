package comy.bean

import java.io.Serializable
import javax.faces.bean.{ManagedBean, SessionScoped}
import javax.faces.context.FacesContext
import javax.servlet.http.HttpSession
import scala.reflect.BeanInfo

@ManagedBean
@SessionScoped
@BeanInfo
class User extends Serializable {
  var loggedInUsername: String = null
  var inputUsername   : String = null

  def logout = {
    val session = FacesContext.getCurrentInstance.getExternalContext.getSession(false).asInstanceOf[HttpSession]
    session.invalidate
    "/user/index?faces-redirect=true"
  }

  def gotoCso = {
    loggedInUsername = inputUsername
    "/user/index?faces-redirect=true"
  }
}
