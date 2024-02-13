package adaa.analytics.rules.consoles;

import adaa.analytics.rules.consoles.config.DatasetConfiguration;
import adaa.analytics.rules.consoles.config.ParamSetWrapper;
import adaa.analytics.rules.consoles.config.TrainElement;
import adaa.analytics.rules.logic.performance.RulePerformanceCounter;
import adaa.analytics.rules.logic.performance.MeasuredPerformance;
import adaa.analytics.rules.logic.representation.ContrastRule;
import adaa.analytics.rules.logic.representation.Logger;
import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.logic.rulegenerator.RuleGenerator;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.operator.OperatorException;
import com.rapidminer.operator.OperatorCreationException;
import org.apache.commons.lang.StringUtils;
import utils.ArffFileLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.logging.Level;

public class TrainProcess {
    private RoleConfigurator roleConfigurator;
    private RuleGenerator ruleGenerator = null;

    private DatasetConfiguration datasetConfiguration;

    private ParamSetWrapper paramSetWrapper;

    private SynchronizedReport trainingReport;

    private String outDirPath;

    public TrainProcess(DatasetConfiguration datasetConfiguration, ParamSetWrapper paramSetWrapper, SynchronizedReport trainingReport, String outDirPath) {
        this.datasetConfiguration = datasetConfiguration;
        this.paramSetWrapper = paramSetWrapper;
        this.trainingReport = trainingReport;
        this.outDirPath = outDirPath;
    }


    public void configure() {

        ruleGenerator = new RuleGenerator();

        // configure role setter
        roleConfigurator = new RoleConfigurator(datasetConfiguration.label);

        List<String[]> roles = datasetConfiguration.generateRoles();

        if (datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
            String contrastAttr = datasetConfiguration.getOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE);

            // use annotation for storing contrast attribute info
            roleConfigurator.configureContrast(contrastAttr);
        }

        if (roles.size() > 0) {
            roleConfigurator.configureRoles(roles);
        }

        for (String key : paramSetWrapper.listKeys()) {
            Object o = paramSetWrapper.getParam(key);
            boolean paramOk = ruleGenerator.getRuleGeneratorParams().contains(key);

            if (paramOk)
                if (o instanceof String) {
                    ruleGenerator.getRuleGeneratorParams().setParameter(key, (String) o);
                } else if (o instanceof List) {
                    ruleGenerator.getRuleGeneratorParams().setListParameter(key, (List<String[]>) o);
                } else {
                    throw new InvalidParameterException("Invalid paramter type: " + key);
                }
            else {
                throw new InvalidParameterException("Undefined parameter: " + key);
            }
        }


    }

    public void executeProcess() throws IOException, OperatorCreationException, OperatorException, com.rapidminer.operator.OperatorException {

        // Train process
        if (datasetConfiguration.trainElements.size() > 0) {
            Logger.log("TRAINING\n"
                    + "Log file: " + trainingReport.getFile() + "\n", Level.INFO);

            for (TrainElement te : datasetConfiguration.trainElements) {
                Logger.log("Building model " + te.modelFile + " from dataset " + te.inFile + "\n", Level.INFO);
                File f = new File(te.modelFile);
                String modelFilePath = f.isAbsolute() ? te.modelFile : (outDirPath + "/" + te.modelFile);
                f = new File(te.inFile);
                String inFilePath = f.isAbsolute() ? te.inFile : (System.getProperty("user.dir") + "/" + te.inFile);

                f = new File(inFilePath);
                String trainFileName = f.getName();

                Logger.log("Train params: \n   Model file path: " + modelFilePath + "\n" +
                        "   Input file path: " + inFilePath + "\n", Level.FINE);

                IExampleSet sourceEs = new ArffFileLoader().load(inFilePath, datasetConfiguration.label);
                roleConfigurator.apply(sourceEs);

                RuleSetBase learnedModel = ruleGenerator.learn(sourceEs);
                ModelFileInOut.write(learnedModel, modelFilePath);

                writeModelToCsv(te.modelCsvFile, (RuleSetBase) learnedModel);

                IExampleSet appliedEs = learnedModel.apply(sourceEs);
                reportModelCharacteristic(learnedModel,  trainFileName);

                if (!datasetConfiguration.hasOptionParameter(ContrastRule.CONTRAST_ATTRIBUTE_ROLE)) {
                    RulePerformanceCounter rpc = new RulePerformanceCounter(appliedEs);
                    rpc.countValues();
                    reportTrainingPerformance(rpc.getResult());
                }

                Logger.log(" [OK]\n", Level.INFO);
            }
        }
    }

    private void writeModelToCsv(String modelCsvFile, RuleSetBase model) throws IOException {
        if (modelCsvFile != null) {
            File f = new File(modelCsvFile);
            String csvFilePath = f.isAbsolute() ? modelCsvFile : (outDirPath + "/" + modelCsvFile);
            BufferedWriter writer = new BufferedWriter(new FileWriter(csvFilePath));
            writer.write(model.toTable());
            writer.close();
        }
    }
    private void reportModelCharacteristic(RuleSetBase ruleModel , String trainFileName) throws IOException {
        // training report
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.repeat("=", 80));
        sb.append("\n");
        sb.append(trainFileName);
        sb.append("\n\n");
        sb.append(ruleModel.toString());

        sb.append("\nModel characteristics:\n");

        List<MeasuredPerformance> performance = RulePerformanceCounter.recalculatePerformance(ruleModel);
        for (MeasuredPerformance mp: performance) {
            sb.append(mp.getName()).append(": ").append(mp.getAverage()).append("\n");
        }

        trainingReport.append(sb.toString());
    }

    private void reportTrainingPerformance(List<MeasuredPerformance> performanceData) throws IOException {
        // training report
        StringBuilder sb = new StringBuilder();
        // if evaluator is enabled
        sb.append("\nTraining set performance:\n");
        // add performance
        for (MeasuredPerformance pc : performanceData) {
            double avg = pc.getAverage();
            sb.append(pc.getName()).append(": ").append(avg).append("\n");
        }
        trainingReport.append(sb.toString());
    }

}
