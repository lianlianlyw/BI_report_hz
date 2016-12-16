package com.ebupt.mrrs.service.rptmanage;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springside.modules.orm.Page;

import com.ebupt.mrrs.dao.rptmanage.ReportInfoDao;
import com.ebupt.mrrs.engine.base.BaseDimension;
import com.ebupt.mrrs.engine.base.BaseDrillDown;
import com.ebupt.mrrs.engine.base.BaseMeasure;
import com.ebupt.mrrs.engine.cache.dao.CacheInitDao;
import com.ebupt.mrrs.engine.design.ReportDesign;
import com.ebupt.mrrs.engine.metadata.entity.ReportInfo;
import com.ebupt.mrrs.engine.metadata.entity.ReportInfoConfig;
import com.ebupt.mrrs.entity.report.ReportDirectory;
import com.ebupt.mrrs.service.BaseService;

/**
 * 报表管理service
 * @author hlb
 *
 */
@Service
@Transactional
public class ReportInfoService  extends BaseService{

	@Resource
	private ReportInfoDao reportInfoDao;

	@Resource
	private CacheInitDao cacheInitDao;

	public void save(ReportInfo entity){
		reportInfoDao.save(entity);
	}

	public void saveOrUpdateDraft(ReportInfo entity){
		if(!reportInfoDao.checkDraft(entity)){
			reportInfoDao.saveDraft(entity);
		}else{
			reportInfoDao.updateDraft(entity);
		}
	}

	public void saveOrUpdateReportConfig(ReportDesign entity){
		ReportInfoConfig result = new ReportInfoConfig();
		result.setReportId(entity.getId());
		StringBuffer measure = new StringBuffer("");
		StringBuffer dimension = new StringBuffer("");
		String sep = "";
		if("0".equals(entity.getType())){
			for(BaseDrillDown drillDown:entity.getCubeDefinition().getDrillDownList()){
				sep = "";
				List<BaseMeasure> measureList = drillDown.getMeasureDefinition().getMeasureList();
				for(BaseMeasure baseMeasure:measureList){
					if(!measure.toString().contains(baseMeasure.getMeasureId())){
						measure.append(sep).append(baseMeasure.getMeasureId());
						sep = ",";
					}
				}
				sep = "";
				List<BaseDimension> dimensionList = drillDown.getDimensionDefinition().getDimensionList();
				for(BaseDimension baseDimension:dimensionList){
					if(!dimension.toString().contains(baseDimension.getDimensionId())){
						dimension.append(sep).append(baseDimension.getDimensionId());
						sep = ",";
					}
				}
			}
		}else{
			sep = "";
			List<BaseMeasure> measureList = entity.getBaseReport().getMeasureDefinition().getMeasureList();
			for(BaseMeasure baseMeasure:measureList){
				measure.append(sep).append(baseMeasure.getMeasureId());
				sep = ",";
			}
			sep = "";
			List<BaseDimension> dimensionList = entity.getBaseReport().getDimensionDefinition().getDimensionList();
			for(BaseDimension baseDimension:dimensionList){
				dimension.append(sep).append(baseDimension.getDimensionId());
				sep = ",";
			}
		}
		result.setDimension(dimension.toString());
		result.setMeasure(measure.toString());
		if(reportInfoDao.checkReportConfig(result)){
			reportInfoDao.saveReportConfig(result);
		}else{
			reportInfoDao.updateReportConfig(result);
		}
	}

	public void delete(String id){
		reportInfoDao.delete(id);
		reportInfoDao.deleteConfig(id);
	}

	@Transactional(propagation=Propagation.NOT_SUPPORTED)
	public ReportInfo get(String id){
		return reportInfoDao.get(id);
	}
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public Page getPageList(Map<String, Object> parameter, Page pageParams) {
		return reportInfoDao.getPageList(parameter, pageParams);
	}

	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public ReportInfo getDraft(String id) {
		return reportInfoDao.getDraft(id);
	}

	public boolean updateSate(String id,String state) {
		int row = reportInfoDao.getSessionFactory().getCurrentSession()
				.createSQLQuery("update t_report_info_draft o set o.state=? where o.table_id=?")
				.setParameter(0, state)
				.setParameter(1, id)
				.executeUpdate();
		if(row >0 ){
			return true;
		}else {
			return false;
		}
	}

	public void saveAuthority(String id,List<String[]> paramsList) {
		reportInfoDao.saveAuthority(id,paramsList);

	}

	/**
	 * 获取报表权限项列表
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public List getAuthList(String id) {
		return reportInfoDao.getSessionFactory().getCurrentSession()
				.createSQLQuery("select a.display_name,a.name,a.url from t_report_authority a where a.report_id=?")
				.setString(0, id).list();
	}

	/**
	 * 删除草稿表
	 * @param id
	 */
	public void deleteDraft(String id) {
		reportInfoDao.getSessionFactory().getCurrentSession()
				.createSQLQuery("delete from t_report_info_draft a where a.TABLE_ID=?")
				.setParameter(0, id)
				.executeUpdate();

	}

	public ReportInfoConfig getConfigById(String reportId){
		ReportInfoConfig entity = cacheInitDao.getConfigById(reportId);
		return entity;
	}

	public void updateReportConfig(ReportDesign reportDesign){
		cacheInitDao.updateReportConfig(reportDesign);
	}

	public List<ReportDirectory> getReportMenu() {

		return reportInfoDao.getReportMenu();
	}

	//获取可以进行配置的报表信息lyw
	@Transactional(propagation=Propagation.NOT_SUPPORTED,readOnly=true)
	public Page getNewDataReport() {
		return reportInfoDao.getNewDataReport();
	}
	//查看报表是否已经存在lyw
	public List<ReportInfo> getReportNum(String tableName){
		return reportInfoDao.getReoprtName(tableName);
	}
	//对选中的报表获取配置信息
	public ReportInfoConfig getConfigInfoByName(String tableName) { return reportInfoDao.getConfigInfoByName(tableName); }
    //生成配置文件后，修改元数据配置表报表状态
    public void updateReportMataData (String tableName){
        reportInfoDao.updateReportMataData(tableName);
    }
    public void updateDraftByName (ReportInfo reportInfo){
        reportInfoDao.updateDraftByName(reportInfo);
    }
}
