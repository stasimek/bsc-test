package cz.stanislavsimek.bsctest.model;

import java.math.BigDecimal;
import net.sf.oval.constraint.Digits;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotNull;

public class Fee {

    @NotNull
    @Min(value = 0, inclusive = false)
    @Digits(maxFraction = 3)
    private final BigDecimal weight;

    /**
     * Delivery fee of package weighing more than or exactly this.weight.
     */
    @NotNull
    @Min(value = 0, inclusive = true)
    @Digits(maxFraction = 2)
    private final BigDecimal fee;

    public Fee(double weight, double fee) {
        this.weight = BigDecimal.valueOf(weight);
        this.fee = BigDecimal.valueOf(fee);
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public BigDecimal getFee() {
        return fee;
    }

    @Override
    public String toString() {
        return weight + " kg => " + fee + " EUR";
    }
}
