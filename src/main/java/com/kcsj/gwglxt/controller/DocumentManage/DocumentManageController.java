package com.kcsj.gwglxt.controller.DocumentManage;

import com.kcsj.gwglxt.entity.*;
import com.kcsj.gwglxt.service.documentManage.DocumentService;
import com.kcsj.gwglxt.util.TeamUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class DocumentManageController {
    @Autowired
    private DocumentService documentService;

    //添加文档
    @RequestMapping("/addDocument")
    public Document addDocument(@RequestBody Document document, HttpSession httpSession, HttpServletResponse response){
        //获取session内容
        LoginCustom loginCustom = (LoginCustom)httpSession.getAttribute("loginerInfo");
        //初始化resul
        String result =null;
        //从对象中获得文档标题
        String documentName = document.getDocumentTitle();
        //将部分属性存入doc对象中
        document.setDocumentId(TeamUtil.getUuid());
        document.setDocumentDept(loginCustom.getGuser().getUserDepartment());
        document.setDocumentUser(loginCustom.getGuser().getUserId());
        document.setDocumentConfidential(1);
        document.setDoucmentContent("  ");
        document.setDocumentRemark("  ");
        document.setDocumentProcess("1");
        document.setDocumentLocation(0);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        document.setCreationTime(df.format(new Date()));
        document.setDocumentState(1);
        document.setDocumentIsdelete(0);
        //将对象存入数据库
        int addResult = documentService.insert(document);
        Log log = new Log();
        //存入日志信息到日志表
        log.setLogId(TeamUtil.getUuid());
        log.setLogUser(loginCustom.getGuser().getUserId());
        log.setLogContent("添加了"+documentName+"文档");
        log.setCreationTime(df.format(new Date()));
        documentService.insertLog(log);
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (addResult==0){
            result = "updateFailed";
        }result = "updateSuccess";
        return document;
    }
    //补充文档信息
    @RequestMapping("/completeDocument")
    public String completeDocument(@RequestBody Document document){
        String result = null;
        int updateResult = documentService.updateByPrimaryKey(document);
        if (updateResult==0){
            result = "updateFailed";
        }result = "updateSuccess";
        return "{\"msg\":\""+result+"\"}";
    }

    //更改文档状态
    @RequestMapping("/updateDocumentState")
    public String updateDocumentState(@RequestBody Document document,HttpSession httpSession, HttpServletResponse response){
        //初始化result
        String result =null;
        //获取session中的信息
        LoginCustom loginCustom = (LoginCustom)httpSession.getAttribute("loginerInfo");
        //根据文档id修改文档状态
        String documentId = document.getDocumentId();
        //定义当前文档类型
        Integer documentState = 2;
        //根据得到的文档id获得文档信息
        Document document1 = documentService.selectByPrimaryKey(documentId);
        //更新文档状态为2
        int updateResult = documentService.updateDocumentState(documentState,documentId);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        Log log = new Log();
        String text = null;
        text = "提交了"+document1.getDocumentTitle()+"文档";
        log.setLogId(TeamUtil.getUuid());
        log.setLogUser(loginCustom.getGuser().getUserId());
        log.setLogContent(text);
        log.setCreationTime(df.format(new Date()));
        int addLogResult = documentService.insertLog(log);
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateResult==0){
            result = "updateFailed";
        }result = "updateSuccess";
        return "{\"msg\":\""+result+"\"}";
    }
    //更改文档当前所处流程的位置
    @RequestMapping("/updateDocumentLocation")
    public String updateDocumentLocation(@PathVariable String documentId,HttpSession httpSession, HttpServletResponse response){
        //初始化result
        String result =null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
        //获取session内容
        LoginCustom loginCustom = (LoginCustom)httpSession.getAttribute("loginerInfo");
        Document document = documentService.selectByPrimaryKey(documentId);
        //生成审核人日志文件
        Log log = new Log();
        log.setLogId(TeamUtil.getUuid());
        log.setLogUser(loginCustom.getGuser().getUserId());
        log.setLogContent("审核了"+document.getDocumentTitle()+"公文");
        log.setCreationTime(df.format(new Date()));
        documentService.insertLog(log);
        //生成被审核人的日志文件
        Log log1 = new Log();
        log1.setLogId(TeamUtil.getUuid());
        log1.setLogUser(document.getDocumentUser());
        log1.setLogContent("您的"+document.getDocumentTitle()+"被"+loginCustom.getGuser().getUserName()+"审核了");
        log1.setCreationTime(df.format(new Date()));
        documentService.insertLog(log1);
        int documentLocation = document.getDocumentLocation() + 1;
        //获取当前所走流程的最大步骤
        int maxStep = documentService.getMaxStep(document.getDocumentProcess());
        //根据流程所处不同节点改变文档状态：当前流程所处节点为1是文档状态改为3，当前流程所处节点位置为最后一位是文档状态改为4
        if(documentLocation==1){
            documentService.updateDocumentState(3,documentId);
        }else if (documentLocation==maxStep){
            documentService.updateDocumentState(4,documentId);
        }
        int updateLocationResult = documentService.updateDocumentLocation(documentLocation,documentId);
        //判断执行文档添加操作返回的结果，返回结果为数据库中受影响行数
        if (updateLocationResult==0){
            result = "updateFailed";
        }result = "updateSuccess";
        return "{\"msg\":\""+result+"\"}";
    }
    //通知下一个节点操作人
    @RequestMapping("/messageNextOne")
    public String messageNextOne(@RequestBody Document document){
        String result = null;
        int messageResult = documentService.insertMessage(document);
        if (messageResult==0){
            result = "updateFailed";
        }result = "updateSuccess";
        return "{\"msg\":\""+result+"\"}";
    }
    //根据文档状态查询文档
    @RequestMapping("/getDocumentByState")
    public List<Document> getDocumentByState(@PathVariable Integer documentState ){
        List<Document> list = documentService.getDocumentByState(documentState);
        return list;
    }
    //根据id查看文档全部信息
    @RequestMapping("/selectByPrimaryKey")
    public Document selectByPrimaryKey(@PathVariable String documentId){
        Document document = documentService.selectByPrimaryKey(documentId);
        return  document;
    }
    //查看文档的所有流程节点
    @RequestMapping("/getProcessNode")
    public List<ProcessNode> getProcessNode(@PathVariable String documentId){
        Document document = documentService.selectByPrimaryKey(documentId);
        List<ProcessNode> list = documentService.getAllProcessNode(document.getDocumentProcess());
        return  list;
    }
    //登录
    @PostMapping("/login")
    public String login(){
        return "{" +
                "  \"code\": 20000," +
                "  \"data\": {" +
                "    \"token\": \"admin\"" +
                "  }" +
                "}";
    }
    @GetMapping("/loginAnother")
    public String loginAnther(){
        return "{" +
                "  \"code\": 20000," +
                "  \"data\": {" +
                "    \"roles\": [\"admin\"]," +
                "    \"name\": \"admin\"," +
                "    \"avatar\": \"[图片]https://wpimg.wallstcn.com/f778738c-e4f8-4870-b634-56703b4acafe.gif\"" +
                "  }" +
                "}";
    }
}
