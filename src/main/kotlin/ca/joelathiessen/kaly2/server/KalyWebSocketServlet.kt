package ca.joelathiessen.kaly2.server

import org.eclipse.jetty.websocket.WebSocket
import org.eclipse.jetty.websocket.WebSocketFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class KalyWebSocketServlet : HttpServlet() {
    private val webSocketFactory: WebSocketFactory

    init {
        webSocketFactory = WebSocketFactory(object : WebSocketFactory.Acceptor {
            override fun checkOrigin(request: HttpServletRequest, origin: String?): Boolean {
                return true
            }

            override fun doWebSocketConnect(request: HttpServletRequest, protocol: String?): WebSocket? {
                return KalyWebSocket()
            }
        })
    }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {

        if(request.contextPath.matches(Regex("^[/][\\d]*$")))
        {
            if (webSocketFactory.acceptWebSocket(request, response)) {
                return
            }
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "WebSocket connections only")
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Robot URI must be followed by a numeric ID or nothing")
        }
    }
}