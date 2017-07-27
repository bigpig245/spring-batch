package hello;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    private Long jobId;

    private static int stepCount = 0;

    @BeforeStep
    public void getInterstepData(StepExecution stepExecution) {
        JobExecution jobExecution = stepExecution.getJobExecution();
        this.jobId = jobExecution.getJobId();
    }

//    @Autowired
//    public DataSource dataSource;

    // tag::readerwriterprocessor[]
    @Bean
    public FlatFileItemReader<Person> reader() {
        FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
        reader.setResource(new ClassPathResource("sample-data.csv"));
        reader.setLineMapper(new DefaultLineMapper<Person>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[]{"firstName", "lastName"});
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }});

        }});
        return reader;
    }

//    public ItemReader<Person> reader() {
////        ExcelReader<Person> reader = new ExcelReader<>(0);
//        PoiItemReader<Person> reader = new PoiItemReader<>();
//        reader.setLinesToSkip(1);
//        reader.setResource(new ClassPathResource("sample-data.xlsx"));
//        reader.setRowMapper(excelRowMapper());
//        return reader;
//    }

    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    public ItemWriter<Person> writer() {
        return new ItemWriter<Person>() {
            @Override
            public void write(List<? extends Person> list) throws Exception {
                System.out.print("AAA");
            }
        };
    }

    public StepListener stepListener(){
        return new StepListener(jobId);
    }

//    private RowMapper<Person> excelRowMapper() {
//        return new PersonExcelRowMapper();
//    }
//    @Bean
//    public JdbcBatchItemWriter<Person> writer() {
//        return new ItemW();
//    }
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(defineStep()).on("CONTINUE")
                .to(defineStep()).on("FINISH").end().build()
                .build();
    }

    public Step defineStep(){
        stepCount ++;
        if(stepCount % 2 == 0){
            return stepList().get("A");
        }
        return stepList().get("B");
    }

    @Bean
    public Map<String,Step> stepList() {
        Map<String, Step> stepMap = new HashMap<>();
        stepMap.put("A", step1());
        stepMap.put("B", step2());
        return stepMap;
    }
    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Person, Person>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .listener(stepListener())
                .build();
    }
    @Bean
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .tasklet((stepContribution, chunkContext) -> {
                    System.out.print("AA");
                    return RepeatStatus.FINISHED;
                })
                .listener(stepListener())
                .build();
    }
    // end::jobstep[]

}
