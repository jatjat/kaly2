package ca.joelathiessen.kaly2.server

import ca.joelathiessen.kaly2.persistence.PersistentStorage
import ca.joelathiessen.util.FloatMath
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import java.util.UUID
import javax.imageio.ImageIO

class KalyServer {

    private val robotSessionManager: RobotSessionManager

    val inprocessAPI: ApplicationAPI

    val SERVER_UUID = UUID.randomUUID()
    val PORT = 9000
    val WEBSOCKET_API_ROBOT_PATH = "/api/ws/robot/*"
    val REST_API_ROBOT_PATH = "/api/rest/robot"
    val ROOT_PATH = "/"

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            KalyServer().serve()
        }
    }

    init {
        robotSessionManager = RobotSessionManager(configureRealFactory(), configureSimFactory())
        inprocessAPI = ApplicationAPI(robotSessionManager)
    }

    fun serve() {
        val servletContextHandler = ServletContextHandler()
        servletContextHandler.addServlet(KalyWebSocketServlet::class.java, WEBSOCKET_API_ROBOT_PATH)
        servletContextHandler.addServlet(RestApiServlet::class.java, REST_API_ROBOT_PATH)
        servletContextHandler.addServlet(DefaultServlet::class.java, ROOT_PATH)
        servletContextHandler.setAttribute("robotSessionManager", robotSessionManager)

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

    fun configureRealFactory(): RobotSessionFactory {
        return configureSimFactory() // TODO: for now...
    }

    fun configureSimFactory(): RobotSessionFactory {
        val IMAGE_LOC = "/images/squareFIlled.png"
        val image = ImageIO.read(javaClass.getResource(IMAGE_LOC))

        val LINE_THRESHOLD = 10.0f
        val CHECK_WITHIN_ANGLE = 0.3f
        val MAX_RATIO = 1.0f

        val MAX_SENSOR_RANGE = 500.0f
        val SENSOR_DIST_STDEV = 0.01f
        val SENSOR_ANG_STDEV = 0.001f
        val SENSOR_START_ANG = 0.0f
        val SENSOR_END_ANG = 2 * FloatMath.PI
        val SENSOR_ANG_INCR = 0.0174533f

        val ODO_ANG_STD_DEV = 0.01f
        val ODO_DIST_STD_DEV = 0.01f
        val STEP_DIST = 2f

        val MIN_WIDTH = 400.0f

        val NN_ASSOC_THRESHOLD = 10.0f

        val OBS_SIZE = 2f
        val SEARCH_DIST = MIN_WIDTH
        val GBL_PTH_PLN_STEP_DIST = 20f
        val GBL_PTH_PLN_ITRS = 100

        val LCL_PLN_ROT_STEP = 0.017f
        val LCL_PLN_DIST_STEP = 1f
        val LCL_PLN_GRID_STEP = 5f
        val LCL_PLN_GRID_SIZE = 2 * MAX_SENSOR_RANGE
        val PILOT_MAX_ROT = 1f
        val PILOT_MAX_DIST = 20f

        val MAP_REMOVE_INVALID_OBS_INTERVAL = 10

        val MIN_MES_TIME = 160L

        val persistentStorage = PersistentStorage(SERVER_UUID, dbInit = PersistentStorage.DbInitTypes.FILE_DB,
                dropTablesFirst = true)
        return SimRobotSessionFactory(ODO_ANG_STD_DEV, ODO_DIST_STD_DEV, STEP_DIST, PILOT_MAX_DIST, PILOT_MAX_ROT,
                SENSOR_START_ANG, SENSOR_END_ANG, SENSOR_ANG_INCR, image, MAX_SENSOR_RANGE, SENSOR_DIST_STDEV,
                SENSOR_ANG_STDEV, LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO, LCL_PLN_ROT_STEP, LCL_PLN_DIST_STEP,
                LCL_PLN_GRID_STEP, LCL_PLN_GRID_SIZE, OBS_SIZE, SEARCH_DIST, GBL_PTH_PLN_STEP_DIST, GBL_PTH_PLN_ITRS,
                MAP_REMOVE_INVALID_OBS_INTERVAL, MIN_MES_TIME, NN_ASSOC_THRESHOLD, persistentStorage)
    }
}