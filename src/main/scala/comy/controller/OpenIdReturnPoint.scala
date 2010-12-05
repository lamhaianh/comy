package comy.controller

import javax.servlet.{Filter, FilterChain, FilterConfig, ServletRequest, ServletResponse}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import comy.bean.User

class OpenIdReturnPoint extends Filter {
  def init(config: FilterConfig) {}

  def destroy {}

  def doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpRequest  = request.asInstanceOf[HttpServletRequest]
    val httpResponse = response.asInstanceOf[HttpServletResponse]

    chain.doFilter(request, response)
  }
}
