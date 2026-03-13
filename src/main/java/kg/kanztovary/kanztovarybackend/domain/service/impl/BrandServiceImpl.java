package kg.kanztovary.kanztovarybackend.domain.service.impl;

import kg.kanztovary.kanztovarybackend.config.datasource.entity.Brand;
import kg.kanztovary.kanztovarybackend.config.datasource.repository.BrandRepository;
import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandDto;
import kg.kanztovary.kanztovarybackend.domain.dto.brand.BrandRequestDto;
import kg.kanztovary.kanztovarybackend.domain.dto.exception.ResponseDataDto;
import kg.kanztovary.kanztovarybackend.domain.enums.ResponseStatus;
import kg.kanztovary.kanztovarybackend.domain.mapper.BrandMapper;
import kg.kanztovary.kanztovarybackend.domain.service.BrandService;
import kg.kanztovary.kanztovarybackend.exception.ResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    @Override
    public BrandDto create(BrandRequestDto request) {
        if (brandRepository.existsByName(request.getName())) {
            throw new ResponseException(
                    ResponseStatus.BRAND_EXISTS.getCode(),
                    ResponseStatus.BRAND_EXISTS.getMessage()
            );
        }
        Brand brand = brandMapper.toEntity(request);
        brandRepository.save(brand);
        log.info("Создан бренд: {}", brand.getName());
        return brandMapper.toDto(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandDto getById(Long id) {
        return brandMapper.toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BrandDto> getAll() {
        return brandRepository.findAll()
                .stream()
                .map(brandMapper::toDto)
                .toList();
    }

    @Override
    public BrandDto update(Long id, BrandRequestDto request) {
        Brand brand = findById(id);
        if (!brand.getName().equals(request.getName()) && brandRepository.existsByName(request.getName())) {
            throw new ResponseException(
                    ResponseStatus.BRAND_EXISTS.getCode(),
                    ResponseStatus.BRAND_EXISTS.getMessage()
            );
        }
        brandMapper.updateEntity(brand, request);
        brandRepository.save(brand);
        log.info("Обновлён бренд id={}", id);
        return brandMapper.toDto(brand);
    }

    @Override
    public ResponseDataDto delete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new ResponseException(
                    ResponseStatus.BRAND_NOT_FOUND.getCode(),
                    ResponseStatus.BRAND_NOT_FOUND.getMessage()
            );
        }
        brandRepository.deleteById(id);
        log.info("Удалён бренд id={}", id);
        return ResponseDataDto.builder()
                .code(ResponseStatus.SUCCESS.getCode())
                .message(ResponseStatus.SUCCESS.getMessage())
                .build();
    }

    private Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResponseException(
                        ResponseStatus.BRAND_NOT_FOUND.getCode(),
                        ResponseStatus.BRAND_NOT_FOUND.getMessage()
                ));
    }
}