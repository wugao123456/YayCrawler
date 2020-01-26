package yaycrawler.dao.domain;


import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * 表示规则的版本
 * Created by ucs_yuananyun on 2016/8/30.
 */
@Entity
@Table(name = "conf_rule_version")
public class RuleVersion {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid")
    private String id;

    @NotNull
    @Column(name = "version", columnDefinition = "int default 0")
    private int version;


}
