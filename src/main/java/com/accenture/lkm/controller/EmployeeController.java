package com.accenture.lkm.controller;

/**
 * Refer the static imports
 * */

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * Refer the static imports
 * */
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.accenture.lkm.business.bean.EmployeeBean;
import com.accenture.lkm.service.EmployeeService;

/** all method of the Controller will return a publisher
either Mono or Flux*/
@RestController
public class EmployeeController {
 
	private EmployeeService employeeService;

	@Autowired
	public EmployeeController(EmployeeService employeeService) {
		super();
		this.employeeService = employeeService;
	}
	
	/**
	 * findAll() of the EmployeeService is invoked.
	 * This method returns the Flux<EmployeeBean>
	 * ServerResponse.ok() returns a Response BodyBuilder.
	 * Using this BodyBuilder’s body() method the response is built and sent.
	 * This method will automatically create the Mono<ServerResponse>.
	 
	 * There is no need to subscribe to the publisher,
	 * as subscription will be done by the front End controller.
	 * In web flux the name of the front end controller is DispatcherHandler.
	 * */
	public Mono<ServerResponse> getAllEmployees(ServerRequest req) {
		Flux<EmployeeBean> emps= employeeService.findAll();
		
        return ServerResponse.ok()
        		.contentType(MediaType.APPLICATION_JSON)
        		.body(emps, EmployeeBean.class);
    }
    
	/**Requirement: It is required to find Employee by Id.
	 * If it is found send ok response else return not found.
	 * In last method getAllEmployees() would always return ok() response.
	 * It cannot check if the details exist or not, as ServerResponse.ok().body()
	 * will always give non empty response.
	 *  
	 * To do the current requirement, In the current method,
	 * From the ServerRequest employeeId parameter is extracted. 
	 * Passing the employeeId extracted above, findById() of the EmployeeService is invoked.
	 * This method returns the Mono<EmployeeBean>
	 * This response is converted to Mono<ServerResponse> using the ServerResponse.ok().body()
	 * But the method: ServerResponse.ok().body() also gives a Mono and
	 * it is also required to convert Mono<EmployeeBean> 
	 * obtained from findById() of EmployeeService.
	 * Hence to do both the operations, both are chained using flatMap().
	 *   
	 * ServerResponse.ok() returns a Response BodyBuilder.
	 * Using this BodyBuilder the response is built and sent using the body() method.
	 * body() method needs a publisher, to give the publisher fromValue is used.
	 * This method will automatically create the Mono<ServerResponse>.
	 * flatMap will return a ServerResponse.ok() if value is found, 
	 * else will return an empty Mono.
	 
     *  if converted Mono is empty then switchIfEmpty is invoked and NotFound result is returned.
	 
	 * There is no need to subscribe to the publisher,
	 * as subscription will be done by the front End controller.
	 * In web flux the name of the front end controller is DispatcherHandler.
	 * */
    public Mono<ServerResponse> getEmployeesById(ServerRequest serverRequest) {
        String employeeId=serverRequest.pathVariable("employeeId");
		Mono<EmployeeBean> empFound=employeeService.findById(Integer.valueOf(employeeId));
        // this response will be sent if nothing is found
		Mono<ServerResponse> notFound =  ServerResponse.notFound().build();
		
		return 	empFound.flatMap(redeemedEmp->ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(fromValue(redeemedEmp))) //creating body from redeemedMono
				.switchIfEmpty(notFound);
    }
    
    /** Request body is obtained by using the serverRequest.bodyToMono(EmployeeBean.class).
     *  This method gives the publisher of type of Mono<EmployeeBean>.
     *  
     *  The actual request body is redeemed using flatMap(). 
     *  and call is made to the save() method of EmployeeService by passing the redeemed body.
     *  This is turn returns a publisher of Type Mono<EmployeeBean>.
     *  
     *  This call to the save() method of EmployeeService is wrapped by the 
     *  ServerResponse.status().body().
     *  This converts the Mono<EmployeeBean> to Mono<ServerResponse>. 
     *    
	 *  There is no need to subscribe to the publisher,
	 *  as subscription will be done by the front End controller.
	 *  In web flux the name of the front end controller is DispatcherHandler.  
	 * */
    public Mono<ServerResponse> saveEmployee(ServerRequest serverRequest) {
		Mono<EmployeeBean> empBodyMono= serverRequest.bodyToMono(EmployeeBean.class);
        return empBodyMono.flatMap(
        							redeemedBody->ServerResponse
        								.status(HttpStatus.CREATED)
        								.contentType(APPLICATION_JSON)
        								.body(  employeeService.save(redeemedBody),
        										EmployeeBean.class
        									 )
        						  );
    }
	
	
	
    /** Request body is obtained by using the serverRequest.bodyToMono(EmployeeBean.class).
     *  This method gives the publisher of type of Mono<EmployeeBean>.
     *  
     *  The actual request body is redeemed using flatMap(). // Refer Line 1 
     *  and call is made to the findById() method of EmployeeService by 
     *  passing the employeeId obtained from redeemed body. //Refer Line2
     *  This is turn returns a publisher of Type Mono<EmployeeBean>.
     *  
     *  Further the outcome of the findById() method of EmployeeService is 
     *  chained to the save() method of the EmployeeService using flatMap().
     *  to the save() method redeemedRequest Body is passed as parameter so as to update
     *  found data with data obtained from the request body.
     *  
     *  This call to the save() method of EmployeeService is wrapped by the 
     *  ServerResponse.ok().body().
     *  This converts the Mono<EmployeeBean> to Mono<ServerResponse>. 
     *    
     *  Refer: Line1,4. flatMap() operator returns the empty Mono or Mono<ServerResponse>.
     *  if converted Mono is empty then switchIfEmpty() is invoked and NotFound result is returned.
     *  Refer Line 3.
     *     
	 *  There is no need to subscribe to the publisher,
	 *  as subscription will be done by the front End controller.
	 *  In web flux the name of the front end controller is DispatcherHandler.  
	 * */
	//1 Redeeming the mono and finding if it is valid
    //2 if data is found then use flatMap to push  the redeemedEmpBody of the request body to database  
    //2 using save 
    //3 switchIfEmpty will be executed if instance is not found
    //4 flatMap will take care of producing the one dimensional publisher/Mono
    public Mono<ServerResponse> updateEmployee(ServerRequest request) {
    	  Mono<EmployeeBean> empBodyMono = request.bodyToMono(EmployeeBean.class);
          Mono<ServerResponse> notFound = ServerResponse.notFound().build();
          return empBodyMono.flatMap(redeemedRequestEmpBody ->//1
          				employeeService.findById(redeemedRequestEmpBody.getEmployeeId()) // 2
          					/*4*/.flatMap(exisitingEmployee->ServerResponse.ok()
          											.contentType(APPLICATION_JSON)
          											.body(employeeService.save(redeemedRequestEmpBody), 
          													EmployeeBean.class) //2
          					   /*3*/).switchIfEmpty(notFound)
                  );
    }

    /**Requirement: It is required to delete Employee by Id.
	 * If it is found send ok response else return not found.
	 * In last method getAllEmployees() would always return ok() response.
	 * It cannot check if the details exist or not, as ServerResponse.ok().body()
	 * will always give non empty response.
	 *  
	 * To do the current requirement, In the current method,
	 * From the ServerRequest employeeId parameter is extracted. 
	 * Passing the employeeId extracted above, findById() of the EmployeeService is invoked.
	 * This method returns the Mono<EmployeeBean>.
	 * 
	 * Outcome of findById() of the EmployeeService is chained 
	 * to the delete() method of EmployeeService.
	 * This method returns the Mono<Void>.
	 * This call to the deelte() method of EmployeeService is wrapped by the 
     * ServerResponse.ok().build().
     * This converts the Mono<Void> to Mono<ServerResponse>.
	 * 
	 * flatMap will return a ServerResponse.ok() if value is found, 
	 * else will return an empty Mono.
	 
     *  if converted Mono is empty then switchIfEmpty is invoked and NotFound result is returned.
	 
	 * There is no need to subscribe to the publisher,
	 * as subscription will be done by the front End controller.
	 * In web flux the name of the front end controller is DispatcherHandler.
	 * */
    public Mono<ServerResponse> deleteEmployee(ServerRequest serverRequest) {
        String employeeId=serverRequest.pathVariable("employeeId");
		Mono<EmployeeBean> empFound=employeeService.findById(Integer.valueOf(employeeId));
        // this response will be sent if nothing is found
		Mono<ServerResponse> notFound =  ServerResponse.notFound().build();
		System.out.println("*** From Delete.."+employeeId);
		return 	empFound.flatMap(redeemedExistingEmp->ServerResponse
								.ok()	
								.build(employeeService.delete(redeemedExistingEmp))
								)
								.switchIfEmpty(notFound);
								
    }
    // for returning mono<void>, we don't use Body(as used in find by id and other methods) 
    //but we return using the build function
	
}
/**Common Note:
 * When ever current operation has to be chained to other operation
 * which in turn is returning an other publisher,
 * but needed output should be a one single dimensional publisher  
 * (not a publisher which is publisher of publishers),
 * Then to have values of both publisher flatMap() is used.
 * As normal map cannot do flattening, hence normal map cannot be used
 * defaultIfEmpty requires a raw value that will be converted to publisher
 * switchIfEmpty requires a publisher only 
*/