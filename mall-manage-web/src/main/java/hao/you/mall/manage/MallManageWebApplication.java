package hao.you.mall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
public class MallManageWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallManageWebApplication.class, args);
    }

}
