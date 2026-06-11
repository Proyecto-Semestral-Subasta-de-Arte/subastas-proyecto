package cl.sda1085.subastas.assembler;

import cl.sda1085.subastas.dto.SubastaResponseDTO;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import cl.sda1085.subastas.controller.SubastaController;

@Component
public class SubastaModelAssembler implements RepresentationModelAssembler<SubastaResponseDTO, SubastaResponseDTO> {

        @Override
        public SubastaResponseDTO toModel(SubastaResponseDTO dto) {

            //Enlace al recurso individual (self) -> /api/subastas/{id}
            dto.add(linkTo(methodOn(SubastaController.class).obtenerPorId(dto.getId())).withSelfRel());

            //Enlace al listado general histórico -> /api/subastas
            dto.add(linkTo(methodOn(SubastaController.class).obtenerTodas()).withRel("subastas"));

            //Enlace condicional dinámico: si está abierta, expone el endpoint para buscar otras del mismo estado
            if ("ABIERTA".equalsIgnoreCase(dto.getEstado())) {
                dto.add(linkTo(methodOn(SubastaController.class).buscarPorEstado(dto.getEstado())).withRel("mismo-estado"));
            }

            return dto;
        }
}
