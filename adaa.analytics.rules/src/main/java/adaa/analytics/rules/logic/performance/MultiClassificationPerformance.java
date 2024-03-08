package adaa.analytics.rules.logic.performance;

import adaa.analytics.rules.rm.example.IAttribute;
import adaa.analytics.rules.rm.example.Example;
import adaa.analytics.rules.rm.example.IExampleSet;
import adaa.analytics.rules.rm.tools.Ontology;

import java.util.*;


/**
 * Measures the accuracy and classification error for both binary classification problems and multi
 * class problems. Additionally, this performance criterion can also compute the kappa statistics
 * for multi class problems. This is calculated as k = (P(A) - P(E)) / (1 - P(E)) with [ P(A) =
 * diagonal sum / number of examples ] and [ P(E) = sum over i of ((sum of i-th row * sum of i-th
 * column) / (n to the power of 2) ].
 *
 */
public class MultiClassificationPerformance extends AbstractPerformanceCounter {


    /**
     * Indicates accuracy.
     */
    public static final int ACCURACY = 0;

    /**
     * Indicates classification error.
     */
    public static final int ERROR = 1;

    /**
     * Indicates kappa statistics.
     */
    public static final int KAPPA = 2;

    /**
     * The names of the criteria.
     */
    public static final String[] NAMES = {"accuracy", "classification_error", "kappa"};

    /**
     * The counter for true labels and the prediction.
     */
    private double[][] counter;

    /**
     * The class names of the label. Used for logging and result display.
     */
    private String[] classNames;

    /**
     * Maps class names to indices.
     */
    private Map<String, Integer> classNameMap = new HashMap<String, Integer>();

    /**
     * The currently used label attribute.
     */
    private IAttribute labelAttribute;

    /**
     * The currently used predicted label attribute.
     */
    private IAttribute predictedLabelAttribute;

    /**
     * The weight attribute. Might be null.
     */
    private IAttribute weightAttribute;

    /**
     * The type of this performance: accuracy or classification error.
     */
    private int type = ACCURACY;


    /**
     * Creates a MultiClassificationPerformance with the given type.
     */
    public MultiClassificationPerformance(int type) {
        this.type = type;
    }


    /**
     * Initializes the criterion and sets the label.
     */
    @Override
    public void startCounting(IExampleSet eSet) {
        hasNominalLabels(eSet, "calculation of classification performance criteria");

        this.labelAttribute = eSet.getAttributes().getLabel();
        this.predictedLabelAttribute = eSet.getAttributes().getPredictedLabel();
        if (this.predictedLabelAttribute == null || !this.predictedLabelAttribute.isNominal()) {
            throw new IllegalStateException("calculation of classification performance criteria " + predictedLabelAttribute.getName());
        }

        this.weightAttribute = eSet.getAttributes().getWeight();

        Collection<String> labelValues = this.labelAttribute.getMapping().getValues();
        Collection<String> predictedLabelValues = this.predictedLabelAttribute.getMapping().getValues();

        // searching for greater mapping for making symmetric matrix in case of different mapping
        // sizes
        Collection<String> unionedMapping = new LinkedHashSet<String>(labelValues);
        unionedMapping.addAll(predictedLabelValues);

        this.counter = new double[unionedMapping.size()][unionedMapping.size()];
        this.classNames = new String[unionedMapping.size()];
        int n = 0;
        for (String labelValue : unionedMapping) {
            classNames[n] = labelValue;
            classNameMap.put(classNames[n], n);
            n++;
        }
    }

    private void hasNominalLabels(IExampleSet es, String algorithm) {

        if (es.getAttributes().getLabel() == null) {
            throw new IllegalStateException("Label is null");
        }
        IAttribute a = es.getAttributes().getLabel();
        if (!Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(), Ontology.NOMINAL)) {
            throw new IllegalStateException("Error in atributes: " + a.getName() + " " + algorithm);
        }
    }

    /**
     * Increases the prediction value in the matrix.
     */
    @Override
    public void countExample(Example example) {
        int label = classNameMap.get(example.getNominalValue(labelAttribute));
        int plabel = classNameMap.get(example.getNominalValue(predictedLabelAttribute));
        double weight = 1.0d;
        if (weightAttribute != null) {
            weight = example.getValue(weightAttribute);
        }
        counter[label][plabel] += weight;
    }

    /**
     * Returns either the accuracy or the classification error.
     */
    @Override
    public double getAverage() {
        double diagonal = 0, total = 0;
        for (int i = 0; i < counter.length; i++) {
            diagonal += counter[i][i];
            for (int j = 0; j < counter[i].length; j++) {
                total += counter[i][j];
            }
        }
        if (total == 0) {
            return Double.NaN;
        }

        // returns either the accuracy, the error, or the kappa statistics
        double accuracy = diagonal / total;
        switch (type) {
            case ACCURACY:
                return accuracy;
            case ERROR:
                return 1.0d - accuracy;
            case KAPPA:
                double pa = accuracy;
                double pe = 0.0d;
                for (int i = 0; i < counter.length; i++) {
                    double row = 0.0d;
                    double column = 0.0d;
                    for (int j = 0; j < counter[i].length; j++) {
                        row += counter[i][j];
                        column += counter[j][i];
                    }
                    // pe += ((row * column) / Math.pow(total, counter.length));
                    pe += row * column / (total * total);
                }
                return (pa - pe) / (1.0d - pe);
            default:
                throw new RuntimeException("Unknown type " + type + " for multi class performance criterion!");
        }
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName() {
        return NAMES[type];
    }
}
