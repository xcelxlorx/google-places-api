package com.gihae.googleplaces.places;

import java.util.List;

public class PlaceResponse {
    public record GetPlaceDto(
            String placeName,
            List<String> images,
            String address,
            String phoneNumber,
            List<String> operationTime
    ){}
}
