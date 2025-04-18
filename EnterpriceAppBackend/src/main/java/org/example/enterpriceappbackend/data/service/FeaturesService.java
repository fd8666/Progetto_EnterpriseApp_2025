package org.example.enterpriceappbackend.data.service;

import org.example.enterpriceappbackend.data.entity.Features;

import java.util.List;

public interface FeaturesService {
    void save(Features feature);
    List<Features> getAllFeatures();
    Features getFeaturesById(int id);
    void deleteFeaturesById(int id);

}
