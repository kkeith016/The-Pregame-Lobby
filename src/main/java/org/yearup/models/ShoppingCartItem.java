package org.yearup.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ShoppingCartItem
{
    private Product product;
    private int quantity = 1;
    private BigDecimal discountPercent = BigDecimal.ZERO;

    public ShoppingCartItem() { }

    public ShoppingCartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.discountPercent = BigDecimal.ZERO;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getDiscountPercent() {
        return discountPercent;
    }

    // Accepts BigDecimal instead of int for precision
    public void setDiscountPercent(BigDecimal discountPercent) {
        if (discountPercent != null) {
            this.discountPercent = discountPercent;
        } else {
            this.discountPercent = BigDecimal.ZERO;
        }
    }

    @JsonIgnore
    public int getProductId() {
        return product != null ? product.getProductId() : 0;
    }

    public BigDecimal getLineTotal() {
        if (product == null) return BigDecimal.ZERO;

        BigDecimal basePrice = product.getPrice();
        BigDecimal qty = new BigDecimal(this.quantity);

        BigDecimal subTotal = basePrice.multiply(qty);
        BigDecimal discountAmount = subTotal.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return subTotal.subtract(discountAmount);
    }

    // Empty setter required for frameworks that need a setter (like Jackson)
    public void setLineTotal(BigDecimal lineTotal) {
        // No-op: lineTotal is calculated dynamically
    }
}