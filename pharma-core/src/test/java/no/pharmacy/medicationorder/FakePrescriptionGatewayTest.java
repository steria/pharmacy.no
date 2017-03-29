package no.pharmacy.medicationorder;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

import org.junit.Test;

import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationOrder;
import no.pharmacy.test.FakePrescriptionGateway;
import no.pharmacy.test.FakeReseptFormidler;
import no.pharmacy.test.PharmaTestData;

public class FakePrescriptionGatewayTest {

    private PharmaTestData testData = new PharmaTestData();

    private FakeReseptFormidler fakeReseptFormidler = new FakeReseptFormidler(testData.getMedicationRepository());

    private PrescriptionGateway gateway = new FakePrescriptionGateway(fakeReseptFormidler, testData.getMedicationRepository());

    private String employeeId = testData.samplePractitioner().getReference().getReference();

    @Test
    public void shouldRetrievePrescriptionList() throws Exception {
        String nationalId = testData.unusedNationalId();
        MedicationOrder medicationOrder = fakeReseptFormidler.addPrescription(nationalId, testData.sampleMedication());

        List<MedicationOrderSummary> orders = gateway.requestMedicationOrdersToDispense(null, nationalId, employeeId);
        assertThat(orders)
            .extracting(o -> o.getMedicationName())
            .contains(medicationOrder.getMedication().getDisplay());

        assertThat(orders.get(0)).hasNoNullFieldsOrProperties();
    }

    @Test
    public void shouldStartPrescriptionDispense() throws Exception {
        MedicationOrder medicationOrder = fakeReseptFormidler.addPrescription(testData.unusedNationalId(), testData.sampleMedication());

        MedicationOrder orderForDispense = gateway.startMedicationOrderDispense(medicationOrder.getPrescriptionId(), null, employeeId);

        assertThat(orderForDispense)
            .isEqualToComparingFieldByField(medicationOrder);
    }

    @Test
    public void shouldCompleteDispense() {
        MedicationOrder medicationOrder = fakeReseptFormidler.addPrescription(testData.unusedNationalId(), testData.sampleMedication());

        MedicationDispense dispense = new MedicationDispense(medicationOrder);
        dispense.setMedication(dispense.getAuthorizingPrescription().getMedication());
        dispense.setPrice(testData.samplePrice());
        dispense.setConfirmedByPharmacist(true);
        dispense.setPackagingControlled(true);
        dispense.setDispensed();

        gateway.completeDispense(dispense, employeeId);

        assertThat(fakeReseptFormidler.getDispensesFor(medicationOrder))
            .extracting(MedicationDispense::getPrice)
            .contains(dispense.getPrice());
    }

}
