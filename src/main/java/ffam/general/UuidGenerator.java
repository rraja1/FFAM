package ffam.general;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidGenerator {

    public UUID randomUUID(){
        return UUID.randomUUID();
    }
}
