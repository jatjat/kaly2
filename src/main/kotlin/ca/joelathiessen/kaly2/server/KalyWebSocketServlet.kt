package ca.joelathiessen.kaly2.server

import org.eclipse.jetty.websocket.WebSocket
import org.eclipse.jetty.websocket.WebSocketFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class KalyWebSocketServlet() : HttpServlet() {
    private val robotSessionManager by lazy { servletContext.getAttribute("robotSessionManager") as RobotSessionManager }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {

        val webSocketFactory = WebSocketFactory(object : WebSocketFactory.Acceptor {
            override fun checkOrigin(request: HttpServletRequest, origin: String?): Boolean {
                return true
            }

            override fun doWebSocketConnect(request: HttpServletRequest, protocol: String?): WebSocket? {
                return KalyWebSocket(robotSessionManager)
            }
        })

        if (webSocketFactory.acceptWebSocket(request, response)) {
            return
        }
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
            "WebSocket connections only")
    }
}