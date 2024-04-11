package adaa.analytics.rules.rm.operator;

import adaa.analytics.rules.data.DataTable;

public class Annotations {

    private DataTable dataTable;

    public Annotations(DataTable dataTable) {
        this.dataTable = dataTable;
    }

    public void setAnnotation(String key, String value) {
        dataTable.setAnnotation(key, value);
    }

    public String getAnnotation(String key) {
        return dataTable.getAnnotation(key);
    }

    public int size() {
        return dataTable.sizeAnnotations();
    }

    public void clear() {
        dataTable.clearAnnotations();
    }

    public boolean containsKey(String key) {
        return dataTable.containsAnnotationKey(key);
    }

    public String get(Object key) {
        return key instanceof String ? this.getAnnotation((String)key) : null;
    }

    public String put(String key, String value) {
        this.setAnnotation(key, value);
        return value;
    }
}
