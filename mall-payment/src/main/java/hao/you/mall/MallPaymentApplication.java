package hao.you.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "hao.you.mall.payment.mapper")
public class MallPaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPaymentApplication.class, args);
    }

}
