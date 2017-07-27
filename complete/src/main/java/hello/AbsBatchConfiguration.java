package hello;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public abstract class AbsBatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public DataSource dataSource;

    private String csvFileName;
    private String[] fieldNames;

    // tag::readerwriterprocessor[]
    @Bean
    public <T> FlatFileItemReader<T> reader(Class<T> pojoClass) {
        FlatFileItemReader<T> reader = new FlatFileItemReader<T>();
        reader.setResource(new ClassPathResource(csvFileName));
        reader.setLineMapper(new DefaultLineMapper<T>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(fieldNames);
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<T>() {{
                setTargetType(pojoClass);
            }});
        }});
        return reader;
    }

    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    @Bean
    public abstract <T> JdbcBatchItemWriter<T> writer();
    // end::readerwriterprocessor[]

    // tag::jobstep[]
    @Bean
    public abstract Job importUserJob(JobCompletionNotificationListener listener);

    @Bean
    public abstract Step step1();
    // end::jobstep[]
}
