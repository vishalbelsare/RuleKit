package adaa.analytics.rules;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.Test;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.ExampleSetFactory;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.operator.preprocessing.filter.ChangeAttributeRole;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.OperatorService;

import adaa.analytics.rules.logic.induction.AbstractSeparateAndConquer;
import adaa.analytics.rules.logic.induction.ActionFinder;
import adaa.analytics.rules.logic.induction.ActionSnC;
import adaa.analytics.rules.logic.induction.ClassificationFinder;
import adaa.analytics.rules.logic.induction.ClassificationSnC;
import adaa.analytics.rules.logic.induction.Covering;
import adaa.analytics.rules.logic.induction.InductionParameters;
import adaa.analytics.rules.logic.quality.ClassificationMeasure;
import adaa.analytics.rules.logic.representation.*;
import common.Assert;

@RunWith(Parameterized.class)
public class ActionTests {
	protected static String testDirectory =  "C:/Users/pmatyszok/Desktop/dane/";
	
	protected InductionParameters params;
	protected String testFile;
	protected String outputFileName;
	protected com.rapidminer.Process process;
	protected ExampleSet exampleSet;
	protected String labelParameter;
	
	@Parameters
	public static Collection<Object[]> testData(){
		return Arrays.asList(new Object[][]{
			//fileName, labelName, measure, pruningEnabled, ignoreMissing, minCov, maxUncov, maxGrowing
		
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"car-reduced.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},
			
			////
			////  Wine dataset
			////
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"wine.arff", "class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},

			///
			/// Monks 1 dataset
			///
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9},
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9},
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"monks1.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},
			
			////
			////  Sonar dataset
			////
			/*{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.RSS), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.C2), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.C2), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Correlation), false, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), true, true, 5.0, 0.05, 0.9},
			{"sonar.arff", "Class", new ClassificationMeasure(ClassificationMeasure.Precision), false, true, 5.0, 0.05, 0.9},
			*/
		});
	}
	
	public ActionTests(String testFileName, String labelParameterName,
			ClassificationMeasure measure,
			boolean enablePruning, boolean ignoreMissing, double minimumCovered,
			double maximumUncoveredFraction, double maxGrowingConditions) {
		testFile = testFileName;
		labelParameter = labelParameterName;
		
		outputFileName = testFileName.substring(0, testFileName.indexOf('.'));
		outputFileName += "-rules-" + measure.getName() + (enablePruning  ? "-pruned" : "")  + ".arff";
		
		params = new InductionParameters();
		params.setInductionMeasure(measure);
		params.setPruningMeasure(measure);
		params.setEnablePruning(enablePruning);
		params.setIgnoreMissing(ignoreMissing);
		params.setMinimumCovered(minimumCovered);
		params.setMaximumUncoveredFraction(maximumUncoveredFraction);
		params.setMaxGrowingConditions(maxGrowingConditions);
		
	}
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		RapidMiner.init();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	protected ExampleSet parseArffFile() throws OperatorException, OperatorCreationException {
		ArffExampleSource arffSource = (ArffExampleSource)OperatorService.createOperator(ArffExampleSource.class);
		//role setter allows for deciding which attribute is class attribute
		ChangeAttributeRole roleSetter = (ChangeAttributeRole)OperatorService.createOperator(ChangeAttributeRole.class);
		
		File arffFile = Paths.get(testDirectory, testFile).toFile();
		
		arffSource.setParameter(ArffExampleSource.PARAMETER_DATA_FILE, arffFile.getAbsolutePath());
		roleSetter.setParameter(ChangeAttributeRole.PARAMETER_NAME, labelParameter);
    	roleSetter.setParameter(ChangeAttributeRole.PARAMETER_TARGET_ROLE, Attributes.LABEL_NAME);
		
    	process = new com.rapidminer.Process();
		process.getRootOperator().getSubprocess(0).addOperator(arffSource);
		process.getRootOperator().getSubprocess(0).addOperator(roleSetter);
		
		arffSource.getOutputPorts().getPortByName("output").connectTo(
				roleSetter.getInputPorts().getPortByName("example set input"));
		
		roleSetter.getOutputPorts().getPortByName("example set output").connectTo(
				process.getRootOperator().getSubprocess(0).getInnerSinks().getPortByIndex(0));
		
		IOContainer c = process.run();
		//parsed arff file
		return (ExampleSet)c.getElementAt(0);
	}
	
	@Test
	public void testActionRuleCoverage() {
		List<String> mapping = Arrays.asList(new String[]{"v0", "v1", "v2"});
		List<String> mapping2 = Arrays.asList(new String[]{"vA", "vB", "vC"});
		SingletonSet s1 = new SingletonSet(0, mapping);
		SingletonSet s2 = new SingletonSet(1, mapping);
		Action fullAction = new Action("a1", s1, s2);
		SingletonSet s3 = new SingletonSet(0, mapping2);
		Action loosedAction = new Action("a2", s3, s3);
		loosedAction.setActionNil(true);
		
		CompoundCondition cc = new CompoundCondition();
		cc.addSubcondition(fullAction);
	
		List<String> klass = Arrays.asList(new String[]{"class1", "class2"});
		SingletonSet c1 = new SingletonSet(0, klass);
		SingletonSet c2 = new SingletonSet(1, klass);
		Action con = new Action("class", c1, c2);
		
		ActionRule rule = new ActionRule(cc, con);
		
		Object[][] data =// new Object[0][0]; 
			{
					{"v0", "vA", "class1"},
					{"v0", "vA", "class1"},
					{"v1", "vC", "class2"},
					{"v1", "vB", "class2"}
			};
		
		ExampleSet set = ExampleSetFactory.createExampleSet(data);
		
		Iterator<Attribute> atr = set.getAttributes().allAttributes();
		Attribute atr0 = atr.next();
		Attribute atr1 = atr.next();
		Attribute atrclass = atr.next();
		
		atr0.setName("a1");
		atr1.setName("a2");
		
		atrclass.setName("class");
		
		set.getAttributes().setLabel(atrclass);
		set.getAttributes().setSpecialAttribute(atrclass, "label");
		
		Covering cov = rule.covers(set);
		junit.framework.Assert.assertEquals(cov.weighted_p, 2.0);
		junit.framework.Assert.assertEquals(cov.weighted_P, 2.0);
		junit.framework.Assert.assertEquals(cov.weighted_n, 0.0);
		junit.framework.Assert.assertEquals(cov.weighted_N, 2.0);
		
		Covering actionCov = rule.actionCovers(set);
		junit.framework.Assert.assertEquals(actionCov.weighted_p, 2.0);
		junit.framework.Assert.assertEquals(actionCov.weighted_P, 2.0);
		junit.framework.Assert.assertEquals(actionCov.weighted_n, 0.0);
		junit.framework.Assert.assertEquals(actionCov.weighted_N, 2.0);
		
		
		rule.getPremise().addSubcondition(loosedAction);
		
		cov = rule.covers(set);
		junit.framework.Assert.assertEquals(cov.weighted_p, 2.0);
		junit.framework.Assert.assertEquals(cov.weighted_P, 2.0);
		junit.framework.Assert.assertEquals(cov.weighted_n, 0.0);
		junit.framework.Assert.assertEquals(cov.weighted_N, 2.0);
		
		actionCov = rule.actionCovers(set);
		junit.framework.Assert.assertEquals(actionCov.weighted_p, 2.0);
		junit.framework.Assert.assertEquals(actionCov.weighted_P, 2.0);
		junit.framework.Assert.assertEquals(actionCov.weighted_n, 0.0);
		junit.framework.Assert.assertEquals(actionCov.weighted_N, 2.0);
	}
	
	@Test
	public void test() throws OperatorCreationException, OperatorException, IOException {
		
		ExampleSet exampleSet = parseArffFile();
		
		AbstractSeparateAndConquer snc = new ActionSnC(new ActionFinder(params), params);
		ActionRuleSet actions = (ActionRuleSet)snc.run(exampleSet);
		
		//AbstractSeparateAndConquer snc = new ClassificationSnC(new ClassificationFinder(params), params);
		//RuleSetBase set = snc.run(exampleSet);
		//System.out.println(set.toString());
		File arffFile = Paths.get(testDirectory, this.outputFileName).toFile();
		
		Long loosedActionsCount = actions.getRules().stream().map(x -> (ActionRule)x).
			mapToLong(x -> x.getPremise().
							getSubconditions().
							stream().
							map(y -> (Action)y).mapToLong(z -> (z.getActionNil() ? 1L : 0L)).sum()).sum();
		
		FileWriter fw = new FileWriter(arffFile);
		fw.write("File name: " + testFile + "\r\n");
		fw.write("Pruning: " + params.isPruningEnabled() + "\r\n");
		fw.write("Loosed actions count" + loosedActionsCount + "\r\n");
		fw.write(actions.toString());
		fw.close();
		
		
		System.out.println("File name: " + testFile);
		System.out.println("Pruning: " + params.isPruningEnabled());
		//System.out.println("Loosed actions count" + loosedActionsCount);
	//	System.out.println("Measure: " + ((ClassificationMeasure)params.getPruningMeasure()).getName(params.getPruningMeasure()));
		System.out.println(actions.toString());
	}

}
