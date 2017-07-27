package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class StepListener extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(StepListener.class);
	private Long jobId;
	private static int count = 1;

	public StepListener(){
		super();
	}

	public StepListener(Long jobId) {
		this.jobId = jobId;
	}

	@BeforeStep
	public void beforeStep(StepExecution execution){
//		System.out.println("Start step" + execution.getStepName() + " step id = " + execution.getId());
		this.jobId = execution.getJobExecutionId();
	}

	@AfterStep
	public ExitStatus afterStep(StepExecution execution) {
//		System.out.println("End step" + execution.getStepName() + " time " + count);
		if(count == 10) return new ExitStatus("FINISH");
		count++;
		return new ExitStatus("CONTINUE");
	}
}
