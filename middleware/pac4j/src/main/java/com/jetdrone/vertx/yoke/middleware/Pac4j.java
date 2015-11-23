package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.jetbrains.annotations.NotNull;
import org.pac4j.core.client.BaseClient;
import org.pac4j.core.client.RedirectAction;
import org.pac4j.core.context.HttpConstants;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.exception.RequiresHttpAction;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.vertx.VertxWebContext;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class Pac4j extends Middleware {

    private final BaseClient<? extends Credentials, ? extends CommonProfile> client;
    private final Boolean isAjax;

    private static String errorPage401 = "authentication required";
    private static String errorPage403 = "forbidden";

    public Pac4j(BaseClient client, boolean isAjax) {
        this.client = client;
        this.isAjax = isAjax;
    }

    public Pac4j(BaseClient<? extends Credentials, ? extends CommonProfile> client) {
        this(client, false);
    }

    @Override
    public void handle(@NotNull YokeRequest request, @NotNull Handler<Object> next) {
        // load
        JsonObject session = request.get("session");

        if (session != null) {
            next.handle(null);
        } else {
            session = request.createSession();

            WebContext webContext = new VertxWebContext(request);
            // requested url to save
            final String requestedUrlToSave = webContext.getFullRequestURL();

            // this gets saved in the end of the request
            session.put("pac4jRequestedUrl", requestedUrlToSave);

            try {
                RedirectAction action = client.getRedirectAction(webContext, true, isAjax);
                switch (action.getType()) {
                    case REDIRECT:
                        request.response().redirect(HttpConstants.TEMP_REDIRECT, action.getLocation());
                        break;
                    case SUCCESS:
                        request.response().setContentType("text/html", "utf-8");
                        request.response().end(action.getContent());
                        break;
                    default:
                        next.handle("Invalid redirect action type");
                }
            } catch (final RequiresHttpAction e) {
                // requires some specific HTTP action
                final int code = e.getCode();

                if (code == HttpConstants.UNAUTHORIZED) {
                    request.response().setStatusCode(HttpConstants.UNAUTHORIZED);
                    request.response().end(errorPage401);
                } else if (code == HttpConstants.FORBIDDEN) {
                    request.response().setStatusCode(HttpConstants.FORBIDDEN);
                    request.response().end(errorPage403);
                } else {
                    next.handle("Unsupported HTTP action : " + code);
                }
            }
        }
    }
}
