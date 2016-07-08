
#### An OO design demo using ZK, JPA & Spring

A demo of "root entities" being responsible for maintaining the integrity 
of lesser objects. 

With reference to the following image: 

![root objects](root-objects1.png "Root Objects 1")

 1. A line items names a quantity of a given product within a contract. 
 1. A contract has many line items.  
 1. A contract has many deliveries. 
 1. A line item can only be in one delivery.

Note that a products and contracts are top level entities. In contrast 
line items and deliveries only exist in the context of a contract. 

The proceeding paragraph leads to a desing decision that a contract have 
a _composition_ relationsip with line items and deliveries. This implies 
that if we delete a contract we delete the line items and the deliveries. 

We have a requirement that a line item should only be in one delivery. 
Where should that logic live? In the contract that holds all the line 
items and the deliveries. That then puts the logic to ensure that the 
contract is not corrupted into the contract. 

See also the discussion on the ["exposed domain model pattern"](http://codereview.stackexchange.com/questions/93511/data-transfer-objects-vs-entities-in-java-rest-server-application/93533#93533)
