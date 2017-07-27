package hello;

import org.springframework.batch.core.job.builder.FlowBuilder;

public class GlassFlow extends FlowBuilder {
    public GlassFlow(String name) {
        super(name);
    }
}
