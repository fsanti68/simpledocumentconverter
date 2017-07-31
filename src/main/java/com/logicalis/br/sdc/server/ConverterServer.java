package com.logicalis.br.sdc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Convertion main application - exposes convertion and user's APIs.
 * 
 * @author Fabio De Santi
 *
 */
@EnableAutoConfiguration
@EnableRedisRepositories
@Configuration
@EnableSwagger2
@ComponentScan("com.logicalis.br.dataprev")
public class ConverterServer {

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		JedisConnectionFactory jcf = new JedisConnectionFactory();
		jcf.setUsePool(true);
		return jcf;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any()).build();
	}

	public static void main(String[] args) {
		// Will configure using converter-server.yml
		System.setProperty("spring.config.name", "converter-server");

		SpringApplication.run(ConverterServer.class, args);
	}
}
