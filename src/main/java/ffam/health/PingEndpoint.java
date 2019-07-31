package ffam.health;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;

@Component
public class PingEndpoint implements Endpoint {

    @Override
    public String id() {
        return "ping";
    }

    @Override
    public boolean enableByDefault() {
        return true;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return null;
    }
}