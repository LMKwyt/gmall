package com.atguigu.gmall.usermanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.**.mapper")
@ComponentScan("com.atguigu.gmall")
public class GmallUsermanageApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUsermanageApplication.class, args);
	}
}
