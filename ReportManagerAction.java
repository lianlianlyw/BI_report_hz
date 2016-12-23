package com.ebupt.mrrs.web.manager.report;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ebupt.mrrs.engine.cache.CacheInitTask;
import com.ebupt.mrrs.engine.metadata.entity.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;
import org.jdom.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springside.modules.orm.Page;
import org.springside.modules.security.springsecurity.SpringSecurityUtils;

import com.ebupt.mrrs.engine.Constants;
import com.ebupt.mrrs.engine.RSException;
import com.ebupt.mrrs.engine.base.BaseDimension;
import com.ebupt.mrrs.engine.base.BaseDrillDown;
import com.ebupt.mrrs.engine.cache.EhcacheUtil;
import com.ebupt.mrrs.engine.design.ReportDesign;
import com.ebupt.mrrs.engine.metadata.MetaDataManager;
import com.ebupt.mrrs.engine.xml.RSXmlLoader;
import com.ebupt.mrrs.entity.report.ReportDirectory;
import com.ebupt.mrrs.service.rptmanage.ReportInfoService;
import com.ebupt.mrrs.utils.DateUtil;
import com.ebupt.mrrs.utils.loging.LogRecord;
import com.ebupt.mrrs.web.CrudActionSupport;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * 报表管理Action.
 *
 * 使用Struts2 convention-plugin annotation定义Action参数.
 *
 */
@Results( { @Result(name = CrudActionSupport.RELOAD, location = "report-manager.action", type = "redirect") })
public class ReportManagerAction extends CrudActionSupport<ReportInfo> {

	private static final long serialVersionUID = 1L;
	protected Log logger = LogFactory.getLog(getClass());

	@Autowired
	private ReportInfoService reportInfoService;
	@Autowired
	private MetaDataManager metaDataManager;

	// 页面属性 //
	private String id;
	private ReportInfo entity;
	private List<ReportInfo> reportInfoList;//资源列表
	private String message;

	//上传文件
	private File file;
	private String fileName;

	@Override
	public String execute() throws Exception {
		return SUCCESS;
	}

	@Override
	protected void prepareModel() throws Exception {
		if(id != null){
			entity = reportInfoService.getDraft(getId());
		}else {
			entity = new ReportInfo();
		}

	}

	@Override
	public String delete() throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			//正式表删除记录
			entity = reportInfoService.getDraft(getId());
			System.out.println(entity.getState());
			if(!StringUtils.equals("0", entity.getState())){
				reportInfoService.delete(getId());
			}

			//再草稿表置状态为0
			reportInfoService.deleteDraft(id);
			//刷新缓存池
			EhcacheUtil.removeCache("xml",id);

			result.put("success", true);
			result.put("msg", "删除成功");
			LogRecord.createMngLog("删除报表:"+entity.getTableName(), "删除成功","");
		} catch (Exception e) {
			LogRecord.createMngLog("删除报表:"+entity.getTableName(), "删除失败","");
			result.put("success", false);
			result.put("msg", "删除失败,请稍后再试");
			e.printStackTrace();
		}
		return writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
	}

	@Override
	public String input() throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		result.put("success", true);
		result.put("id", entity.getReportId());
		result.put("desc", entity.getDescription());
		result.put("dir", entity.getPublishDir());
		result.put("url", entity.getPublishUrl());
		result.put("subsribe", entity.getEnableSub());

		return writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));

	}

	@Override
	public String list() throws Exception {
		return SUCCESS;
	}


	public String save() throws Exception {
		HttpServletRequest request = ServletActionContext.getRequest();
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			reportInfoService.save(entity);
//			addActionMessage(getMessage() + SUCCESSED);
			result.put("success", true);
			result.put("msg", "报表发布成功");
			ReportDesign reportDesign = RSXmlLoader.load(request.getSession().getServletContext().getRealPath("report")+Constants.FSP+getId()+".xml");
			reportInfoService.saveOrUpdateReportConfig(reportDesign);
//			reportXMLCacheManager.set(getId(), reportDesign);
			LogRecord.createMngLog("新增报表:"+entity.getTableName(), "新增成功","");
		} catch (Exception e) {
			result.put("success", true);
			LogRecord.createMngLog("新增报表:"+entity.getTableName(), "新增失败","");
			e.printStackTrace();
		}

		return writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
	}

	public void prepareSaveDraft() throws Exception {
		entity = new ReportInfo();
		entity.setReportId(id);
		entity.setCreateTime(DateUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
		entity.setCreator(SpringSecurityUtils.getCurrentUserName());
	}
	/**
	 * 保存报表编辑草稿
	 * @return
	 * @throws Exception
	 */
	public void saveDraft() throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			reportInfoService.saveOrUpdateDraft(entity);
			LogRecord.createMngLog("新增报表草稿:"+entity.getTableName(), "新增成功","");
			result.put("success", true);
			result.put("msg", "新建报表成功");
		} catch (Exception e) {
			LogRecord.createMngLog("新增报表草稿:"+entity.getTableName(), "新增失败","");
			result.put("success", false);
			result.put("msg", "新建报表失败,请稍后重试");
			e.printStackTrace();
		}
		writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
	}

	/**
	 * 获取草稿报表信息
	 * @return
	 * @throws Exception
	 */
	public String getDraft() throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		HttpServletRequest request = ServletActionContext.getRequest();
		String id = request.getParameter("id");
		try {
			entity = reportInfoService.getDraft(id);
			result.put("success", true);
			result.put("id", entity.getReportId());
			result.put("desc", entity.getDescription());
			result.put("dir", entity.getPublishDir());
			result.put("url", entity.getPublishUrl());
			result.put("subsribe", entity.getEnableSub());
			result.put("dirname", entity.getDirName());
		} catch (Exception e) {
			result.put("success", false);
			e.printStackTrace();
		}
		return writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
	}


	public void prepareUpdateDraft() throws Exception {
		entity = reportInfoService.getDraft(getId());
		//当报表状态为1时再编辑保存时，草稿表状态置为2 编辑待发布状态
		if(StringUtils.equals("1", entity.getState())){
			entity.setState("2");
		}
		entity.setCreateTime(DateUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
		entity.setCreator(SpringSecurityUtils.getCurrentUserName());
	}
	/**
	 * 更新报表编辑草稿
	 * @return
	 * @throws Exception
	 */
	public void updateDraft() throws Exception {
		Map<String,Object> result = new HashMap<String,Object>();
		try {
			System.out.println(entity.getEnableSub());
			reportInfoService.saveOrUpdateDraft(entity);
			LogRecord.createMngLog("更新报表草稿:"+entity.getTableName(), "更新成功","");
			result.put("success", true);
			result.put("msg", "更新报表信息成功");
		} catch (Exception e) {
			LogRecord.createMngLog("更新报表草稿:"+entity.getTableName(), "更新失败","");
			result.put("success", false);
			result.put("msg", "更新报表信息失败,请稍后重试");
			e.printStackTrace();
		}

		writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
	}


	public void preparePublish() throws Exception {
		entity = reportInfoService.getDraft(getId());
		entity.setCreateTime(DateUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
		entity.setCreator(SpringSecurityUtils.getCurrentUserName());
		entity.setState("1");
	}
	/**
	 * 报表发布功能
	 * @throws IOException
	 */
	public void publish() throws IOException{
		Map<String,Object> result = new HashMap<String,Object>();
		boolean flag = false;
		try {
			//先更新草稿表里的状态信息
			flag = reportInfoService.updateSate(id,"1");
			//再往正式发布表里插入一条正式发布报表记录
			reportInfoService.save(entity);
			//放入缓存
			HttpServletRequest request = ServletActionContext.getRequest();
			String savePath = request.getSession().getServletContext().getRealPath("report");
			File savedir = new File(savePath);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}
			File xmlFile = new File(savedir+Constants.FSP+getId()+".xml");
			if(xmlFile.exists()){
				try{
					//reportDesign放入缓存
					ReportDesign reportDesign = RSXmlLoader.load(xmlFile);
					EhcacheUtil.putElementToCache("xml",entity.getReportId(),reportDesign);
					//更新report-config表
					reportInfoService.updateReportConfig(reportDesign);
					//report-config信息放入缓存
					ReportInfoConfig reportInfoConfig = reportInfoService.getConfigById(id);
					reportInfoConfig.setDimensionList(metaDataManager.getDimensionByIdString(reportInfoConfig.getDimension()));
					reportInfoConfig.setMeasureList(metaDataManager.getMeasureByIdString(reportInfoConfig.getMeasure()));
					EhcacheUtil.putElementToCache("report",entity.getReportId(),reportInfoConfig);
				}catch(RSException rse){
					result.put("success", flag);
					result.put("success", "报表配置XML解析失败,请稍后重新发布");
					rse.printStackTrace();
				}
			}

			result.put("success", flag);
			result.put("msg", "报表发布成功");
			LogRecord.createMngLog("发布报表:"+entity.getTableName(), "发布成功","");
		} catch (Exception e) {
			result.put("success", flag);
			result.put("success", "报表发布失败,请稍后重新发布");
			LogRecord.createMngLog("发布报表:"+entity.getTableName(), "发布失败","");
			e.printStackTrace();
		}
		writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
	}

	/**
	 * 保存报表权限项
	 * @throws IOException
	 */
	public void saveAuthority() throws IOException{
		HttpServletRequest request = ServletActionContext.getRequest();
		String authList = request.getParameter("auth");
		String id = request.getParameter("id");

		entity = reportInfoService.getDraft(getId());

		Map<String,Object> result = new HashMap<String,Object>();
		if(StringUtils.isNotBlank(authList)){
			List<String[]> paramsList = new ArrayList<String[]>();
			String[] authArray = authList.split("\\|");
			for(String auth : authArray){
				String[] authority = auth.split(",");
				if(authority.length == 3 ){
					paramsList.add(authority);
				}
			}
			try {
				reportInfoService.saveAuthority(id, paramsList);
				result.put("success", true);
				result.put("msg", "保存报表权限成功");
				LogRecord.createMngLog("保存报表:"+entity.getTableName()+" 权限", "保存失败","");
			} catch (Exception e) {
				result.put("success", false);
				result.put("msg", "保存报表权限失败,请稍后重试");
				LogRecord.createMngLog("保存报表:"+entity.getTableName()+" 权限", "保存失败","");
				e.printStackTrace();
			}

		}else {
			result.put("success", false);
			result.put("msg", "参数异常");
		}
		writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));

	}

	@Override
	public ReportInfo getModel() {
		return entity;
	}

	public void view() throws Exception{
		HttpServletRequest request = ServletActionContext.getRequest();
		Map<String, Object> result = new HashMap<String, Object>();
		try{
			ReportDesign reportDesign = RSXmlLoader.load(request.getSession().getServletContext().getRealPath("report")+Constants.FSP+getId()+".xml");
			result.put("success", true);
			result.put("msg", "请求数据成功");
			result.put("reportData",generateReportJson(reportDesign));
		}catch(Exception e){
			result.put("success", false);
			result.put("msg", "报表配置文件解析失败");
			e.printStackTrace();
		}
		writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
	}

	/**
	 * 异步获取页面分页数据
	 * @param
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void page() throws IOException{
		HttpServletRequest request = ServletActionContext.getRequest();
		Map<String,Object> parameter = getRequestParams(request);
		System.out.println(request.getParameter("rid"));
		Page pageParams = getPageParams(request);

		String result = null;
		try {
			Page pageList = reportInfoService.getPageList(parameter,pageParams);
			result = generateJqGridData(pageList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		writerMessage2Client(ServletActionContext.getResponse(), result);
	}

	/**
	 * 异步获取表头元数据
	 * @throws IOException
	 */
	public void getColumnMetaData() throws IOException{
		HttpServletRequest request = ServletActionContext.getRequest();
		String tableName = request.getParameter("tableName");

		Map<String,Object> result = new HashMap<String,Object>();
		Map<String,Object> metaDataList = null;
		try {
			//TODO  表头
			metaDataList = null;

		} catch (Exception e) {
			result.put("success", false);
			result.put("msg", "请求数据失败");
			e.printStackTrace();
		}
		writerMessage2Client(ServletActionContext.getResponse(), generateColumnMetaData(metaDataList,result));
	}

	/**
	 * 生成表格表头字段数据
	 * @param metaDataList
	 * @return
	 */
	public String generateColumnMetaData(Map<String,Object> metaDataList,Map<String, Object> result) {
		result.put("success", true);
		int size = metaDataList.size();
		if(metaDataList == null && size <= 0){
			result.put("msg", "没有找到符合条件的记录");
		}else {
			result.put("msg", "请求数据成功");
			result.put("colNames", metaDataList.get("colNames"));
			result.put("colModel", metaDataList.get("colModel"));
		}
		logger.info("异步获取表头元数据：" + mapper.toJson(result));
		return mapper.toJson(result);
	}

	public String uploadFile() throws Exception{
		HttpServletResponse response = ServletActionContext.getResponse();
		HttpServletRequest request = ServletActionContext.getRequest();
		Map<String, Object> result = new HashMap<String, Object>();

		System.out.println("上传文件："+file);
		ReportDesign reportDesign = RSXmlLoader.load(file);
		if(StringUtils.isBlank(getId())){
			System.out.println(reportDesign.getId());
			result.put("id", reportDesign.getId());
			result.put("name", reportDesign.getName());
			result.put("reportData",generateReportJson(reportDesign));
			setId(reportDesign.getId());
		}
		String savePath = request.getSession().getServletContext().getRealPath("report");
		File savedir = new File(savePath);
		if (!savedir.exists()) {
			savedir.mkdirs();
		}
		try{
			//重复上传文件控制，旧文件删除

			File xmlFile = new File(savedir+Constants.FSP+getId()+".xml");
			if(xmlFile.exists()&&xmlFile.isFile()){
				xmlFile.delete();
			}
			copyFile(file, new File(savedir+Constants.FSP+getId()+".xml"));
			LogRecord.createMngLog("上传报表XML配置文件", "上传配置文件成功","");
		}catch(IOException e){
			result.put("success", false);
			result.put("desc", "文件上传失败 !");
			LogRecord.createMngLog("上传报表XML配置文件", "上传配置文件失败","");
			e.printStackTrace();
		}
		System.out.println("3333333333333333333333333333333333333333");
		result.put("success", true);
		result.put("desc", "文件上传成功 !");
		System.out.println( mapper.toJson(result));
		return writerMessage2Client(response, mapper.toJson(result));
	}

	/**
	 * 生成JqGrid表格数据
	 * @param pageList
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private String generateJqGridData(Page pageList) {
		Map result = new HashMap();
		long count = pageList.getTotalCount();
		result.put("success", true);
		if(pageList == null && count == 0){
			result.put("msg", "没有找到符合条件的记录");
		}else {
			int totalPages = (int)(Math.ceil((double)pageList.getTotalCount()/(double)pageList.getPageSize()));
			result.put("msg", "请求数据成功");
			result.put("currpage", pageList.getPageNo());
			result.put("totalpages", totalPages);
			result.put("totalrecords", pageList.getTotalCount());
			result.put("rows", pageList.getResult());
		}
		logger.info( mapper.toJson(result));
		return mapper.toJson(result).toLowerCase();
	}

	@SuppressWarnings("unchecked")
	public Page getPageParams(HttpServletRequest request){
		Page page = new Page();
		String pageNo = request.getParameter("page");
		String pageSize = request.getParameter("rows");
		String orderBy = request.getParameter("sidx");
		String order = request.getParameter("sord");

		if( StringUtils.isNotBlank(pageNo)){
			page.setPageNo(Integer.valueOf(pageNo));
		}
		if( StringUtils.isNotBlank(pageSize)){
			page.setPageSize(Integer.valueOf(pageSize));
		}
		if( StringUtils.isNotBlank(orderBy)){
			page.setOrderBy(orderBy);
		}
		if( StringUtils.isNotBlank(order)){
			page.setOrder(order);
		}
		return page;
	}

	public Map<String,Object> getRequestParams(HttpServletRequest request){
		Map<String,Object> rqParams = new HashMap<String,Object>();
		String[] rqKey = {"searchKey","state","rid"};

		for(String key:rqKey){
			String value = request.getParameter(key);
			if(StringUtils.isNotBlank(value)){
				rqParams.put(key, value);
			}
		}
		System.out.println(rqParams);
		return rqParams;
	}

	/**
	 * 向浏览器返回JSON格式相应信息
	 * @param response
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public String writerMessage2Client(HttpServletResponse response,String message) throws IOException{
		response.reset();
		response.setContentType("application/text;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		writer.append(message.replaceAll("null", "\"\"").replaceAll("\r\n", "  "));
		writer.flush();
		return null;
	}

	// 复制文件
	public static void copyFile(File sourceFile, File targetFile) throws IOException {
		BufferedInputStream inBuff = null;
		BufferedOutputStream outBuff = null;
		try {
			// 新建文件输入流并对它进行缓冲
			inBuff = new BufferedInputStream(new FileInputStream(sourceFile));

			// 新建文件输出流并对它进行缓冲
			outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));

			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
		} finally {
			// 关闭流
			if (inBuff != null)
				inBuff.close();
			if (outBuff != null)
				outBuff.close();
		}
	}

	private Map<String, Object> generateReportJson(ReportDesign reportDesign){
		Map<String, Object> report = new HashMap<String, Object>();
		if(reportDesign != null){
			report.put("name", reportDesign.getName());
			report.put("type", reportDesign.getType());
			report.put("param", reportDesign.getParameterDefinition().getParameterList());

			List<Map<String, Object>> drillDowns = new ArrayList<Map<String, Object>>();

			if(StringUtils.equals("0", reportDesign.getType())){
				for(Iterator<BaseDrillDown> iter = reportDesign.getCubeDefinition().getDrillDownList().iterator();iter.hasNext();){
					Map<String, Object> map = new HashMap<String, Object>();
					BaseDrillDown drill = iter.next();
					map.put("dimension", drill.getDimensionDefinition().getDimensionList());
					map.put("measure", drill.getMeasureDefinition().getMeasureList());
					logger.info(drill.getDrillGroup());
//					map.put("dimension", metaDataManager.getDimensionByIdString(drill.getDrillGroup()));
					//系统之前维度组合的名字是用维度的名字拼接的，现改成直接用维度组合的名字表示
					map.put("drillName",drill.getDrillValueChn());

					drillDowns.add(map);
				}
				report.put("drill", drillDowns);
			}

			Map<String, Object> baseReport = new HashMap<String, Object>();
			if(StringUtils.equals("1", reportDesign.getType())){
				StringBuffer dimensionId = new StringBuffer();
				String seq = ",";
				for(Iterator<BaseDimension> itertor = reportDesign.getBaseReport().getDimensionDefinition().getDimensionList().iterator();itertor.hasNext();){
					dimensionId.append(itertor.next().getDimensionId()).append(seq);
				}
				System.out.println(dimensionId.substring(0, dimensionId.lastIndexOf(seq)));
				baseReport.put("dimension",metaDataManager.getDimensionByIdString(dimensionId.substring(0, dimensionId.lastIndexOf(seq))));
				baseReport.put("measure", reportDesign.getBaseReport().getMeasureDefinition().getMeasureList());

			}
			report.put("base", baseReport);

			report.put("auth", reportInfoService.getAuthList(reportDesign.getId()));
		}
		return report;
	}

	public String getReportMenu() throws IOException{
		HttpServletResponse response = ServletActionContext.getResponse();
		StringBuffer message = new StringBuffer("{");
		List<ReportDirectory> records = reportInfoService.getReportMenu();
		message.append("\"success\": true");
		message.append(",\"desc\": \"数据请求成功\"");
		message.append(",\"data\": [");
		//{"id":1,"pId":0,"name":"报表管理","open":true}
		message.append("{\"id\":1,\"pId\":0,\"name\":\"报表管理\",\"open\":true}");
		String url ="/manager/report/report-manager.action?rid=";
		for(ReportDirectory obj : records){

			message.append(",{");
			message.append("\"id\":"+obj.getDirId());
			message.append(",\"pId\":"+obj.getParentId());
			message.append(",\"name\":\""+obj.getTitle());
			message.append("\",\"target\":\"_container\"");
			message.append(",\"url\":\""+url+obj.getDirId()+"\"");
			message.append("}");
		}

		message.append("]}");
		System.out.println(message.toString());
		return this.writerMessage2Client(response, message.toString());
	}

	/**
	 * 根据仓库提供的元数据配置表获取可以进行配置的报表信息
	 * @author lyw
	 * @throws IOException
	 */
	public void getNewDataReport() throws IOException {
		String report = new String();
		try {
			Page reportInfoList = reportInfoService.getNewDataReport();
			report = generateJqGridData(reportInfoList);
			/*for(ReportInfoConfig obj : reportInfoList){
				List<String> drilldownList = new ArrayList<String>();
				for(Drilldown drilldown : obj.getDrilldownList()){
					drilldownList.add(drilldown.getDrilldownName());
				}
				result.put("tableName",obj.getTableName());
				result.put("tableCycleType",obj.getReportType());
				result.put("drilldown",drilldownList);
			}*/

		} catch (Exception e) {
			e.printStackTrace();
		}
		//writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(result));
		writerMessage2Client(ServletActionContext.getResponse(), report);

	}

	/**
	 * 配置页面，生成配置文件后，对生成的报表保存报表描述、发布目录等信息
	 * @author lyw
	 * @throws IOException
	 */
	public void saveReportDraft() throws IOException{
		HttpServletRequest request = ServletActionContext.getRequest();
		String tableName = request.getParameter("tableName");
		String tableId = request.getParameter("tableId");
		String description = request.getParameter("description");
		String dir = request.getParameter("dir");
		if((StringUtils.isBlank(description))&&(StringUtils.isBlank(dir))){
			writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson("success"));
			return;
		}
		try{
			List<ReportInfo> reportInfos = reportInfoService.getReportNum(tableName);
			ReportInfo reportInfo = reportInfos.get(0);
			if(StringUtils.isNotBlank(description)){
				reportInfo.setDescription(description);
			}
			if(StringUtils.isNotBlank(dir)){
				reportInfo.setPublishDir(dir);
			}

			//根据报表名称修改报表信息
			reportInfoService.updateDraftByName(reportInfo);

			writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson("success"));

			/*ReportInfo reportInfo = new ReportInfo();
			reportInfo.setReportId(tableId);
			reportInfo.setTableName(tableName);
			reportInfo.setDescription(description);
			reportInfo.setPublishDir(dir);
			reportInfo.setPublishUrl("");
			reportInfo.setEnableSub("");
			reportInfo.setCreator(SpringSecurityUtils.getCurrentUserName());
			reportInfo.setCreateTime(DateUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
			reportInfo.setState("0");

			reportInfoService.saveOrUpdateDraft(reportInfo);*/
		}catch (Exception e){
			e.printStackTrace();
			writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson("false"));
		}


	}

	/**
	 * 报表生成器主体功能，生成后刷新缓存
	 * @author lyw
	 * @throws IOException
	 * @throws RSException
	 */
	public void generateDeployFile() throws IOException,RSException{
		HttpServletRequest request = ServletActionContext.getRequest();
		String tableName = request.getParameter("tableName");//报表名字
		String tableCycleType = request.getParameter("tableCycleType");//报表类型：日报月报
		if(tableCycleType.equals("日报表")){
			tableCycleType = "day";
		}else if(tableCycleType.equals("周报表")){
			tableCycleType = "week";
		}else if (tableCycleType.equals("当月累计报表")){
			tableCycleType = "month";
		}else{
			tableCycleType = "month";
		}
		String state = new String();
		//查找报表草稿表中是否有该报表
		List<ReportInfo> reportInfos = reportInfoService.getReportNum(tableName);
		//获取选中报表的配置信息
		ReportInfoConfig reportInfoConfig = reportInfoService.getConfigInfoByName(tableName);
		if(reportInfos.size() == 0){
			//成功返回报表ID 失败返回false
			state = generateReport(tableName,tableCycleType,reportInfoConfig,new ReportInfo(),request);

		}else{
			//修改报表配置信息，删除以前的xml
			for(ReportInfo reportInfo :reportInfos){
				//getid
				String id = reportInfo.getReportId();

				try{
					//删除sql文件
					/*ReportDesign reportDesign = RSXmlLoader.load(request.getSession().getServletContext().getRealPath("report")+Constants.FSP+id+".xml");
					for(Iterator<BaseDrillDown> iter = reportDesign.getCubeDefinition().getDrillDownList().iterator();iter.hasNext();){
						Map<String, Object> map = new HashMap<String, Object>();
						BaseDrillDown drill = iter.next();
						String sqlId = drill.getQueryString().getId();

						String savePath = request.getSession().getServletContext().getRealPath("report/vm");
						File savedir = new File(savePath);
						File xmlFile = new File(savedir+Constants.FSP+sqlId+".sql.vm");
						if(xmlFile.exists()&&xmlFile.isFile()){
							xmlFile.delete();
						}

					}

					//删除xml
					String savePath = request.getSession().getServletContext().getRealPath("report");
					File savedir = new File(savePath);
					File xmlFile = new File(savedir+Constants.FSP+id+".xml");
					if(xmlFile.exists()&&xmlFile.isFile()){
						xmlFile.delete();
					}*/

				}catch (Exception e){
					e.printStackTrace();
				}

			}
			//重新生成xml，并修改数据库
			state = generateReport(tableName,tableCycleType,reportInfoConfig,reportInfos.get(0),request);
		}
		//生成xml后刷新缓存
		CacheInitTask cacheInitTask = new CacheInitTask(ServletActionContext.getRequest().getSession().getServletContext());
		cacheInitTask.run();


		writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson(state));//返回是否生成xml生成xml成功
	}

	/**
	 * 生成报表函数，包括生成xml文件、sql文件、将报表ID和报表名称存库、修改元数据配置表信息
	 * @author lyw
	 * @param tableName
	 * @param tableCycleType
	 * @param reportInfoConfig
	 * @param reportInfo
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public String generateReport(String tableName, String tableCycleType, ReportInfoConfig reportInfoConfig, ReportInfo reportInfo,HttpServletRequest request) throws IOException{
		String state = new String();
		//新增报表配置信息
		String reportId = generateXmlFile(tableName,tableCycleType,reportInfoConfig,request);
		String sqlState = new String();
		if(reportId != null){
			sqlState = generateSqlFile(reportInfoConfig.getDrilldownList(),tableCycleType,request);
		}

		if ((sqlState.equals("success")) && (reportId != null)){
			try{
				//生成xml和sql成功就存库
				//String reportId = UUID.randomUUID().toString().replaceAll("-", "");
				if(StringUtils.isBlank(reportInfo.getTableName())){
					reportInfo.setReportId(reportId);
					reportInfo.setTableName(tableName);
					reportInfo.setCreateTime(DateUtil.getCurrentTime("yyyy-MM-dd HH:mm:ss"));
					reportInfo.setCreator(SpringSecurityUtils.getCurrentUserName());
					reportInfo.setDescription("");
					reportInfo.setEnableSub("");
					reportInfo.setPublishDir("");
					reportInfo.setPublishUrl("");
					reportInfoService.saveOrUpdateDraft(reportInfo);
					LogRecord.createMngLog("新增报表草稿:"+reportInfo.getTableName(), "新增成功","");
					logger.info("新增报表草稿:"+reportInfo.getTableName()+"成功");
				}else{
					reportInfo.setReportId(reportId);
					if(StringUtils.equals("1", reportInfo.getState())){
						reportInfo.setState("2");
					}
					reportInfoService.updateDraftByName(reportInfo);
					LogRecord.createMngLog("修改报表草稿:"+reportInfo.getTableName(), "修改成功","");
					logger.info("修改报表草稿:"+reportInfo.getTableName()+"成功");
				}

				//修改元数据配置表报表状态
				reportInfoService.updateReportMataData(tableName);
				state = reportId;

			}catch(Exception e){
				e.printStackTrace();
				LogRecord.createMngLog("保存报表草稿:"+reportInfo.getTableName(), "保存失败","");
				logger.info("保存报表草稿:"+reportInfo.getTableName()+"失败");
				state = "false";
				//writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson("success"));
				//return;
			}

		}else{
			state = "false";
		}

		/*if((!sqlState.equals("success")) || (reportId == null)){
			writerMessage2Client(ServletActionContext.getResponse(), mapper.toJson("success"));
			return;
		}*/

		return state;
	}

	/**
	 * 生成xml文件函数
	 * @author lyw
	 * @param tableName
	 * @param tableCycleType
	 * @param reportInfoConfig
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public String generateXmlFile(String tableName,String tableCycleType,ReportInfoConfig reportInfoConfig,HttpServletRequest request) throws IOException{
		String reportId = UUID.randomUUID().toString().replaceAll("-", "");
		Dimension dimensionStartDate = new Dimension();
		Dimension dimensionEndDate = new Dimension();

		// 创建根节点 并设置它的属性 ;
		List<Attribute> attributes = new ArrayList<Attribute>();
		//Attribute attribute = new Attribute("name",tableName);
		attributes.add(new Attribute("name",tableName));
		attributes.add(new Attribute("type","0"));
		attributes.add(new Attribute("id",reportId));
		attributes.add(new Attribute("cycleType",tableCycleType));

		Element root = new Element("report").setAttributes(attributes);
		// 将根节点添加到文档中；
		Document Doc = new Document(root);

		//parameters
		Element parameters = new Element("parameters");
		for (int i = 0; i < reportInfoConfig.getDimensionList().size(); i++) {
			if(reportInfoConfig.getDimensionList().get(i).getName().equalsIgnoreCase("RECORD_DAY")||reportInfoConfig.getDimensionList().get(i).getName().equalsIgnoreCase("RECORD_MONTH")){

				dimensionStartDate.setParameterId(UUID.randomUUID().toString().replaceAll("-", ""));
				dimensionStartDate.setName("start_date");
				dimensionStartDate.setNameChn("开始日期");

				dimensionEndDate.setParameterId(UUID.randomUUID().toString().replaceAll("-", ""));
				dimensionEndDate.setName("end_date");
				dimensionEndDate.setNameChn("结束日期");
				Element parameter = new Element("parameter").setAttribute("id",dimensionStartDate.getParameterId());
				parameter.addContent(new Element("dataType").setText("java.Lang.Date"));
				parameter.addContent(new Element("exType").setText("Date"));
				parameter.addContent(new Element("visible").setText("true"));
				parameter.addContent(new Element("allowInput").setText("true"));
				parameter.addContent(new Element("allowEmpty").setText("true"));
				parameter.addContent(new Element("allowMultiValues").setText("true"));
				parameter.addContent(new Element("defaultValue"));
				parameter.addContent(new Element("value"));
				parameter.addContent(new Element("name").setText(dimensionStartDate.getName()));
				parameter.addContent(new Element("showName").setText(dimensionStartDate.getNameChn()));
				parameters.addContent(parameter);

				parameter = new Element("parameter").setAttribute("id",dimensionEndDate.getParameterId());
				parameter.addContent(new Element("dataType").setText("java.Lang.Date"));
				parameter.addContent(new Element("exType").setText("Date"));
				parameter.addContent(new Element("visible").setText("true"));
				parameter.addContent(new Element("allowInput").setText("true"));
				parameter.addContent(new Element("allowEmpty").setText("true"));
				parameter.addContent(new Element("allowMultiValues").setText("true"));
				parameter.addContent(new Element("defaultValue"));
				parameter.addContent(new Element("value"));
				parameter.addContent(new Element("name").setText(dimensionEndDate.getName()));
				parameter.addContent(new Element("showName").setText(dimensionEndDate.getNameChn()));
				parameters.addContent(parameter);

				continue;
			}
			String dimensionId = UUID.randomUUID().toString().replaceAll("-", "");
			reportInfoConfig.getDimensionList().get(i).setParameterId(dimensionId);
			// 创建节点 ;
			Element parameter = new Element("parameter").setAttribute("id",dimensionId);
			// 给节点添加子节点并赋值；
			parameter.addContent(new Element("dataType").setText("java.Lang.String"));
			parameter.addContent(new Element("exType").setText("text"));
			parameter.addContent(new Element("visible").setText("true"));
			parameter.addContent(new Element("allowInput").setText("true"));
			parameter.addContent(new Element("allowEmpty").setText("true"));
			parameter.addContent(new Element("allowMultiValues").setText("true"));
			parameter.addContent(new Element("defaultValue"));
			parameter.addContent(new Element("value"));
			parameter.addContent(new Element("name").setText(reportInfoConfig.getDimensionList().get(i).getName().toUpperCase()));
			parameter.addContent(new Element("showName").setText(reportInfoConfig.getDimensionList().get(i).getNameChn()));

			parameters.addContent(parameter);
		}
		root.addContent(parameters);

		//cube
		Element cube = new Element("cube");
		for(int i = 0 ; i < reportInfoConfig.getDrilldownList().size(); i++){
			String drilldownId = UUID.randomUUID().toString().replaceAll("-", "");
			reportInfoConfig.getDrilldownList().get(i).setDrilldownId(drilldownId);

			List<Attribute> attributesD = new ArrayList<Attribute>();
			attributesD.add(new Attribute("id",drilldownId));
			attributesD.add(new Attribute("drillValueChn",reportInfoConfig.getDrilldownList().get(i).getDrilldownName()));

			Element drilldown = new Element("drillDown").setAttributes(attributesD);
			Element parameterSet = new Element("parameterSet");
			//dimensions
			Element dimensions = new Element("dimensions");
			//measures
			Element measurs = new Element("measures");
			int dimensionLevel = 1;
			for (int j = 0;j < reportInfoConfig.getDrilldownList().get(i).getDimensions().size();j++){
				Dimension dim = reportInfoConfig.getDrilldownList().get(i).getDimensions().get(j);
				//对日期进行特殊处理
				if(dim.getName().equalsIgnoreCase("RECORD_DAY")||dim.getName().equalsIgnoreCase("RECORD_MONTH")){
					Element parameterStartDate = new Element("parameter").setAttribute("id",dimensionStartDate.getParameterId());
					parameterSet.addContent(parameterStartDate);
					Element parameterEndDate = new Element("parameter").setAttribute("id",dimensionEndDate.getParameterId());
					parameterSet.addContent(parameterEndDate);
					continue;
				}
				for(int k = 0;k<reportInfoConfig.getDimensionList().size();k++){
					if (dim.getName().equals(reportInfoConfig.getDimensionList().get(k).getName())) {
						reportInfoConfig.getDrilldownList().get(i).getDimensions().get(j).setParameterId(reportInfoConfig.getDimensionList().get(k).getParameterId());
						break;
					}
				}
				Element parameter = new Element("parameter").setAttribute("id",reportInfoConfig.getDrilldownList().get(i).getDimensions().get(j).getParameterId());
				//dimensions
				if(dim.getIsQuery().equals("1")){
					//查询列放到dimensions标签
					Element dimension = new Element("dimension").setAttribute("id",UUID.randomUUID().toString().replaceAll("-", ""));
					//dimension.addContent(new Element("dimensionId").setText(String.valueOf(dim.getId())));
					dimension.addContent(new Element("copyFrom").setText(reportInfoConfig.getDrilldownList().get(i).getDimensions().get(j).getParameterId()));
					dimension.addContent(new Element("drillLevel").setText(String.valueOf(dimensionLevel)));
					dimension.addContent(new Element("dimTable").setText(dim.getDimTableOwner()+"."+dim.getDimTableName()));
					dimensions.addContent(dimension);
					dimensionLevel++;

				}
				//查询列和非查询列都默认加入到指标中
				Element dimensionMeasure = new Element("measure").setAttribute("id",UUID.randomUUID().toString().replaceAll("-", ""));
				dimensionMeasure.addContent(new Element("name").setText(dim.getName().toUpperCase()));
				dimensionMeasure.addContent(new Element("dataType").setText("java.Lang.String"));
				dimensionMeasure.addContent(new Element("dataset").setText(dim.getTableOwner()+'.'+dim.getTableName()));
				dimensionMeasure.addContent(new Element("column").setText(dim.getName()));
				dimensionMeasure.addContent(new Element("showName").setText(dim.getNameChn()));
				dimensionMeasure.addContent(new Element("colDesc").setText(dim.getDescription()));
				dimensionMeasure.addContent(new Element("showFormat"));
				measurs.addContent(dimensionMeasure);

				parameterSet.addContent(parameter);
			}
			drilldown.addContent(parameterSet);

			String sqlId = UUID.randomUUID().toString().replaceAll("-", "");
			reportInfoConfig.getDrilldownList().get(i).setQuerySqlId(sqlId);
			Element queryString = new Element("queryString").setAttribute("id",sqlId);
			queryString.addContent(new Element("language").setText("sql"));
			queryString.addContent(new Element("vmFile").setText(sqlId+".sql.vm"));
			queryString.addContent(new Element("expression"));
			drilldown.addContent(queryString);

			drilldown.addContent(dimensions);


			for (int j = 0;j < reportInfoConfig.getDrilldownList().get(i).getMeasures().size();j++){
				Measure mea = reportInfoConfig.getDrilldownList().get(i).getMeasures().get(j);
				Element measure = new Element("measure").setAttribute("id",UUID.randomUUID().toString().replaceAll("-", ""));
				measure.addContent(new Element("name").setText(mea.getName().toUpperCase()));
				measure.addContent(new Element("dataType").setText("Java.Lang.Long"));
				measure.addContent(new Element("dataset").setText(mea.getTableOwner()+"."+mea.getTableName()));
				measure.addContent(new Element("column").setText(mea.getName()));
				measure.addContent(new Element("showName").setText(mea.getNameChn()));
				measure.addContent(new Element("colDesc").setText(mea.getDescription()));
				measure.addContent(new Element("showFormat"));
				measurs.addContent(measure);
			}
			drilldown.addContent(measurs);

			cube.addContent(drilldown);
		}

		root.addContent(cube);

		// 使xml文件 缩进效果
        try{
            Format format = Format.getPrettyFormat();
            XMLOutputter XMLOut = new XMLOutputter(format);
            XMLOut.output(Doc, new FileOutputStream("D:/"+reportId+".xml"));

			//输出到服务器
			/*String savePath = request.getSession().getServletContext().getRealPath("report");
			File savedir = new File(savePath);
			if (!savedir.exists()) {
				savedir.mkdirs();
			}

			//重复上传文件控制，旧文件删除
			File xmlFile = new File(savedir+Constants.FSP+getId()+".xml");
			if(xmlFile.exists()&&xmlFile.isFile()){
				xmlFile.delete();
			}
			//copyFile(file, new File(savedir+Constants.FSP+getId()+".xml"));
			XMLOut.output(Doc, new FileOutputStream(savedir+Constants.FSP+reportId+".xml"));*/

			logger.info(reportId+".xml"+"文件生成成功");


        }catch(Exception e){
            e.printStackTrace();
			logger.info(reportId+".xml"+"文件生成失败");
            reportId = null;
        }
        return reportId;
	}

	/**
	 * 生成sql文件函数
	 * @author lyw
	 * @param drilldownList
	 * @param tableType
	 * @param request
	 * @return
	 */
    public String generateSqlFile(List<Drilldown> drilldownList,String tableType,HttpServletRequest request) {
        for (Drilldown drilldown : drilldownList){
            try{
				//输出到服务器
				/*String savePath = request.getSession().getServletContext().getRealPath("report/vm");
				File savedir = new File(savePath);
				if (!savedir.exists()) {
					savedir.mkdirs();
				}
				Writer w=new FileWriter(savedir+Constants.FSP+drilldown.getQuerySqlId()+".sql.vm");*/

                Writer w=new FileWriter("D:/"+drilldown.getQuerySqlId()+".sql.vm");
                BufferedWriter buffWriter=new BufferedWriter(w);

                StringBuffer buffer = new StringBuffer();
                buffer.append("SELECT \n\t");
                /*for(Dimension dimension : drilldown.getDimensions()){
                    buffer.append(dimension.getName()+",");
                }*/
                if(tableType.equals("day")||tableType.equals("week")){
                    buffer.append("RECORD_DAY ");
                }else{
                    buffer.append("RECORD_MONTH ");
                }
                //buffer.deleteCharAt(buffer.length()-1);
                buffer.append(" \n").append("#set($sep=\",\") \n");
                buffer.append("#foreach($value in $measure) \n\t").append("$sep \n\t").append("$value \n").append("#end \n");
                buffer.append("FROM \n\t").append(drilldown.getTableOwner()+"."+drilldown.getTableName()+" \n");
                buffer.append("WHERE 1=1 \n\t");
                if(tableType.equals("day")||tableType.equals("week")){
                    //日报表：1 周报表：2
                    buffer.append("#if($start_date && $end_date ) \n\t\t");
                    buffer.append("AND RECORD_DAY >= '$start_date' \n\t\t");
                    buffer.append("AND RECORD_DAY <= '$end_date' \n\t");
                    buffer.append("#else \n\t\t");
                    buffer.append("AND RECORD_DAY = '$start_date' \n\t");
                    buffer.append("#end \n\t");

                }else{
                    //当月累计报表:3 月报表：4
                    buffer.append("#if($start_date && $end_date ) \n\t\t");
                    buffer.append("AND RECORD_MONTH <= '$start_date' \n\t\t");
                    buffer.append("AND RECORD_MONTH <= '$end_date' \n\t");
                    buffer.append("#else \n\t\t");
                    buffer.append("AND RECORD_MONTH = '$start_date' \n\t");
                    buffer.append("#end \n\t");
                }

                //筛选条件
                for(Dimension dimension : drilldown.getDimensions()){
                    if(dimension.getName().equalsIgnoreCase("province_id")){
						buffer.append("#if($PROVINCE_ID && $CITY_ID) \n\t\t");
						buffer.append("AND ( province_id in ('$PROVINCE_ID') or city_id in ('$CITY_ID')) \n\t");
						buffer.append("#elseif($PROVINCE_ID) \n\t\t");
						buffer.append("AND province_id in ('$PROVINCE_ID') \n\t");
						buffer.append("#elseif($CITY_ID) \n\t\t");
						buffer.append("AND city_id in ('$CITY_ID') \n\t").append("#end \n\t");
                    }else if((dimension.getName().equalsIgnoreCase("city_id"))||(dimension.getName().equalsIgnoreCase("record_day"))||(dimension.getName().equalsIgnoreCase("record_month"))){
                        continue;
                    }else{
						if(dimension.getIsQuery().equals("1")){
							//查询列
							buffer.append("#if($"+dimension.getName().toUpperCase()+ ")  \n\t\t");
							buffer.append("AND "+dimension.getName().toLowerCase()+ " in ('$" +dimension.getName().toUpperCase()+"') \n\t");
							buffer.append("#end \n\t");
						}/*else{
							//非查询列
							buffer.append("AND "+dimension.getName().toLowerCase()+ " = '$dim_" +dimension.getName().toLowerCase()+"' \n\t");
						}*/

                    }
                }


                buffWriter.write(buffer.toString());

				logger.info(drilldown.getQuerySqlId()+".sql文件生成成功");

                buffWriter.close();
                w.close();
            }catch (Exception e){
                e.printStackTrace();
				logger.info(drilldown.getQuerySqlId()+".sql文件生成失败");
                return "false";
            }
        }
        return "success";
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ReportInfo getEntity() {
		return entity;
	}

	public void setEntity(ReportInfo entity) {
		this.entity = entity;
	}

	public List<ReportInfo> getReportInfoList() {
		return reportInfoList;
	}

	public void setReportInfoList(List<ReportInfo> reportInfoList) {
		this.reportInfoList = reportInfoList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


}
