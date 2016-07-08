package com.github.simbo1905.rootobjects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:dataSourceContext.xml","classpath:application-context.xml"})
@Transactional
public class RootObjectTests {

	@Autowired
	protected DataSource dataSource;

	@Autowired
	protected ProductRespository productRespository;

    @Autowired
	protected ContractRespository contractRespository;

    @Autowired
    protected DeliveryRepository deliveryRepository;

    @Autowired
    protected LineItemRepository lineItemRepository;

    @Autowired
    protected DeliveryLineItemRepository deliveryLineItemRepository;

	@Autowired
	protected ContractService contractService;

	@Autowired
    protected EntityManager entityManager;

    /**
     * Test that we can round trip a product to the database.
     */
	@Test
	public void testProductRespository() throws Exception {
		final String name = "Heavy Tank";
		final Product product = new Product(name);
		productRespository.save(product);
		final Product loaded = productRespository.findByName(name);
		Assert.assertTrue(product.getId() == loaded.getId());
	}

    /**
     * Test that we can round trip an empty contract to the database.
     */
    @Test
    public void testContractRespository() throws Exception {
        final String name = "Heavy Tank Contract";
        final Contract contract = new Contract(name);

        final Delivery delivery = contract.createDelivery(new Date(), "London");

        contractRespository.save(contract);
        deliveryRepository.save(delivery);
        final Contract loaded = contractRespository.findByName(name);
        Assert.assertTrue(contract.getContractId() == loaded.getContractId());

        Assert.assertTrue(!loaded.getDeliveries().isEmpty());
        Assert.assertEquals(loaded.getDeliveries().iterator().next().getLocation(), "London");
    }

    /**
     * Test that we can round trip a line item containing a quantity of a product to the database.
     */
    @Test
    public void testLineItemRepository() throws Exception {
        final String cname = "Heavy Tank Contract";
        final Contract contract = new Contract(cname);
        contractRespository.save(contract);
        final String pname = "Heavy Tank";
        final Product product = new Product(pname);
        productRespository.save(product);
        final LineItem twoTanks = new LineItem(product, 2);
        lineItemRepository.save(twoTanks);
        final LineItem loaded = lineItemRepository.getOne(twoTanks.getId());
        Assert.assertTrue(loaded.getProduct().getName() == pname);
    }

    /**
     * Test that we can round trip a delivery-to-line-item join table entity to the database.
     */
    @Test
    public void testDeliveryLineItemRepository() throws Exception {
        final DeliveryLineItem deliveryLineItem = new DeliveryLineItem(1L, 2L);
        this.deliveryLineItemRepository.save(deliveryLineItem);
        final DeliveryLineItem loaded = this.deliveryLineItemRepository.getOne(deliveryLineItem.getKey());
        Assert.assertEquals(deliveryLineItem.getKey(), loaded.getKey());
        Assert.assertEquals(deliveryLineItem.getDeliveryId(), loaded.getDeliveryId());
        Assert.assertEquals(deliveryLineItem.getLineItemId(), loaded.getLineItemId());
    }

    /**
     */
    @Test
    public void testCanSaveDeliveries() throws Exception {
        // given two different products in the database
        final Product heavyTank = new Product("Heavy Tank Product");
        final Product lightTank = new Product("Light Tank Product");
        productRespository.save(heavyTank);
        productRespository.save(lightTank);

        // when we create a couple of deliveries
        final Contract contract = new Contract("Heavy Tank Contract");
        final Delivery delivery = contract.createDelivery(new Date(), "London");
        contract.addProductToDelivery(heavyTank, delivery.getLocation(), 2);
        contract.addProductToDelivery(lightTank, delivery.getLocation(), 3);

        // and save them to the database
        contractService.save(contract);

        // and clear the object cache infront of the database
        entityManager.clear();

        // then loaded baack
        final Contract loadedContract = contractRespository.findOne(contract.getContractId()); //contractService.loadByName("Heavy Tank Contract");


        // then
        Assert.assertThat(loadedContract.getDeliveries().size(), is(1));
        final Delivery loadedDelivery = loadedContract.getDeliveries().get(0);
        Assert.assertThat(loadedDelivery.getLineItems().size(), is(2));



    }
}
