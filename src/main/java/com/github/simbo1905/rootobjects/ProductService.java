package com.github.simbo1905.rootobjects;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by simon on 08/07/2016.
 */
@Named("productService")
public class ProductService {
    @Inject ProductRespository productRespository;

}
