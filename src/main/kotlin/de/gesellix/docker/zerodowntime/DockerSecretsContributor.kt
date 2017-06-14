package de.gesellix.docker.zerodowntime

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


@Component
class DockerSecretsContributor : InfoContributor {

    override fun contribute(builder: Info.Builder) {
        val secretsBasePath = Paths.get("/run/secrets")
        if (!secretsBasePath.toFile().isDirectory) {
            builder.withDetail("secrets", "base directory not found")
        } else {
            val secrets = hashMapOf<String, String>()
            Files.walk(secretsBasePath)
                    .filter { it.toFile().isFile }
                    .forEach { path -> secrets.put(path.toFile().path, readFile(path)) }
            builder.withDetail("secrets", secrets.keys)

//            if (debug//sensitive)
            secrets.forEach { k, v -> builder.withDetail(k, v) }
        }
    }

    fun readFile(path: Path): String {
        try {
            // maybe we should append a trailing line separator?
            return path.toFile().readLines()
                    .fold(arrayListOf<String>()) { acc, line -> acc.add(line); acc }
                    .joinToString("\n")
        } catch (e: IOException) {
            System.err.println("Couldn't read $path")
            return ""
        }
    }
}
