
package org.example.enterpriceappbackend.configuration.config;

import org.example.enterpriceappbackend.data.entity.Biglietto;
import org.example.enterpriceappbackend.dto.BigliettoCreateDTO;
import org.example.enterpriceappbackend.dto.BigliettoInfoDTO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true);

        configureBigliettoMappings(modelMapper);

        return modelMapper;
    }

    private void configureBigliettoMappings(ModelMapper modelMapper) {
        // Configurazione per Biglietto -> BigliettoInfoDTO
        modelMapper.typeMap(Biglietto.class, BigliettoInfoDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getEvento().getId(), BigliettoInfoDTO::setEventoId);
            mapper.map(src -> src.getTipoPosto().getId(), BigliettoInfoDTO::setTipoPostoId);
            mapper.map(src -> src.getPagamento() != null ? src.getPagamento().getId() : null,
                    BigliettoInfoDTO::setPagamentoId);
        });

        // Configurazione per BigliettoCreateDTO -> Biglietto
        modelMapper.typeMap(BigliettoCreateDTO.class, Biglietto.class).addMappings(mapper -> {
            mapper.skip(Biglietto::setId);
            mapper.skip(Biglietto::setDataCreazione);
            mapper.skip(Biglietto::setDeleted);
        });
    }
}