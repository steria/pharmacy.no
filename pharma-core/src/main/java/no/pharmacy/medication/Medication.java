package no.pharmacy.medication;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import no.pharmacy.core.Money;

@EqualsAndHashCode(of={"productId", "display"})
@ToString(exclude="xml")
@NoArgsConstructor
public class Medication {

    @Getter @Setter
    private Money trinnPrice;
    @Getter @Setter
    private Money retailPrice;

    @Getter @Setter
    private String productId;
    @Getter @Setter
    private String display;

    @Getter @Setter
    private String substitutionGroup;

    @Getter @Setter
    private String xml;

    public Medication(String productId, String name, int trinnPriceInCents, int retailPriceInCents) {
        this.productId = productId;
        this.display = name;
        this.trinnPrice = Money.inCents(trinnPriceInCents);
        this.retailPrice = Money.inCents(retailPriceInCents);
    }

    public Money getUncoveredAmount() {
        return retailPrice.isGreaterThan(trinnPrice) ? retailPrice.minus(trinnPrice) : Money.zero();
    }

    public Money getCoveredAmount() {
        return retailPrice.isGreaterThan(trinnPrice) ? trinnPrice : retailPrice;
    }

}
