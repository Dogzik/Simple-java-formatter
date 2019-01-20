import generated.JavaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JavaListener {
    private class SafeWriter implements AutoCloseable {
        private final Writer inner;
        private Exception error;

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

        Exception getError() {
            return error;
        }

        @Override
        public void close() throws Exception {
            inner.close();
        }
    }

    private final SafeWriter writer;
    private int depth;

    JavaListener(Path output) throws IOException {
        writer = new SafeWriter(Files.newBufferedWriter(output));
        depth = 0;
    }

    Exception format(JavaParser parser) {
        Exception ans = null;
        writeClassDeclaration(parser.classDeclaration());
        try {
            ans = writer.getError();
            writer.close();
        } catch (Exception e) {
            if (writer.error != null) {
                ans = e;
            }
        }
        return ans;
    }

    private void writeTabs() {
        for (int i = 0; i < depth; ++i) {
            writer.write("    ");
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
        TerminalNode final_mod = ctx.FINAL();
        if (final_mod != null) {
            writer.write(final_mod.getText() + " ");
        }
        writeType(ctx.type());
        writer.write(" " + ctx.IDENTIFIER().getText());
    }

    private void writeArgumentList(JavaParser.ArgumentListContext ctx) {
        List<JavaParser.ArgumentContext> args = ctx.argument();
        if (!args.isEmpty()) {
            writeArgument(args.get(0));
            args.subList(1, args.size()).forEach(arg -> {
                writer.write(", ");
                writeArgument(arg);
            });
        }
    }

    private void writeField(JavaParser.FieldContext ctx) {
        writer.write("\n");
        writeTabs();
        TerminalNode access_mod = ctx.ACCESS_MOD();
        if (access_mod != null) {
            writer.write(access_mod.getText() + " ");
        }
        TerminalNode final_mod = ctx.FINAL();
        if (final_mod != null) {
            writer.write(final_mod.getText() + " ");
        }
        writeType(ctx.type());
        ctx.L_SQ_PAREN().forEach(leftParen -> {
            writer.write(leftParen.getText() + "]");
        });
        writer.write(" " + ctx.IDENTIFIER().getText() + ";");
    }


    private void writeFunctionParam(JavaParser.FunctionParamContext ctx) {
        writeExpression(ctx.expression());
    }

    private void writeFunctionParamList(JavaParser.FunctionParamListContext ctx) {
        List<JavaParser.FunctionParamContext> params = ctx.functionParam();
        if (!params.isEmpty()) {
            writeFunctionParam(params.get(0));
            params.subList(1, params.size()).forEach(param -> {
                writer.write(", ");
                writeFunctionParam(param);
            });
        }
    }

    private void writeFunctionCall(JavaParser.FunctionCallContext ctx) {
        writer.write(ctx.IDENTIFIER().getText() + "(");
        writeFunctionParamList(ctx.functionParamList());
        writer.write(")");
    }

    private void writeClassFunctionCall(JavaParser.ClassFunctionCallContext ctx) {
        writer.write(ctx.IDENTIFIER().getText());
        ctx.functionCall().forEach(functionCall -> {
            writer.write(".");
            writeFunctionCall(functionCall);
        });
    }

    private void writeElemFunctionCall(JavaParser.ElemFunctionCallContext ctx) {
        writer.write(ctx.IDENTIFIER().getText());
        ctx.expression().forEach(expression -> {
            writer.write("[");
            writeExpression(expression);
            writer.write("]");
        });
    }

    private void writeAnyFunctionCall(JavaParser.AnyFunctionCallContext ctx) {
        if (ctx.functionCall() != null) {
            writeFunctionCall(ctx.functionCall());
        } else if (ctx.elemFunctionCall() != null) {
            writeElemFunctionCall(ctx.elemFunctionCall());
        } else {
            writeClassFunctionCall(ctx.classFunctionCall());
        }
    }

    private void writeNewStatement(JavaParser.NewStatementContext ctx) {
        writer.write(ctx.NEW().getText() + " ");
        writeType(ctx.type());
        if (ctx.functionParamList() != null) {
            writer.write("(");
            writeFunctionParamList(ctx.functionParamList());
            writer.write(")");
        } else {
            ctx.expression().forEach(expression -> {
                writer.write("[");
                writeExpression(expression);
                writer.write("]");
            });
        }
    }

    private void writeAssigment(JavaParser.AssigmentContext ctx) {
        writer.write(ctx.IDENTIFIER().getText() + " = ");
        if (ctx.expression() != null) {
            writeExpression(ctx.expression());
        } else {
            writeNewStatement(ctx.newStatement());
        }
    }

    private void writeLocalVariable(JavaParser.LocalVariableContext ctx) {
        if (ctx.FINAL() != null) {
            writer.write(ctx.FINAL().getText() + " ");
        }
        writeType(ctx.type());
        ctx.L_SQ_PAREN().forEach(leftParen -> {
            writer.write(leftParen.getText() + "]");
        });
        writer.write(" " + ctx.IDENTIFIER());
    }


    private void writeExpression(JavaParser.ExpressionContext ctx) {
        if (ctx.IDENTIFIER() != null) {
            writer.write(ctx.IDENTIFIER().getText());
        } else if (ctx.LITERAL() != null) {
            writer.write(ctx.LITERAL().getText());
        } else if (ctx.anyFunctionCall() != null) {
            writeAnyFunctionCall(ctx.anyFunctionCall());
        } else if (ctx.NOT() != null) {
            writer.write(ctx.NOT().getText());
            writeExpression(ctx.expression(0));
        } else if (ctx.BINARY_OP() != null) {
            writeExpression(ctx.expression(0));
            writer.write(" " + ctx.BINARY_OP().getText() + " ");
            writeExpression(ctx.expression(1));
        } else {
            writer.write("(");
            writeExpression(ctx.expression(0));
            writer.write(")");
        }
    }

    private void writeCondition(JavaParser.ConditionContext ctx) {
        writeExpression(ctx.expression());
    }

    private void writeIfStatement(JavaParser.IfStatementContext ctx) {
        List<JavaParser.FunctionBodyContext> bodies = ctx.functionBody();
        writer.write(ctx.IF() + " (");
        writeCondition(ctx.condition());
        writer.write(") {");
        ++depth;
        writeFunctionBody(bodies.get(0));
        --depth;
        writer.write("\n");
        writeTabs();
        writer.write("}");
        if (ctx.ELSE() != null) {
            writer.write(" " + ctx.ELSE() + " {");
            ++depth;
            writeFunctionBody(bodies.get(1));
            --depth;
            writer.write("\n");
            writeTabs();
            writer.write("}");
        }
    }

    private void writeWhileStatement(JavaParser.WhileStatementContext ctx) {
        writer.write(ctx.WHILE().getText() + " (");
        writeCondition(ctx.condition());
        writer.write(") {");
        ++depth;
        writeFunctionBody(ctx.functionBody());
        --depth;
        writer.write("\n");
        writeTabs();
        writer.write("}");
    }

    private int f(int x) {
        return 2 + x;
    }

    private int g(int x) {
        return 3 + x;
    }

    private void writeStatement(JavaParser.StatementContext ctx) {
        if (ctx.localVariable() != null) {
            writeLocalVariable(ctx.localVariable());
        } else if (ctx.assigment() != null) {
            writeAssigment(ctx.assigment());
        } else if (ctx.BREAK() != null) {
            writer.write(ctx.BREAK().getText());
        } else if (ctx.CONTINUE() != null) {
            writer.write(ctx.CONTINUE().getText());
        } else {
            writeAnyFunctionCall(ctx.anyFunctionCall());
        }
        writer.write(";");
    }

    private void writeComposedStatement(JavaParser.ComposedStatementContext ctx) {
        if (ctx.statement() != null) {
            writeStatement(ctx.statement());
        } else if (ctx.whileStatement() != null) {
            writeWhileStatement(ctx.whileStatement());
        } else if (ctx.RETURN() != null) {
            writer.write(ctx.RETURN().getText() + " ");
            writeExpression(ctx.expression());
            writer.write(";");
        } else {
            writeIfStatement(ctx.ifStatement());
        }
    }

    private void writeFunctionBody(JavaParser.FunctionBodyContext ctx) {
        ctx.composedStatement().forEach(composedStatement -> {
            writer.write("\n");
            writeTabs();
            writeComposedStatement(composedStatement);
        });
    }

    private void writeFunction(JavaParser.FunctionContext ctx) {
        writer.write("\n");
        writeTabs();
        TerminalNode access_mod = ctx.ACCESS_MOD();
        if (access_mod != null) {
            writer.write(access_mod.getText() + " ");
        }
        TerminalNode final_mod = ctx.FINAL();
        if (final_mod != null) {
            writer.write(final_mod.getText() + " ");
        }
        writeType(ctx.type());
        writer.write(" " + ctx.IDENTIFIER().getText() + "(");
        writeArgumentList(ctx.argumentList());
        writer.write(") {");
        ++depth;
        writeFunctionBody(ctx.functionBody());
        --depth;
        writer.write("\n");
        writeTabs();
        writer.write("}");
    }

    private void writeConstructorBody(JavaParser.ConstructorBodyContext ctx) {
        if (ctx.SUPER() != null) {
            writer.write("\n");
            writeTabs();
            writer.write(ctx.SUPER().getText() + "(");
            writeFunctionParamList(ctx.functionParamList());
            writer.write(");");
        }
        writeFunctionBody(ctx.functionBody());
    }

    private void writeConstructor(JavaParser.ConstructorContext ctx) {
        writer.write("\n");
        writeTabs();
        writer.write(ctx.IDENTIFIER() + "(");
        writeArgumentList(ctx.argumentList());
        writer.write(") {");
        ++depth;
        writeConstructorBody(ctx.constructorBody());
        --depth;
        writer.write("\n");
        writeTabs();
        writer.write("}");
    }

    private void writeClassBody(JavaParser.ClassBodyContext ctx) {
        List<JavaParser.FieldContext> fields = ctx.field();
        List<JavaParser.FunctionContext> functions = ctx.function();
        List<JavaParser.ConstructorContext> constructors = ctx.constructor();
        fields.forEach(this::writeField);
        if (!constructors.isEmpty()) {
            if (!fields.isEmpty()) {
                writer.write("\n");
            }
            writeConstructor(constructors.get(0));
            constructors.subList(1, constructors.size()).forEach(constructor -> {
                writer.write("\n");
                writeConstructor(constructor);
            });
        }
        if (!functions.isEmpty()) {
            if (!fields.isEmpty() || !constructors.isEmpty()) {
                writer.write("\n");
            }
            writeFunction(functions.get(0));
            functions.subList(1, functions.size()).forEach(function -> {
                writer.write("\n");
                writeFunction(function);
            });
        }
    }

    private void writeClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
        TerminalNode access_mod = ctx.ACCESS_MOD();
        if (access_mod != null) {
            writer.write(access_mod.getText() + " ");
        }
        TerminalNode final_mod = ctx.FINAL();
        if (final_mod != null) {
            writer.write(final_mod.getText() + " ");
        }
        writer.write(ctx.CLASS().getText() + " " + ctx.IDENTIFIER().getText());
        TerminalNode extends_mod = ctx.EXTENDS();
        if (extends_mod != null) {
            writer.write(" " + extends_mod.getText() + " ");
            writeType(ctx.type());
        }
        TerminalNode implements_mod = ctx.IMPLEMENTS();
        if (implements_mod != null) {
            writer.write(" " + implements_mod.getText() + " ");
            writeTypeList(ctx.typeList());
        }
        writer.write(" {");
        ++depth;
        writeClassBody(ctx.classBody());
        --depth;
        writer.write("\n}\n");
    }
}
