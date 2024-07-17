package br.com.sunsoft.planetapi.web;

import static br.com.sunsoft.planetapi.common.PlanetConstants.PLANET;
import static br.com.sunsoft.planetapi.common.PlanetConstants.PLANETS;
import static br.com.sunsoft.planetapi.common.PlanetConstants.TATOOINE;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.sunsoft.planetapi.domain.Planet;
import br.com.sunsoft.planetapi.domain.PlanetService;

@WebMvcTest(PlanetController.class)
class PlanetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PlanetService planetService;

	@Test
	void createPlanet_WithValidData_ReturnsCreated() throws Exception {
		when(planetService.create(PLANET)).thenReturn(PLANET);

	    mockMvc
	        .perform(
	            post("/planets").content(objectMapper.writeValueAsString(PLANET))
	                .contentType(MediaType.APPLICATION_JSON))
	        .andExpect(status().isCreated())
	        .andExpect(jsonPath("$").value(PLANET));
	}

	@Test
	void createPlanet_WithInvalidData_ReturnsBadRequest() throws Exception {
		Planet emptyPlanet = new Planet();
		Planet invalidPlanet = new Planet("", "", "");

		mockMvc.perform(post("/planets").content(objectMapper.writeValueAsBytes(emptyPlanet))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity());

		mockMvc.perform(post("/planets").content(objectMapper.writeValueAsBytes(invalidPlanet))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnprocessableEntity());
	}

	@Test
	void createPlanet_WithExistingName_ReturnsConflict() throws Exception {
		when(planetService.create(any())).thenThrow(DataIntegrityViolationException.class);
		
		mockMvc.perform(post("/planets").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(PLANET)))
		.andExpect(status().isConflict());
	}

	@Test
	void getPlanet_ByExistingId_ReturnsPlanet() throws Exception {
		when(planetService.get(any())).thenReturn(Optional.of(PLANET));
		
		mockMvc.perform(get("/planets/{id}", "1")).andExpect(status().isOk()).andExpect(jsonPath("$").value(PLANET));
	}

	@Test
	void getPlanet_ByUnexistingId_ReturnNotFound() throws Exception {
		when(planetService.get(any())).thenReturn(Optional.empty());
		
		mockMvc.perform(get("/planets/{id}", "1")).andExpect(status().isNotFound());
	}

	@Test
	void getPlanet_ByExistingName_ReturnsPlanet() throws Exception{
		when(planetService.getByName(any())).thenReturn(Optional.of(PLANET));
		
		mockMvc.perform(get("/planets/name/{name}", "test")).andExpect(status().isOk()).andExpect(jsonPath("$").value(PLANET));
	}

	@Test
	void getPlanet_ByUnexistingName_ReturnsNotFound() throws Exception {

		mockMvc.perform(get("/planets/name/{name}", "test")).andExpect(status().isNotFound());
	}

	@Test
	void listPlanets_ReturnsFilteredPlanets() throws Exception {
		when(planetService.list(null, null)).thenReturn(PLANETS);
		when(planetService.list(TATOOINE.getTerrain(), TATOOINE.getClimate())).thenReturn(List.of(TATOOINE));

		mockMvc.perform(get("/planets"))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(3)));
		
		mockMvc.perform(get("/planets").param("terrain", TATOOINE.getTerrain()).param("climate", TATOOINE.getClimate()))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$[0]").value(TATOOINE));
	}

	@Test
	void listPlanets_ReturnsNoPlanets() throws Exception {
		when(planetService.list(null, null)).thenReturn(Collections.emptyList());
		mockMvc.perform(get("/planets"))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void removePlanet_WithExistingId_ReturnsNoContent() throws Exception {
		mockMvc.perform(delete("/planets/{id}", "1")).andExpect(status().isNoContent());
	}

	@Test
	void removePlanet_WithUnexistingId_ReturnsNotFound() throws Exception {
		final Long id = 1L;
		doThrow(new EmptyResultDataAccessException(1)).when(planetService).remove(id);
		mockMvc.perform(delete("/planets/{id}", id)).andExpect(status().isBadRequest());
	}

}
