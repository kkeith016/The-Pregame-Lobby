package org.yearup.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order
{
    private int orderId;
    private int userId;
    private LocalDate orderDate;
    private String shippingAddress;
    private BigDecimal shippingAmount;
    private List<OrderLineItem> lineItems = new ArrayList<>();

    public Order()
    {
        this.orderDate = LocalDate.now();
        this.shippingAmount = BigDecimal.ZERO;
    }

    public Order(int orderId, int userId, LocalDate orderDate, String shippingAddress, BigDecimal shippingAmount)
    {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.shippingAddress = shippingAddress;
        this.shippingAmount = shippingAmount;
    }

    public int getOrderId()
    {
        return orderId;
    }

    public void setOrderId(int orderId)
    {
        this.orderId = orderId;
    }

    public int getUserId()
    {
        return userId;
    }

    public void setUserId(int userId)
    {
        this.userId = userId;
    }

    public LocalDate getOrderDate()
    {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate)
    {
        this.orderDate = orderDate;
    }

    public String getShippingAddress()
    {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress)
    {
        this.shippingAddress = shippingAddress;
    }

    public BigDecimal getShippingAmount()
    {
        return shippingAmount;
    }

    public void setShippingAmount(BigDecimal shippingAmount)
    {
        this.shippingAmount = shippingAmount;
    }

    public List<OrderLineItem> getLineItems()
    {
        return lineItems;
    }

    public void setLineItems(List<OrderLineItem> lineItems)
    {
        this.lineItems = lineItems;
    }

    public void addLineItem(OrderLineItem lineItem)
    {
        this.lineItems.add(lineItem);
    }

    public BigDecimal getTotal()
    {
        BigDecimal total = lineItems.stream()
                .map(OrderLineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return total.add(shippingAmount);
    }
}