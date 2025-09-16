package net.the.tower

import io.quarkus.runtime.StartupEvent
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import net.the.tower.config.StaticResourceConfig
import org.jboss.logging.Logger
import java.io.File

@ApplicationScoped
class StaticResource @Inject constructor(
    val config: StaticResourceConfig
) {
    private val log = Logger.getLogger(StaticResource::class.java)

    fun installRoute(@Observes event: StartupEvent, router: Router) {
        if (config.path().startsWith("classpath:")) {
            // Using UI bundled with the JAR. Serve that up.
            val classpathDirectory = config.path().substring("classpath:".length)
            router.route().path("/*").handler(StaticHandler.create(classpathDirectory))
            log.info("Serving up UI bundled with the application")
        } else {
            val externalDirectory = File(config.path())
            if (externalDirectory.exists() && externalDirectory.isDirectory) {
                router.route().path("/*").handler(StaticHandler.create(config.path()))
                log.info("Serving up UI in external path ${config.path()}")
            } else {
                router.route().path("/*").handler(StaticHandler.create("META-INF/resources"))
                log.warn("Serving up default UI instead of configured one! The configured directory did not exist!")
            }
        }
    }
}