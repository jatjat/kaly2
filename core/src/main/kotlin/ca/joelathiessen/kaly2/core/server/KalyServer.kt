package ca.joelathiessen.kaly2.core.server

import ca.joelathiessen.kaly2.core.persistence.PersistentStorage
import ca.joelathiessen.kaly2.core.ev3.SerialConnectionCreator
import ca.joelathiessen.util.FloatMath
import ca.joelathiessen.util.image.AndroidJVMImage
import ca.joelathiessen.util.image.jvm.JVMImage
import ca.joelathiessen.util.isAndroid
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.handler.HandlerList
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.util.log.Log
import org.eclipse.jetty.util.log.StdErrLog
import java.net.InetAddress
import kotlin.concurrent.thread

class KalyServer(
    mapImage: AndroidJVMImage? = null,
    robotSerialConnectionCreator: SerialConnectionCreator? = null,
    sensorSerialConnectionCreator: SerialConnectionCreator? = null
) {

    private val robotSessionManager: RobotSessionManager

    val inprocessAPI: ApplicationAPI

    val PORT = 9000
    val WEBSOCKET_API_ROBOT_PATH = "/api/ws/robot"
    val REST_API_ROBOT_PATH = "/api/rest/robot"
    val ROOT_PATH = "/"
    val ERR_SLEEP = 5000L

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            KalyServer().serve()
        }
    }

    init {
        var realRobotSessionFactory: RealRobotSessionFactory? = null
        if (robotSerialConnectionCreator != null && sensorSerialConnectionCreator != null) {
            realRobotSessionFactory = configureRealFactory(robotSerialConnectionCreator, sensorSerialConnectionCreator)
        }
        robotSessionManager = RobotSessionManager(realRobotSessionFactory, configureSimFactory(mapImage))
        inprocessAPI = ApplicationAPI(robotSessionManager)
    }

    fun serve() {
        thread (name = "KalyServerWrapper") {
            val logger = StdErrLog()
            logger.level = StdErrLog.LEVEL_DEBUG
            Log.setLog(logger)

            val servletContextHandler = ServletContextHandler()
            servletContextHandler.setAttribute("robotSessionManager", robotSessionManager)
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
                    webserver.dump(System.err)
                    webserver.join()
                } catch (e: Throwable) {
                    e.printStackTrace(System.err)
                    Thread.sleep(ERR_SLEEP)
                }
            }
        }
    }

    fun configureSimFactory(mapImage: AndroidJVMImage?): SimRobotSessionFactory {
        val IMAGE_LOC = "/images/squareFIlled.png"
        val image: AndroidJVMImage = mapImage ?: JVMImage(IMAGE_LOC)

        val LINE_THRESHOLD = 10.0f
        val CHECK_WITHIN_ANGLE = 0.3f
        val MAX_RATIO = 1.0f

        val MAX_SENSOR_RANGE = 500.0f
        val SENSOR_DIST_STDEV = 0.01f
        val SENSOR_ANG_STDEV = 0.001f
        val SENSOR_START_ANG = 0.0f
        val SENSOR_END_ANG = 2 * FloatMath.PI
        val SENSOR_ANG_INCR = 0.0174533f

        val DEFAULT_NUM_PARTICLES = 20
        val DEFAULT_DIST_VARIANCE = 1.0f
        val DEFAULT_ANG_VARIANCE = 0.01f
        val IDENTITY_VARIANCE = 0.2f

        val ODO_ANG_STD_DEV = 0.01f
        val ODO_DIST_STD_DEV = 0.01f
        val STEP_DIST = 2f

        val MAX_ROT = 1.0f
        val MAX_SPEED = 1.0f

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

        val MAP_REMOVE_INVALID_OBS_INTERVAL = 10

        val MIN_MES_TIME = 160L

        val DB_INIT = if (isAndroid()) PersistentStorage.DbInitTypes.ANDROID_FILE_DB
        else PersistentStorage.DbInitTypes.FILE_DB

        val uniqueServerName = if (isAndroid()) "Android" else ({
            val ipAddress = byteArrayOf(127, 0, 0, 1)
            val address = InetAddress.getByAddress(ipAddress)

            address.canonicalHostName
        }())

        val persistentStorage = PersistentStorage(uniqueServerName, dbInit = DB_INIT, dropTablesFirst = true)
        return SimRobotSessionFactory(ODO_ANG_STD_DEV, ODO_DIST_STD_DEV, STEP_DIST,
                MAX_ROT, MAX_SPEED, SENSOR_START_ANG, SENSOR_END_ANG, SENSOR_ANG_INCR, DEFAULT_NUM_PARTICLES, DEFAULT_DIST_VARIANCE,
                DEFAULT_ANG_VARIANCE, IDENTITY_VARIANCE, image, MAX_SENSOR_RANGE, SENSOR_DIST_STDEV,
                SENSOR_ANG_STDEV, LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO, LCL_PLN_ROT_STEP, LCL_PLN_DIST_STEP,
                LCL_PLN_GRID_STEP, LCL_PLN_GRID_SIZE, OBS_SIZE, SEARCH_DIST, GBL_PTH_PLN_STEP_DIST, GBL_PTH_PLN_ITRS,
                MAP_REMOVE_INVALID_OBS_INTERVAL, MIN_MES_TIME, NN_ASSOC_THRESHOLD, persistentStorage)
    }

    fun configureRealFactory(robotConnectionCreator: SerialConnectionCreator, sensorConnectionCreator: SerialConnectionCreator): RealRobotSessionFactory {
        val WHEEL_RADIUS = 10.0f
        val MAX_DRIVE_ANGLE = 1f
        val MAX_SPEED_RADS_PER_SECOND = 2.0f
        val MAX_ROT = 1.0f
        val DIST_TO_SPEED_CONV = 10.0f
        val ROBOT_SIZE = 5.0f

        val LINE_THRESHOLD = 10.0f
        val CHECK_WITHIN_ANGLE = 0.3f
        val MAX_RATIO = 1.0f

        val MAX_SENSOR_RANGE = 500.0f

        val DEFAULT_NUM_PARTICLES = 20
        val DEFAULT_DIST_VARIANCE = 1.0f
        val DEFAULT_ANG_VARIANCE = 0.01f
        val IDENTITY_VARIANCE = 0.2f

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

        val MAP_REMOVE_INVALID_OBS_INTERVAL = 10

        val MIN_MES_TIME = 160L

        val DB_INIT = if (isAndroid()) PersistentStorage.DbInitTypes.ANDROID_FILE_DB
        else PersistentStorage.DbInitTypes.FILE_DB

        val uniqueServerName = if (isAndroid()) "Android" else ({
            val ipAddress = byteArrayOf(127, 0, 0, 1)
            val address = InetAddress.getByAddress(ipAddress)

            address.canonicalHostName
        }())

        val persistentStorage = PersistentStorage(uniqueServerName, dbInit = DB_INIT, dropTablesFirst = true)
        return RealRobotSessionFactory(WHEEL_RADIUS, MAX_DRIVE_ANGLE, MAX_SPEED_RADS_PER_SECOND, ROBOT_SIZE,
                MAX_ROT, DIST_TO_SPEED_CONV, DEFAULT_NUM_PARTICLES, DEFAULT_DIST_VARIANCE, DEFAULT_ANG_VARIANCE,
                IDENTITY_VARIANCE, LINE_THRESHOLD, CHECK_WITHIN_ANGLE, MAX_RATIO, LCL_PLN_ROT_STEP, LCL_PLN_DIST_STEP,
                LCL_PLN_GRID_STEP, LCL_PLN_GRID_SIZE, OBS_SIZE, SEARCH_DIST, GBL_PTH_PLN_STEP_DIST, GBL_PTH_PLN_ITRS,
                MAP_REMOVE_INVALID_OBS_INTERVAL, MIN_MES_TIME, NN_ASSOC_THRESHOLD, persistentStorage,
                robotConnectionCreator, sensorConnectionCreator)
    }
}
