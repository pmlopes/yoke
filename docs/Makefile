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

EXTRAS =	Rest.html

%.html: %.md
# First escape < and >
	@cp $< /tmp/escaped.md
	@sed -i 's/</\&lt;/g' /tmp/escaped.md
	@sed -i 's/>/\&gt;/g' /tmp/escaped.md
# Replace into template
	@echo '/MARKDOWN_FILE/ {\nr /tmp/escaped.md\nd }' > /tmp/script.sed
	@sed -f /tmp/script.sed < template/template.html > $@
# adjust title
	@sed -i 's/<title><\/title>/<title>Yoke a middleware framework for Vert.x: $(patsubst %.html,%,$@)<\/title>/g' $@

all: $(BASE) $(TUTORIALS) $(EXTENSIONS) $(EXTRAS)
	@tar -czvf yoke-gradle-template.tar.gz yoke-gradle-template

clean:
	@rm *.html yoke-gradle-template.tar.gz || true