package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.service.IShippingService;
import com.mmall.service.impl.pojo.Shipping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 用户地址的实现类
 *
 * @author Liupeng
 * @createTime 2018-05-14 22:22
 **/
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService{

    @Autowired
    private ShippingMapper shippingMapper;
    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        //注意，当前insert的sql语句返回的是插入shipping后的id号
        //可查看insert的sql语句配置
        int rowCount = shippingMapper.insert(shipping);
        if(rowCount > 0){
            //与前端约定为，新增成功后，返回该id值，并且id为valye值
            Map resultMap = Maps.newHashMap();
            resultMap.put("shippingId",shipping.getId());
            return  ServerResponse.createBySuccess("新建地址成功",resultMap);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse<String> del(Integer userId,Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);
        if(resultCount > 0){
            return  ServerResponse.createBySuccessMessage("删除地址成功");
        }
        return  ServerResponse.createBySuccessMessage("删除地址失败");
    }

    public ServerResponse update(Integer userId, Shipping shipping){
        //注意，此时应该再赋值一次userid，因为
        //如果不赋值，用户可能根据该接口的参数，传入另外一个id值，从而修改其他人的信息.
        shipping.setUserId(userId);

        //sql语句中，将userid和productId进行绑定，从而避免横向越权问题
        int rowCount = shippingMapper.updateByShipping(shipping);
        if(rowCount > 0){
            return  ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        //sql语句中，将userid和productId进行绑定，从而避免横向越权问题
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);
        if(shipping != null){
            return ServerResponse.createBySuccess("查询地址成功",shipping);
        }
            return ServerResponse.createByErrorMessage("无法查询到该地址");
    }

    public ServerResponse<PageInfo> list(Integer userId,Integer pageNum,Integer pageSize){
        //开始pageHelper
        PageHelper.startPage(pageNum,pageSize);
        //排序逻辑
       List<Shipping> ResultList =  shippingMapper.selectByUserId(userId);
       //pageHelper设置
        PageInfo pageInfo = new PageInfo(ResultList);
        //将当前查询到的信息返回到前端
        return  ServerResponse.createBySuccess(pageInfo);
    }
}