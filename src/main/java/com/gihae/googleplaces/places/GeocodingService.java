package com.gihae.googleplaces.places;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Service
public class GeocodingService {

    @Value("${google.api.places.key}")
    private String apiKey;

    @Value("${google.api.places.geocoding.url}")
    private String geocodingUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getGooglePlaceId(String name, String address) {
        String url = createGooglePlaceIdUrl(name, address);
        String response = restTemplate.getForObject(url, String.class);
        return extractPlaceId(response);
    }

    private String createGooglePlaceIdUrl(String name, String address) {
        StringBuilder addressBuilder = new StringBuilder();

        if (name != null) {
            addressBuilder.append(name);
        }

        if (name != null && address != null) {
            addressBuilder.append(",");
        }

        if (address != null) {
            addressBuilder.append(address);
        }

        return UriComponentsBuilder.fromHttpUrl(geocodingUrl)
                .queryParam("language", "ko")
                .queryParam("address", addressBuilder)
                .queryParam("key", apiKey)
                .build()
                .toString();
    }

    private String extractPlaceId(String response) {
        try {
            JsonNode results = objectMapper.readTree(response).path("results");
            if (results.size() == 0) {
                throw new RuntimeException();
            }
            return results.get(0).path("place_id").asText();
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }
}
