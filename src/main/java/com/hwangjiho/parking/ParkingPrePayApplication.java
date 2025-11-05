package com.hwangjiho.parking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.hwangjiho.parking.mapper")
public class ParkingPrePayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingPrePayApplication.class, args);
	}

}
