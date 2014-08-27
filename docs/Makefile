BASE =	index.html \
		Benchmark.html

TUTORIALS =	Java-Tutorial.html \
			Groovy-Tutorial.html \
			JavaScript-Tutorial.html \
			Persona.html

EXTRAS =	Rest.html

%.html: %.md
# First escape < and >
	@cp $< /tmp/escaped.md
	@sed -i 's/\&/\&amp;/g' /tmp/escaped.md
	@sed -i 's/</\&lt;/g' /tmp/escaped.md
	@sed -i 's/>/\&gt;/g' /tmp/escaped.md
# Replace into template
	@echo '/MARKDOWN_FILE/ {\nr /tmp/escaped.md\nd }' > /tmp/script.sed
	@sed -f /tmp/script.sed < template/template.html > $@
# adjust title
	@sed -i 's/<title><\/title>/<title>Yoke a middleware framework for Vert.x: $(patsubst %.html,%,$@)<\/title>/g' $@

all: $(BASE) $(TUTORIALS) $(EXTRAS)
    @mkdir -p html
	@mv *.html html

clean:
	@rm *.html || true