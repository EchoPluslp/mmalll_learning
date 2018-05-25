package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.service.impl.pojo.Category;
import com.mmall.service.impl.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品模块接口的实现
 *
 * @author Liupeng
 * @create 2018-05-01 14:54
 **/
@Service("iProductService")
public class ProductServiceImpl implements IProductService{

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;


    public ServerResponse SaveOrUpdateProduct(Product product){
        if(product != null){
            //判断子图是否为空， 因为主图会采用第一个子图
            if(StringUtils.isNotBlank(product.getSubImages())){
                //与前端约定，图片的utl分割为,号
                String[] imgs = product.getSubImages().split(",");
                if(imgs.length>0) {
                    product.setMainImage(imgs[0]);//将第一个子图赋主图
                }
            }

            //判断product是否有id值，如果有，则是更新操作，如果没有，这是保存操作
            if(product.getId()!=null){
                //更新操作//因为id是在数据库中生成的,所以当传入参数有id是，是更新
                int resultCount = productMapper.updateByPrimaryKey(product);
                if(resultCount>0){
                    return ServerResponse.createBySuccessMessage("更新商品成功");
                }
                return ServerResponse.createByErrorMessage("更新商品失败");
            }else{
                //保存操作
                int resultCount = productMapper.insert(product);
                if(resultCount>0){
                    return ServerResponse.createBySuccessMessage("保存商品成功");
                }
                return ServerResponse.createByErrorMessage("保存商品失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或者更新产品的参数不正确");
    }

    /**
     * 修改商品销售状态成功
     * @param productId
     * @param status
     * @return
     */
    public ServerResponse<String> setSaleStatus(Integer productId,Integer status){
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
//新建商品信息，用于更新操作
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);

        int resultCount = productMapper.updateByPrimaryKeySelective(product);
        if(resultCount >0){
            return ServerResponse.createBySuccessMessage("更新商品销售状态成功");
        }
        return  ServerResponse.createBySuccessMessage("更新商品销售状态失败");
    }

    /**
     * 后台获取商品详情
     * @param productId
     * @return
     */
    //后台实现使用manage开头
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId == null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("没有找到该id下的商品，商品下架或者被删除了");
        }

        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return  ServerResponse.createBySuccess(productDetailVo);
    }


    /**
     * 初始化vo 值对象
     * @param product
     * @return
     */
    public ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();

        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImage(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());

        //imageHost
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        //得到parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//默认为根节点
        }else {
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        //createTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        //updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }


    /**
     *后台商品列表动态分页功能与展示信息的封装
     * @param pageNum
     * @param pageSize
     * @return
     */
    public ServerResponse<PageInfo> getProductList(int pageNum,int pageSize){
        //pageHelper使用方法：
        //1.startPage-start
        PageHelper.startPage(pageNum,pageSize);

        //2.填充自己的sql查询逻辑
        List<ProductListVo> productListVoList = new ArrayList<>();

        List<Product> productList = productMapper.selectList();//返回当前商品的指定信息
        for(Product productItem : productList){
            //通过vo类，可以指定我们想要在展示页面展示的信息！！！
            ProductListVo productListVo = assembleProductListVo(productItem);
            //将想要展示信息的类放到集合中
            productListVoList.add(productListVo);
        }
        //3.pageHelp收尾
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        //返回想要展现的信息集合
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 使用product类，生成 productListVo类
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;

    }

    public ServerResponse<PageInfo> searchProduct(java.lang.String productName, Integer productId, int pageNum, int pageSize){
      //1.开启分页
        PageHelper.startPage(pageNum,pageSize);
        //2.sql逻辑
        if(StringUtils.isNotBlank(productName)){
            //拼接sql语句，使得可以模糊查询
            productName = new StringBuilder().append('%').append(productName).append('%').toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);
        //将product循环产生listVo
        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product productItem : productList){
            //通过vo类，可以指定我们想要在展示页面展示的信息！！！
            ProductListVo productListVo = assembleProductListVo(productItem);
            //将想要展示信息的类放到集合中
            productListVoList.add(productListVo);
        }
        //3.pageHelp收尾
        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        //返回想要展现的信息集合
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 前端页面生成商品列表页
     * @param productId
     * @return
     */
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("没有找到该id下的商品，商品下架或者被删除了");
        }
        if(product.getStatus()!= Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("商品下架或者被删除了");
        }
        //找到对应的Product后，生成对应的VO对象
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return  ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     *
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     *Const.ProductListOrderBy.PRICE_ASC_DESC 该常量类使用set方法是因为contains方法的时间复杂度是O1
     *      而List的contains的时间复杂度是On
     */
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){
        if(StringUtils.isBlank(keyword)&&categoryId == null){
            return  ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryList = Lists.newArrayList();
        if(categoryId!=null){
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //说明没有该分类，并且还没有该关键字，这时候应该返回一个空集合，而不是返回错误
                PageHelper.startPage(pageNum,pageSize);
                List<ProductDetailVo> list = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(list);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        //拼接关键字
        if(StringUtils.isNotBlank(keyword)){
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        //PageHelper开始
        PageHelper.startPage(pageNum,pageSize);
        //动态排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(Const.ProductListOrderBy.PRICE_ASC_DESC)){
                //使用pageHelper进行动态排序 需要返回的格式pageHelper.orderby("price" "asc");
                String[] orders = orderBy.split("_");
                PageHelper.orderBy(orders[0] + " " + orders[1]);
            }
        }
        //通过
        //对keyword和categoryID进行if判断，如果不为空，则传值，如果为null，则传入null
        //使得dao层的in没有值
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryList.size()==0?null:categoryList);

        //将productList赋值到vo中
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for(Product product : productList){
            ProductListVo ProductListVo = assembleProductListVo(product);
            productListVoList.add(ProductListVo);
        }
        //将数据交给pagehelper处理
        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}