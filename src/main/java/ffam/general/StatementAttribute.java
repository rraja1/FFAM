package ffam.general;

import lombok.Value;

@Value
public class StatementAttribute {
    Type type;
    Object value;

    public static StatementAttribute attribute(Type type, Object value) {
        return new StatementAttribute(type, value);
    }

    public enum Type {API, DATA}
}
