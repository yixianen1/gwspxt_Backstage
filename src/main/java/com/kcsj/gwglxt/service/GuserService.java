package com.kcsj.gwglxt.service;

import com.kcsj.gwglxt.DTO.CountUserByMouth;
import com.kcsj.gwglxt.entity.Guser;
import com.kcsj.gwglxt.entity.GuserExample;
import com.kcsj.gwglxt.DTO.LoginCustom;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface GuserService {
    int countByExample(GuserExample example);

    int deleteByExample(GuserExample example);

    int deleteByPrimaryKey(String userId);

    int insert(Guser record);

    int insertSelective(Guser record);

    List<Guser> selectByExample(GuserExample example);

    Guser selectByPrimaryKey(String userId);

    int updateByExampleSelective(@Param("record") Guser record, @Param("example") GuserExample example);

    int updateByExample(@Param("record") Guser record, @Param("example") GuserExample example);

    int updateByPrimaryKeySelective(Guser record);

    int updateByPrimaryKey(Guser record);

    LoginCustom loginInfo(String userId);
    LoginCustom loginFunction(String userAccount);
    //查询个人信息
    LoginCustom getPersonalInfo(String userId);

    CountUserByMouth countUserByMouth();

    int countAllUser();

    int countAllDocument(String department,String user);
}
