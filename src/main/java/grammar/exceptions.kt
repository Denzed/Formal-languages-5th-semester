package grammar

abstract class GrammarException(message: String?) : Exception(message)

class InvalidRuleException(symbol: String) :
        GrammarException("Rule Symbol should be non-terminal, but got \"$symbol\"")

class LR0ConflictFound(type: String) :
        GrammarException("Found $type conflict in LR(0) table => not an LR(0) language")