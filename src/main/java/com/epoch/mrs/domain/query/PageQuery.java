package com.epoch.mrs.domain.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

@Data
public class PageQuery {
    private Long pageNo;
    private Long pageSize;
    private String sortBy;
    private Boolean isAsc;

    public <T> Page<T> toMpPage(OrderItem... orders){
        // 1.分页条件
        Page<T> p = Page.of(pageNo, pageSize);
        // 2.排序条件
        // 2.1.先看前端有没有传排序字段
        if (sortBy != null && !sortBy.isEmpty()) {
            p.addOrder(new OrderItem().setAsc(isAsc).setColumn(sortBy));
            return p;
        }
        // 2.2.再看有没有手动指定排序字段
        if(orders != null && orders.length > 0){
            p.addOrder(orders);
        }
        return p;
    }

    public <T> Page<T> toMpPageDefaultSortByCreateTimeDesc() {
        return this.toMpPage(new OrderItem().setAsc(false).setColumn("create_time"));
    }

    public <T> Page<T> toMpPageDefaultSortByTimestampDesc() {
        return this.toMpPage(new OrderItem().setAsc(false).setColumn("timestamp"));
    }
}