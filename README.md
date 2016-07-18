
# Domain Driven Design: Entities, Value Objects, Aggregates and Roots with JPA

This sample app is written up as a [series of blog posts](https://simbo1905.wordpress.com/category/domain-drive-design/). 

### Running Or Modifying The Code

You can run the tests on the commandline with `mvn test`. The code is written using IntelliJ community edition. Create a new project "from source" selecting "maven" as the type. You can then run the test class which round-trips all the objects to an in-memory database.  

The diagrams are authored with UMLet. 

### The Problem Space

The following image shows the toy modelling problem: 

![root objects](root-objects1.png "Root Objects 1")

 1. A `contract` has many `lineitems` 
 1. A `contract` has many `deliveries` 
 1. A `delivery` to a location contains some `lineitems`
 1. A `lineitem` is a quantity of a given `product` within a `contract`
 1. A `lineitem` can only be in zero or one `deliveries`
 1. Altering the `lineitems` within a `contract` updates the total cost of a contract

A `lineitem` can be in zero `deliveries` so that a customer can 
decide upon the products and even pay for a contract independently of 
arranging for one or more `deliveries`. 

Note that `Money` is a value object type. It has no identity so its not 
an entity. I wont be discussing value objects further as they are not 
a complex concept. 

Note that in the diagram the lines with the black diamonds on the end denote 
UML "composition". To quote wikipedia (ephasis mine and with very minor edits): 

> Composition is a kind of association where the composite object has 
> sole responsibility for the disposition of the component parts. 
> The relationship between the composite and the component is a strong 
> “has a” relationship, as the composite object takes ownership of the 
> component. This means the composite is responsible for the **creation and 
> destruction** of the component parts. A [contained] object may only be part 
> of one composite. If the composite object is destroyed, all the [contained 
> objects] must be destroyed 

Simply put we are saying that the `contract` owns and controls both the `deliveries` and 
`lineitems` that it contains. If the `contract` gets cancelled or deleted then 
all the `lineitems` are cancelled and deleted. Also in this example updating 
the quantity of a `product` in the `contract` or adding and removing `deliveries` 
to the `contract` implies we are updating the `contract`. Is this valid? 
Only if the users of the system agree with this way of describing the problem 
domain. We should prototype with this model and get feedback from the users
to validate the design. 

The `product` and `contract` classes are labelled as root entities. To 
quote the blog page at the link above: 

> Aggregates draw a boundary around one or more Entities. 
> An Aggregate enforces invariants for all its Entities 
> for any operation it supports.  Each Aggregate has a Root 
> Entity, which is the only member of the Aggregate that any 
> object outside the Aggregate is allowed to hold a reference to. 

This says that to get at the quantity of a `product` in a `contract` or the 
`deliveries` in a `contract` (or whatever) in the object world we load the 
contract and go via it. This implies that to load things from a database 
into memory we will query and load one or more `contracts` to work with rather than 
query and load `deliveries` or `line items` directly. Is that the 
right things to do? Test it out with your users if they are always talking about working 
with a contract to manage its line items and deliveries then yes. If they 
are asking you to build screens that work primarily with deliveries and 
you discover that you can move a delivery between contracts then you 
may need to make `delivery` a root entity. 

Consider the business rule that altering the `lineitems` within a `contract`
updates the total cost of a contract. Expressed another way it says that 
it is a rule (aka an invariant) that the total cost field of the contract 
is sum of the cost of the individual line items within the contract. Which 
class should take care of that? The `contract`. Why? Objects should 
encapsulate state and related behaviour. Implication? You ask the `contract` 
to alter the quantity of a line item, else add or remove a line item. It 
can then ensure that the `totalCost` is updated. 

Consider the business rule that a `lineitem` can only be within one 
`delivery`. Which class should take care of that? The `contract`. Why? 
If we don't it is a corruption of the state of a `contract`. So we should 
keep the logic that stops it from getting corrupted within it. That logic will 
be tested when we test that class and will be easier to keep working 
as we evolve the logic of that class. 

The book [Domain Driven Design by Eric Evans](http://domainlanguage.com/ddd/) has the theory of how to do design 
this way and the book [Pojos In Action by Chris Richardson](https://www.manning.com/books/pojos-in-action) is an 
old but excellent book on how to do DDD in Java with Spring. 

### The Implementation

Lets have a look at the relational table model that goes the UML model above: 

![root objects2](root-objects2.png "Root Objects 2")

The major difference is that we have one more database table than we 
have UML entities. The alien in the room is `delivery_lineitem` which is a 
join table between `delivery` and `lineitem` which records that a line 
item has been put into a delivery. Note that in the relational world we 
don't really need `contract_id` on the join table; it only needs two 
columns which can be the primary key. The reason that the table has the 
`contract_id` is so that JPA can "see" the join table entities as part of 
the `contract` root object to load them when ever we load the `contract`. 

Another compromise is that if you run the unit tests they create the join 
table with a fourth column which is a generated primary key. Why? Because 
JPA put up a fight when I tried to create any type of compound primary key 
out of two fields and if you fight JPA you mostly loose (your mind). Do I 
care that has two more columns than a database designer would use? A little 
but I probably have better things to be doing with my time than optimising 
a few bytes away when database servers now have terabytes of disk and hundreds 
of gigs of memory. From a code perspective every collection is mapped the same 
way so optimising the join table with distinct JPA code makes the solution 
more complex to maintain. Lazy or pragmatic? Whatever. 

Why is the join entity an alien? Because in our example it wasn't in the UML 
model as it wasn't discovered in the elaboration of the domain model with the 
users. If it was a "real thing" the users would have given it a name and talked 
about it having tangible attributes and we would have added it to the UML model. 
So it's only an technical artifact of the relational model. We should hide it 
and not make it part of the public API. Why? Because it is not part of our 
problem domain and with DDDD we model the problem not the solution. 

How do we handle this? We make the `contract` the responsible class and 
put both the business logic, and the logic to keep the object and relational 
book work in sync, into this "all things contract related" class: 

 1. We add a Java class entity for the join table but don't make it a public class. 
 2. We don't let code directly manipulate the list of lineitems within a delivery. 
  We ask the contract to do the work. The contract can create or remove a 
   join entity and also update the list of lineitems within the delivery.
 3. We add a `@PostLoad` to the contract that is run immediately after JPA has 
 loaded a contract, its deliveries, its lineitems, and its join table entities 
 from the db. In that method we can scan the list of join table entities 
 to know now to recover the state of the list of lineitems in each delivery.

So that is three things the `contact` root entity is doing: 

 1. Ensuring that the total cost is kept up to date. 
 2. Ensuring that a `lineitem` can only be in one delivery. 
 3. Ensuring that the alien join entity isn't exposed to the outside world. 

All of the above nicely illustrates the power of the aggregate and root entity 
concepts. We can create a java package per root entity with a service or 
system class that lets you load the root object only.  Force code outside 
of the package to use methods on the root object. Then the root object can 
enforce that everything is maintained in a proper state so that we don't 
get corruptions of the state across the aggregated entities. 
 
How do we stop code outside of the `contract` package from corrupting 
the relationships by adding or removing lineitems from deliveries without 
going via methods on contract which ensure the join table is kept in sync? 
The following code show how the list of lineitems in a delivery is declared: 
    
    @Transient
    List<LineItem> lineItems = new ArrayList<>();
    
    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

That syntax says we have a non-public list (invisible outside of the 
Java package) that is transient (JPA wont try to save it into 
the database) as it is maintained entirely by Java logic. The getter that 
returns the list wraps it in an unmodifiable list. That is a proxy object 
that lets you get at items in the list but throws an exception if you try 
to modify the list. Ninja. 

By applying the same approach everywhere we arrange it so that you can "see" both 
`contract`, `lineitem` and `delivery` objects outside of the package that 
they are defined in; but you have to call methods on the `contract` objects 
to modify anything. To load and save contract objects you use a public 
`ContractServce` system class that has methods to query the database to 
load contracts. 

### Pro-Tips For Industrial Strength Financial Code

Aggregate roots and the OO approach outlined in this sample app make it 
very easy to add industrial strength features: 

**Audit Everything.** Root entities make it easier to keep and access a 
full audit train. Just add:

 * `int version` (make this part of a compound primary key of the entity)
 * `Date modifiedAt` 
 * `String modifiedBy` 
 * `Date deletedAt` 
 * `String deletedBy`

Have the root entity at every mutation or deletion simply create 
a copy of the entity that updates these fields. In all the normal getters 
filter to show only the highest version of each entity. You can also add 
methods that see the historic versions. Why? So that you can write admin 
screens to show the full audit trail of who did what to all entities in 
the system. 

A good audit trail is a killer feature for any system that handles serious 
amounts of money. You can also add a "restore to date" feature so that a user 
can reverse out changes to the system easily and in an auditable way. 
Such killer features that are easy to add when using the techniques 
documented here but are very expensive to code into a system that isn't 
using these techniques. 

Such audit logic sits nicely into the service classes that can update the 
version fields of the root entities when saving them, can then query for 
a list of versions with modification dates, and can then load a specific 
version of a root entity. The root entities themselves have the logic to 
version the entities of the objects they control. They can also have getter 
methods that take a date to be able to filter entities within the aggregate 
to a specific historic point in time.  

**Use A Locking Pattern.** Add either optimistic, or both optimistic and 
pessimistic locking capabilities, into every root entity. This is 
remarkably easy as all modifications go via the root entity. So only root 
entities need locking fields.

JPA has locking features. I have used the built-in JPA optimistic locking. 
With pessimistic locking we had custom logic to tell the users things were 
locked, and let them break locks. This was implemented as normal fields 
`Date lockedAt` and `String lockedBy` fields on all the root entities and 
simple queries and logic in the service classes to either honour or break 
a lock. The service class could easily audit that the user had asked to 
break a lock. If you do let people override pessimistic locks then you 
should also use optimistic locking to stop user overwriting each others 
changes. 

### See Also

See also the discussion on the ["exposed domain model pattern"](http://codereview.stackexchange.com/questions/93511/data-transfer-objects-vs-entities-in-java-rest-server-application/93533#93533)
