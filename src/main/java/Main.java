import core.Value;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {

    public static void main(String[] args)
    throws Exception {

        CharStream input;

        PulseLexer lexer;
        CommonTokenStream tokens;
        PulseParser parser;

        Visitor   visitor;
        ParseTree tree;

        input = CharStreams.fromFileName("src/main/resources/test.pulse");

        lexer = new PulseLexer(input);
        tokens = new CommonTokenStream(lexer);
        parser = new PulseParser(tokens);

        visitor = new Visitor();
        tree = parser.prog();

        Value result = visitor.visit(tree);

    }

}
