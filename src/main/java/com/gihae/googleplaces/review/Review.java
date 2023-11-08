package com.gihae.googleplaces.review;

public record Review (
    String name,
    String profileUrl,
    int rating,
    String text
){}
