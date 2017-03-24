package no.pharmacy.order;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.Test;

import no.pharmacy.dispense.MedicationDispense;
import no.pharmacy.dispense.MedicationDispenseAction;
import no.pharmacy.medication.Medication;
import no.pharmacy.test.JdbcMedicationDispenseRepository;
import no.pharmacy.test.PharmaTestData;
import no.pharmacy.test.TestDataSource;

public class MedicationDispenseRepositoryTest {

    private DataSource dataSource = TestDataSource.instance();
    private PharmaTestData testData = new PharmaTestData();
    private MedicationDispenseRepository repository = new JdbcMedicationDispenseRepository(dataSource, testData.getMedicationRepository());

    @Test
    public void retrievesSimpleDispenseOrder() {
        DispenseOrder order = new DispenseOrder();
        order.addMedicationOrder(testData.sampleMedicationOrder());

        repository.saveDispenseOrder(order);

        assertThat(order).hasNoNullFieldsOrProperties();
        DispenseOrder retrievedOrder = repository.getDispenseOrderById(order.getIdentifier());
        assertThat(retrievedOrder)
            .isEqualToComparingFieldByField(order);
        assertThat(retrievedOrder.getMedicationOrders().get(0))
            .isEqualToIgnoringGivenFields(order.getMedicationOrders().get(0), "alternatives");
    }

    @Test
    public void populatesAlternativeMedicationsOnRetrieve() {
        DispenseOrder order = new DispenseOrder();
        Medication medication = testData.medicationWithSubstitutes();
        order.addMedicationOrder(testData.sampleMedicationOrder(medication));

        repository.saveDispenseOrder(order);
        MedicationOrder retrievedPrescription = repository.getDispenseOrderById(order.getIdentifier()).getMedicationOrders().get(0);
        assertThat(retrievedPrescription.getAlternatives())
            .extracting(m -> m.getSubstitutionGroup())
            .containsOnly(medication.getSubstitutionGroup());
        assertThat(retrievedPrescription.getAlternatives().size())
            .isGreaterThan(1);
    }

    @Test
    public void populatesMedicationDispense() {
        DispenseOrder order = new DispenseOrder();

        MedicationOrder medicationOrder = testData.sampleMedicationOrder();
        order.addMedicationOrder(medicationOrder);

        assertThat(order.getMedicationDispenses()).hasSize(1);
        MedicationDispense dispense = order.getMedicationDispenses().get(0);
        assertThat(dispense.getMedication()).isNull();
        assertThat(dispense.getAuthorizingPrescription())
            .isEqualTo(medicationOrder);
    }

    @Test
    public void itUpdatesMedicationDispense() {
        DispenseOrder order = new DispenseOrder();

        MedicationOrder medicationOrder = testData.sampleMedicationOrder();
        order.addMedicationOrder(medicationOrder);
        repository.saveDispenseOrder(order);

        MedicationDispense prescription = order.getMedicationDispenses().get(0);
        prescription.setPrice(testData.samplePrice());
        prescription.setMedication(medicationOrder.getMedication());
        repository.update(prescription);

        DispenseOrder retrieved = repository.getDispenseOrderById(order.getIdentifier());
        assertThat(retrieved.getMedicationDispenses().get(0))
            .isEqualToComparingFieldByField(prescription);
    }

    @Test
    public void shouldSaveInteractionWarnings() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        Medication ritalin = testData.sampleMedication("500595");
        Medication aurorix = testData.sampleMedication("466813");
        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);
        dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(aurorix)).setMedication(aurorix);

        repository.saveDispenseOrder(dispenseOrder);

        dispenseOrder.createWarnings();
        repository.update(ritalinDispense);

        MedicationDispense retrieved = repository.getDispenseOrderById(dispenseOrder.getIdentifier())
            .getMedicationDispenses().get(0);
        assertThat(retrieved).isEqualToComparingFieldByField(ritalinDispense);
    }

    @Test
    public void shouldUpdateDispenseActions() {
        DispenseOrder dispenseOrder = new DispenseOrder();
        Medication ritalin = testData.sampleMedication("500595");
        Medication aurorix = testData.sampleMedication("466813");
        MedicationDispense ritalinDispense = dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(ritalin));
        ritalinDispense.setMedication(ritalin);
        dispenseOrder.addMedicationOrder(testData.sampleMedicationOrder(aurorix)).setMedication(aurorix);

        repository.saveDispenseOrder(dispenseOrder);

        dispenseOrder.createWarnings();
        repository.update(ritalinDispense);
        for (MedicationDispenseAction action : ritalinDispense.getWarningActions()) {
            action.setAction("2");
            action.setRemark("Test remark");
        }
        repository.update(ritalinDispense);

        MedicationDispense retrieved = repository.getDispenseOrderById(dispenseOrder.getIdentifier())
            .getMedicationDispenses().get(0);
        assertThat(retrieved).isEqualToComparingFieldByField(ritalinDispense);

    }

}
