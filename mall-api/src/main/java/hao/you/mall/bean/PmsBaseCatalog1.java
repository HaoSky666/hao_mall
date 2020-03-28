package hao.you.mall.bean;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * @param
 * @return
 */
public class PmsBaseCatalog1 implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String name;

    // 不是数据库中字段
    @Transient
    private List<PmsBaseCatalog2> catalog2;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PmsBaseCatalog2> getCatalog2() {
        return catalog2;
    }

    public void setCatalog2(List<PmsBaseCatalog2> catalog2) {
        this.catalog2 = catalog2;
    }
}

