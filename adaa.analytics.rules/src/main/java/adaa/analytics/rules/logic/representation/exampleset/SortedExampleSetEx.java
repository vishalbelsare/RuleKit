package adaa.analytics.rules.logic.representation.exampleset;

import adaa.analytics.rules.data.*;
import adaa.analytics.rules.data.metadata.EColumnSortDirections;
import adaa.analytics.rules.data.condition.ICondition;
import adaa.analytics.rules.data.metadata.EStatisticType;
import adaa.analytics.rules.data.row.Example;
import adaa.analytics.rules.logic.representation.IntegerBitSet;
import adaa.analytics.rules.logic.representation.rule.SurvivalRule;
import org.jetbrains.annotations.NotNull;
import tech.tablesaw.api.DoubleColumn;

import java.util.*;
import java.util.function.Consumer;

public class SortedExampleSetEx implements IExampleSet {

    private IExampleSet delegateExampleSet;

    public double[] labels;
    public double[] weights;
    public double[] labelsWeighted;
    public double[] totalWeightsBefore;
    public double meanLabel = 0;

    public Map<IAttribute, IntegerBitSet> nonMissingVals = new HashMap<>();

    public SortedExampleSetEx(IExampleSet parent, IAttribute sortingAttribute, EColumnSortDirections sortingDirection) {
        this.delegateExampleSet = parent;
        sortBy(sortingAttribute.getName(), sortingDirection);
        fillLabelsAndWeights();
    }

    protected final void fillLabelsAndWeights() {
        labels = new double[this.size()];
        labelsWeighted = new double[this.size()];
        weights = new double[this.size()];
        totalWeightsBefore = new double[this.size() + 1];

        boolean weighted = getAttributes().getWeight() != null;

        for (IAttribute a: this.getAttributes()) {
            nonMissingVals.put(a, new IntegerBitSet(this.size()));
        }

        int i = 0;
        int sumWeights = 0;

        for (Example e: this) {
            double y = e.getLabelValue();
            double w = weighted ? e.getWeightValue() : 1.0;

            labels[i] = y;
            weights[i] = w;
            labelsWeighted[i] = y * w;
            totalWeightsBefore[i] = sumWeights;
            meanLabel += y;

            for (IAttribute a: this.getAttributes()) {
                if (!Double.isNaN(e.getValue(a))) {
                    nonMissingVals.get(a).add(i);
                }
            }

            ++i;
            sumWeights += w;
        }

        totalWeightsBefore[size()] = sumWeights;
        meanLabel /= size();
    }




    @Override
    public DataTableAnnotations getAnnotations() {
        return delegateExampleSet.getAnnotations();
    }

    @Override
    public Object clone() {
        return delegateExampleSet.clone();
    }

    @Override
    public boolean equals(Object var1) {
        return delegateExampleSet.equals(var1);
    }

    @Override
    public int hashCode() {
        return delegateExampleSet.hashCode();
    }

    @Override
    public IAttributes getAttributes() {
        return delegateExampleSet.getAttributes();
    }

    @Override
    public int size() {
        return delegateExampleSet.size();
    }

    @Override
    public Example getExample(int var1) {
        return delegateExampleSet.getExample(var1);
    }

    @Override
    public IExampleSet filter(ICondition cnd) {
        return delegateExampleSet.filter(cnd);
    }

    @Override
    public IExampleSet filterWithOr(List<ICondition> cndList) {
        return delegateExampleSet.filterWithOr(cndList);
    }

    @Override
    public IExampleSet updateMapping(IExampleSet mappingSource) {
        return delegateExampleSet.updateMapping(mappingSource);
    }

    @NotNull
    @Override
    public Iterator<Example> iterator() {
        return delegateExampleSet.iterator();
    }

    @Override
    public void forEach(Consumer<? super Example> action) {
        delegateExampleSet.forEach(action);
    }

    @Override
    public Spliterator<Example> spliterator() {
        return delegateExampleSet.spliterator();
    }


    @Override
    public void sortBy(String columnName, EColumnSortDirections sortDir) {
        delegateExampleSet.sortBy(columnName, sortDir);
    }

    @Override
    public void addNewColumn(IAttribute var1) {
        delegateExampleSet.addNewColumn(var1);
    }

    @Override
    public DoubleColumn getDoubleColumn(IAttribute attr) {
        return delegateExampleSet.getDoubleColumn(attr);
    }

    @Override
    public double getDoubleValue( int colIdx, int rowIndex) {
        return delegateExampleSet.getDoubleValue(colIdx, rowIndex);
    }

    @Override
    public void setDoubleValue(IAttribute att, int rowIndex, double value) {
        delegateExampleSet.setDoubleValue(att, rowIndex, value);
    }

    @Override
    public int columnCount() {
        return delegateExampleSet.columnCount();
    }


    @Override
    public int getColumnIndex(String attributeName) {
        return delegateExampleSet.getColumnIndex(attributeName);
    }

    @Override
    public void recalculateStatistics(EStatisticType stateType, String colName) {
        delegateExampleSet.recalculateStatistics(stateType, colName);
    }
}