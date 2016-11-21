package com.ebupt.mrrs.engine.metadata.entity;

import java.io.Serializable;

import javax.persistence.*;

//import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/*
 * 报表知识库---指标实体
 * @author xienjiang
 * @since 2014-07-02 10:51
 */
@Entity
@Table(name="T_REPORT_MEASURE")
//默认的缓存策略.
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Measure implements Serializable {

	private static final long serialVersionUID = 1L;

	/*指标id 唯一标识*/
	private long id;
	/*指标名称*/
	private String name;
	/*指标中文名*/
	private String nameChn;
	/*指标中文描述*/
	private String description;
	/*指标统计口径*/
	private String statistical;
	/*指标最后创建或更新者*/
	private String lastCreator;
	/*指标最后创建或更新时间时间*/
	private String lastCreateTime;

	/**指标对应业务表表名属主*/
	@Transient
	private String tableOwner;

	/**指标对应数据库业务表表名*/
	@Transient
	private String tableName;

	@Transient
	public String getTableOwner() {
		return tableOwner;
	}

	public void setTableOwner(String tableOwner) {
		this.tableOwner = tableOwner;
	}

	@Transient
	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Id
	@Column(name = "id")
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	
	@Column(name = "name",nullable=false,length=32)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name = "name_chn")
	public String getNameChn() {
		return nameChn;
	}
	public void setNameChn(String nameChn) {
		this.nameChn = nameChn;
	}
	@Column(name = "description_")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	@Column(name = "last_creator")
	public String getLastCreator() {
		return lastCreator;
	}
	public void setLastCreator(String lastCreator) {
		this.lastCreator = lastCreator;
	}
	@Column(name = "last_create_time",length=32)
	public String getLastCreateTime() {
		return lastCreateTime;
	}
	public void setLastCreateTime(String lastCreateTime) {
		this.lastCreateTime = lastCreateTime;
	}
	@Column(name = "statistical")
	public String getStatistical() {
		return statistical;
	}
	public void setStatistical(String statistical) {
		this.statistical = statistical;
	}
	
//	@Override
//	public String toString() {
//		return ToStringBuilder.reflectionToString(this);
//	}
	
}
