package ca.joelathiessen.kaly2.server

import org.eclipse.jetty.websocket.WebSocket
import org.eclipse.jetty.websocket.WebSocketFactory
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class KalyWebSocketServlet() : HttpServlet() {
    private val robotsManager by lazy { servletContext.getAttribute("robotsManager") as RobotsManager }

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        var rid: Long
        val numberStr = Regex("^[/][\\d]*$").find(request.contextPath)

        if (numberStr != null) {
            rid = numberStr.groupValues[0].substring(1).toLong()
        } else if (request.contextPath.length == 0) {
            rid = robotsManager.getUnspecifiedRid()
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Robot URI must be followed by a numeric ID or nothing")
            return
        }

        val webSocketFactory = WebSocketFactory(object : WebSocketFactory.Acceptor {
            override fun checkOrigin(request: HttpServletRequest, origin: String?): Boolean {
                return true
            }

            override fun doWebSocketConnect(request: HttpServletRequest, protocol: String?): WebSocket? {
                return KalyWebSocket(robotsManager, rid)
            }
        })

        if (webSocketFactory.acceptWebSocket(request, response)) {
            return
        }
        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                "WebSocket connections only")
    }
}