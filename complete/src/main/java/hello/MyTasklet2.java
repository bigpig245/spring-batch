package hello;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.List;

public class MyTasklet2 implements Tasklet {
    private static int count = 0;
    private static final int chunkSize = 3;

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        FlatFileItemReader<Person> reader = reader();
        System.out.println("Bat dau ghi lan " + count);
        try {
            reader.open(chunkContext.getStepContext().getStepExecution()
                    .getExecutionContext());

            List<Person> items = new ArrayList<>(chunkSize); // (2)
            Person item = null;
            do {
                // Pseudo operation of ItemReader
                for (int i = 0; i < chunkSize; i++) { // (3)
                    item = reader.read();
                    if (item == null) {
                        break;
                    }
                    // Pseudo operation of ItemProcessor
                    // do some processes.

                    items.add(item);
                }

                // Pseudo operation of ItemWriter
                if (!items.isEmpty()) {
                    writer().write(items); // (4)
                    items.clear();
                }
            } while (item != null);
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                // do nothing.
            }
        }
        if (count < 10) {
            count++;
            return RepeatStatus.CONTINUABLE;
        }
        return RepeatStatus.FINISHED;
    }

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

    public ItemWriter<Person> writer() {
        return new ItemWriter<Person>() {
            @Override
            public void write(List<? extends Person> list) throws Exception {
                System.out.println("Bat dau ghi" + list.size());
            }
        };
    }
}
