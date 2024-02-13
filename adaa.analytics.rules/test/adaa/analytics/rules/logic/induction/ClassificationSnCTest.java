package adaa.analytics.rules.logic.induction;

import adaa.analytics.rules.logic.representation.model.RuleSetBase;
import adaa.analytics.rules.logic.rulegenerator.OperatorCommandProxy;
import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import utils.RuleSetComparator;
import utils.TestResourcePathFactory;
import utils.config.TestConfig;
import utils.config.TestConfigParser;
import utils.reports.Const;
import utils.reports.TestReportWriter;
import utils.testcases.TestCase;
import utils.testcases.TestCaseFactory;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

@RunWith(Theories.class)
public class ClassificationSnCTest {

    private static final String CLASS_NAME = ClassificationSnCTest.class.getSimpleName();
    private static final String REPORTS_DIRECTORY = Const.REPORTS_IN_DIRECTORY_PATH + CLASS_NAME + '/';

    @DataPoints("Test cases")
    public static TestCase[] getTestCases() throws ParseException {
        String configFilePath = Const.CONFIG_DIRECTORY + CLASS_NAME + ".xml";
        configFilePath = TestResourcePathFactory.get(configFilePath).toString();
        HashMap<String, TestConfig> configs = new TestConfigParser().parse(configFilePath);
        List<TestCase> testCases = TestCaseFactory.make(configs, REPORTS_DIRECTORY);
        return testCases.toArray(new TestCase[0]);
    }

    public ClassificationSnCTest() {
        File directory = new File(Const.REPORTS_OUT_DIRECTORY_PATH + CLASS_NAME);
        if (! directory.exists()){
            directory.mkdir();
        }
    }

    private void writeReport(TestCase testCase, RuleSetBase ruleSet) {
        if (!testCase.isUsingExistingReportFile()) {
            try {
                TestReportWriter reportWriter = new TestReportWriter(CLASS_NAME + '/' + testCase.getName());
                reportWriter.write(ruleSet);
                reportWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void test_confidence(IExampleSet prediction) {
        for (int i = 0; i < prediction.size(); i++) {
            Example example = prediction.getExample(i);
            IAttribute label = example.getAttributes().getLabel();
            List<String> labelValues = label.getMapping().getValues();

            double confidenceSum = 0;
            for (String labelValue : labelValues) {
                confidenceSum += example.getValue(example.getAttributes().get("confidence_" + labelValue));
            }
            Assert.assertEquals(1.0, confidenceSum, 0.01);
        }
    }

    @Theory
    public void runTestCase(@FromDataPoints("Test cases") TestCase testCase) throws OperatorException, OperatorCreationException, IOException {
        ClassificationFinder finder = new ClassificationFinder(testCase.getParameters());
        ClassificationSnC snc = new ClassificationSnC(finder, testCase.getParameters());
        snc.setOperatorCommandProxy(new OperatorCommandProxy());
        RuleSetBase ruleSet = snc.run(testCase.getExampleSet());

        IExampleSet prediction = ruleSet.apply(testCase.getExampleSet());
        test_confidence(prediction);

        this.writeReport(testCase, ruleSet);
        RuleSetComparator.assertRulesAreEqual(testCase.getReferenceReport().getRules(), ruleSet.getRules());
    }
}