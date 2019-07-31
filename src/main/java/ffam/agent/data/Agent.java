package ffam.agent.data;

import lombok.Value;

@Value
public class Agent {
    private String agentId;
    private String agentName;
    private boolean skill1;
    private boolean skill2;
    private boolean skill3;
}
