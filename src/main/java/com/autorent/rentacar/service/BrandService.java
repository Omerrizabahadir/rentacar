package com.autorent.rentacar.service;

import com.autorent.rentacar.exception.BrandDeleteException;
import com.autorent.rentacar.exception.BrandDuplicateException;
import com.autorent.rentacar.exception.BrandINameCanNotBeEmptyException;
import com.autorent.rentacar.exception.BrandNotFoundException;
import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.repository.BrandRepository;
import com.autorent.rentacar.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    private final CarRepository carRepository;

    public Brand createBrand(Brand brand){
        Optional<Brand> optionalBrand = brandRepository.findBrandByName(brand.getName());
        if (optionalBrand.isPresent()){
            throw new BrandDuplicateException("Brand name is already defined : " + brand.getName());
        }
        if(brand.getName().isEmpty()){
            throw new BrandINameCanNotBeEmptyException("Brand name cannot be empty!");
        }
        return brandRepository.save(brand);
    }
    public List<Brand> getAllBrandList(){
        List <Brand> brandList = brandRepository.findAll();
        brandList.sort(Comparator.comparing(Brand::getId));
        return brandList;
    }
    public Brand getBrand(Long id){
        return brandRepository.findById(id).orElseThrow(() -> new BrandNotFoundException("Brand not found id : " + id));

    }
    public Brand updateBrand(Brand brand){
        return brandRepository.save(brand);
    }
    public void deleteBrand(Long id){
        Long countBrandOfCar = carRepository.getCountBrandOfCarByBrandId(id);
        if (countBrandOfCar > 0 ){
            throw  new BrandDeleteException("You can not delete this brand because brand has " +countBrandOfCar + "car(s)");
        }
        brandRepository.deleteById(id);
    }
}
