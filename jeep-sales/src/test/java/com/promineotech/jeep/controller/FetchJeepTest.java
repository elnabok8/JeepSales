package com.promineotech.jeep.controller;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doThrow;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import com.promineotech.Constants;
import com.promineotech.jeep.controller.support.FetchJeepTestSupport;
import com.promineotech.jeep.entity.Jeep;
import com.promineotech.jeep.entity.JeepModel;
import com.promineotech.jeep.service.JeepSalesService;




@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Sql(scripts = {"classpath:V1.0__Jeep_Schema.sql", "classpath:V1.1__Jeep_Data.sql"},
config = @SqlConfig(encoding = "utf-8"))
class FetchJeepTest{
	@Nested
	@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
	@ActiveProfiles("test")
	@Sql(scripts = {"classpath:V1.0__Jeep_Schema.sql", "classpath:V1.1__Jeep_Data.sql"},
	config = @SqlConfig(encoding = "utf-8"))

	class TestsThatDoNotPolluteTheApplicationContext extends FetchJeepTestSupport{
		@Test
		void testThatJeepsAreReturnedWhenAValidModelAndTrimAreSupplied() {
			// Given: a valid model, trim, and URI
			JeepModel model = JeepModel.WRANGLER;
			String trim = "Sport";
			String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
			
			System.out.println(uri);
			// When: a connection is made to the URI
			ResponseEntity<List<Jeep>> response =
			getRestTemplate().exchange(uri, HttpMethod.GET, null,  new ParameterizedTypeReference<>() {});
			// Then: a success (OK - 200) status code is returned
			assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
			
			// And: the actual list returned is the same as the expected List
			
			List<Jeep> actual = response.getBody();
			List <Jeep> expected = buildExpected();
			
			actual.forEach(jeep -> jeep.setModelPK(null));
			assertThat(response.getBody()).isEqualTo(expected);
		}
			
			@Test
			void testThatAnErrorMessageIsReturnedWhenAnUnknownTrimIsSupplied() {
				// Given: a valid model, trim, and URI
				JeepModel model = JeepModel.WRANGLER;
				String trim = "Unknown Value";
				String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
				
				System.out.println(uri);
				// When: a connection is made to the URI
				ResponseEntity<Map<String, Object> > response = 
				getRestTemplate().exchange(uri, HttpMethod.GET, null,  new ParameterizedTypeReference<>() {});
				// Then: a not found (404) status code is returned
				assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
				
				// And: the an error message is returned
				Map<String, Object> error = response.getBody();
				
				assertErrorMessageValid(error, HttpStatus. NOT_FOUND);
			}
			
			@ParameterizedTest
			@MethodSource("com.promineotech.jeep.controller.FetchJeepTest#parametersForInvalidInput")
			void testThatAnErrorMessageIsReturnedWhenAnInvalidValueIsSupplied(String model, String trim, String reason) {
				// Given: an invalid model, trim, and URI
				String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
				
				// When: a connection is made to the URI
				ResponseEntity<Map<String, Object>> response =
				getRestTemplate().exchange(uri, HttpMethod.GET, null,  new ParameterizedTypeReference<>() {});
				
				// Then: a not found (404) status code is returned
				assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
				
				// And: an error message is returned
			Map<String, Object> error = response.getBody();
			
			assertErrorMessageValid(error, HttpStatus.BAD_REQUEST);
			}
			
			
		
			
	}//end of nonpoluting test
	static Stream<Arguments>parametersForInvalidInput() {
		return Stream.of(
				arguments("WRANGLER", "@#$%^&&%", "Trim contains non-alpha-numeric elements"),
		arguments("WRANGLER", "C".repeat(Constants.TRIM_MAX_LENGTH + 1), "Trim length blah blah blah"),
		arguments("INVALID", "Sport", "Model is not enum value")
	)
	;}
	@Nested
	@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
	@ActiveProfiles("test")
	@Sql(scripts = {"classpath:V1.0__Jeep_Schema.sql", "classpath:V1.1__Jeep_Data.sql"},
			config = @SqlConfig(encoding = "utf-8"))
class TestsThatPolluteTheApplicationContext extends FetchJeepTestSupport{ 
@MockBean
private JeepSalesService jeepSalesService;

@Test
void testThatAnUnplannedErrorResultsina500Status() {
	// Given: a valid model, trim, and URI
	JeepModel model = JeepModel.WRANGLER;
	String trim = "invlalid";
	String uri = String.format("%s?model=%s&trim=%s", getBaseUri(), model, trim);
	
	doThrow(new RuntimeException("rats")).when(jeepSalesService).fetchJeeps(model, trim);
	
	System.out.println(uri);
	// When: a connection is made to the URI
	ResponseEntity<Map<String, Object> > response = 
	getRestTemplate().exchange(uri, HttpMethod.GET, null,  new ParameterizedTypeReference<>() {});
	// Then: an internal server error shows up
	assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
	
	// And: the an error message is returned
	Map<String, Object> error = response.getBody();
	
	assertErrorMessageValid(error, HttpStatus. INTERNAL_SERVER_ERROR);
}
}


}


	
