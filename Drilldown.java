package com.ebupt.mrrs.engine.metadata.entity;

import java.io.Serializable;
import java.util.List;


/**
 * 报表维度组合类
 * by lyw
 */
@SuppressWarnings("serial")
public class Drilldown  implements Serializable{
    /**维度组合所属报表id*/
    private String reportId;

    /**维度组合对应业务表的属主*/
    private String tableOwner;

    /**维度组合对应的业务表名称*/
    private String tableName;

    /**维度组合对应的sqlId*/
    private String querySqlId;

    /**维度组合名字*/
    private String drilldownName;

    /**维度组合包含的维度*/
    private List<Dimension> dimensions;

    /**维度组合中的parameterSet
     * 所有的维度，包括查询列和非查询列，即配置文件中的parameterSet（所有维度组合的集合去重是报表的所有维度）
     * */
    //private List<Dimension> parameterSet;

    /**维度组合包含的指标*/
    private List<Measure> measures;

    /**配置文件中xmlID*/
    private String drilldownId;

    public String getDrilldownId() {
        return drilldownId;
    }

    public void setDrilldownId(String drilldownId) {
        this.drilldownId = drilldownId;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    /*public List<Dimension> getParameterSet() {
        return parameterSet;
    }

    public void setParameterSet(List<Dimension> parameterSet) {
        this.parameterSet = parameterSet;
    }*/

    public void setMeasures(List<Measure> measures) {
        this.measures = measures;
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void setDimensions(List<Dimension> dimensions) {
        this.dimensions = dimensions;
    }

    public String getTableOwner() {
        return tableOwner;
    }

    public void setTableOwner(String tableOwner) {
        this.tableOwner = tableOwner;
    }

    public String getDrilldownName() {
        return drilldownName;
    }

    public void setDrilldownName(String drilldownName) {
        this.drilldownName = drilldownName;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    public String getQuerySqlId() {
        return querySqlId;
    }

    public void setQuerySqlId(String querySqlId) {
        this.querySqlId = querySqlId;
    }
}
