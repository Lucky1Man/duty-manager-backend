package com.duty.manager.service.impl;

import com.duty.manager.dto.GetSecuredDTO;
import com.duty.manager.entity.Role;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

@RequiredArgsConstructor
public class RoleBasedMappingService extends AuthenticationAwareService {

    private final ModelMapper modelMapper;

    public <S, D extends GetSecuredDTO> D mapToDto(S source, Class<D> destination, @Nullable Role forbiddenRole) {
        return roleAwareCall(searchResult -> {
            D mappedDTO = modelMapper.map(source, destination);
            mappedDTO.setSecured(searchResult);
            return mappedDTO;
        }, forbiddenRole);
    }

    public <S, D extends GetSecuredDTO> D mapToDto(S source, Class<D> destination) {
        return mapToDto(source, destination, null);
    }

}
