package ffam.task.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ffam.task.domain.TaskPriority;
import lombok.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Value
public class TaskRequest {
    private TaskPriority taskPriority;
    private boolean skill1;
    private boolean skill2;
    private boolean skill3;

    //Need to explicitly define this since jackson and lombok don't like each other anymore for some reason
    @JsonCreator
    public TaskRequest(@JsonProperty("taskPriority") TaskPriority taskPriority,
                       @JsonProperty("skill1") boolean skill1,
                       @JsonProperty("skill2") boolean skill2,
                       @JsonProperty("skill3") boolean skill3){
        this.taskPriority = taskPriority;
        this.skill1 = skill1;
        this.skill2 = skill2;
        this.skill3 = skill3;
    }
}
