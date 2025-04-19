package org.example.enterpriceappbackend.data.service.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.example.enterpriceappbackend.data.repository.StrutturaRepository;
import org.example.enterpriceappbackend.data.service.StrutturaService;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StrutturaServiceImpl implements StrutturaService {

    private final StrutturaRepository strutturaRepository;

}
