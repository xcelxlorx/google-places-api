package com.gihae.googleplaces.places;

import com.gihae.googleplaces.review.Review;

import java.util.List;

public class PlaceResponse {
    public record GetPlaceDto(
            String placeName,
            String address,
            String phoneNumber,
            String rating,
            List<String> images,
            List<String> openingHours,
            List<Review> reviews
    ){}
}
