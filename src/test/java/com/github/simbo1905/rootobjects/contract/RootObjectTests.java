package com.github.simbo1905.rootobjects.contract;

import com.github.simbo1905.rootobjects.Money;
import com.github.simbo1905.rootobjects.product.Product;
import com.github.simbo1905.rootobjects.product.ProductService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:dataSourceContext.xml","classpath:application-context.xml"})
@Transactional
public class RootObjectTests {

	@Autowired
	protected DataSource dataSource;

	@Autowired
	protected ProductService productService;

    @Autowired
	protected ContractRespository contractRespository;

    @Autowired
    protected DeliveryRepository deliveryRepository;

    @Autowired
    protected LineItemRepository lineItemRepository;

	@Autowired
	protected ContractService contractService;

	@Autowired
    protected EntityManager entityManager;

    @Autowired
    private DeliveryLineItemRepository deliveryLineItemRepository;

    static Money FIVE_MILLION_USD = new Money("USD", new BigDecimal("5000000.00"));

    static Money TEN_MILLION_USD = new Money("USD", new BigDecimal("10000000.00"));

    static Money ZERO_USD = new Money("USD", new BigDecimal("0.00"));

    /**
     * Test that we can round trip a product to the database.
     */
	@Test
	public void testProductRespository() throws Exception {
		final String name = "Heavy Tank";
		final Product product = new Product(name, FIVE_MILLION_USD);
		productService.save(product);
		final Product loaded = productService.findByName(name);
		Assert.assertTrue(product.getId() == loaded.getId());
	}

    /**
     * Test that we can round trip a contract with a delivery to the database
     */
    @Test
    public void testDeliverySavedWithContract() throws Exception {
        deliveryWithContract();
    }

    private Delivery deliveryWithContract() {
        final String name = "Heavy Tank Contract";
        final Contract contract = new Contract(name);

        final Delivery delivery = contract.createDelivery(new Date(), "London");

        contractService.save(contract);

        entityManager.flush();
        entityManager.clear();

        final Contract loaded = contractRespository.findByName(name);
        Assert.assertEquals(contract.getContractId(), loaded.getContractId());

        Assert.assertTrue(!loaded.getDeliveries().isEmpty());
        Assert.assertEquals("London", loaded.getDeliveries().iterator().next().getLocation());

        return delivery;
    }

    /**
     * Test that if we remove a delivery from a contract it is deleted from the database
     */
    @Test(expected = EntityNotFoundException.class)
    public void testDeliveryDeletedWhenOrphaned() throws Exception {
        // check is in the database
        final Delivery delivery = deliveryWithContract();
        final Delivery loaded = this.deliveryRepository.getOne(delivery.deliveryId());
        Assert.assertNotNull(loaded);

        final Contract contract = delivery.getContract();
        final boolean deleted = contract.deleteDelivery(delivery);
        Assert.assertTrue(deleted);

        contractService.save(contract);

        entityManager.flush();
        entityManager.clear();

        // should throw EntityNotFoundException but doesn't throw until you try to assertNotNull
        final Delivery doesNotExist = this.deliveryRepository.getOne(delivery.deliveryId());
        Assert.assertNull(doesNotExist);
    }

    /**
     * Test that we can round trip a contract with a line item to the database
     */
    @Test
    public void testLineItemSavedWithContract() throws Exception {
       lineItemWithContract();
    }

    @Test
    public void testCostLogic() throws Exception {
        TEN_MILLION_USD.equals(FIVE_MILLION_USD.times(2));
        TEN_MILLION_USD.equals(FIVE_MILLION_USD.add(FIVE_MILLION_USD));
        FIVE_MILLION_USD.equals(TEN_MILLION_USD.subtract(FIVE_MILLION_USD));

    }

    private LineItem lineItemWithContract() {
        final String pname = "Heavy Tank";
        final Product product = new Product(pname, FIVE_MILLION_USD);
        productService.save(product);

        final String cname = "Heavy Tank Contract";
        final Contract contract = new Contract(cname);

        final LineItem lineItem = contract.createLineItem(product, 2);
        this.contractService.save(contract);

        entityManager.flush();
        entityManager.clear();

        final Contract loaded = this.contractService.loadByName("Heavy Tank Contract");
        Assert.assertNotNull(loaded);
        Assert.assertThat(loaded.getLineItems().get(0).getProduct().getName(), is(pname));
        Assert.assertTrue(loaded.getTotalCost().equals(TEN_MILLION_USD));

        return lineItem;
    }

    /**
     * Test that when we delete a line item from the contract it is delelted from the database
     */
    @Test(expected = EntityNotFoundException.class)
    public void testLineItemDeletedWhenOrphaned(){
        final LineItem lineItem = lineItemWithContract();

        final LineItem loaded = this.lineItemRepository.getOne(lineItem.lineItemId());
        Assert.assertNotNull(loaded);
        Assert.assertTrue(loaded.getContract().getTotalCost().equals(TEN_MILLION_USD));

        Assert.assertTrue(lineItem.getContract().deleteLineItem(lineItem));
        Assert.assertEquals(ZERO_USD, lineItem.getContract().getTotalCost());

        this.contractService.save(lineItem.getContract());

        entityManager.flush();
        entityManager.clear();

        // should throw EntityNotFoundException but doesn't throw until you try to assertNotNull
        final LineItem doesNotExist = this.lineItemRepository.getOne(lineItem.lineItemId());
        Assert.assertNull(doesNotExist);
    }

    /**
     * Test that we can round trip a contract with a delivery-line-item to the database
     */
    @Test
    public void testDeliveryLineItemSavedWithContract() throws Exception {
        final DeliveryLineItem deliveryLineItem = contractWithDeliveryLineItem();
    }

    private DeliveryLineItem contractWithDeliveryLineItem() {
        final String pname = "Heavy Tank";
        final Product product = new Product(pname, FIVE_MILLION_USD);
        productService.save(product);
        final String cname = "Heavy Tank Contract";
        final Contract contract = new Contract(cname);
        final Delivery delivery = contract.createDelivery(new Date(), "London");
        final LineItem lineItem = contract.createLineItem(product, 2);
        contract.addLineItemToDelivery(lineItem, delivery);
        contractService.save(contract);

        entityManager.flush();
        entityManager.clear();

        Assert.assertTrue(contract.getDeliveries().iterator().next().getLineItems().contains(lineItem));
        Assert.assertNotNull(delivery.deliveryId());
        Assert.assertNotNull(lineItem.lineItemId());

        final Contract loaded = this.contractService.loadByName("Heavy Tank Contract");
        Assert.assertNotNull(loaded);
        final List<DeliveryLineItem> loadedDliSet = loaded.getDeliveryLineItems();
        Assert.assertTrue(!loadedDliSet.isEmpty());
        final DeliveryLineItem loadedDli = loadedDliSet.iterator().next();
        Assert.assertEquals(delivery.deliveryId(), loadedDli.getDelivery().deliveryId());
        Assert.assertEquals(lineItem.lineItemId(), loadedDli.getLineItem().lineItemId());
        return loadedDli;
    }

    /**
     * Test that if we remove a delivery-line-item from a contract it is deleted from the database
     */
    @Test(expected = EntityNotFoundException.class)
    public void testDeliveryLineItemDeletedWhenOrphaned() throws Exception {
        // check is in the db
        final DeliveryLineItem deliveryLineItem = contractWithDeliveryLineItem();
        final DeliveryLineItem loaded = this.deliveryLineItemRepository.getOne(deliveryLineItem.getDeliveryLineItemId());
        Assert.assertNotNull(loaded);

        final Contract contract = deliveryLineItem.getContract();
        final boolean deleted = contract.removeLineItemFromDelivery(deliveryLineItem.getLineItem(), deliveryLineItem.getDelivery());

        contractService.save(contract);
        entityManager.flush();
        entityManager.clear();

        final Contract loadedAfterDelete = this.contractService.loadByName("Heavy Tank Contract");
        Assert.assertNotNull(loadedAfterDelete);
        final List<DeliveryLineItem> loadedDliSet = loadedAfterDelete.getDeliveryLineItems();
        Assert.assertTrue(loadedDliSet.isEmpty());
        final DeliveryLineItem doesNotExist = this.deliveryLineItemRepository.getOne(deliveryLineItem.getDeliveryLineItemId());
        Assert.assertNull(doesNotExist);
    }

    /**
     * Test that if we add a line item to a new delivery it is removed from the old delivery.
     */
    @Test
    public void testMoveLineItemBetweenDeliveries() throws Exception {
        final String pname = "Heavy Tank";
        final String cname = "Heavy Tank Contract";
        final Contract contract = new Contract(cname);

        {
            // save a contract, two deliveries, and one line item in the moscow delivery
            final Product product = new Product(pname, FIVE_MILLION_USD);
            productService.save(product);
            contract.createDelivery(new Date(), "London");
            final Delivery moscow = contract.createDelivery(new Date(), "Moscow");
            final LineItem lineItem = contract.createLineItem(product, 2);
            contract.addLineItemToDelivery(lineItem, moscow);
            contractService.save(contract);
            Assert.assertEquals(moscow, contract.getLineItems().get(0).delivery.get());
        }

        // wipe the cache assocated with our transaction.
        entityManager.flush();
        entityManager.clear();

        {
            // load the contract and find the constituent parts

            final Contract loadContract = this.contractService.loadByName("Heavy Tank Contract");
            final Delivery loadedMoscow = loadContract.getLineItems().get(0).delivery.get();
            Assert.assertEquals("Moscow", loadedMoscow.getLocation());
            final LineItem loadedLineItem = loadedMoscow.getLineItems().get(0);
            Assert.assertEquals(loadedMoscow, loadedLineItem.delivery.get());
            Assert.assertEquals("Heavy Tank", loadedLineItem.getProduct().getName());

            final Delivery loadedLondon =
                    loadContract.getDeliveries().stream().filter(
                            d -> d.getLocation().equals("London")).findFirst().get();

            // move the line item to the other delivery
            loadContract.addLineItemToDelivery(loadedLineItem, loadedLondon);

            // it has moved between deliveries
            Assert.assertEquals(pname, loadedLondon.getLineItems().get(0).getProduct().getName());
            Assert.assertTrue(loadedMoscow.getLineItems().isEmpty());
            Assert.assertEquals(loadedLondon, loadedLineItem.delivery.get());
        }


    }
}
