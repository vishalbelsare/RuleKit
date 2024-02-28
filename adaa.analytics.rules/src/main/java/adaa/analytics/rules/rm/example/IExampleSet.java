package adaa.analytics.rules.rm.example;

import adaa.analytics.rules.rm.example.table.IExampleTable;
import adaa.analytics.rules.rm.operator.IResultObject;

public interface IExampleSet extends IResultObject, Cloneable, Iterable<Example> {
    long serialVersionUID = 4100925167567270064L;

    Object clone();

    boolean equals(Object var1);

    int hashCode();

    default void cleanup() {
    }

    IAttributes getAttributes();

    int size();

    IExampleTable getExampleTable();

    Example getExampleFromId(double var1);

    int[] getExampleIndicesFromId(double var1);

    Example getExample(int var1);

    void remapIds();

//    void writeDataFile(File var1, int var2, boolean var3, boolean var4, boolean var5, Charset var6) throws IOException;
//
//    void writeAttributeFile(File var1, File var2, Charset var3) throws IOException;
//
//    void writeSparseDataFile(File var1, int var2, int var3, boolean var4, boolean var5, boolean var6, Charset var7) throws IOException;
//
//    void writeSparseAttributeFile(File var1, File var2, int var3, Charset var4) throws IOException;

    void recalculateAllAttributeStatistics();

    void recalculateAttributeStatistics(IAttribute var1);

    double getStatistics(IAttribute var1, String var2);

    double getStatistics(IAttribute var1, String var2, String var3);
}