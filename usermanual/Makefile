DOC=usermanual
BIB=usermanual
PDFLATEX=pdflatex
BIBTEX=bibtex
GRAPHICSIDR=graphics

all: paper
#all: images paper

paper:  $(DOC).tex $(BIB).bib 
	$(PDFLATEX) $(DOC).tex
	$(BIBTEX) $(DOC)
	$(PDFLATEX) $(DOC).tex
	$(PDFLATEX) $(DOC).tex
	$(BIBTEX) $(DOC)

#images: 
#	cd $(GRAPHICSIDR) && make

check:
	$(TEXIDATE) $(DOC).tex

wc:
	@echo "Paper has:" `pdftotext $(DOC).pdf - | wc -w 2> /dev/null` "words!"

clean:	
	-rm $(DOC).log $(DOC).aux $(DOC).blg $(DOC).bbl $(DOC).dvi $(DOC).ps $(DOC).out $(DOC).toc $(DOC).lof $(DOC).lot 
#	cd $(GRAPHICSIDR) && make clean

hyperclean: clean
	-rm *~ *.backup
