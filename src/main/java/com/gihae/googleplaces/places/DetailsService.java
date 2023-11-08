package com.gihae.googleplaces.places;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gihae.googleplaces.review.Review;
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

    @Value("${google.api.places.details.url}")
    private String detailsUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PhotoService photoService;
    private final GeocodingService geocodingService;

    public PlaceResponse.GetPlaceDto getPlaceDetails(String name, String address) {
        String googlePlaceId = geocodingService.getGooglePlaceId(name, address);
        String url = createPlaceUrl(googlePlaceId);
        String response = restTemplate.getForObject(url, String.class);
        System.out.println("response=" + response);
        return extractPlaceDetails(response, name);
    }

    private String createPlaceUrl(String placeId) {
        return UriComponentsBuilder.fromHttpUrl(detailsUrl)
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

            //이름
            String name = result.path("name").asText();
            if (!placeName.equals(name)) {
                throw new RuntimeException();
            }

            //주소, 전화번호, 평점
            String address = result.path("formatted_address").asText();
            String phoneNumber = result.path("formatted_phone_number").asText();
            String rating = result.path("rating").asText();

            //사진
            List<String> images = new ArrayList<>();
            JsonNode photoNode = result.path("photos");
            for (int i = 0; i < photoNode.size(); i++) {
                String photoReference = photoNode.get(i).path("photo_reference").asText();
                String photoUrl = photoService.getPlacePhotoUrl(photoReference);
                images.add(photoUrl);
            }

            //영업 시간
            List<String> openingHours = new ArrayList<>();
            JsonNode weekdayTextNode = result.path("opening_hours").path("weekday_text");
            for (int i = 0; i < weekdayTextNode.size(); i++) {
                String openingHour = weekdayTextNode.get(i).asText();
                openingHours.add(openingHour);
            }

            //리뷰
            List<Review> reviews = new ArrayList<>();
            JsonNode reviewNode = result.path("reviews");
            for (int i = 0; i < reviewNode.size(); i++) {
                String authorName = reviewNode.get(i).path("author_name").asText();
                String profileUrl = reviewNode.get(i).path("profile_photo_url").asText();
                String authorRating = reviewNode.get(i).path("rating").asText();
                String text = reviewNode.get(i).path("text").asText();
                reviews.add(new Review(authorName, profileUrl, authorRating, text));
            }

            return new PlaceResponse.GetPlaceDto(name, address, phoneNumber, rating, images, openingHours, reviews);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }
}