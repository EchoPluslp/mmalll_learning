package com.mmall.controller.backend;

import com.google.common.collect.Maps;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.service.impl.pojo.Product;
import com.mmall.service.impl.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台产品模块
 *
 * @author Liupeng
 * @create 2018-05-01 14:45
 **/
@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 保存或者更新商品信息， 具体操作根据id值是否存在来判断
     * 有id则为更新操作，没有id则为保存操作
     * @param session 当前session 用于判断用户权限
     * @param product 商品信息封装类
     * @return 操作是否成功
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }

        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //执行保存或者更新操作
            return iProductService.SaveOrUpdateProduct(product);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

    /**
     * 修改商品的状态信息
     * @param session 当前session 用于判断用户权限
     * @param productId 需要修改的商品id
     * @param status 需要修改的状态
     * @return 是否修改成功
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session,Integer productId,Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }

        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //修改商品的状态
            return iProductService.setSaleStatus(productId,status);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

    /**
     * 后台展示商品详情
     * @param session 当前session 用于判断用户权限
     * @param productId 通过id查找商品详细
     * @return 返回商
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session,Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }

        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //后台的商品信息详情
                return iProductService.manageProductDetail(productId);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

    /**
     *
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }

        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //后台的详情
          //  return iProductService.manageProductDetail(productId);
            return iProductService.getProductList(pageNum,pageSize);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }

    /**
     * 查询商品信息
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session, String productName,Integer productId,@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                  @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }

        //判断是否是管理员
        if(iUserService.checkAdminRole(user).isSuccess()){
            //搜索功能
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员,请登陆管理员用户");
        }
    }


    /**
     *
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session,@RequestParam(value = "upload_file",required = false
            ) MultipartFile file, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录，请登陆管理员");
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //上传图片功能
            String path = request.getSession().getServletContext().getRealPath("upload");
            //得到新建文件对象的名称
            String targetFileName = iFileService.upload(file,path);
            //对图片的url进行拼接!!!
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;

            Map filemap = new HashMap();//通过map，将图片上传的信息，返给前端
            filemap.put("uri",targetFileName);
            filemap.put("url",url);
            return ServerResponse.createBySuccess(filemap);
        }else{
            return  ServerResponse.createByErrorMessage("该用户不是管理员 ,请登陆管理员用户");
        }
    }

    /**
     *富文本上传
     * @param session
     * @param file
     * @param request
     * @return
     *     富文本中对于返回值有自己的要求.我们使用的是simditor富文本编辑器，所以按照simditor的要求进行返回
            {
                "success":true/false
                    "msg":"error msg",#Optional
                    "file_path":"[real file path]"
            }
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false
    ) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        //通过map来对simditor返回的文档格式要求，进行记录
        Map resultMap = Maps.newHashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            resultMap.put("success",false);
            resultMap.put("msg","用户未登录，请登陆管理员");
            return resultMap;
        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //上传图片功能
            String path = request.getSession().getServletContext().getRealPath("upload");
            //得到新建文件对象的名称
            String targetFileName = iFileService.upload(file,path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            //对图片的url进行拼接!!!
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            //按照simditor的要求 设置返回格式！！！
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            //按照simditor的要求，对response头进行添加.
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作，请登陆管理员");
            return resultMap;
        }
    }




}