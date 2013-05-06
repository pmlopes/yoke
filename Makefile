FILES = index.html BasicAuth.html BodyParser.html

all: html

%.html: %.md
	pandoc $< -s --highlight-style tango -c style.css --toc -o $@
	sed -i 's/<title><\/title>/<title>Yoke: $(patsubst %.html,%,$@)<\/title>/g' $@

html: $(FILES)

clean:
	rm *.html