package org.example.enterpriceappbackend.data.service.serviceImpl;


import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.repository.BigliettoRepository;
import org.example.enterpriceappbackend.data.service.BigliettoService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BigliettoServiceImpl implements BigliettoService {

    private final BigliettoRepository bigliettoRepository;
}
