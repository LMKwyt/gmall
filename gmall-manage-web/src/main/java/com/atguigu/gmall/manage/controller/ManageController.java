package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseCatalog1;
import com.atguigu.gmall.bean.BaseCatalog2;
import com.atguigu.gmall.bean.BaseCatalog3;
import com.atguigu.gmall.service.ManageService;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ManageController {

    @Reference
    ManageService manageService;
//跳转到平台属性的界面
    @RequestMapping("attrListPage")
    public String getAttrListPage(){
        return "attrListPage";
    }
    //跳转到Spu的界面
    @RequestMapping("spuListPage")
    public String getSpuListPage(){
        return "spuListPage";
    }



    @GetMapping("index")
    public String index(){
        return "index";
    }

    @GetMapping("catalog1ForList")
    @ResponseBody
    public List<BaseCatalog1> catalog1ForAttrList(){
        List<BaseCatalog1> allBaseCatalog1List = manageService.getAllBaseCatalog1List();
        return allBaseCatalog1List;
    }
    @GetMapping("catalog2ForList")
    @ResponseBody
    public List<BaseCatalog2> catalog2ForAttrList(@RequestParam("catalog1Id") String catalog1Id){
        List<BaseCatalog2> allBaseCatalog2List = manageService.getAllBaseCatalog2List(catalog1Id);
        return allBaseCatalog2List;
    }
    @GetMapping("catalog3ForList")
    @ResponseBody
    public List<BaseCatalog3> catalog3ForAttrList(@RequestParam("catalog2Id") String catalog2Id){
        List<BaseCatalog3> allBaseCatalog3List = manageService.getAllBaseCatalog3List(catalog2Id);
        return allBaseCatalog3List;
    }

}
