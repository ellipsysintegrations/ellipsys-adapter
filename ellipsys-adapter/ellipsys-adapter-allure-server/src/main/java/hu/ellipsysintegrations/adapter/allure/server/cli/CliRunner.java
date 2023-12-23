package hu.ellipsysintegrations.adapter.allure.server.cli;

import hu.ellipsysintegrations.adapter.allure.server.api.client.AllureServerClient;
import hu.ellipsysintegrations.adapter.allure.server.api.dto.ResultContainer;
import hu.ellipsysintegrations.adapter.allure.server.api.dto.SendResultsRequestDto;
import hu.ellipsysintegrations.adapter.allure.server.exception.AllureServerAdapterException;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;

@Getter
@Log4j2
@Component
@Accessors(fluent = true)
public class CliRunner implements CommandLineRunner {

    @Autowired
    private ApplicationContext ac;

    @Autowired
    private AllureServerClient allureServerClient;

    @Autowired
    private Environment environment;

    @Override
    public void run(String... args) {
        RunResult runResult = RunResult.SUCCESS;

        try {
            CommandLineParser parser = new DefaultParser();
            Options options = createOptions();
            CommandLine cmd = parser.parse(options, args, true);

            //Log related environment properties
            log.debug(LogMessages.ALLURE_ADAPTER_PROPERTIES);
            log.debug(LogMessages.PROTOCOL, environment.getProperty(AllureProperties.ALLURE_SERVER_PROTOCOL));
            log.debug(LogMessages.HOST, environment.getProperty(AllureProperties.ALLURE_SERVER_HOST));
            log.debug(LogMessages.PORT, environment.getProperty(AllureProperties.ALLURE_SERVER_PORT));
            log.debug(LogMessages.PROJECT_ID, environment.getProperty(AllureProperties.ALLURE_PROJECT_ID));
            log.debug(LogMessages.RESULTS_DIR, environment.getProperty(AllureProperties.ALLURE_RESULTS_DIR));

            if (cmd.hasOption(OptionsAndArguments.OPTION_HELP)) {
                printHelp(options);
            } else if (cmd.hasOption(OptionsAndArguments.ARGUMENT_PROJECT_ID)) {
                if (!uploadAllureResults(cmd)) {
                    runResult = RunResult.FAILURE;
                }
            } else {
                log.error(ErrorMessages.COMMAND_NOT_FOUND);
                runResult = RunResult.ERROR;
                printHelp(options);
            }
        } catch (ParseException ex) {
            log.error(ErrorMessages.PARSING_COMMAND_LINE_ARGUMENTS_FAILED, ex);
            runResult = RunResult.ERROR;
        } catch (AllureServerAdapterException ex) {
            log.error(ex.getMessage(), ex);
            runResult = RunResult.ERROR;
        } catch (Exception ex) {
            log.error(ErrorMessages.UNKNOWN_ERROR_OCCURRED, ex);
            runResult = RunResult.ERROR;
        }

        shutdown(runResult);
    }

    private boolean uploadAllureResults(CommandLine cmd) {
        boolean success = true;
        log.info(LogMessages.ALLURE_UPLOAD_SETTINGS);

        String projectId = cmd.getOptionValue(OptionsAndArguments.ARGUMENT_PROJECT_ID);
        log.info(LogMessages.PROJECT_ID, projectId);

        String resultsDirPath = cmd.getOptionValue(OptionsAndArguments.ARGUMENT_RESULTS_DIR,
                allureServerClient().properties().defaultFolder());
        log.info(LogMessages.RESULTS_DIR, resultsDirPath);

        allureServerClient.postSendResults(projectId, requestDtoBuilder(resultsDirPath).build());
        reportUrlSystem(projectId);
        allureServerClient.getCleanResults(projectId.toLowerCase());

        return success;
    }

    private Options createOptions() {
        Option allureResultsOption = new Option(OptionsAndArguments.ARGUMENT_RESULTS_DIR, OptionsAndArguments.ARGUMENT_RESULTS_DIR_LONG, true, OptionsAndArguments.ARGUMENT_RESULTS_DIR_DESC);
        allureResultsOption.setArgName(OptionsAndArguments.ARGUMENT_RESULTS_DIR);

        Option allureServerOption = new Option(OptionsAndArguments.ARGUMENT_PROJECT_ID, OptionsAndArguments.ARGUMENT_PROJECT_ID_LONG, true, OptionsAndArguments.ARGUMENT_PROJECT_ID_DESC);
        allureServerOption.setArgName(OptionsAndArguments.ARGUMENT_PROJECT_ID);

        Option helpOption = new Option(OptionsAndArguments.OPTION_HELP, OptionsAndArguments.OPTION_HELP_LONG, false, OptionsAndArguments.OPTION_HELP_DESC);

        return new Options()
                .addOption(allureResultsOption)
                .addOption(allureServerOption)
                .addOption(helpOption);
    }

    private File loadResultsDirectory(String directory) {
        log.debug(LogMessages.DIRECTORY, directory);
        File resource = new File(directory);

        if (resource == null || !resource.isDirectory()) {
            throw new AllureServerAdapterException(String.format(OptionsAndArguments.ARGUMENT_RESULTS_DIR_DOES_NOT_EXIST, directory));
        }

        log.info(LogMessages.ALLURE_RESULTS_DIRECTORY_LOADED, resource.getAbsolutePath());
        return resource;
    }

    private SendResultsRequestDto.SendResultsRequestDtoBuilder requestDtoBuilder(String resultsDirPath) {
        SendResultsRequestDto.SendResultsRequestDtoBuilder requestDtoBuilder = SendResultsRequestDto.builder();

        File resultsDirectory = loadResultsDirectory(resultsDirPath);
        File[] files = resultsDirectory.listFiles();
        ArrayList<String> encodedContents = new ArrayList<>();

        for (File file : files) {
            String filePath = file.getAbsolutePath();
            byte[] fileContent;

            try {
                fileContent = FileUtils.readFileToByteArray(new File(filePath));
            } catch (Exception e) {
                throw new AllureServerAdapterException(String.format(OptionsAndArguments.ARGUMENT_RESULTS_DIR_DOES_NOT_EXIST, filePath));
            }

            String encodedContent = Base64.getEncoder().encodeToString(fileContent);
            encodedContents.add(encodedContent);
            log.debug(LogMessages.FILE_ENCODED, file.getName());
            log.debug(LogMessages.BASE_64_CONTENT, encodedContent);
            requestDtoBuilder.result(ResultContainer.builder().fileName(file.getName()).contentBase64(encodedContent).build());
        }

        return requestDtoBuilder;
    }

    private void reportUrlSystem(String projectId) {
        String reportUrlSystemKey = allureServerClient().properties().allureReportUrlSystemKey();

        System.setProperty(reportUrlSystemKey, "");
        log.info(LogMessages.SYSTEM_PROPERTY_ERASED, reportUrlSystemKey);

        String reportUrlSystemValue = allureServerClient.getGenerateReport(projectId.toLowerCase())
                .jsonPath().get(JsonPath.DATA_REPORT_URL);

        System.setProperty(reportUrlSystemKey, reportUrlSystemValue);
        log.info(LogMessages.SYSTEM_PROPERTY_WAS_SET, reportUrlSystemKey, reportUrlSystemValue);
    }

    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(OptionsAndArguments.OPTIONS_TITLE, options);
    }

    private void shutdown(RunResult runResult) {
        log.info(OptionsAndArguments.APPLICATION_EXIT_CODE, runResult.getExitCode());
        int exitCode = SpringApplication.exit(ac, runResult::getExitCode);
        System.exit(exitCode);
    }

    private static final class OptionsAndArguments {
        private static final String OPTIONS_TITLE = "Allure Server Adapter";
        private static final String OPTION_HELP = "h";
        private static final String OPTION_HELP_LONG = "help";
        private static final String OPTION_HELP_DESC = "Show usage help";
        private static final String ARGUMENT_RESULTS_DIR = "resultsDir";
        private static final String ARGUMENT_RESULTS_DIR_LONG = "allureResultsDir";
        private static final String ARGUMENT_RESULTS_DIR_DESC = "Allure results source directory";
        private static final String ARGUMENT_RESULTS_DIR_DOES_NOT_EXIST = "Filepath [%s] does not exist";
        private static final String ARGUMENT_PROJECT_ID = "projectId";
        private static final String ARGUMENT_PROJECT_ID_LONG = "allureProjectId";
        private static final String ARGUMENT_PROJECT_ID_DESC = "Allure Server ProjectId";
        private static final String APPLICATION_EXIT_CODE = "Stopping AllureServerAdapterApplication, exit code: {}";
    }

    private static final class ErrorMessages {
        private static final String COMMAND_NOT_FOUND = "Command not found.";
        private static final String UNKNOWN_ERROR_OCCURRED = "Unknown error occurred. Check detailed logs for more information.";
        private static final String PARSING_COMMAND_LINE_ARGUMENTS_FAILED = "Parsing command line arguments failed.";
    }

    private static final class AllureProperties {
        public static final String ALLURE_SERVER_PROTOCOL = "allure.server.protocol";
        public static final String ALLURE_SERVER_HOST = "allure.server.host";
        public static final String ALLURE_SERVER_PORT = "allure.server.port";
        public static final String ALLURE_PROJECT_ID = "allure.projectId";
        public static final String ALLURE_RESULTS_DIR = "allure.resultsDir";
    }

    private static final class LogMessages {
        public static final String
                ALLURE_ADAPTER_PROPERTIES = "= Allure adapter properties =====================================";
        public static final String PROTOCOL = "protocol: {}";
        public static final String HOST = "host: {}";
        public static final String PORT = "port: {}";
        public static final String PROJECT_ID = "projectId: {}";
        public static final String RESULTS_DIR = "resultsDir: {}";
        public static final String ALLURE_UPLOAD_SETTINGS = "= Allure upload settings ===================================";
        public static final String DIRECTORY = "Directory: {}";
        public static final String ALLURE_RESULTS_DIRECTORY_LOADED = "Allure results directory loaded: {}";
        public static final String FILE_ENCODED = "File encoded: {}";
        public static final String BASE_64_CONTENT = "Base64 content: {}";
        public static final String SYSTEM_PROPERTY_ERASED = "System property erased: {}";
        public static final String SYSTEM_PROPERTY_WAS_SET = "System property was set {}={}";
    }

    private static final class JsonPath {
        private static final String DATA_REPORT_URL = "data.report_url";
    }

}
