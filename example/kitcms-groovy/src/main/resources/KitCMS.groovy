import com.jetdrone.vertx.yoke.*

try {
    def yoke = new GYoke(vertx)

    def server = vertx.createHttpServer()
    yoke.listen(server)
    server.listen(8080)
} catch (e) {
    println e
}