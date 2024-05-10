package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.condition.AbstractCondition;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.condition.StringCondition;
import adaa.analytics.rules.data.IAttribute;
import adaa.analytics.rules.data.IExampleSet;
import adaa.analytics.rules.data.INominalMapping;
import adaa.analytics.rules.data.metadata.EStatisticType;

import java.util.ArrayList;
import java.util.List;

public class ContrastRegressionExampleSet extends ContrastExampleSet {

    /** Training set estimator. */
    protected double trainingEstimator;

    /** Collection of Kaplan-Meier estimators for contrast groups. */
    protected List<Double> groupEstimators = new ArrayList<Double>();

    /** Gets {@link #groupEstimators} */
    public List<Double> getGroupEstimators() { return groupEstimators; }

    /** Gets {@link #trainingEstimator}}. */
    public double getTrainingEstimator() { return trainingEstimator; }

    public ContrastRegressionExampleSet(IExampleSet exampleSet) {
        super(exampleSet);

        EStatisticType averageName = (exampleSet.getAttributes().getWeight() != null)
                ? EStatisticType.AVERAGE_WEIGHTED : EStatisticType.AVERAGE;

        // establish training  estimator
        IAttribute label = exampleSet.getAttributes().getLabel();
        label.recalculateStatistics();
        trainingEstimator = label.getStatistic(averageName);

        // establish contrast groups  estimator
//        try {
            INominalMapping mapping = contrastAttribute.getMapping();

            for(String value : mapping.getValues()) {
                ICondition cnd = new StringCondition(contrastAttribute.getName(), AbstractCondition.EComparisonOperator.EQUALS, value);

                //TODO jaki to ma sens?
                IExampleSet conditionedSet = exampleSet.filter(cnd);
                label = conditionedSet.getAttributes().getLabel();
                label.recalculateStatistics();
//                groupEstimators.add(conditionedSet.getStatistics(label, averageName));
                groupEstimators.add(label.getStatistic(averageName));
            }
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//        catch (ExpressionEvaluationException e) {
//            e.printStackTrace();
//        }
    }
}