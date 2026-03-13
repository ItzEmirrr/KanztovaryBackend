package kg.kanztovary.kanztovarybackend.domain.service;

import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandDto;
import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandRequestDto;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;

import java.util.List;

public interface BrandService {
    BrandDto create(BrandRequestDto request);
    BrandDto getById(Long id);
    List<BrandDto> getAll();
    BrandDto update(Long id, BrandRequestDto request);
    ResponseDataDto delete(Long id);
}