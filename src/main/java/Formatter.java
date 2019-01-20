import generated.JavaLexer;
import generated.JavaParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Formatter {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Expected 2 arguments: <input_file> <output_file>");
        }
        try {
            Path input = Paths.get(args[0]);
            Path output = Paths.get(args[1]);
            JavaLexer lexer = new JavaLexer(CharStreams.fromPath(input));
            JavaParser parser = new JavaParser(new CommonTokenStream(lexer));
            JavaListener listener = new JavaListener(output);
            Exception error = listener.format(parser);
            if (error != null) {
                throw error;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
