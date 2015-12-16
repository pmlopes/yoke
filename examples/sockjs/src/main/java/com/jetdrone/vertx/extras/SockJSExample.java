package com.jetdrone.vertx.extras;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.middleware.ErrorHandler;
import com.jetdrone.vertx.yoke.middleware.Static;
import com.jetdrone.vertx.yoke.sockjs.*;
import io.vertx.core.AbstractVerticle;

/*
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class SockJSExample extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    final Yoke yoke = new Yoke(this);

    // Allow outbound traffic to the news-feed address

    BridgeOptions options = new BridgeOptions().addOutboundPermitted(new PermittedOptions().setAddress("news-feed"));

    // TODO: This MUST be /eventbus since the regex's at the yoke side are hardcoded
    yoke.use("/eventbus", new SockJS(vertx, new SockJSHandlerOptions()).bridge(options, event -> {

      // You can also optionally provide a handler like this which will be passed any events that occur on the bridge
      // You can use this for monitoring or logging, or to change the raw messages in-flight.
      // It can also be used for fine grained access control.

      if (event.type() == BridgeEventType.SOCKET_CREATED) {
        System.out.println("A socket was created");
      }

      // This signals that it's ok to process the event
      event.complete(true);

    }));

    yoke.use(new BodyParser());
    yoke.use(new ErrorHandler(true));
    yoke.use(new Static("static"));

    yoke.listen(8080);

    System.out.println("Yoke server listening on port 8080");

    // Publish a message to the address "news-feed" every second
    vertx.setPeriodic(1000, t -> vertx.eventBus().publish("news-feed", "news from the server!"));
  }
}
