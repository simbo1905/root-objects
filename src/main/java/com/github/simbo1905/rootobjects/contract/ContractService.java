package com.github.simbo1905.rootobjects.contract;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * This class is a public service class that lets us load and save contracts which are root entity.
 */
@Named("contractService")
public class ContractService {

    @Inject ContractRespository contractRespository;
    @Inject DeliveryRepository deliveryRepository;
    @Inject LineItemRepository lineItemRepository;

    @Transactional
    public void save(Contract contract) {
        contractRespository.save(contract);
        contract.getLineItems().forEach(i -> lineItemRepository.save(i));
        contract.getDeliveries().forEach(i -> deliveryRepository.save(i));
    }

    @Transactional
    public Contract loadByName(String name) {
        return contractRespository.findByName(name);
    }
}
