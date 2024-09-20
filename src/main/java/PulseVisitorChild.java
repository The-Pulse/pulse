import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PulseVisitorChild extends PulseBaseVisitor<Value> {

    private final Map<String, Object> memory = new HashMap<>();

    @Override
    public Value visitProg(PulseParser.ProgContext context) {
        Value result = new Value(0);

        for (PulseParser.StatContext statContext: context.stat()) {
            result = visit(statContext);
        }

        return result;
    }

    @Override
    public Value visitStat(PulseParser.StatContext context) {
        if (context.istc() != null) {
            return visit(context.istc());
        } else if (context.VAR() != null) {
            String variableName = context.VAR().getText();
            Value variableValue = visit(context.expr());

            memory.put(variableName, variableValue);

            return variableValue;
        } else if (context.expr() != null) {
            return visit(context.expr());
        }

        return new Value(null);
    }

    @Override
    public Value visitExpr(PulseParser.ExprContext context) {
        return visit(context.ranExpr());
    }

    @Override
    public Value visitRanExpr(PulseParser.RanExprContext context) {
        Value result,
              minValue,
              maxValue;

        if (context.RANGE() != null) {
            minValue = visit(context.addExpr(0));
            maxValue = visit(context.addExpr(1));

            if (!minValue.isInt() || !maxValue.isInt()) {
                System.out.println(">> RANGE MUST HAVE INTEGERS ! <<");

                return new Value(null);
            }

            int[] range = new int[2];
            range[0] = minValue.asInt();
            range[1] = maxValue.asInt();

            result = new Value(range);
        } else {
            result = visit(context.eqlExpr());
        }

        return result;
    }

    @Override
    public Value visitEqlExpr(PulseParser.EqlExprContext context) {
        Value result,
              right;

        String operator;

        result = visit(context.orExpr(0));

        for (int i = 1; i < context.orExpr().size(); i++) {
            operator = context.getChild(2 * i - 1).getText().trim();
            right = visit(context.orExpr(i));

            switch (operator) {
                case "==":
                    result = new Value(result.equals(right));
                    break;
                case "!=":
                    result = new Value(!result.equals(right));
                    break;
                case ">":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() > right.asInt());
                    } else {
                        result = new Value(result.asFloat() > right.asFloat());
                    }
                    break;
                case ">=":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() >= right.asInt());
                    } else {
                        result = new Value(result.asFloat() >= right.asFloat());
                    }
                    break;
                case "<":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() < right.asInt());
                    } else {
                        result = new Value(result.asFloat() < right.asFloat());
                    }
                    break;
                case "<=":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() <= right.asInt());
                    } else {
                        result = new Value(result.asFloat() <= right.asFloat());
                    }
                    break;
            }
        }

        return result;
    }

    @Override
    public Value visitOrExpr(PulseParser.OrExprContext context) {
        Value result = visit(context.andExpr(0));

        for (int i = 1; i < context.andExpr().size(); i++) {
            String operator = context.getChild(2 * i - 1).getText().trim();
            Value right = visit(context.andExpr(i));

            switch (operator) {
                case "||":
                case "or":
                    result = new Value(result.asBool() || right.asBool());
                    break;
            }
        }

        return result;
    }

    @Override
    public Value visitAndExpr(PulseParser.AndExprContext context) {
        Value result = visit(context.xorExpr(0));

        for (int i = 1; i < context.xorExpr().size(); i++) {
            String operator = context.getChild(2 * i - 1).getText().trim();
            Value right = visit(context.xorExpr(i));

            switch (operator) {
                case "&&":
                case "and":
                    result = new Value(result.asBool() && right.asBool());
                    break;
            }
        }

        return result;
    }

    @Override
    public Value visitXorExpr(PulseParser.XorExprContext context) {
        Value result = visit(context.addExpr(0));

        for (int i = 1; i < context.addExpr().size(); i++) {
            String operator = context.getChild(2 * i - 1).getText().trim();
            Value right = visit(context.addExpr(i));

            switch (operator) {
                case "^^":
                case "xor":
                    result = new Value(result.asBool() ^ right.asBool());
                    break;
            }
        }

        return result;
    }

    @Override
    public Value visitAddExpr(PulseParser.AddExprContext context) {
        Value result = visit(context.mulExpr(0));

        for (int i = 1; i < context.mulExpr().size(); i++) {
            String operator = context.getChild(2 * i - 1).getText().trim();
            Value right = visit(context.mulExpr(i));

            switch (operator) {
                case "+":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() + right.asInt());
                    } else if (result.canBeFloat() && right.canBeFloat()) {
                        result = new Value(result.asFloat() + right.asFloat());
                    } else {
                        result = new Value(result.asString() + right.asString());
                    }

                    break;

                case "-":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() - right.asInt());
                    } else {
                        result = new Value(result.asFloat() - right.asFloat());
                    }
                    break;
            }
        }

        return result;
    }

    @Override
    public Value visitMulExpr(PulseParser.MulExprContext context) {
        Value result = visit(context.powExpr(0));

        for (int i = 1; i < context.powExpr().size(); i++) {
            String operator = context.getChild(2 * i - 1).getText().trim();
            Value right = visit(context.powExpr(i));

            switch (operator) {
                case "*":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() * right.asInt());
                    } else if (result.canBeFloat() && right.canBeFloat()) {
                        result = new Value(result.asFloat() * right.asFloat());
                    } else if (result.isString()
                               && right.isInt()
                               && right.asInt() > 0) {

                        StringBuilder stringMulResult;
                        stringMulResult = new StringBuilder();

                        for (int t = 0; t < right.asInt(); t++) {
                            stringMulResult.append(result.asString());
                        }

                        result = new Value(stringMulResult.toString());
                    } else {
                        System.out.println("INVALID MULTIPLICATION EXPRESSION");
                        result = new Value(null);
                    }
                    break;

                case "/":
                    result = new Value(result.asFloat() / right.asFloat());
                    break;

                case "%":
                case "mod":
                    if (result.isInt() && right.isInt()) {
                        result = new Value(result.asInt() % right.asInt());
                    } else {
                        result = new Value(result.asFloat() % right.asFloat());
                    }
                    break;
            }
        }

        return result;
    }

    @Override
    public Value visitPowExpr(PulseParser.PowExprContext context) {
        Value result = visit(context.funExpr(0));

        for (int i = 1; i < context.funExpr().size(); i++) {
            String operator = context.getChild(2 * i - 1).getText().trim();
            Value right = visit(context.funExpr(i));

            switch (operator) {
                case "^":
                case "**":
                case "pow":
                    result = new Value(Math.pow(result.asFloat(), right.asFloat()));
                    break;
            }
        }

        return result;
    }

    @Override
    public Value visitFunExpr(PulseParser.FunExprContext context) {
        Value result;

        if (context.funcall() != null) {
            result = visit(context.funcall());
        } else {
            result = visit(context.base());
        }

        return result;
    }

    @Override
    public Value visitBase(PulseParser.BaseContext context) {
        if (context.list() != null) {
            List<Value> list = new ArrayList<>();

            for (PulseParser.ExprContext exprContext: context.list().expr()) {
                list.add(visit(exprContext));
            }

            return new Value(list);
        } else if (context.dict() != null) {
            HashMap<String, Value> dictionnary = new HashMap<>();

            PulseParser.DicasgContext dicasgContext;

            for (int i = 0; i < context.dict().dicasg().size(); i++) {
                dicasgContext = context.dict().dicasg(i);

                dictionnary.put(dicasgContext.VAR().getText(),
                                visit(dicasgContext.expr()));
            }

            return new Value(dictionnary);
        } else if (context.STRING() != null) {
            String stringExpression,
                   string;

            stringExpression = context.STRING().getText();
            string = stringExpression.substring(
                1, stringExpression.length() - 1);

            return new Value(string);
        } else if (context.INT() != null) {
            return new Value(Integer.parseInt(context.INT().getText()));
        } else if (context.FLOAT() != null) {
            return new Value(Float.parseFloat(context.FLOAT().getText()));
        } else if (context.BOOL() != null) {
            return new Value(context.BOOL().getText().equals("true"));
        } else if (context.NULL() != null) {
            return new Value(null);
        } else if (context.LPAREN() != null && context.RPAREN() != null) {
            return visit(context.expr());
        } else if (context.VAR() != null) {
            Object value;
            value = memory.getOrDefault(context.VAR().getText(),
                                        new Value(null));

            if (!(value instanceof Value)) {
                return new Value(null);
            }

            return (Value) value;
        }

        System.out.println(">> INVALID BASE ! <<");

        return new Value(null);
    }

    @Override
    public Value visitIstc(PulseParser.IstcContext context) {
        if (context.PRINT() != null) {
            Value value;

            for (PulseParser.ExprContext exprContext: context.muex().expr()) {
                value = visit(exprContext);
                System.out.println(value);
            }
        } else if (context.IMPORT() != null) {
            CharStream input;
            ParseTree tree;

            PulseLexer    lexer;
            CommonTokenStream tokens;
            PulseParser       parser;
            PulseVisitorChild visitor;

            String fileName;

            for (PulseParser.ExprContext exprContext: context.muex().expr()) {
                fileName = visit(exprContext).asString();

                try {
                    input = CharStreams.fromFileName("src/main/resources/"
                                                     + fileName);
                } catch (IOException e) {
                    System.out.println(fileName
                                       + " file not found.");

                    input = CharStreams.fromString("");
                }

                lexer = new PulseLexer(input);
                tokens = new CommonTokenStream(lexer);
                parser = new PulseParser(tokens);

                visitor = this;

                tree = parser.prog();
                visitor.visit(tree);
            }
        } else if (context.ifst() != null) {
            visit(context.ifst());
        } else if (context.forst() != null) {
            visit(context.forst());
        } else if (context.whlst() != null) {
            visit(context.whlst());
        } else if (context.funst() != null) {
            visit(context.funst());
        }

        return new Value(null);
    }

    @Override
    public Value visitIfst(PulseParser.IfstContext context) {
        Value ifStatement;
        ifStatement = visit(context.expr());

        if (ifStatement.canBeBool() && ifStatement.asBool()) {
            List<PulseParser.StatContext> statContexts;
            statContexts = context.blck(0).stat();

            for (PulseParser.StatContext statContext: statContexts) {
                visit(statContext);
            }
        } else if (context.ELSE() != null) {
            if (context.blck().size() > 1
                && ifStatement.canBeBool()
                && !ifStatement.asBool()) {

                List<PulseParser.StatContext> statContexts;
                statContexts = context.blck(1).stat();

                for (PulseParser.StatContext statContext: statContexts) {
                    visit(statContext);
                }
            } else if (context.ifst() != null) {
                visit(context.ifst());
            }
        }

        return new Value(null);
    }

    @Override
    public Value visitForst(PulseParser.ForstContext context) {
        Value range;

        PulseParser.RanExprContext ranExprContext;

        String keyName,
               variableName;

        ranExprContext = context.inExpr().ranExpr();
        range = visit(ranExprContext);

        if (!range.isDictionary()) {
            variableName = context.inExpr().VAR(0).getText();

            if (!range.isList()) {
                int minValue,
                    maxValue;

                if (range.isRange()) {
                    int[] rangeArray;
                    rangeArray = range.asRange();

                    minValue = rangeArray[0];
                    maxValue = rangeArray[1];
                } else if (range.isInt()) {
                    minValue = 0;
                    maxValue = range.asInt();
                } else {
                    System.out.println(">> RANGE MUST HAVE INTEGERS ! <<");

                    return new Value(null);
                }

                for (int i = minValue; i < maxValue; i++) {
                    memory.put(variableName, new Value(i));
                    visit(context.blck());
                }
            } else {
                for (Value element: range.asList()) {
                    memory.put(variableName, new Value(element));
                    visit(context.blck());
                }
            }
        } else {
            keyName = context.inExpr().VAR(0).getText();
            variableName = context.inExpr().VAR(1).getText();

            range.asDictionary().keySet().forEach(key -> {
                memory.put(keyName, new Value(key));
                memory.put(variableName,
                           new Value(range.asDictionary().get(key)));

                visit(context.blck());
            });
        }

        return new Value(null);
    }

    @Override
    public Value visitWhlst(PulseParser.WhlstContext context) {
        Value conditionValue;
        conditionValue = visit(context.eqlExpr());

        if (!conditionValue.canBeBool()) {
            System.out.println("WHILE CONDITION MUST BE A BOOLEAN EXPRESSION");

            return new Value(null);
        }

        while (conditionValue.asBool()) {
            visit(context.blck());
            conditionValue = visit(context.eqlExpr());
        }

        return new Value(null);
    }

    @Override
    public Value visitFunst(PulseParser.FunstContext context) {
        HashMap<String, Object> parameters;
        Function function;

        parameters = new HashMap<>();

        if (context.muvar() != null) {
            for (TerminalNode parameter: context.muvar().VAR()) {
                parameters.put(parameter.getText(), null);
                // TODO: default parameter value?!
            }
        }

        function = new Function(parameters, context.blck());

        memory.put(context.VAR().getText(),
                   function);

        return new Value(null);
    }

    @Override
    public Value visitFuncall(PulseParser.FuncallContext context) {
        Function function;
        String functionName;

        functionName = context.VAR().getText();

        if (!memory.containsKey(functionName)) {
            System.out.println(functionName
                             + " function does no longer exist.");

            return new Value(null);
        }

        function = (Function) memory.get(functionName);

        return visit(function.getContext());
    }

}