package de.gesellix.docker.zerodowntime

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory
import org.springframework.context.annotation.Bean


@SpringBootApplication
class ZeroDowntimeDemoApplication {

    @Bean
    fun gracefulShutdown(): GracefulShutdown {
        return GracefulShutdown()
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
