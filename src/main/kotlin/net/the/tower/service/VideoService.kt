package net.the.tower.service

import io.quarkus.runtime.StartupEvent
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.bytedeco.javacv.Java2DFrameConverter
import org.bytedeco.javacv.OpenCVFrameGrabber
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.function.Supplier
import javax.imageio.ImageIO

@ApplicationScoped
class VideoService @Inject constructor(
    val blockingExecutorService: ScheduledExecutorService
) {
    companion object {
        val VIDEO_FRAME_HEADER = "--frame\r\nContent-Type: image/jpeg\r\n\r\n".toByteArray()
        val VIDEO_FRAME_FOOTER = "\r\n".toByteArray()
    }

    private val grabber = OpenCVFrameGrabber(0)
    val converter = Java2DFrameConverter()
    private var hotFrameStream: Multi<ByteArray>? = null


    fun init(@Observes event: StartupEvent) {
        //Start the webcam on startup
        grabber.start()
        hotFrameStream = Multi.createBy().repeating().uni(Supplier {
            Uni.createFrom().item { takeSingleFrame() }
        })
            .withDelay(Duration.ofMillis(5))
            .indefinitely()
            .toHotStream()
            .onCancellation().invoke { grabber.stop() }
        hotFrameStream?.subscribe()?.with {
            //No op to start the stream
        }
    }

    private fun takeSingleFrame(): ByteArray {
        val stream = ByteArrayOutputStream()
        stream.writeBytes(VIDEO_FRAME_HEADER)
        takePicture(stream)
        stream.writeBytes(VIDEO_FRAME_FOOTER)
        return stream.toByteArray()
    }

    fun streamImages(): Multi<ByteArray> {
        if (hotFrameStream != null) {
            return Multi.createBy().replaying().ofMulti(hotFrameStream)
        }
        return Multi.createFrom().empty()
    }


    fun takePicture(): ByteArray {
        val stream = ByteArrayOutputStream()
        takePicture(stream)
        return stream.toByteArray()
    }

    private fun takePicture(stream: OutputStream) {
        val frame = grabber.grab()
        val image = converter.convert(frame)
        ImageIO.write(image, "JPEG", stream)
    }

}