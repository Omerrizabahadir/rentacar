package com.autorent.rentacar.helper;

import com.autorent.rentacar.model.Brand;

import java.util.ArrayList;
import java.util.List;

public class BrandDOFactory {

    public Brand getBrandWithId(Long id){
        Brand brand = new Brand();
          brand.setId(id);
          brand.setName("TEST_BRAND");

          return brand;
    }
    public Brand getBrandWithoutId(){
        Brand brand = new Brand();
        brand.setName("TEST_BRAND");

        return brand;
    }
    public List<Brand> getBrandListWithId(){
        Brand brand1 = new Brand();
        brand1.setId(1L);
        brand1.setName("TEST_BRAND_1");

        Brand brand2 = new Brand();
        brand2.setId(2L);
        brand2.setName("TEST_BRAND_2");

        Brand brand3 = new Brand();
        brand3.setId(3L);
        brand3.setName("TEST_BRAN_3");

        List<Brand> brandList = new ArrayList<>();
        brandList.add(brand1);
        brandList.add(brand2);
        brandList.add(brand3);

        return brandList;
    }
}
