package com.atguigu.gmall.manage.servce.impl;

import com.alibaba.dubbo.config.annotation.Service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;

import com.atguigu.gmall.manage.consts.RedisConst;
import com.atguigu.gmall.manage.mapper.*;

import com.atguigu.gmall.service.ManageService;
import com.atguigu.gmall.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class ManagerServiceImpl implements ManageService {
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    BaseSpuListMapper baseSpuListMapper;
    @Autowired
    SpuSaleAtterMapper spuSaleAtterMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMpper skuImageMpper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    RedisUtils redisUtils;

    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(Long.parseLong(spuId));
        return skuSaleAttrValueList;
    }


    public SkuInfo getSkuInfoById(String skuId) {
        Jedis jedis=null;
        try {
              jedis = redisUtils.getJedis();
              //实际值的名字
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;

            //访问先查缓存
            String skuInfoJson = jedis.get(skuKey);

            if (skuInfoJson != null && skuInfoJson.length() > 0) {
                if ("empty".equals(skuInfoJson)) {
                    System.err.println(Thread.currentThread().getName() + "：值不存在, 关闭连接");
                 //   jedis.close();
                    return null;
                } else {
                    System.err.println(Thread.currentThread().getName() + "：命中缓存");
                    SkuInfo skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
              //      jedis.close();
                    return skuInfo;
                }

            } else {
                System.err.println(Thread.currentThread().getName() + "未命中");
                //先检查是否能获得锁，同时尝试获得锁
                //锁的名字
                String skuLockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_PREFIX;
                System.err.println(Thread.currentThread().getName() + "设置3秒锁");
                String ifLocked = jedis.set(skuLockKey, "locked", "NX", "EX", 3);
                if (ifLocked == null) {
                    System.err.println(Thread.currentThread().getName() + "未获得锁，开始自旋");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //线程自旋
                    return getSkuInfoById(skuId);
                }else{
                    //缓存中没有 先去数据库中查
                    System.err.println(Thread.currentThread().getName() + "使用数据库查询数据###### ##");
                    SkuInfo skuInfoForDB = getSkuInfoByIdForDB(skuId);
                    //判断数据库中是否存在 解决数据库没有一直点击的问题
                    if (skuInfoForDB == null) {
                        System.err.println(Thread.currentThread().getName() + "数据库中没有这个值###### ##");
                        jedis.setex(skuKey, RedisConst.SKUKEY_TIMEOUT, "empty");
                    } else {
                        //存到缓存中
                        String skuInfojsonString = JSON.toJSONString(skuInfoForDB);
                        System.err.println(Thread.currentThread().getName() + "：将查到的数据库放入Redis######");
                        jedis.setex(skuKey, RedisConst.SKUKEY_TIMEOUT, skuInfojsonString);

                    }
                    return skuInfoForDB;
                }

               // jedis.close();

            }
        } catch (JedisException e) {
            e.printStackTrace();
        }finally {
          if(jedis!=null){
               jedis.close();
            }
        }
              return getSkuInfoByIdForDB(skuId);
    }


    public SkuInfo getSkuInfoByIdForDB(String skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
         if(skuInfo==null){
            return null;
        }
        SkuImage skuImageQuery = new SkuImage();

        skuImageQuery.setSkuId(skuInfo.getId());
        List<SkuImage> skuImageList = skuImageMpper.select(skuImageQuery);

        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }

    public List<SkuInfo> getSkuInfoListBySpu(String spuId) {
        List<SkuInfo> skuInfoList = skuInfoMapper.selectSkuInfoListBySpu(Long.parseLong(spuId));
        return skuInfoList;

    }

    public void saveSkuInfo(SkuInfo skuInfo) {
        //判断sku的ID是否为空值
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }


        //清空sku图片的数据库在向里面添加
        SkuImage skuImageDel = new SkuImage();
        skuImageDel.setSpuImgId(skuInfo.getSpuId());
        skuImageMpper.delete(skuImageDel);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            if (skuImage.getId() != null && skuImage.getId().length() == 0) {
                skuImage.setId(null);
            }
            skuImageMpper.insertSelective(skuImage);
        }

        //清空数据库中sku平台属性值
        Example skuAttrValueExample = new Example(SkuAttrValue.class);
        skuAttrValueExample.createCriteria().andEqualTo("skuId", skuInfo.getId());
        skuAttrValueMapper.deleteByExample(skuAttrValueExample);

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            if (skuAttrValue.getId() != null && skuAttrValue.getId().length() == 0) {
                skuAttrValue.setId(null);
            }
            skuAttrValueMapper.insertSelective(skuAttrValue);
        }


        Example skuSaleAttrValueExample = new Example(SkuSaleAttrValue.class);
        skuSaleAttrValueExample.createCriteria().andEqualTo("skuId", skuInfo.getId());
        skuSaleAttrValueMapper.deleteByExample(skuSaleAttrValueExample);

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValue.setId(null);
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

    }


    public List<SpuSaleAttrValue> getspuSaleAttrValue(String SaleAttrId, String spuId) {
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        //根据商品属性的ID 获取所有的商品属性值
        spuSaleAttrValue.setSaleAttrId(SaleAttrId);
        spuSaleAttrValue.setSpuId(spuId);
        List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
        return spuSaleAttrValueList;
    }

    public List<BaseAttrInfo> getattrInfoList(String catalog3Id) {
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectattrInfoList(Long.parseLong(catalog3Id));
        return baseAttrInfoList;
    }

    public List<SpuSaleAttr> getspuSaleAttrList(String spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAtterMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
        return spuSaleAttrList;

    }

    public List<SpuSaleAttr> getspuSaleAttrListByskuId(String spuId, String skuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAtterMapper.selectSpuSaleAttrListBySku(Long.parseLong(spuId), Long.parseLong(skuId));
        return spuSaleAttrList;

    }


    public List<SpuImage> getspuImageList(String spuId) {
        //查询spu所有的图片集合
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        return spuImageList;
    }

    public SpuInfo getSpuinfo(String spuId) {

        //查询一个空的spu实例
        SpuInfo spuInfo = spuInfoMapper.selectByPrimaryKey(spuId);
        //查询spu所有的图片集合
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        spuInfo.setSpuImageList(spuImageList);
        //查询spu所有的商品属性集合
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAtterMapper.select(spuSaleAttr);
        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            //查询spu素有的商品属性值得集合
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            //根据商品属性的ID 获取所有的商品属性值
            spuSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
            saleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
        }
        //查询结束将商品实例的集合返回设置
        spuInfo.setSpuSaleAttrList(spuSaleAttrList);
        return spuInfo;

    }

    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存主表 通过主键存在判断是修改 还是新增
        if (spuInfo.getId() == null || spuInfo.getId().length() == 0) {
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        } else {
            spuInfoMapper.updateByPrimaryKey(spuInfo);
        }


        //获取SPu的ID值以便于后面添加
        String infoId = spuInfo.getId();
        //获取SPu图片对象的集合
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        //清空SPU的图片保存路径在向里面重新写入值！
        SpuImage spuImagedel = new SpuImage();
        spuImagedel.setSpuId(infoId);
        spuImageMapper.delete(spuImagedel);
        //循环靖SPU图片的每个对象保存到数据库中
        if (spuImageList != null) {
            for (SpuImage spuImage : spuImageList) {
                if (spuImage.getId() != null && spuImage.getId().length() == 0) {
                    spuImage.setId(null);
                }

                // 为每一个SPU图片加上SPU的ID值
                spuImage.setSpuId(infoId);
                spuImageMapper.insertSelective(spuImage);
            }
        }
        //获取SPU的编辑属性的集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //清空SPU的属性的数据库的值
        SpuSaleAttr spuSaleAttrDel = new SpuSaleAttr();
        //根据SPU的ID值删除所有原有的属性列表
        spuSaleAttrDel.setSpuId(infoId);
        spuSaleAtterMapper.delete(spuSaleAttrDel);

        //清空每个平台属性的平台属性值 重新添加
        SpuSaleAttrValue spuSaleAttrValueDel = new SpuSaleAttrValue();
        spuSaleAttrValueDel.setSpuId(infoId);
        spuSaleAttrValueMapper.delete(spuSaleAttrValueDel);

        if (spuSaleAttrList != null) {

            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                if (spuSaleAttr.getId() != null && spuSaleAttr.getId().length() == 0) {
                    spuSaleAttr.setId(null);
                }

                spuSaleAttr.setSpuId(infoId);
                spuSaleAtterMapper.insertSelective(spuSaleAttr);
                //每个平台属性对象获取平台属性值得集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();


                //循环平台属性值集合 得到每个平台属性值对象
                for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                    if (spuSaleAttrValue.getId() != null && spuSaleAttrValue.getId().length() == 0) {
                        spuSaleAttrValue.setId(null);
                    }

                    //为每个平台属性值设置SPU的ID 便于查询
                    spuSaleAttrValue.setSpuId(infoId);
                    //向数据库中传值
                    spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                }
            }

        }
    }

    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }


    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);

        List<SpuInfo> spuInfoList = baseSpuListMapper.select(spuInfo);
        return spuInfoList;
    }


    public List<BaseCatalog1> getAllBaseCatalog1List() {
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }

    public List<BaseCatalog2> getAllBaseCatalog2List(String catalog1Id) {
        BaseCatalog2 baseCatalog2query = new BaseCatalog2();
        baseCatalog2query.setCatalog1Id(catalog1Id);
        List<BaseCatalog2> baseCatalog2List = baseCatalog2Mapper.select(baseCatalog2query);
        return baseCatalog2List;

    }

    public List<BaseCatalog3> getAllBaseCatalog3List(String catalog2Id) {
        BaseCatalog3 baseCatalog3query = new BaseCatalog3();
        baseCatalog3query.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3query);
        return baseCatalog3List;
    }

    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        return baseAttrInfoList;
    }

    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //判断baseAttrInfo的主键ID是否有
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            //有执行修改
            baseAttrInfoMapper.updateByPrimaryKey(baseAttrInfo);
        } else {
            //防止主键被赋上一个空字符串
            if (baseAttrInfo.getId().length() == 0) {
                baseAttrInfo.setId(null);
            }
            //没有的执行添加
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        //把原属性值全部清空
        BaseAttrValue baseAttrValue4Del = new BaseAttrValue();
        baseAttrValue4Del.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue4Del);
        //才能使主键重复的值写入进去
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getBaseAttrValueList();
        String id = baseAttrInfo.getId();
        for (BaseAttrValue baseAttrValue : baseAttrValueList) {
            //防止主键被赋上一个空字符串
            if (baseAttrValue.getId() != null && baseAttrValue.getId().length() == 0) {
                baseAttrValue.setId(null);
            }

            baseAttrValue.setAttrId(id);
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }
    }

    public List<BaseAttrValue> getAttrValueList(String attrid) {
        BaseAttrValue baseAttrValueQuery = new BaseAttrValue();
        baseAttrValueQuery.setAttrId(attrid);
        List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValueQuery);
        return baseAttrValueList;

    }

    public void delAttrInfo(BaseAttrInfo baseAttrInfo) {
        baseAttrInfoMapper.deleteByPrimaryKey(baseAttrInfo);
        BaseAttrValue baseAttrValueQuery = new BaseAttrValue();
        baseAttrValueQuery.setAttrId(baseAttrInfo.getId());
        Example example = new Example(BaseAttrValue.class);
        example.createCriteria().andEqualTo("attrId", baseAttrInfo.getId());
        baseAttrValueMapper.deleteByExample(example);
    }
}
