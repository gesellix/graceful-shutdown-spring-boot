package de.gesellix.docker.zerodowntime

import org.apache.catalina.connector.Connector
import org.apache.tomcat.util.threads.ThreadPoolExecutor
import org.slf4j.LoggerFactory
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextClosedEvent
import java.util.concurrent.TimeUnit


class GracefulShutdown(val shutdownTimeout: Long, val unit: TimeUnit) : TomcatConnectorCustomizer, ApplicationListener<ContextClosedEvent> {

    @Volatile private var connector: Connector? = null

    override fun customize(connector: Connector) {
        this.connector = connector
    }

    override fun onApplicationEvent(event: ContextClosedEvent) {
        this.connector!!.pause()
        val executor = this.connector!!.protocolHandler.executor
        if (executor is ThreadPoolExecutor) {
            try {
                val threadPoolExecutor = executor
                threadPoolExecutor.shutdown()
                if (!threadPoolExecutor.awaitTermination(shutdownTimeout, unit)) {
                    log.warn("Tomcat thread pool did not shut down gracefully within $shutdownTimeout $unit. Proceeding with forceful shutdown")
                }
            } catch (ex: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GracefulShutdown::class.java)
    }
}
