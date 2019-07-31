package ffam.general;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Builder
@ToString
@EqualsAndHashCode
public class LogBuilder {

    private static String app = "FFAM_SYSTEM";

    String eventType;
    String api;
    String methodType;
    String message;
    Boolean success;

    public String log() {
        List<String> fields = new ArrayList<>();
        add(fields, "app", app);
        add(fields, "eventType", eventType);
        add(fields, "api", api);
        add(fields, "methodType", methodType);
        add(fields, "success", success);
        add(fields, "message", message);
        return StringUtils.join(fields, ", ");
    }

    private void add(List<String> fields, String key, Object value) {
        if (StringUtils.isNotEmpty(key) && value != null) {
            fields.add(key + "=\"" + value.toString() + "\"");
        }
    }
}
