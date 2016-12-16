package com.ebupt.mrrs.dao.rptmanage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.ebupt.mrrs.engine.metadata.entity.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springside.modules.orm.Page;
import org.springside.modules.orm.hibernate.HibernateDao;

import com.ebupt.mrrs.entity.report.ReportDirectory;


/**
 * 报表管理DAO
 * @author hlb
 *
 */
@Repository
public class ReportInfoDao extends HibernateDao<ReportInfo,String>  {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 保存报表数据至草稿表
	 * @param entity
	 */
	public void saveDraft(ReportInfo entity){
		String sep = " , ";
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("INSERT INTO T_REPORT_INFO_DRAFT(TABLE_ID,TABLE_NAME,REMARK,ENABLE_SUB,PUBLISH_DIR,PUBLISH_URL,CREATOR,CREATE_TIME,STATE)")
				.append(" VALUES (")
				.append("'" + entity.getReportId() + "'").append(sep)
				.append("'" + entity.getTableName() + "'").append(sep)
				.append("'" + entity.getDescription() + "'").append(sep)
				.append("'" + entity.getEnableSub() + "'").append(sep)
				.append("'" + entity.getPublishDir() + "'").append(sep)
				.append("'" + entity.getPublishUrl() + "'").append(sep)
				.append("'" + entity.getCreator() + "'").append(sep)
				.append("'" + entity.getCreateTime() + "'").append(sep)
				.append("'0')");
		jdbcTemplate.update(sqlBuff.toString());
	}

	public void deleteDraft(ReportInfo entity){
		String sql = "DELETE FROM T_REPORT_INFO_DRAFT WHERE TABLE_ID = '"+entity.getReportId()+"'";
		jdbcTemplate.update(sql);
	}

	public void deleteConfig(String id){
		String sql = "DELETE FROM T_REPORT_CONFIG WHERE REPORT_ID = '"+id+"'";
		jdbcTemplate.update(sql);
	}

	/**
	 * 更新草稿表数据 当报表为正式发布状态再编辑信息时，状态置为2 编辑待发布
	 * @param entity
	 */
	public void updateDraft(ReportInfo entity){
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("UPDATE T_REPORT_INFO_DRAFT A SET " +
				" A.REMARK=?" +
				" ,A.ENABLE_SUB=?" +
				" ,A.PUBLISH_DIR=?" +
				" ,A.PUBLISH_URL=?" +
				" ,A.CREATOR=?" +
				" ,A.CREATE_TIME=?"+
				" ,A.STATE=?"+
				" WHERE A.TABLE_ID=?");
		Object[] args = {
				entity.getDescription(),
				entity.getEnableSub(),
				entity.getPublishDir(),
				entity.getPublishUrl(),
				entity.getCreator(),
				entity.getCreateTime(),
				entity.getState(),
				entity.getReportId()
		};
		System.out.println(entity.getEnableSub());
		System.out.println(sqlBuff.toString());
		jdbcTemplate.update(sqlBuff.toString(),args);
	}

	public boolean checkDraft(ReportInfo entity){
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("SELECT COUNT(*) FROM T_REPORT_INFO_DRAFT WHERE TABLE_ID='"+entity.getReportId()+"'");
		long result = jdbcTemplate.queryForLong(sqlBuff.toString());
		if(result==0){
			return false;
		}else{
			return true;
		}
	}

	public boolean checkReportConfig(ReportInfoConfig entity){
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("SELECT COUNT(*) FROM T_REPORT_CONFIG WHERE REPORT_ID='"+entity.getReportId()+"'");
		long result = jdbcTemplate.queryForLong(sqlBuff.toString());
		if(result==0){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 保存报表数据至草稿表
	 * @param entity
	 */
	public void saveReportConfig(ReportInfoConfig entity){
		String sep = " , ";
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("INSERT INTO T_REPORT_CONFIG(REPORT_ID,REMARK,MEASURE,DEMENSION)")
				.append(" VALUES (")
				.append("'" + entity.getReportId() + "'").append(sep)
				.append("'" + entity.getRemark() + "'").append(sep)
				.append("'" + entity.getMeasure() + "'").append(sep)
				.append("'" + entity.getDimension() + "')");
		jdbcTemplate.update(sqlBuff.toString());
	}

	public void updateReportConfig(ReportInfoConfig entity){
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("UPDATE T_REPORT_CONFIG SET " +
				" ,REMARK=?" +
				" ,MEASURE=?" +
				" ,DEMENSION=?" +
				" WHERE REPORT_ID=?");
		Object[] args = {
				entity.getRemark(),
				entity.getMeasure(),
				entity.getDimension(),
				entity.getReportId()
		};
		jdbcTemplate.update(sqlBuff.toString(),args);
	}

	/**
	 * 获取指标分页数据
	 * @param parameter
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Page getPageList(final Map<String, Object> parameter,final Page pageParams) {
		final String sql = buildQuerySql(parameter,pageParams);
		final String countSql = buildCountSql(parameter);

		List dataList = jdbcTemplate.queryForList(sql);
		long count = jdbcTemplate.queryForLong(countSql);

		pageParams.setResult(dataList);
		pageParams.setTotalCount(count);

		return pageParams;
	}

	private String buildQuerySql(final Map<String, Object> parameter,final Page pageParams) {
		int startIndex = (pageParams.getPageNo() - 1) * pageParams.getPageSize();
		int endIndex = pageParams.getPageNo()  * pageParams.getPageSize();

		String orderBy = pageParams.getOrderBy().toString();
		String order = pageParams.getOrder().toString();

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM ( SELECT T.*,row_number() over( order by " + orderBy + " " + order + ") rn " +
				"  FROM (SELECT " +
				"   TABLE_ID as id," +
				"   TABLE_NAME as tableName," +
				"   remark as remark," +
				"   ENABLE_SUB as enableSub," +
				"   B.DIRNAME as dirName," +
				"   PUBLISH_URL as url," +
				"   CREATOR as creator," +
				"   CREATE_TIME as createTime," +
				"   STATE as STATE" +
				" FROM T_REPORT_INFO_DRAFT A,T_REPORT_DIRECTORY B" +
				" WHERE 1=1 AND A.PUBLISH_DIR=B.DIR_ID");

		Object searchKey = parameter.get("searchKey");
		if(searchKey != null){
			sql.append(" AND A.table_name like '%" + searchKey + "%'") ;
		}
		if(parameter.get("state") != null){
			sql.append(" AND A.state ='" + parameter.get("state").toString() + "'") ;
		}
		if((parameter.get("rid") != null) && (!parameter.get("rid").equals("null"))){
			sql.append(" AND A.publish_dir ='" + parameter.get("rid").toString() + "'") ;
		}
		sql.append(" )T )");
		sql.append(" WHERE rn > " + startIndex + " AND rn <= " + endIndex);
		return sql.toString();
	}

	private String buildCountSql(Map<String, Object> parameter){
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT count(*)" +
				" FROM T_REPORT_INFO A " +
				" WHERE 1=1");
		Object searchKey = parameter.get("searchKey");
		if(searchKey != null){
			sql.append("AND (A.TABLE_NAME like '%" + searchKey + "%' or A.REMARK like '%" + searchKey + "%')") ;
		}
		return sql.toString();
	}

	public ReportInfo getDraft(final String id) {
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("SELECT" +
				" TABLE_ID " +
				" ,TABLE_NAME " +
				" ,REMARK" +
				" ,ENABLE_SUB" +
				" ,PUBLISH_DIR" +
				" ,B.DIRNAME" +
				" ,PUBLISH_URL" +
				" ,CREATOR" +
				" ,CREATE_TIME"+
				" ,STATE"+
				" FROM T_REPORT_INFO_DRAFT A,T_REPORT_DIRECTORY B" +
				" WHERE A.PUBLISH_DIR=B.DIR_ID");
		if(StringUtils.isNotBlank(id)){
			sqlBuff.append(" AND A.TABLE_ID='").append(id).append("'");
		}
		System.out.println(sqlBuff.toString());

		final ReportInfo reportInfo = new ReportInfo();
		jdbcTemplate.query(sqlBuff.toString(), new RowCallbackHandler(){
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				reportInfo.setTableName(rs.getString("TABLE_NAME"));
				reportInfo.setDescription(rs.getString("REMARK"));
				reportInfo.setEnableSub(rs.getString("ENABLE_SUB"));
				reportInfo.setPublishDir(rs.getString("PUBLISH_DIR"));
				reportInfo.setDirName(rs.getString("DIRNAME"));
				reportInfo.setPublishUrl(rs.getString("PUBLISH_URL"));
				reportInfo.setCreator(rs.getString("CREATOR"));
				reportInfo.setCreateTime(rs.getString("CREATE_TIME"));
				reportInfo.setState(rs.getString("STATE"));
				reportInfo.setReportId(rs.getString("TABLE_ID"));

			}});

		return reportInfo;
	}

	/**
	 * 保存报表权限项
	 */
	public void saveAuthority(final String id,final List<String[]> paramsList) {
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("INSERT INTO T_REPORT_AUTHORITY(REPORT_ID,DISPLAY_NAME,NAME,URL,ID)VALUES (?,?,?,?,?)");

		jdbcTemplate.batchUpdate(sqlBuff.toString(), new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				ps.setString(1, id);
				ps.setString(2, paramsList.get(i)[0]);
				ps.setString(3, paramsList.get(i)[1]);
				ps.setString(4, paramsList.get(i)[2]);
				ps.setString(5, String.valueOf(System.currentTimeMillis()));
			}
			@Override
			public int getBatchSize() {
				return paramsList.size();
			}
		});
	}

	public List<ReportDirectory> getReportMenu() {

		StringBuffer sql = new StringBuffer();
		final List<ReportDirectory> reportDirList = new ArrayList<ReportDirectory>();
		sql.append("select" +
				"   a.dir_id as dirId," +
				"   a.dirname as title," +
				"   a.parent_id as parentId" +
				"   from T_REPORT_DIRECTORY a" +
				"   where a.dir_id != 1");
		System.out.println(sql);
		jdbcTemplate.query(sql.toString(), new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				do{
					ReportDirectory record = new ReportDirectory();
					record.setDirId(rs.getString("dirId"));
					record.setParentId(rs.getString("parentId"));
					record.setTitle(rs.getString("title"));
					reportDirList.add(record);
				}while(rs.next());
			}
		});
		return reportDirList;
	}
	//完成配置文件后，修改报表元数据配置表中报表状态
    public void updateReportMataData(String tableName){
        if(StringUtils.isBlank(tableName)){
            return;
        }
        StringBuffer sqlBuff = new StringBuffer();
        sqlBuff.append("UPDATE CONF_REPORT_METADATA SET STATE = '2' WHERE REPORT_NAME_CHN = '" + tableName +"' ");

        jdbcTemplate.update(sqlBuff.toString());
    }

    //根据报表名字更新报表ID 状态 描述 目录信息
    public void  updateDraftByName(ReportInfo reportInfo){
        StringBuffer sqlBuff = new StringBuffer();
        sqlBuff.append("UPDATE T_REPORT_INFO_DRAFT " +
                        " SET STATE = '" + reportInfo.getState() + "' , " +
                        " TABLE_ID = '"+ reportInfo.getReportId() +"' , " +
                        " REMARK = '" + reportInfo.getDescription() +"' , " +
                        " PUBLISH_DIR = '"+ reportInfo.getPublishDir() +"' " +
                        " WHERE TABLE_NAME = '" + reportInfo.getTableName() +"' ");

        jdbcTemplate.update(sqlBuff.toString());
    }

	//获取可以进行配置的报表信息lyw
	public Page getNewDataReport() {
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("SELECT DISTINCT" +
				" (REPORT_NAME_CHN)," +
				" REPORT_TYPE " +
				" FROM CONF_REPORT_METADATA" +
				" WHERE STATE = '0' OR STATE = '1' ");
		System.out.println(sqlBuff.toString());

		//final List<ReportInfo> reportInfos = new ArrayList<ReportInfo>();
		final List<ReportInfoConfig> reportInfoConfigList = new ArrayList<ReportInfoConfig>();

		jdbcTemplate.query(sqlBuff.toString(), new RowCallbackHandler(){
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				do{
					//ReportInfo reportInfo = new ReportInfo();
					//reportInfo.setTableName(rs.getString("REPORT_NAME_CHN"));
					//reportInfos.add(reportInfo);
					ReportInfoConfig reportInfoConfig = new ReportInfoConfig();
					reportInfoConfig.setReportType(rs.getString("REPORT_TYPE"));
					reportInfoConfig.setTableName(rs.getString("REPORT_NAME_CHN"));
					reportInfoConfigList.add(reportInfoConfig);
				}while(rs.next());

			}});

		for(ReportInfoConfig rInfoConfig : reportInfoConfigList){
			if(rInfoConfig.getReportType().equals("1")){
				rInfoConfig.setReportType("日报表");
			}else if (rInfoConfig.getReportType().equals("2")){
				rInfoConfig.setReportType("周报表");
			}else if (rInfoConfig.getReportType().equals("3")){
				rInfoConfig.setReportType("当月累计报表");
			}else{
				rInfoConfig.setReportType("月报表");
			}
			rInfoConfig = getDrilldownsByReport(rInfoConfig);

			StringBuffer drill = new StringBuffer();
			for(Drilldown drilldown :rInfoConfig.getDrilldownList()){
				drill.append(drilldown.getDrilldownName()+",");
			}
			drill.deleteCharAt(drill.length()-1);
			rInfoConfig.setDrilldownName(drill.toString());

			//报表添加维度
			/*HashSet<Dimension> hs = new HashSet<Dimension>(dimensionListR);
			rInfoConfig.setDimensionList(dimensionListR);
			HashSet<Measure> hsm = new HashSet<Measure>(measureListR);
			rInfoConfig.setMeasureList(measureListR);*/

		}

		Page page = new Page();
		page.setResult(reportInfoConfigList);
		page.setTotalCount(reportInfoConfigList.size());
		page.setPageSize(10);

		return page;
	}

	//获取报表的维度组合信息
	public ReportInfoConfig getDrilldownsByReport(ReportInfoConfig reportInfoConfig){
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("SELECT" +
				" DRILL_NAME_CHN, " +
				" TABLE_NAME, " +
				" TABLE_OWNER " +
				" FROM CONF_REPORT_METADATA" +
				" WHERE  REPORT_NAME_CHN = '" + reportInfoConfig.getTableName() + "' " );
		final List<Drilldown> drilldownList = new ArrayList<Drilldown>();
		//为每张报表添加维度组合

		jdbcTemplate.query(sqlBuff.toString(), new RowCallbackHandler(){
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				do{
					Drilldown drilldown = new Drilldown();
					drilldown.setDrilldownName(rs.getString("DRILL_NAME_CHN"));
					drilldown.setTableName(rs.getString("TABLE_NAME"));
					drilldown.setTableOwner(rs.getString("TABLE_OWNER"));
					drilldownList.add(drilldown);
				}while(rs.next());

			}});

		reportInfoConfig.setDrilldownList(drilldownList);

		return reportInfoConfig;
	}

	//根据维度组合获取维度和指标信息
	public List<Drilldown> getDeminsonsMeasuresByDrilldown (List<Drilldown> drilldownList){
		for(Drilldown drilldown :drilldownList){
			StringBuffer sqlBuff = new StringBuffer();
			sqlBuff.append("SELECT" +
					" COL_NAME, " +
					" COL_CHN_NAME, " +
                    " TABLE_OWNER," +
                    " TABLE_NAME, " +
					" IS_QUERY " +
					" FROM CONF_REPROT_TABLE_DICTIONARY " +
					" WHERE  TABLE_OWNER = '" + drilldown.getTableOwner() + "' " +
					" AND TABLE_NAME = '" + drilldown.getTableName() + "' " +
					" AND COL_TYPE = '1' " );//查出维度-查询列和非查询列
			final List<Dimension> dimensionList = new ArrayList<Dimension>();
			//添加维度

			jdbcTemplate.query(sqlBuff.toString(), new RowCallbackHandler(){
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					do{
						Dimension dimension = new Dimension();
						dimension.setName(rs.getString("COL_NAME"));
						dimension.setNameChn(rs.getString("COL_CHN_NAME"));
						dimension.setIsQuery(rs.getString("IS_QUERY"));
                        dimension.setTableOwner(rs.getString("TABLE_OWNER"));
                        dimension.setTableName(rs.getString("TABLE_NAME"));
						dimensionList.add(dimension);
						//dimensionListR.add(dimension);
					}while(rs.next());

				}});

			/*sqlBuff.setLength(0);
			sqlBuff.append("SELECT" +
					" COL_NAME, " +
					" COL_CHN_NAME, " +
					" TABLE_OWNER," +
					" TABLE_NAME, " +
					" IS_QUERY," +
					" D.ID AS ID " +
					" FROM CONF_REPROT_TABLE_DICTIONARY C , T_REPORT_DIMENSION D " +
					" WHERE C.COL_CHN_NAME = D.NAME_CHN " +
					" AND TABLE_OWNER = '" + drilldown.getTableOwner() + "' " +
					" AND TABLE_NAME = '" + drilldown.getTableName() + "' " +
					" AND COL_TYPE = '1' " +
					" AND IS_QUERY = '1' ");//查出维度-查询列（同时查出维度ID）

			jdbcTemplate.query(sqlBuff.toString(), new RowCallbackHandler(){
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					do{
						Dimension dimension = new Dimension();
						dimension.setId(Long.parseLong(rs.getString("ID")));
						dimension.setName(rs.getString("COL_NAME"));
						dimension.setNameChn(rs.getString("COL_CHN_NAME"));
						dimension.setIsQuery(rs.getString("IS_QUERY"));
						dimension.setTableOwner(rs.getString("TABLE_OWNER"));
						dimension.setTableName(rs.getString("TABLE_NAME"));
						dimensionList.add(dimension);
					}while(rs.next());

				}});*/

			drilldown.setDimensions(dimensionList);

			sqlBuff.setLength(0);
			sqlBuff.append("SELECT" +
					" COL_NAME, " +
					" COL_CHN_NAME," +
					" TABLE_OWNER," +
					" TABLE_NAME " +
					" FROM CONF_REPROT_TABLE_DICTIONARY " +
					" WHERE  TABLE_OWNER = '" + drilldown.getTableOwner() + "' " +
					" AND TABLE_NAME = '" + drilldown.getTableName() + "' " +
					" AND COL_TYPE = '2' ");//查出指标
			final List<Measure> measureList = new ArrayList<Measure>();
			//添加指标

			jdbcTemplate.query(sqlBuff.toString(), new RowCallbackHandler(){
				@Override
				public void processRow(ResultSet rs) throws SQLException {
					do{
						Measure measure = new Measure();
						measure.setName(rs.getString("COL_NAME"));
						measure.setNameChn(rs.getString("COL_CHN_NAME"));
						measure.setTableName(rs.getString("TABLE_NAME"));
						measure.setTableOwner(rs.getString("TABLE_OWNER"));
						measureList.add(measure);
						//measureListR.add(measure);
					}while(rs.next());

				}});
			drilldown.setMeasures(measureList);

		}

		return drilldownList;
	}


	//对选中要求新增的报表获取配置信息
	public ReportInfoConfig getConfigInfoByName(String tableName) {
		ReportInfoConfig reportInfoConfig = new ReportInfoConfig();
		List<Dimension> dimensionListR = new ArrayList<Dimension>();
		List<Measure> measureListR = new ArrayList<Measure>();

		reportInfoConfig.setTableName(tableName);
		//获取报表的维度组合
		reportInfoConfig = getDrilldownsByReport(reportInfoConfig);
		//获取维度组合的维度和指标
		reportInfoConfig.setDrilldownList(getDeminsonsMeasuresByDrilldown(reportInfoConfig.getDrilldownList()));

		//获取报表包含的维度和指标
		for (Drilldown drilldown : reportInfoConfig.getDrilldownList()){
			dimensionListR.addAll(drilldown.getDimensions());
			measureListR.addAll(drilldown.getMeasures());
		}

		HashSet<Dimension> dim = new HashSet<Dimension>(dimensionListR);
		HashSet<Measure> mea = new HashSet<Measure>(measureListR);

		reportInfoConfig.setDimensionList(new ArrayList<Dimension>(dim));
		reportInfoConfig.setMeasureList(new ArrayList<Measure>(mea));

		return reportInfoConfig;
	}

	//查看报表是否已经存在lyw
	public List<ReportInfo> getReoprtName(String tableName){
		/*//查找count
		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("SELECT" +
				" COUNT(*) " +
				" FROM T_REPORT_INFO_DRAFT" );
		if(StringUtils.isNotBlank(tableName)){
			sqlBuff.append(" WHERE TABLE_NAME = '").append(tableName).append("'");
		}

		//返回有几条名字为tablename的记录
		return jdbcTemplate.queryForInt(sqlBuff.toString());*/

		StringBuffer sqlBuff = new StringBuffer();
		sqlBuff.append("SELECT" +
				" TABLE_ID " +
				" ,TABLE_NAME " +
				" ,REMARK" +
				" ,ENABLE_SUB" +
				" ,PUBLISH_DIR" +
				//" ,DIRNAME" +
				" ,PUBLISH_URL" +
				" ,CREATOR" +
				" ,CREATE_TIME"+
				" ,STATE"+
				" FROM T_REPORT_INFO_DRAFT" );
		if(StringUtils.isNotBlank(tableName)){
			sqlBuff.append(" WHERE TABLE_NAME = '").append(tableName).append("'");
		}

		final List<ReportInfo> reportInfos = new ArrayList<ReportInfo>();
		jdbcTemplate.query(sqlBuff.toString(), new RowCallbackHandler(){
			@Override
			public void processRow(ResultSet rs) throws SQLException {
				do{
					ReportInfo reportInfo = new ReportInfo();
					reportInfo.setTableName(rs.getString("TABLE_NAME"));
					reportInfo.setDescription(rs.getString("REMARK"));
					reportInfo.setEnableSub(rs.getString("ENABLE_SUB"));
					reportInfo.setPublishDir(rs.getString("PUBLISH_DIR"));
					//reportInfo.setDirName(rs.getString("DIRNAME"));
					reportInfo.setPublishUrl(rs.getString("PUBLISH_URL"));
					reportInfo.setCreator(rs.getString("CREATOR"));
					reportInfo.setCreateTime(rs.getString("CREATE_TIME"));
					reportInfo.setState(rs.getString("STATE"));
					reportInfo.setReportId(rs.getString("TABLE_ID"));

					reportInfos.add(reportInfo);
				}while(rs.next());

			}});
		return reportInfos;
	}


}
