CP  		= "src/main/java"

all: test

test: L.output
	@(diff L.output L.answer && echo "Test succeeded!") || echo "Test failed!"

L.output: compile
	java -jar build/LLexer.jar L.test > L.output

compile:
	jflex src/main/jflex/L.flex -d $(CP)
	[ -d build ] || mkdir build
	javac -cp $(CP) -source 8 $(CP)/LLexer.java $(CP)/symbols/*.java -d build
	cd build && jar -cfe LLexer.jar LLexer .
	
clean:
	rm -rf build $(CP)/LLexer.java{,~} L.output
