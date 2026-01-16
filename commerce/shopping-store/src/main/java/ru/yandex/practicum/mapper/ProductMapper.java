package ru.yandex.practicum.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.dto.shoppingStore.ProductDto;
import ru.yandex.practicum.model.Product;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    ProductDto toDto(Product product);

    Product toEntity(ProductDto productDto);

    @Mapping(target = "productId", ignore = true)
    void updateEntity(ProductDto productDto, @MappingTarget Product product);

    List<ProductDto> toDtoList(List<Product> products);

    List<Product> toEntityList(List<ProductDto> productDtos);
}
