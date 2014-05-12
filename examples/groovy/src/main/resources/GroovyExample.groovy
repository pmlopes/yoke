import com.jetdrone.vertx.yoke.middleware.*
import com.jetdrone.vertx.yoke.GYoke
import com.jetdrone.vertx.yoke.engine.GroovyTemplateEngine
import com.jetdrone.vertx.yoke.extras.engine.*
import com.jetdrone.vertx.yoke.engine.HandlebarsEngine
import com.jetdrone.vertx.yoke.engine.Jade4JEngine

def webServerHost = '192.168.1.6';

new GYoke(vertx, container)
  .engine('html', new GroovyTemplateEngine()) // No signature of method: com.jetdrone.vertx.yoke.GYoke.engine() is applicable for argument types:(java.lang.String, com.jetdrone.vertx.yoke.engine.GroovyTemplateEngine) values: [html, com.jetdr
  .engine('hdb', new HandlebarsEngine()) // ..
  .engine('jade', new Jade4JEngine())
  .use(new ErrorHandler(true))
  .use("/content", new Static("content"))
  .use(new GRouter()
    .get("/hello") { request ->
        request.response.end "Hello World!"
    }
    .get("/template") { request, next ->
        request.put('session', [id: '1'])
        request.response.render 'template.html', next
    }
	.get("/templateWithLayout") { request, next ->		
		request.put('session', [id: '1'])
		request.put('engineName', 'Groovy')
		request.response.render 'bodyOnly.html','layout.html', next
	}
	.get("/handleBarsTemplateWithLayout") { request, next ->
		request.put('engineName', 'Handlebars')
		request.response.render 'bodyOnly.hdb','layout.hdb', next
	}
	.get("/jadeTemplateWithLayout") { request, next ->		
		request.put('engineName', 'Jade4J')
		request.put('fullName', 'Napoleon Bonaparte')
		request.response.render 'bodyOnly.jade','layout.jade', next
	}
	)
  .use {request ->
    request.response.end "This is the default route: maybe you should go to /hello or /template!"
  }.listen(8086,webServerHost)
