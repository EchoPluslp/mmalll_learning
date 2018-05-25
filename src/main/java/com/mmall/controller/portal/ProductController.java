package com.mmall.controller.portal;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.service.IProductService;
import com.mmall.vo.ProductDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 前台的商品控制类
 *
 * @author Liupeng
 * @create 2018-05-02 10:39
 **/
@Controller
@RequestMapping("/product/")
public class ProductController {
    @Autowired
    private IProductService iProductService;

    /**
     * 通过商品id准确查找商品
     * @param productId 商品id号码
     * @return 符合条件的商品信息
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    /**
     * 用户端的产品搜索
     * 可传入关键字keyword或者categoryId进行模糊查询
     *
     * @param keyword 需要查询的商品名称
     * @param categoryId 需要查询的分类Id
     * @param pageNum 默认page数量
     * @param pageSize 默认每页展示的数量
     * @param orderBy 排序方式
     * @return 符合条件的list集合，用于前台展示数据
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword",required = false)String keyword,
     @RequestParam(value = "categoryId",required = false)Integer categoryId,
      @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
      @RequestParam(value = "pageSize",defaultValue = "10")int pageSize,
      @RequestParam(value = "orderBy",defaultValue = "")String orderBy){
        return iProductService.getProductByKeywordCategory(keyword, categoryId, pageNum, pageSize, orderBy);
    }
}