package com.github.simbo1905.rootobjects.contract;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

@Named("contractService")
public class ContractService {

    @Inject ContractRespository contractRespository;

    @Transactional
    public void save(Contract contract) {
        contractRespository.save(contract);
    }

    @Transactional
    public Contract loadByName(String name) {
        return contractRespository.findByName(name);
    }
}
