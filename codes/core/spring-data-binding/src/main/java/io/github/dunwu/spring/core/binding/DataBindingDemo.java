package io.github.dunwu.spring.core.binding;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.validation.DataBinder;

/**
 * 数据绑定示例
 *
 * @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
 * @date 2023-01-11
 */
public class DataBindingDemo {

    public static void main(String[] args) {

        MutablePropertyValues mpv = new MutablePropertyValues();
        mpv.add("num", "10");

        TestBean testBean = new TestBean();
        DataBinder db = new DataBinder(testBean);

        db.bind(mpv);
        System.out.println(testBean);
    }

}
