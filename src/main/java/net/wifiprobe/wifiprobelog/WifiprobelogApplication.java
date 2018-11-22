package net.wifiprobe.wifiprobelog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@MapperScan("net.wifiprobe.wifiprobelog.business.dao")
@PropertySource(value={"file:/home/iov/adx/deploy/wifiprobe/wifiprobelog/application.properties"})
public class WifiprobelogApplication {

	public static void main(String[] args) {
		SpringApplication.run(WifiprobelogApplication.class, args);
	}
}
