package br.com.sunsoft.planetapi.domain;

import static br.com.sunsoft.planetapi.common.PlanetConstants.PLANET;
import static br.com.sunsoft.planetapi.common.PlanetConstants.TATOOINE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Example;
import org.springframework.test.context.jdbc.Sql;;

@DataJpaTest
class PlanetRepositoryTest {

	@Autowired
	private PlanetRepository planetRepository;

	@Autowired
	private TestEntityManager testEntityManager;

	@AfterEach
	void afterEach() {
		PLANET.setId(null);
	}

	@BeforeEach
	void beforeEach() {

	}

	@Test
	void createPlanet_WithValidData_ReturnsPlanet() {
		Planet planet = planetRepository.save(PLANET);

		Planet sut = testEntityManager.find(Planet.class, planet.getId());

		assertThat(sut).isNotNull();
		assertThat(sut.getName()).isEqualTo(PLANET.getName());
		assertThat(sut.getClimate()).isEqualTo(PLANET.getClimate());
		assertThat(sut.getTerrain()).isEqualTo(PLANET.getTerrain());
	}

	@Test
	void createPlanet_WithInvalidData_ThrowsException() {
		Planet emptyPlanet = new Planet();
		Planet invalidPlanet = new Planet("", "", "");

		assertThatThrownBy(() -> planetRepository.save(emptyPlanet)).isInstanceOf(RuntimeException.class);
		assertThatThrownBy(() -> planetRepository.save(invalidPlanet)).isInstanceOf(RuntimeException.class);
	}

	@Test
	void createPlanet_WithExistingName_ThrowsException() {

		Planet planet = testEntityManager.persistFlushFind(PLANET);
		testEntityManager.detach(planet);
		planet.setId(null);

		assertThatThrownBy(() -> planetRepository.save(planet)).isInstanceOf(RuntimeException.class);
		testEntityManager.clear();
	}

	@Test
	void getPlanet_ByExistingId_ReturnsPlanet() {
		Planet planet = testEntityManager.persistFlushFind(PLANET);
		Optional<Planet> optPlanet = planetRepository.findById(planet.getId());
		assertThat(optPlanet).isNotEmpty();
		assertThat(optPlanet.get()).isEqualTo(planet);

	}

	@Test
	void getPlanet_ByUnexistingId_ReturnsEmpty() {

		Optional<Planet> optPlanet = planetRepository.findById(1L);
		assertThat(optPlanet).isNotPresent();
	}

	@Test
	void getPlanet_ByExistingName_ReturnsPlanet() {
		Planet sutPlanet = testEntityManager.persistAndFlush(PLANET);
		Optional<Planet> optPlanet = planetRepository.findByName(sutPlanet.getName());
		assertThat(optPlanet).isNotEmpty();
		assertThat(optPlanet.get()).isEqualTo(sutPlanet);
	}

	@Test
	void getPlanet_ByUnexistingName_ReturnsEmpty() {
		Optional<Planet> opt = planetRepository.findByName("");
		assertThat(opt).isNotPresent();
	}

	@Sql(scripts = "/import_planets.sql")
	@Test
	void listPlanets_ReturnsFilteredPlanets() throws Exception {
		Example<Planet> queryWithoutFilters = QueryBuilder.makeQuery(new Planet());
		Example<Planet> queryWithFilters = QueryBuilder
				.makeQuery(new Planet(TATOOINE.getClimate(), TATOOINE.getTerrain()));

		List<Planet> listWithoutFilters = planetRepository.findAll(queryWithoutFilters);
		List<Planet> listWithFilters = planetRepository.findAll(queryWithFilters);

		assertThat(listWithoutFilters).isNotEmpty();
		assertThat(listWithoutFilters).hasSize(3);

		assertThat(listWithoutFilters).isNotEmpty();
		assertThat(listWithFilters.get(0)).isEqualTo(TATOOINE);
	}

	@Test
	void listPlanets_ReturnsNoPlanets() throws Exception {
		Example<Planet> query = QueryBuilder.makeQuery(new Planet());

		List<Planet> list = planetRepository.findAll(query);

		assertThat(list).isEmpty();
	}

	@Test
	void removePlanet_WithExistingId_RemovesPlanetFromDatabase() {
		Planet planet = testEntityManager.persistAndFlush(PLANET);
		planetRepository.deleteById(planet.getId());

		Planet removedPlanet = testEntityManager.find(Planet.class, planet.getId());

		assertThat(removedPlanet).isNull();

	}

	@Test
	void removePlanet_WithUnexistingId_ThrowsException() {

		assertThatThrownBy(() -> planetRepository.deleteById(1L)).isInstanceOf(EmptyResultDataAccessException.class);
	}
}
