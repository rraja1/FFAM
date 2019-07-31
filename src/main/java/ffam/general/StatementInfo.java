package ffam.general;

import lombok.Value;

import java.util.Arrays;
import java.util.List;

@Value
public class StatementInfo {
    List<StatementAttribute> attributes;

    public static StatementInfo withAttributes(StatementAttribute... attributes) {
        return new StatementInfo(Arrays.asList(attributes));
    }
}
