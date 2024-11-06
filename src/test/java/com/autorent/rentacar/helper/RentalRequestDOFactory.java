package com.autorent.rentacar.helper;

import com.autorent.rentacar.dto.RentalCarInfo;
import com.autorent.rentacar.dto.RentalRequest;

import java.time.LocalDateTime;
import java.util.List;

public class RentalRequestDOFactory {

    public RentalRequest getRentalRequest(int quantity, Long carId, Long customerId) {
        RentalRequest rentalRequest = new RentalRequest();
        rentalRequest.setRentalList(List.of(getRentalCarInfo(quantity, carId)));
        rentalRequest.setCustomerId(customerId);

        return rentalRequest;
    }
    public RentalCarInfo getRentalCarInfo(int quantity, Long carId) {
        RentalCarInfo rentalCarInfo = new RentalCarInfo();
        rentalCarInfo.setQuantity(quantity);
        rentalCarInfo.setStartRentalDate(LocalDateTime.now());
        rentalCarInfo.setEndRentalDate(LocalDateTime.now().plusDays(5));
        rentalCarInfo.setCarId(carId);

        return rentalCarInfo;
    }
}
