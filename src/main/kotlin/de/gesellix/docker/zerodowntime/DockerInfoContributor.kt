package de.gesellix.docker.zerodowntime

import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

@Component
class DockerInfoContributor : InfoContributor {

    internal val dockerClient: DockerClient = DockerClientImpl()

    override fun contribute(builder: Info.Builder) {
        try {
            if (!dockerClient.ping()?.status?.success!!) {
                builder.withDetail("docker", "not available (ping failed)")
            } else {
                val info = dockerClient.info()?.content
                builder.withDetail("docker", info)
            }
        } catch (e: Exception) {
            builder.withDetail("docker", "not available (connection failed)")
        }
    }
}
