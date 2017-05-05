# Graceful Shutdown with Spring Boot

In a modern world, we expect webapps to always be available.
Yet, deployments trigger downtimes, which is why we're talking about zero downtime.

Yeah, it's easy to put a proxy in front of your services and let it balance across multiple instances of your service.
Now you can take them down one at a time, and deploy your fancy new version.

But how would you handle long running requests which are directly connected to a specific instance?

This project is a showcase for an example of how to gracefully shut down a Spring Boot app.
The code is more or less a plain copy from https://github.com/spring-projects/spring-boot/issues/4657#issuecomment-161354811
with the only difference that I wanted to use Kotlin instead of standard Java.

## Usage

- run the app, e.g. via `./gradlew bootRun`
- perform a looooong download: `curl -X GET "http://localhost:8080/endless" > /dev/null`
- send `SIGTERM` to the Spring process (you can find the `<pid>` in the application logs): `kill <pid>`

## Expected Behaviour

The `GracefulShutdown` class listens to application events of type `ContextClosedEvent`. It waits 30 seconds
for the Tomcat `ThreadPoolExecutor` to be shut down. Essentially, it blocks the JVM shutdown to wait for the Tomcat
to be finished with pending requests.

Please note that you won't get any guarantees that the JVM will wait endlessly for your shutdown hook to return.
It might interrupt your shutdown hook without further notice. My JVM implementation on macOS seems to be patient enough :)
