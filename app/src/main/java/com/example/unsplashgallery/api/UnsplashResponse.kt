package com.example.unsplashgallery.api

import com.example.unsplashgallery.data.UnsplashPhotoModel

data class UnsplashResponse(
    val results: List<UnsplashPhotoModel>
)