package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.service.impl.pojo.Category;

import java.util.List;

/**
 * 分类模块的接口
 *
 * @author Liupeng
 * @create 2018-04-30 23:01
 **/
public interface ICategoryService {
    ServerResponse addCategory(String categoryName, Integer parentId);

    ServerResponse updateCategoryName(Integer categoryId, String categoryName);

    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId);
}
