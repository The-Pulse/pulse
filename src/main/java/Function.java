import java.util.HashMap;

public class Function {

    private HashMap<String, Object> parameters;
    private PulseParser.BlckContext context;

    public Function(HashMap<String, Object> parameters,
                    PulseParser.BlckContext context) {

        this.parameters = parameters;
        this.context = context;
    }

    public HashMap<String, Object> getParameters() {
        return this.parameters;
    }

    public PulseParser.BlckContext getContext() {
        return this.context;
    }

}
