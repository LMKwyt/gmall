package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AttrInfoController {
    @Reference
    ManageService manageService;

    @RequestMapping("attrList")
    public List<BaseAttrInfo> getattrList(@RequestParam("catalog3Id") String catalog3Id ){
        List<BaseAttrInfo> attrList = manageService.getAttrList(catalog3Id);
        return attrList;
    }
     @RequestMapping(value="saveAttrInfo",method = RequestMethod.POST)
     public void saveAttrInfo(BaseAttrInfo baseAttrInfo){
       manageService.saveAttrInfo(baseAttrInfo);
    }


    @RequestMapping("getAttrValueList")
    public String getAttrValueList(@RequestParam("attrId") String attrid){
        List<BaseAttrValue> attrValueList = manageService.getAttrValueList(attrid);
        return JSON.toJSONString(attrValueList);
    }
    @RequestMapping(value="delAttrInfo",method = RequestMethod.POST)
    public void  delAttrInfo(BaseAttrInfo baseAttrInfo){
      manageService.delAttrInfo(baseAttrInfo);
    }

    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> getattrInfoList(@RequestParam("catalog3Id") String catalog3Id ){
        List<BaseAttrInfo> attrInfoList = manageService.getattrInfoList(catalog3Id);
        return attrInfoList;
    }


}
