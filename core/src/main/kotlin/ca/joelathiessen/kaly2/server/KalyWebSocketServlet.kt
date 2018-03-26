package ca.joelathiessen.kaly2.server

import org.eclipse.jetty.websocket.WebSocket
import org.eclipse.jetty.websocket.WebSocketFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.ServletException

class KalyWebSocketServlet() : HttpServlet() {
    private lateinit var robotSessionManager: RobotSessionManager

    @Throws(ServletException::class)
    override fun init() {
        robotSessionManager = servletContext.getAttribute("robotSessionManager") as RobotSessionManager
    }

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