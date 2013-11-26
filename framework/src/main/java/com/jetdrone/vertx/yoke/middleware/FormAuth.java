package com.jetdrone.vertx.yoke.middleware;

import com.jetdrone.vertx.yoke.Middleware;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.logging.Logger;

public class FormAuth extends Middleware {

    private final AuthHandler authHandler;

    public FormAuth() {
        // allow configure paths /login, /logout
        authHandler = null;
    }

    @Override
    public void handle(YokeRequest request, Handler<Object> next) {
        // if method GET and url == /login
        //  render login

        // if method POST and url == /login
        authHandler.handle(request.getFormParameter("username"), request.getFormParameter("password"), new Handler<Boolean>() {
            @Override
            public void handle(Boolean success) {
//                if (user) {
//                    req.session.regenerate(function () {
//
//                        req.session.user = user;
//                        req.session.success = 'Authenticated as ' + user.username + ' click to <a href="/logout">logout</a>. ' + ' You may now access <a href="/restricted">/restricted</a>.';
//                        res.redirect('/');
//                    });
//                } else {
//                    req.session.error = 'Authentication failed, please check your ' + ' username and password.';
//                    res.redirect('/login');
//                }
            }
        });

        // if method GET and url == /logout
//        req.session.destroy(function () {
//            res.redirect('/');
//        });
    }

    public static class RequiredAuth extends Middleware {
        @Override
        public void handle(YokeRequest request, Handler<Object> next) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public static class UserExists extends Middleware {
        @Override
        public void handle(YokeRequest request, Handler<Object> next) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
