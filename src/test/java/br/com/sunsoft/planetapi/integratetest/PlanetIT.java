package br.com.sunsoft.planetapi.integratetest;

import static br.com.sunsoft.planetapi.common.PlanetConstants.PLANET;
import static br.com.sunsoft.planetapi.common.PlanetConstants.TATOOINE;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import br.com.sunsoft.planetapi.domain.Planet;

//@Sql(scripts = { "/import_planets.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
//@Sql(scripts = { "/remove_planets.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("it")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Sql(scripts = { "classpath:import_planets.sql" }, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = { "classpath:remove_planets.sql" }, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class PlanetIT {

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void createPlanet_ReturnsCreated() {
		ResponseEntity<Planet> sut = restTemplate.postForEntity("/planets", PLANET, Planet.class);

		assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(sut.getBody().getId()).isNotNull();
		assertThat(sut.getBody().getName()).isEqualTo(PLANET.getName());
		assertThat(sut.getBody().getClimate()).isEqualTo(PLANET.getClimate());
		assertThat(sut.getBody().getTerrain()).isEqualTo(PLANET.getTerrain());
	}

	@Test
	void getPlanet_ReturnsPlanet() {
		ResponseEntity<Planet> sut = restTemplate.getForEntity("/planets/" + TATOOINE.getId(), Planet.class);

		assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(sut.getBody()).isEqualTo(TATOOINE);
	}

	@Test
	void getPlanetByName_ReturnsPlanet() {
		ResponseEntity<Planet> sut = restTemplate.getForEntity("/planets/name/" + TATOOINE.getName(), Planet.class);

		assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(sut.getBody()).isEqualTo(TATOOINE);
	}

	@Test
	void listPlanets_ReturnsAllPlanets() {
		ResponseEntity<Planet[]> sut = restTemplate.getForEntity("/planets", Planet[].class);

		assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(sut.getBody()).hasSize(3);
		assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
	}

	@Test
	void listPlanets_ByTerrain_ReturnsPlanets() {
		ResponseEntity<Planet[]> sut = restTemplate.getForEntity("/planets?terrain=" + TATOOINE.getTerrain(),
				Planet[].class);

		assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(sut.getBody()).hasSize(1);
		assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
	}

	@Test
	void listPlanets_ByClimate_ReturnsPlanets() {
		ResponseEntity<Planet[]> sut = restTemplate.getForEntity("/planets?climate=" + TATOOINE.getClimate(),
				Planet[].class);

		assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(sut.getBody()).hasSize(1);
		assertThat(sut.getBody()[0]).isEqualTo(TATOOINE);
	}

	@Test
	void removePlanet_ReturnsNoContent() {
		ResponseEntity<Void> sut = restTemplate.exchange("/planets/" + TATOOINE.getId(), HttpMethod.DELETE, null,
				Void.class);

		assertThat(sut.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

}
