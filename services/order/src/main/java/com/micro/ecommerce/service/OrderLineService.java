package com.micro.ecommerce.service;

import com.micro.ecommerce.dto.OrderLineRequest;
import com.micro.ecommerce.dto.OrderLineResponse;
import com.micro.ecommerce.mapper.OrderLineMapper;
import com.micro.ecommerce.repository.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderLineService {

    private final OrderLineRepository orderLineRepository;
    private final OrderLineMapper mapper;

    public Integer saveOrderLine(OrderLineRequest orderLineRequest) {
        var Order = mapper.toOrderLine(orderLineRequest);
        return orderLineRepository.save(Order).getId();
    }

    public List<OrderLineResponse> findAllByOrderId(Integer orderId) {
        return orderLineRepository.findAllByOrderId(orderId)
                .stream()
                .map(mapper::toOrderLineResponse)
                .collect(Collectors.toList());
    }
}
