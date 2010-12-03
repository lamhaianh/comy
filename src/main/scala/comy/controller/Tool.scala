package comy.controller

import javax.faces.bean._
import scala.reflect.BeanProperty

@ManagedBean
class Tool {
  @BeanProperty var url    = "http://mobion.jp/"
  @BeanProperty var key    = ""
  @BeanProperty var result = ""

  def shorten {
    println("hi")
  }
}
