package co.plageopa.DTO;

import java.util.List;

public record RegistroPageResponseDto(
    List<RegistroResponseDto> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
