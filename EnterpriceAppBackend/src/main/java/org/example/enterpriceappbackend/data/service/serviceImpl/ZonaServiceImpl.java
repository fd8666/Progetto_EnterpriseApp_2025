package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.repository.ZonaRepository;
import org.example.enterpriceappbackend.data.service.ZonaService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ZonaServiceImpl implements ZonaService {

    private final ZonaRepository zonaRepository;

}
