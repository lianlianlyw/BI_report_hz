package com.ebupt.mrrs.engine.metadata.entity;

import java.io.Serializable;
import java.util.List;

/**
 * 报表配置信息类
 * @author hlb
 * @since 2014-07-18
 *
 */
@SuppressWarnings("serial")
public class ReportInfoConfig implements Serializable{
	/**报表ID*/
	private String reportId;

	/**报表表名*/
	private String tableName;

	/**报表描述*/
	private String remark;

	/**报表指标信息*/
	private String measure;

	/**报表维度信息*/
	private String dimension;

	/**报表创建时间*/
	private String createTime;

	/**报表权限*/
	private String viewRole;

	/**报表url*/
	private String url;

	private List<Measure> measureList;

	private List<Dimension> dimensionList;

	//lyw报表所含维度组合
	private List<Drilldown> drilldownList;

	//lyw从元数据配置表中获取报表类型
	private String reportType;

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
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

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	public List<Measure> getMeasureList() {
		return measureList;
	}

	public void setMeasureList(List<Measure> measureList) {
		this.measureList = measureList;
	}

	public List<Dimension> getDimensionList() {
		return dimensionList;
	}

	public void setDimensionList(List<Dimension> dimensionList) {
		this.dimensionList = dimensionList;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getViewRole() {
		return viewRole;
	}

	public void setViewRole(String viewRole) {
		this.viewRole = viewRole;
	}

	//lyw
	public List<Drilldown> getDrilldownList() {
		return drilldownList;
	}

	public void setDrilldownList(List<Drilldown> drilldownList) {
		this.drilldownList = drilldownList;
	}

}
