import generated.JavaBaseListener;
import generated.JavaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class JavaListener {
    private class SafeWriter {
        private final Writer inner;
        private IOException error;

        SafeWriter(Writer writer) {
            inner = writer;
            error = null;
        }

        void write(String s) {
            if (error != null) {
                return;
            }
            try {
                inner.write(s);
            } catch (IOException e) {
                error = e;
            }
        }

        IOException getError() {
            return error;
        }
    }

    private final SafeWriter writer;
    private int depth;

    JavaListener(Path output) throws IOException {
        writer = new SafeWriter(Files.newBufferedWriter(output));
        depth = -1;
    }

    private void writeTabs() {
        for (int i = 0; i < depth; ++i) {
            writer.write("\t");
        }
    }

    private void writeType(JavaParser.TypeContext ctx) {
        if (ctx.PRIMITIVE_TYPE() != null) {
            writer.write(ctx.PRIMITIVE_TYPE().getText());
        } else {
            writer.write(ctx.IDENTIFIER().getText());
        }
    }

    private void writeTypeList(JavaParser.TypeListContext ctx) {
        List<JavaParser.TypeContext> types = ctx.type();
        writeType(types.get(0));
        types.subList(1, types.size()).forEach(type -> {
            writer.write(", ");
            writeType(type);
        });
    }

    private void writeArgument(JavaParser.ArgumentContext ctx) {
        writeType(ctx.type());
        writer.write(" " + ctx.IDENTIFIER().getText());
    }

    private void writeField(JavaParser.FieldContext ctx) {
        writer.write("\n");
        writeTabs();
        TerminalNode access_mod = ctx.ACCESS_MOD();
        if (access_mod != null) {
            writer.write(access_mod.getText() + " ");
        }
        writeType(ctx.type());
        writer.write(" " + ctx.IDENTIFIER().getText() + ";");
    }

    private void writeFunction(JavaParser.FunctionContext ctx) {
        writer.write("\n");
        writeTabs();
        TerminalNode access_mod = ctx.ACCESS_MOD();
        if (access_mod != null) {
            writer.write(access_mod.getText() + " ");
        }
        writer.write(" " + ctx.IDENTIFIER().getText() + " (");
        List<JavaParser.ArgumentContext> args = ctx.argument();
        if (!args.isEmpty()) {
            writeArgument(args.get(0));
            args.subList(1, args.size()).forEach(arg -> {
                writer.write(", ");
                writeArgument(arg);
            });
        }
        writer.write(") {");
        ++depth;
        //HERE BODY
        --depth;
        writer.write("\n");
        writeTabs();
        writer.write("}");
    }

    private void writeClassBody(JavaParser.ClassBodyContext ctx) {
        List<JavaParser.FieldContext> fields = ctx.field();
        fields.forEach(this::writeField);

        List<JavaParser.FunctionContext> functions = ctx.function();
        functions.forEach(this::writeFunction);
    }

    private void writeClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        TerminalNode public_mod = ctx.PUBLIC();
        if (public_mod != null) {
            writer.write(public_mod.getText() + " ");
        }
        writer.write(ctx.IDENTIFIER().getText());
        TerminalNode extends_mod = ctx.EXTENDS();
        if (extends_mod != null) {
            writer.write(" " + extends_mod.getText() + " ");
            writeType(ctx.type());
        }

    }
}
