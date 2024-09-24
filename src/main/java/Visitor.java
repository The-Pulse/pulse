import core.Value;

import java.util.HashMap;
import java.util.Map;

public class Visitor extends PulseBaseVisitor<Value> {

    private final Map<String, Object> memory = new HashMap<>();

    @Override
    public Value visitProg(PulseParser.ProgContext context) {
        Value result = new Value(null);

        for (PulseParser.StatContext statContext: context.stat()) {
            result = visit(statContext);
        }

        return result;
    }

    @Override
    public Value visitStat(PulseParser.StatContext context) {
        // TODO: stat process

        return new Value(null);
    }

}