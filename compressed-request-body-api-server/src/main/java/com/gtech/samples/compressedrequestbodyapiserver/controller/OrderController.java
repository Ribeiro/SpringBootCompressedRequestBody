package com.gtech.samples.compressedrequestbodyapiserver.controller;

import com.gtech.samples.compressedrequestbodyapiserver.model.Order;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
public class OrderController {

    @PostMapping("/orders")
    Order newOrder(@RequestBody Order newOrder) {
        return newOrder;
    }

}
