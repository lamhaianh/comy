package comy.controller

import javax.faces.bean._
import scala.reflect.BeanProperty

@ManagedBean
class Login {
  @BeanProperty
  var username = ""

  def gotoCso {
    println("hi")
  }
}
