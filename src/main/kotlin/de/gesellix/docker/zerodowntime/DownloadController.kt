package de.gesellix.docker.zerodowntime

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.stream.IntStream
import javax.servlet.http.HttpServletResponse


@CrossOrigin
@RestController
class CustomerController {

    @GetMapping("/echo/{text}")
    fun findByLastName(@PathVariable text: String)
            = "Hello $text!"

    @GetMapping("/endless")
    fun downloadEndlessly(servletResponse: HttpServletResponse) {
        val writer = servletResponse.writer

        writer.println("here we go...")
        servletResponse.flushBuffer()

        IntStream.iterate(0) { i -> i + 2 }
//                .filter { it % 1000000 == 0 }
                .forEach({
                    writer.println(it)
                })
    }
}
