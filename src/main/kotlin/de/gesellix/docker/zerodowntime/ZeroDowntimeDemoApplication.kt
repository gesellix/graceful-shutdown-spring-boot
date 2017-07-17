package de.gesellix.docker.zerodowntime

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.context.annotation.Bean
import java.util.concurrent.TimeUnit

@SpringBootApplication
class ZeroDowntimeDemoApplication {

    @Value("\${catalina.threadpool.execution.timeout.seconds}")
    var shutdownTimeoutSeconds: Long = 30

    @Bean
    fun gracefulShutdown(): GracefulShutdown {
        return GracefulShutdown(shutdownTimeoutSeconds, TimeUnit.SECONDS)
    }

    @Bean
    fun tomcatCustomizer(): EmbeddedServletContainerCustomizer {
        return EmbeddedServletContainerCustomizer { container ->
            if (container is TomcatEmbeddedServletContainerFactory) {
                container.addConnectorCustomizers(gracefulShutdown())
            }
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(ZeroDowntimeDemoApplication::class.java, *args)
}
