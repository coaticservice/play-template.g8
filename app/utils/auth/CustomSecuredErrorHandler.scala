package utils.auth

import javax.inject.Inject

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import play.api.http.ContentTypes
import play.api.i18n.{ I18nSupport, Messages, MessagesApi }
import play.api.libs.json.Json
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent.Future

/**
 * Custom secured error handler.
 *
 * @param messagesApi The Play messages API.
 */
class CustomSecuredErrorHandler @Inject() (val messagesApi: MessagesApi)
  extends SecuredErrorHandler
  with I18nSupport
  with RequestExtractors
  with Rendering {

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(implicit request: RequestHeader) =
    produceResponse(Unauthorized, Messages("silhouette.not.authenticated"), true)

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(implicit request: RequestHeader) =
    produceResponse(Unauthorized, Messages("silhouette.access.denied"), true)

  protected def produceResponse[S <: Status](status: S, msg: String, authorized: Boolean)(
    implicit
    request: RequestHeader
  ): Future[Result] =
    Future.successful(render {
      case Accepts.Json() => status(toJsonError(msg))
      case _ =>  if(authorized) {
        Redirect(controllers.routes.SignInController.view()).flashing("error" -> Messages("access.denied"))
      } else {
        Redirect(controllers.routes.SignInController.view())
      }
    })

  protected def toJsonError(message: String) =
    Json.obj("message" -> message)
}