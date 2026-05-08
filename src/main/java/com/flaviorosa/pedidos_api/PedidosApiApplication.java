package com.flaviorosa.pedidos_api;

import com.flaviorosa.pedidos_api.infrastructure.security.JwtService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class PedidosApiApplication {

	public static void main(String[] args) {

		SpringApplication.run(PedidosApiApplication.class, args);

	}

}
