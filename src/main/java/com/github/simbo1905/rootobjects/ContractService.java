package com.github.simbo1905.rootobjects;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 */
@Named("contractService")
public class ContractService {

    @Inject ContractRespository contractRespository;
    @Inject DeliveryRepository deliveryRepository;
    @Inject LineItemRepository lineItemRepository;

    @Transactional
    public void save(Contract contract) {
        contractRespository.save(contract);
        contract.getDeliveries().stream().forEach(i -> deliveryRepository.save(i));
        contract.getLineItems().stream().forEach(i -> lineItemRepository.save(i));
    }

    @Transactional
    public Contract loadByName(String name) {
        return contractRespository.findByName(name);
    }


//	@Inject
//	protected ReminderRepository reminderRepository;
//
//	@Transactional(readOnly=true)
//	public List<Reminder> findAll(){
//		return this.reminderRepository.findAll();
//	}
//
//	@Transactional(readOnly=false,propagation = Propagation.REQUIRED)
//	public void persist(Reminder reminder){
//		this.reminderRepository.save(reminder);
//	}
//
//	@Transactional(readOnly=false,propagation = Propagation.REQUIRED)
//	public void delete(Reminder reminder)  {
//		this.reminderRepository.delete(reminder);
//	}


}
