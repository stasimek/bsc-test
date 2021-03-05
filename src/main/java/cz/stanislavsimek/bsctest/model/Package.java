package cz.stanislavsimek.bsctest.model;

import java.math.BigDecimal;

import net.sf.oval.constraint.Digits;
import net.sf.oval.constraint.MatchPattern;
import net.sf.oval.constraint.Min;
import net.sf.oval.constraint.NotNull;

public class Package {

    @NotNull
    @Min(value = 0, inclusive = false)
    @Digits(maxFraction = 3)
    private final BigDecimal weightInKg;

    @NotNull
    @MatchPattern(pattern = "\\d{5}", message = "five digits expected in postal code")
    private final String destinationPostalCode;

    public Package(double weightInKg, String destinationPostalCode) {
        this.weightInKg = BigDecimal.valueOf(weightInKg);
        this.destinationPostalCode = destinationPostalCode;
    }

    public BigDecimal getWeightInKg() {
        return weightInKg;
    }

    public String getDestinationPostalCode() {
        return destinationPostalCode;
    }

    @Override
    public String toString() {
        return weightInKg + " kg => " + destinationPostalCode;
    }
}
