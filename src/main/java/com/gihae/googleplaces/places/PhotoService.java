package com.gihae.googleplaces.places;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.HttpURLConnection;

@RequiredArgsConstructor
@Service
public class PhotoService {

    @Value("${google.api.places.key}")
    private String apiKey;

    @Value("${google.api.places.photo.url}")
    private String photoUrl;

    private final RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory() {
        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) {
            connection.setInstanceFollowRedirects(false);
        }
    });

    public String getPlacePhotoUrl(String photoReference){
        String imageUrl = createImageUrl(photoReference);
        return getRedirectedUrl(imageUrl);
    }

    private String createImageUrl(String photoReference) {
        return UriComponentsBuilder.fromHttpUrl(photoUrl)
                .queryParam("maxwidth", 400)
                .queryParam("photoreference", photoReference)
                .queryParam("key", apiKey)
                .build()
                .toString();
    }

    private String getRedirectedUrl(String originalUrl) {
        ResponseEntity<String> response = restTemplate.exchange(originalUrl, HttpMethod.GET, null, String.class);
        return response.getHeaders().getLocation() == null ? "" : response.getHeaders().getLocation().toString();
    }
}
