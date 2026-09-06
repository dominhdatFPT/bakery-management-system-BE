package bakery.dto;

import java.math.BigDecimal;

public class IngredientResponse {

    private Long id;
    private String name;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal lowStockThreshold;

    public IngredientResponse(Long id, String name, String unit,
                               BigDecimal currentStock, BigDecimal lowStockThreshold) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.currentStock = currentStock;
        this.lowStockThreshold = lowStockThreshold;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUnit() { return unit; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public BigDecimal getLowStockThreshold() { return lowStockThreshold; }
}