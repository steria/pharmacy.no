package no.pharmacy.web.dispense;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eaxy.Document;
import org.eaxy.Element;
import org.eaxy.Xml;

import no.pharmacy.dispense.DispenseOrder;
import no.pharmacy.dispense.MedicationDispenseRepository;
import no.pharmacy.dispense.MedicationOrder;

public class PrescriptionsController extends HttpServlet {


    private PrescriptionsSource prescriptionsSource;
    private MedicationDispenseRepository medicationDispenseRepository;


    public PrescriptionsController(PrescriptionsSource prescriptionsSource, MedicationDispenseRepository medicationDispenseRepository) {
        this.prescriptionsSource = prescriptionsSource;
        this.medicationDispenseRepository = medicationDispenseRepository;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Document doc = Xml.readResource("/pharma-webapp/index.html.template");

        String nationalId = req.getParameter("nationalId");
        if (nationalId != null) {
            Element results = doc.find("...", "#results").first();
            results.text("Results for " + nationalId);

            List<MedicationOrder> orders = prescriptionsSource.prescriptionsForPerson(nationalId);
            for (MedicationOrder order : orders) {
                results.add(Xml.el("div",
                        Xml.el("label",
                            Xml.el("input")
                                .type("checkbox").name("prescriptionId").val(order.getPrescriptionId()),
                            //Xml.el("a", order.getPrescriber().getDisplay()).attr("href", "/practitioner/" + order.getPrescriber().getReference()),
                            Xml.el("span", order.getMedication().getDisplay()))));
            }
        }


        resp.setContentType("text/html");
        doc.writeTo(resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DispenseOrder collection = new DispenseOrder();

        for (String id : req.getParameterValues("prescriptionId")) {
            collection.addMedicationOrder(prescriptionsSource.getById(id));
        }

        medicationDispenseRepository.saveDispenseOrder(collection);
        resp.sendRedirect("/dispenseOrder/" + collection.getIdentifier());
    }
}
