package com.gihae.googleplaces.places;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final DetailsService detailsService;

    @GetMapping
    public ResponseEntity<?> getPlaces(@RequestParam String name, @RequestParam String address){
        PlaceResponse.GetPlaceDto response = detailsService.getPlaceDetails(name, address);
        return ResponseEntity.ok().body(response);
    }
}
