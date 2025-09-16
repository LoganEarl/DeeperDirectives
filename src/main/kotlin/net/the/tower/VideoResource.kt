package net.the.tower

import io.smallrye.mutiny.Multi
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.jboss.resteasy.reactive.RestStreamElementType

@Path("/api")
class VideoResource @Inject constructor(
   val videoService: VideoService
){
    @Path("/stream/cam0")
    @Produces("multipart/x-mixed-replace;boundary=--frame")
    @RestStreamElementType(MediaType.APPLICATION_OCTET_STREAM)
    fun stream(): Multi<ByteArray> {
        return videoService.streamImages()
    }

    @GET
    @Path("/picture")
    @Produces("image/jpeg")
    fun takePicture() : ByteArray {
        return videoService.takePicture()
    }


}