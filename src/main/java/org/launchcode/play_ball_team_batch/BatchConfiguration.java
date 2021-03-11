package org.launchcode.play_ball_team_batch;

import org.launchcode.play_ball_team_batch.fieldSetMappers.TeamFieldSetMapper;
import org.launchcode.play_ball_team_batch.listeners.JobCompletionNotificationListener;
import org.launchcode.play_ball_team_batch.models.Team;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing              // adds many critical beans that support jobs
@ComponentScan("org.launchcode.play_ball_team_batch.listeners")
public class BatchConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Value("classpath*:1982/*.TEAM")
    private Resource[] inputFiles;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/play_ball");
        dataSource.setUsername("java_techjobs_202011");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(){
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public MultiResourceItemReader<Team> multiResourceItemreader() {
        MultiResourceItemReader<Team> reader = new MultiResourceItemReader<>();
        reader.setDelegate(reader());
        reader.setResources(inputFiles);
        return reader;
    }

    @Bean
    // create an ItemReader that reads in a flat file
    // https://www.programcreek.com/java-api-examples/?api=org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
    public FlatFileItemReader<Team> reader() {
        DefaultLineMapper<Team> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(new DelimitedLineTokenizer(","));
        lineMapper.setFieldSetMapper(new TeamFieldSetMapper());

        return new FlatFileItemReaderBuilder<Team>()
                .name("TeamItemReader")
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Team> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Team>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO Team (id, league_id, name, city, year, game_type) VALUES " +
                        "(:id, :leagueId, :name, :city, 1982, 'R');")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<Team> writer) {
        return stepBuilderFactory.get("step1")
                .<Team, Team> chunk(10)
                .reader(multiResourceItemreader())
                .writer(writer)
                .build();
    }

    @Bean
    public Job job1(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("job1")
                .incrementer(new RunIdIncrementer())
                .listener(jdbcTemplate())
                .flow(step1)
                .end()
                .build();
    }
}
