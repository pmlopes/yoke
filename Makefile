PROJECT=Yoke
FILES = index.html

all: html

%.html: %.md
	pandoc $< -s --highlight-style tango -c style.css --toc -o $@
	sed -i 's/<title><\/title>/<title>Yoke: $(patsubst %.html,%,$@)<\/title>/g' $@

html: $(FILES)