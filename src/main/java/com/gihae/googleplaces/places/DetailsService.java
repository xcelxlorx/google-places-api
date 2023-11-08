package com.gihae.googleplaces.places;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class DetailsService {

    @Value("${google.api.places.key}")
    private String apiKey;

    private final String PLACES_API_URL = "https://maps.googleapis.com/maps/api/place/details/json";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PhotoService photoService;
    private final GeocodingService geocodingService;

    public PlaceResponse.GetPlaceDto getPlaceDetails(String name, String address) {
        String googlePlaceId = geocodingService.getGooglePlaceId(name, address);
        String url = createPlaceUrl(googlePlaceId);
        String response = restTemplate.getForObject(url, String.class);
        return extractPlaceDetails(response, name);
    }

    private String createPlaceUrl(String placeId) {
        return UriComponentsBuilder.fromHttpUrl(PLACES_API_URL)
                .queryParam("language", "ko")
                .queryParam("place_id", placeId)
                .queryParam("key", apiKey)
                .build()
                .toString();
    }

    private PlaceResponse.GetPlaceDto extractPlaceDetails(String response, String placeName) {
        try {
            JsonNode result = objectMapper.readTree(response).path("result");
            if (result.isEmpty()) {
                throw new RuntimeException();
            }

            //볼링장 이름
            String name = result.path("name").asText();
            if (!placeName.equals(name)) {
                throw new RuntimeException();
            }

            //볼링장 주소, 전화번호
            String address = result.path("formatted_address").asText();
            String phoneNumber = result.path("formatted_phone_number").asText();

            //볼링장 사진
            List<String> images = new ArrayList<>();
            JsonNode photoNode = result.path("photos");
            for (int i = 0; i < photoNode.size(); i++) {
                String photoReference = photoNode.get(i).path("photo_reference").asText();
                String photoUrl = photoService.getPlacePhotoUrl(photoReference);
                images.add(photoUrl);
            }

            //볼링장 영업 시간
            List<String> operationTimes = new ArrayList<>();
            JsonNode weekdayTextNode = result.path("opening_hours").path("weekday_text");
            for (int i = 0; i < weekdayTextNode.size(); i++) {
                String operationTime = weekdayTextNode.get(i).asText();
                operationTimes.add(operationTime);
            }

            return new PlaceResponse.GetPlaceDto(name, images, address, phoneNumber, operationTimes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }
}