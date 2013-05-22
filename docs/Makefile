BASE =	index.html \
		BasicAuth.html \
		BodyParser.html \
		CookieParser.html \
		ErrorHandler.html \
		Favicon.html \
		Limit.html \
		MethodOverride.html \
		ResponseTime.html \
		Router.html \
		Static.html \
		Timeout.html \
		Vhost.html \
		Benchmark.html

EXTENSIONS =	YokeRequest.html \
				YokeResponse.html

TUTORIALS =	Java-Tutorial.html \
			Groovy-Tutorial.html \
			JavaScript-Tutorial.html \
			Persona.html

ENGINES =	StringPlaceholderEngine.html \
			GroovyTemplate.html \
			MicroTemplateEngine.html

%.html: %.md
	@pandoc $< -s --highlight-style tango -c style.css --toc --toc-depth=2 -A footer.txt -o $@
	@sed -i 's/<title><\/title>/<title>Yoke a middleware framework for Vert.x: $(patsubst %.html,%,$@)<\/title>/g' $@

all: $(BASE) $(TUTORIALS) $(EXTENSIONS)
	@tar -czvf yoke-gradle-template.tar.gz yoke-gradle-template

clean:
	@rm *.html yoke-gradle-template.tar.gz || true