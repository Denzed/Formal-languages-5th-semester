JFLEX		= /bin/jflex
JAVA		= /bin/java
JAVAC		= /bin/javac
JAR 		= /bin/jar
CP  		= "src/main/java"

all: test

test: L.output
	@(diff L.output L.answer && echo "Test succeeded!") || echo "Test failed!"

L.output: compile
	java -jar build/LLexer.jar L.test > L.output

compile: dependencies
	$(JFLEX) src/main/jflex/L.flex -d $(CP)
	[ -d build ] || mkdir build
	$(JAVAC) -cp $(CP) -source 8 $(CP)/LLexer.java $(CP)/symbols/*.java -d build
	cd build && $(JAR) -cfe LLexer.jar LLexer .

dependencies: 
	[ -f "${JFLEX}" ] || (echo "JFlex not found. Installing..." && sudo apt-get install jflex)
	[ -f "${JAVAC}" ] || (echo "Java not found. Installing..." && sudo apt-get install oracle-java8-installer)
	[ -f "${JAVA}" ] || (echo "Something is really broken there" && exit 1)
	[ -f "${JAR}" ] || (echo "Something is really broken there" && exit 1)

clean:
	rm -rf build $(CP)/LLexer.java{,~} L.output
