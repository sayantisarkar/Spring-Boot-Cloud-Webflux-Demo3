package com.accenture.lkm;

import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
//import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.accenture.lkm.controller.EmployeeController;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> routes(EmployeeController controller ){
		return 
		route(GET("/employees").and(accept(APPLICATION_JSON)), serverReq->controller.getAllEmployees(serverReq))
		.andRoute(POST("/saveEmployee").and(contentType(APPLICATION_JSON)), serverReq->controller.saveEmployee(serverReq))
		.andRoute(GET("/employee/{employeeId}").and(accept(APPLICATION_JSON)), serverReq->controller.getEmployeesById(serverReq))
		.andRoute(PUT("/updateEmployee").and(contentType(APPLICATION_JSON)), serverReq->controller.updateEmployee(serverReq))
		.andRoute(DELETE("/employee/{employeeId}").and(accept(APPLICATION_JSON)), controller::deleteEmployee); 

	}
	
}
