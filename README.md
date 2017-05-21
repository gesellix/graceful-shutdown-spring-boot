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

### Basics

Clone (or [download](https://github.com/gesellix/graceful-shutdown-spring-boot/archive/master.zip)) this repository:

    git clone https://github.com/gesellix/graceful-shutdown-spring-boot
    cd graceful-shutdown-spring-boot

You'll need a Java Runtime Environment (JRE) to run the example without Docker.

### Without Docker (really?)

- run the app, e.g. via `./gradlew bootRun`
- perform a looooong download: `curl -X GET "http://localhost:8080/endless" > /dev/null`
- send `SIGTERM` to the Spring process (you can find the `<pid>` in the application logs): `kill <pid>`

### Docker Stack/Service

#### Some background information

Using Docker Stack or Docker Services, you'll need to be aware of the underlying mechanics of a rolling update.
 You can find a detailed discussion about that topic at [github.com/moby/moby/issues/30321](https://github.com/moby/moby/issues/30321).

In a Spring Boot world, you'll need to tweak the following settings:
 
- Your Spring Boot webapp needs to register a shutdown hook to pause/prevent the JVM shutdown.
  The example application shows how an implementation looks like in an Apache Tomcat environment.
  You can tweak the shutdown timeout with the example application property `catalina.threadpool.execution.timeout.seconds`.
- During a service update Docker will send the `TERM` signal to your container and wait for 10 seconds
  until Docker sends the `KILL` signal to finally stop your container. If you want your downloads to keep
  running longer than 10 seconds, you'll need to configure the `stop-grace-period` to match your needs.
  Since the Spring Boot app defaults to wait 30s, I chose 60 seconds as `services.app.stop_grace_period` in the `stack.yml`.
- Your replicated service tasks shouldn't be shutdown simultaneously. That's why the `stack.yml`
  configures the `services.app.deploy.update_config.parallelism` to be sequentially. Additionally,
  I configured the `services.app.deploy.update_config.delay` to be 60s.

#### Perform a zero downtime deployment

Now the exciting part: you may want to update your app to use a fresh image or add some environment parameter.
 Such updates would trigger a short downtime of your running containers, which effectively would stop your
 running download. Depending on your needs, that's what you actually want. But maybe you would like to
 give running downloads a chance to finish. The grace period needs to be configurable, because only you
 know how long a typical download should be kept running in your individual service.

Deploy a minimal stack with reverse proxy and two instances of the example app:

    docker swarm init # you may add more worker nodes, but that's not necessary for the demo.
    docker stack deploy -c stack.yml grace

We can now start the demo scenario by first starting a download and then trying to update the app service.
 Open your browser at [http://localhost](http://localhost/) and click _go for it_, or
 use your shell to perform an endless download: `curl -X GET "http://localhost:8080/endless" > /dev/null`.

You can watch your service logs with `docker service logs -f grace_app`. The browser will also show
 you the increasing number of downloaded bytes.

An update with task downtime can easily be triggered:

    docker service update --env-add "foo=bar" grace_app

Now keep your eye on the browser download "stats" and the service logs where you can see the Docker service
 update shutting down your tasks one by one. One of your tasks will be shut down instantly, because it won't be
 "locked" by your download. But the other task shutdown should be prevented to approximately 30 seconds.
 Only then the Spring Boot shutdown will continue and Docker won't enforce the shutdown through `SIGKILL`. 

Long story short: you should catch the `TERM` signal to prevent an instant process shutdown, Docker needs
 to wait long enough before actually killing your process, and you should configure the parallelism
 with a value smaller than your number of replicas.

## How does it work?

The `GracefulShutdown` class listens to application events of type `ContextClosedEvent`. It waits 30 seconds
 (or whatever you configured) for the Tomcat `ThreadPoolExecutor` to be shut down. Essentially,
 it blocks the JVM shutdown to wait for the Tomcat to be finished with pending requests.

Please note that you won't get any guarantees that the JVM will wait endlessly for your shutdown hook to return.
It might interrupt your shutdown hook without further notice. My JVM implementation on macOS and the one in an alpine
container seems to be patient enough :)
