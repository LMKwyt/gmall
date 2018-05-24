package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.bean.SpuSaleAttrValue;
import com.atguigu.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class SpuInfoController {
    @Value("${fileServer.url}")
    String fileUrl;

    @Reference
    ManageService manageService;



    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return "success";
    }

    @RequestMapping("spuList")
    public String getSpuList(@RequestParam("catalog3Id") String catalog3Id) {

        List<SpuInfo> spuList = manageService.getSpuList(catalog3Id);
        String s = JSON.toJSONString(spuList);
        return s;
    }


    @RequestMapping(value = "/fileUpload", method = RequestMethod.POST)
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException, MyException {
        //http://file.server.com 从配置文件中获取的服务器地址
        String imgUrl = fileUrl;
        if (file != null) {
            //点文件名字和文件大小
            System.out.println("multipartFile = " + file.getName() + "|" + file.getSize());
            //读取tracker。conf的配置文件
            String configFile = this.getClass().getResource("/tracker.conf").getFile();
            //客户端根据配置文件初始化
            ClientGlobal.init(configFile);
            //建立一个路由器
            TrackerClient trackerClient = new TrackerClient();
            //获取路由器连接
            TrackerServer trackerServer = trackerClient.getConnection();
            //真实存储服务器
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //获取文件名字
            String filename = file.getOriginalFilename();
            //使用StringUtile 截取最后一个.的后缀
            String extName = StringUtils.substringAfterLast(filename, ".");
            //获取下载地址的一个数组
            String[] upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            imgUrl = fileUrl;
            //循环将地址进行拼接
             //     s = group1
             //    s = M00/00/00/wKi2tlrz8U2AWwWDAAJNRKzW3GQ495.jpg
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl += "/" + path;
            }

        }

        return imgUrl;
    }



    @RequestMapping("getSpuinfo")
     public String getSpuinfo(@RequestParam("spuId") String spuId){
        SpuInfo spuinfo = manageService.getSpuinfo(spuId);
        String jsonString = JSON.toJSONString(spuinfo);
        return jsonString;
    }

    @RequestMapping("getspuImageList")
    public List<SpuImage> getspuImageList(@RequestParam("spuId") String spuId){
        List<SpuImage> spuImageList = manageService.getspuImageList(spuId);

        return spuImageList;
    }


    @RequestMapping("getspuSaleAttrList")
    public List<SpuSaleAttr> getgetspuSaleAttrList(@RequestParam("spuId") String spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getspuSaleAttrList(spuId);


        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            Map map=new HashMap();
            map.put("total",spuSaleAttrValueList.size());
            map.put("rows",spuSaleAttrValueList);
            spuSaleAttr.setSpuSaleAttrValueJson(map);
        }

        return spuSaleAttrList;
    }


    @RequestMapping("getspuSaleAttrValueList")
    public String getspuSaleAttrValue(@RequestParam("SaleAttrId") String SaleAttrId,@RequestParam("spuId") String spuId){
        List<SpuSaleAttrValue> spuSaleAttrValueList = manageService.getspuSaleAttrValue(SaleAttrId,spuId);
        String jsonString = JSON.toJSONString(spuSaleAttrValueList);
        return jsonString;
    }



}




