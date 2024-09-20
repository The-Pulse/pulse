import java.util.*;

public class Value {

    private final Object value;

    public Value(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isInt() {
        return this.value instanceof Integer;
    }

    public boolean isFloat() {
        return this.value instanceof Float;
    }

    public boolean isBool() {
        return this.value instanceof Boolean;
    }

    public boolean isRange() {
        return this.value instanceof int[]
               && ((int[]) this.value).length == 2;
    }

    public boolean isList() {
        if (this.value instanceof List<?>) {
            List<?> list;
            list = (List<?>) this.value;

            return list.isEmpty()
                   || list.getFirst() instanceof Value;
        }

        return false;
    }

    public boolean isDictionary() {
        if (this.value instanceof HashMap<?, ?>) {
            HashMap<?, ?> dictionary;
            dictionary = (HashMap<?, ?>) this.value;

            Set<?> keys;
            Collection<?> values;

            keys = dictionary.keySet();
            values = dictionary.values();

            return dictionary.isEmpty()
                   || keys.iterator().next() instanceof String
                   && values.iterator().next() instanceof Value;
        }

        return false;
    }

    public boolean isString() {
        return this.value instanceof String;
    }

    public boolean isNull() {
        return this.value == null;
    }

    public boolean canBeBool() {
        return this.isInt() || this.isFloat() || this.isBool();
    }

    public boolean canBeFloat() {
        return this.isInt() || this.isFloat();
    }

    public int asInt() {
        return (Integer) value;
    }

    public float asFloat() {
        return this.isInt()
            ? (float)(Integer) this.value
            : (Float) value;
    }

    public boolean asBool() {
        return this.isInt() || this.isFloat()
            ? this.asFloat() != 0f
            : (Boolean) this.value;
    }

    public int[] asRange() {
        int[] range = new int[2];

        if (this.isRange()) {
            range[0] = ((int[]) this.value)[0];
            range[1] = ((int[]) this.value)[1];
        }

        return range;
    }

    public List<Value> asList() {
        return this.isList()
           ? (List<Value>) this.value
           : null;
    }

    public HashMap<String, Value> asDictionary() {
        return this.isDictionary()
            ? (HashMap<String, Value>) this.value
            : null;
    }

    public String asString() {
        return this.value.toString();
    }

    public String getType() {
        String typeName;

        if (this.isNull()) {
            typeName = "Null";
        } else if (this.isInt()) {
            typeName = "Integer";
        } else if (this.isFloat()) {
            typeName = "Float";
        } else if (this.isBool()) {
            typeName = "Boolean";
        } else {
            typeName = "Unknown";
        }

        return typeName;
    }

    public boolean equals(Value other) {
        boolean equals,
                areIntOrFloat,
                areBoolAndIntOrFloat,
                areIntOrFloatAndBool;

        areIntOrFloat = (this.isInt() || this.isFloat())
                        && (other.isInt() || other.isFloat());

        areBoolAndIntOrFloat = this.isBool()
                               && (other.isInt() || other.isFloat());

        areIntOrFloatAndBool = (this.isInt() || this.isFloat())
                               && other.isBool();

        if (areIntOrFloat) {
            equals = this.asFloat() == other.asFloat();
        } else if (areBoolAndIntOrFloat) {
            equals = this.asBool()
                ? other.asFloat() != 0f
                : other.asFloat() == 0f;
        } else if (areIntOrFloatAndBool) {
            equals = other.isBool()
                ? this.asFloat() != 0f
                : this.asFloat() == 0f;
        } else if (this.isBool() && other.isBool()) {
            equals = this.asBool() == other.asBool();
        } else if (this.isRange() && other.isRange()) {
            equals = Arrays.equals(this.asRange(), other.asRange());
        } else if (this.isList() && other.isList()) {
            if (this.asList().size() != other.asList().size()) {
                return false;
            }

            equals = true;

            for (int i = 0; i < this.asList().size(); i++) {
                if (!this.asList().get(i).equals(other.asList().get(i))) {
                    equals = false;
                    break;
                }
            }
        } else if (this.isDictionary() && other.isDictionary()) {
            if (this.asDictionary().size() != other.asDictionary().size()) {
                return false;
            }

            equals = true;

            Object[] thisKeys,
                     thisValues,
                     otherKeys,
                     otherValues;

            Value thisValue,
                  otherValue;

            thisKeys = this.asDictionary().keySet().toArray();
            thisValues = this.asDictionary().values().toArray();

            otherKeys = other.asDictionary().keySet().toArray();
            otherValues = other.asDictionary().values().toArray();

            for (int i = 0; i < thisKeys.length; i++) {
                thisValue = (Value) thisValues[i];
                otherValue = (Value) otherValues[i];

                if (!thisKeys[i].equals(otherKeys[i])
                    || !thisValue.equals(otherValue)) {

                    equals = false;
                    break;
                }
            }
        } else if (this.isString() && other.isString()) {
            equals = this.asString().equals(other.asString());
        } else {
            equals = this.isNull() && other.isNull();
        }

        return equals;
    }

    @Override
    public String toString() {
        return this.value != null
            ? this.value.toString()
            : null;
    }
}
