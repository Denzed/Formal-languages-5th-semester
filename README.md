# Formal-languages

## Constructive proof of the fact that intersection of CF-grammar and regular language is a CF-grammar

### Building
- if gradle is installed
```
gradle jar
```
- else if on *nix
```
./gradlew jar
```
- else
```
gradlew.bat jar
```
Resulting JAR-file can be found at `./build/libs/hw06-1.0-SNAPSHOT.jar`

### Running
```
java -jar hw06-1.0-SNAPSHOT.jar automatonPath grammarPath outputPath
```

### Automaton format
- should be described in DOT format
- is a digraph
- starting vertex has a "red" color attribute
- terminal vertices have "doublecircle" shape attribute
- edges have one symbol labels

### CF-grammar format
- rules are written in form `[non-terminal]: [symbol] (" " [symbol])*`
- non-terminals are represented by capital latin letters, terminals -- by lowercase latin letters
- `eps` symbol matches empty string
- initial non-terminal is the left non-terminal of the first rule
