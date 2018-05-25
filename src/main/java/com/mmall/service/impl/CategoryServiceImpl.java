package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.service.impl.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 分类模块的实现类
 *
 * @author Liupeng
 * @create 2018-04-30 23:02
 **/
@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService{

    @Autowired
    private CategoryMapper categoryMapper;


    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);
    /**
     * 增加分类信息
     * @param categoryName 分类名称
     * @param parentId 父类ID
     * @return 添加分类是否成功
     */
    @Override
    public ServerResponse addCategory(String categoryName,Integer parentId){
        //判断传入的参数是否为空
        if(parentId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("添加分类的参数错误");
        }

        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);//设置父节点
        category.setStatus(true);//新添加的分类可用

        int resultCount = categoryMapper.insert(category);
        if(resultCount > 0){
            return ServerResponse.createBySuccessMessage("添加分类信息成功");
        }
        return ServerResponse.createByErrorMessage("添加分类信息失败");
    }

    /**
     * 通过id修改分类的名称
     * @param categoryId 分类id
     * @param categoryName 分类名称
     * @return 修改分类名称是否成功
     */
    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if(categoryId == null || StringUtils.isBlank(categoryName)){
            return ServerResponse.createByErrorMessage("更新分类的参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);//新名称
        int resultCount = categoryMapper.updateByPrimaryKeySelective(category);
        if(resultCount>0){
            return ServerResponse.createBySuccessMessage("更新分类名称成功");
        }
        return ServerResponse.createByErrorMessage("更新分类名称失败");
    }

    /**
     * 通过id查找该id下的所有平级子节点
     * @param categoryId id节点
     * @return 所以平级子节点的list集合
     */
    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId){
        //通过category
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)){
             logger.info("为找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查找当前id和子节点的id，并返回list集合
     * @param categoryId 查找当前节点下的所有子节点
     * @return 所有list集合
     */
    public ServerResponse selectCategoryAndChildrenById(Integer categoryId){
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet,categoryId);//通过递归实现查找当前节点和子节点的id

        List<Integer> categoryList = Lists.newArrayList();
        if(categoryList != null){
        for(Category category : categorySet) {
            categoryList.add(category.getId());//循环遍历set集合，并将值付给list集合中，便于返回
         }
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    //递归：得出所有子节点
    private Set<Category> findChildCategory(Set<Category> categorySet,Integer categoryId){
        Category category = categoryMapper.selectByPrimaryKey(categoryId);//得到当前节点的id
        if(category != null){
            categorySet.add(category);
        }
        //递归遍历当前节点下的所有子节点
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        //注意：因为Mybatis如果查找不到信息时，它不会返回null，所以categoryList就不会为空，就不需要进行空判断
        //递归出口：
         for(Category categoryItem : categoryList){
                findChildCategory(categorySet,categoryItem.getId());//递归查找
        }
        return categorySet;
    }
}