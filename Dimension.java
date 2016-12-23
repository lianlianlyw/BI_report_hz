package com.ebupt.mrrs.engine.metadata.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * 报表知识库---维度实体
 * @author xienjiang
 * @since 2014-07-02 10:51
 */
@Entity
@Table(name="T_REPORT_DIMENSION")
//默认的缓存策略.
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Dimension implements Serializable {

	private static final long serialVersionUID = 1L;

	/*维度id 唯一标识*/
	private long id;
	/*维度名称*/
	private String name;
	/*维度中文名*/
	private String nameChn;
	/*维度中文描述*/
	private String description;
	/*维度最后创建或更新者*/
	private String lastCreator;
	/*维度最后创建或更新时间时间*/
	private String lastCreateTime;
	/*是否关联维表 0 未关联 1 关联*/
	private String assoFlag;
	/*关联维表表名    当维度关联维度时必填*/
	private String assoTableName;
	/**自定义维度枚举值*/
	@JsonIgnore
	private List<DimensionEnum> dimensionEnum = new ArrayList<DimensionEnum>();

	@Transient
	/**用来标示该维度是否是查询列
	 * 配置文件中的dimensions只列举非查询列
	 * parameterSet列举的是所有维度
	 * */
	private String isQuery;

	//用来存储配置文件中的ID
	@Transient
	private String parameterId;

    //维度所在业务表和业务表属主
    @Transient
    private  String tableOwner;

    @Transient
    private String tableName;

	@Transient
	//维度表属主
	private String dimTableOwner;

	@Transient
	//维度表表名
	private String dimTableName;

	public String getDimTableOwner() {
		return dimTableOwner;
	}

	public void setDimTableOwner(String dimTableOwner) {
		this.dimTableOwner = dimTableOwner;
	}

	public String getDimTableName() {
		return dimTableName;
	}

	public void setDimTableName(String dimTableName) {
		this.dimTableName = dimTableName;
	}

	public String getTableOwner() {
        return tableOwner;
    }

    public void setTableOwner(String tableOwner) {
        this.tableOwner = tableOwner;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getParameterId() {
		return parameterId;
	}

	public void setParameterId(String parameterId) {
		this.parameterId = parameterId;
	}

	public String getIsQuery() {
		return isQuery;
	}

	public void setIsQuery(String isQuery) {
		this.isQuery = isQuery;
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
	@Column(name = "last_creator",length=32)
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

	@Column(name = "asso_flag",nullable=false,length=2)
	public String getAssoFlag() {
		return assoFlag;
	}
	public void setAssoFlag(String assoFlag) {
		this.assoFlag = assoFlag;
	}
	@Column(name = "asso_table_name",length=32)
	public String getAssoTableName() {
		return assoTableName;
	}
	public void setAssoTableName(String assoTableName) {
		this.assoTableName = assoTableName;
	}

	//使用@OneToMany和@JoinColumn来标注，这种方式是在多的一方的表中增加一个外键列来保存关系
	@OneToMany(cascade={CascadeType.ALL},mappedBy="dimensionId", orphanRemoval=true)
	@Fetch(FetchMode.SUBSELECT)
	public List<DimensionEnum> getDimensionEnum() {
		return dimensionEnum;
	}
	public void setDimensionEnum(List<DimensionEnum> dimensionEnum) {
		this.dimensionEnum = dimensionEnum;
	}

	public void addDimensionEnum(DimensionEnum dimensionEnum){
		dimensionEnum.setDimensionId(this);
		this.dimensionEnum.add(dimensionEnum);
	}
	//重定义equals方法
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Dimension other = (Dimension) obj;
		if(!this.getName().equals(other.getName()))
			return false;
		return true;
	}
}
