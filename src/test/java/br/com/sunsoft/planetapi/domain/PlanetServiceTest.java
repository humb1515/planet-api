package br.com.sunsoft.planetapi.domain;

import static br.com.sunsoft.planetapi.common.PlanetConstants.INVALID_PLANET;
import static br.com.sunsoft.planetapi.common.PlanetConstants.PLANET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

@ExtendWith(MockitoExtension.class)
//@SpringBootTest(classes = PlanetService.class)
class PlanetServiceTest {

	@InjectMocks
	// @Autowired
	private PlanetService planetService;

	@Mock
	// @MockBean
	private PlanetRepository planetRepository;

	@Test
	void createPlanet_WithValidData_ReturnsPlanet() {
		when(planetRepository.save(PLANET)).thenReturn(PLANET);
		Planet sut = planetService.create(PLANET);
		assertThat(sut).isEqualTo(PLANET);
	}

	@Test
	void createPlanet_WithInvalidData_ThrowsException() {
		when(planetRepository.save(INVALID_PLANET)).thenThrow(RuntimeException.class);
		assertThatThrownBy(()-> planetService.create(INVALID_PLANET)).isInstanceOf(RuntimeException.class);
	}

	@Test
	void getPlanet_ByExistingId_ReturnsPlanet() {
		when(planetRepository.findById(anyLong())).thenReturn(Optional.of(PLANET));
		Optional<Planet> sut = planetService.get(1L);
		assertThat(sut).isEqualTo(Optional.of(PLANET));
	}

	@Test
	void getPlanet_ByUnexistingId_ReturnsEmpty() {
		when(planetRepository.findById(anyLong())).thenReturn(Optional.empty());
		Optional<Planet> sut = planetService.get(1L);
		assertThat(sut).isEmpty();
	}

	@Test
	void getPlanet_ByExistingName_ReturnsPlanet() {
		when(planetRepository.findByName(PLANET.getName())).thenReturn(Optional.of(PLANET));
		Optional<Planet> sut = planetService.getByName("name");
		assertThat(sut).isEqualTo(Optional.of(PLANET));
	}

	@Test
	void getPlanet_ByUnexistingName_ReturnsEmpty() {
		String unexisting = "Unexisting name";
		when(planetRepository.findByName(unexisting)).thenReturn(Optional.empty());
		Optional<Planet> sut = planetService.getByName(unexisting);
		assertThat(sut).isEmpty();
	}

	@Test
	void listPlanets_ReturnsAllPlanets() {
		List<Planet> planets = new ArrayList<>();
		planets.add(PLANET);

		Example<Planet> query = QueryBuilder.makeQuery(new Planet(PLANET.getClimate(), PLANET.getTerrain()));

		when(planetRepository.findAll(query)).thenReturn(planets);
		List<Planet> sut = planetService.list(PLANET.getTerrain(), PLANET.getClimate());
	    assertThat(sut).isNotEmpty();
	    assertThat(sut).hasSize(1);
	    assertThat(sut.get(0)).isEqualTo(PLANET);
	}

	@Test
	void listPlanets_ReturnsNoPlanets() {		
		when(planetRepository.findAll(any())).thenReturn(Collections.emptyList());
		
		List<Planet> sut = planetService.list(PLANET.getClimate(), PLANET.getTerrain());
		
		assertThat(sut).isEmpty();
	}

	@Test
	void removePlanet_WithExistingId_doesNotThrowAnyException() {
		assertThatCode(() -> planetService.remove(1L)).doesNotThrowAnyException();
	}

	@Test
	void removePlanet_WithUnexistingId_throwsException() {
		doThrow(new RuntimeException()).when(planetRepository).deleteById(99L);

		assertThatThrownBy(() -> planetService.remove(99L)).isInstanceOf(RuntimeException.class);

	}

}
