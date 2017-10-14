JFLEX		= /bin/jflex
JAVA		= /bin/java
JAVAC		= /bin/javac
SOURCEDIR	= "src/"
CP  		= "src/main/java"

all: test

test: L.output
	@(diff L.output L.answer && echo "Test succeeded!") || echo "Test failed!"

L.output: compile
	java -cp $(CP) LLexer L.test > L.output

compile: dependencies
	$(JFLEX) src/main/jflex/L.flex -d $(CP)
	$(JAVAC) -cp $(CP) -source 8 $(CP)/LLexer.java

dependencies: 
	[ -f "${JFLEX}" ] || (echo "JFlex not found. Installing..." && sudo apt-get install jflex)
	[ -f "${JAVAC}" ] || (echo "Java not found. Installing..." && sudo apt-get install oracle-java8-installer)
	[ -f "${JAVA}" ] || (echo "Java compiler exists, but not JVM runner. Something is really broken there" && exit 1)

clean:
	rm -f $(shell find $(SOURCEDIR) -iname "*.class")
	rm -f $(shell find $(SOURCEDIR) -iname "*~")
	rm -f $(shell find $(SOURCEDIR) -name "LLexer.java")
	rm -f L.output
