package ca.joelathiessen.kaly2.server

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler

class KalyServer {
    companion object {
        val PORT = 8080
        val WEBSOCKET_API_ROBOT_PATH = "/api/ws/robot/*"
        val REST_API_ROBOT_PATH = "/api/rest/robot"
        val ROOT_PATH = "/"

        @JvmStatic fun main(args: Array<String>) {
            val servletContextHandler = ServletContextHandler()
            servletContextHandler.addServlet(KalyWebSocketServlet::class.java, WEBSOCKET_API_ROBOT_PATH)
            servletContextHandler.addServlet(RestApiServlet::class.java, REST_API_ROBOT_PATH)
            servletContextHandler.addServlet(DefaultServlet::class.java, ROOT_PATH)

            val contexts = HandlerList()
            contexts.handlers = arrayOf(servletContextHandler)

            while (true) {
                try {
                    val webserver = Server(PORT)
                    webserver.handler = contexts
                    webserver.start()
                    webserver.join()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
        }
    }
}