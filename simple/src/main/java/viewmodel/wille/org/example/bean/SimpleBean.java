package viewmodel.wille.org.example.bean;

/**
 * 创建人员：杨浩
 * 创建日期：2018/7/31
 * 功能简介：
 */
public class SimpleBean {
    String simple = null;

    public String getSimple() {
        return simple;
    }

    public void setSimple(String simple) {
        this.simple = simple;
    }

    private static SimpleBean sSimpleBean = null;

    public static SimpleBean getSimple(String info) {
        if (sSimpleBean == null) {
            sSimpleBean = new SimpleBean();
        }
        sSimpleBean.setSimple(info);
        return sSimpleBean;
    }
}
