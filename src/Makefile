FILES =	index.html \
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
		Vhost.html

%.html: %.md
	@pandoc $< -s --highlight-style tango -c style.css --toc -o ../$@
	@sed -i 's/<title><\/title>/<title>Yoke: $(patsubst %.html,%,$@)<\/title>/g' ../$@

all: $(FILES)