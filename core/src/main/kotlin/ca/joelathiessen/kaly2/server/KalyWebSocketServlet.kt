package ca.joelathiessen.kaly2.server

import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest
import org.eclipse.jetty.websocket.servlet.WebSocketCreator


class CustomWebSocketCreator(private val robotSessionManager: RobotSessionManager) : WebSocketCreator {

    override fun createWebSocket(req: ServletUpgradeRequest, resp: ServletUpgradeResponse): Any {
        return KalyWebSocket(robotSessionManager)
    }
}


class KalyWebSocketServlet : WebSocketServlet() {
    override fun configure(factory: WebSocketServletFactory?) {
        factory!!.creator = CustomWebSocketCreator(robotSessionManager)
        factory.register(KalyWebSocket::class.java)
    }

    private val robotSessionManager by lazy { servletContext.getAttribute("robotSessionManager") as RobotSessionManager }
}
