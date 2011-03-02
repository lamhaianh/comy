package comy.action.user

import xitrum.action.Action

import comy.model.{DB, SaveUrlResult}

class Shorten extends Action {
  override def execute {
    val url = param("url").trim
    if (url.isEmpty) {
      jsRenderHtml("result", <p class="error">URL must not be empty</p>)
      return
    }

    val keyo = {
      val ret = param("key").trim
      if (ret.isEmpty) None else Some(ret)
    }

    val (resultCode, resultString) = DB.saveUrl(url, keyo)

    resultCode match {
      case SaveUrlResult.VALID =>
        val absoluteUrl = "http://localhost:8364/" + resultString
        jsRenderHtml("result",
          <div>
            <hr />
            <div>{absoluteUrl}</div>
            <a href={absoluteUrl} target="_blank"><img src={urlFor[QRCode]("url" -> absoluteUrl)} /></a>
          </div>
        )

      case SaveUrlResult.INVALID =>
        jsRenderHtml("result", <p class="error">{resultString}</p>)

      case SaveUrlResult.DUPLICATE =>
        jsRenderHtml("result", <p class="error">Key has been chosen</p>)

      case SaveUrlResult.ERROR =>
        jsRenderHtml("result", <p class="error">Server error</p>)
    }
  }
}
