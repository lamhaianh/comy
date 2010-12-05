package comy.controller

import java.io.Serializable
import javax.faces.bean.{ManagedBean, SessionScoped}
import scala.reflect.BeanInfo

@ManagedBean
@SessionScoped
@BeanInfo
class User extends Serializable {
  var loggedInUsername: String = _
  var inputUsername = ""

  def logout {
    loggedInUsername = null
  }

  def gotoCso = {
    loggedInUsername = inputUsername
    "/index"
  }
}
