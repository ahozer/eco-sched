package codes.erbil.vms.service;

import codes.erbil.vms.config.model.Bid;
import codes.erbil.vms.config.model.TestCase;
import codes.erbil.vms.entity.TestInstance;
import codes.erbil.vms.solver.workflow.Workflow;
import io.quarkus.panache.common.Parameters;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@ApplicationScoped
public class WorkflowService {

    @Inject
    Random randomG;

    @ConfigProperty(name = "vms.workflow.converter.dependency.probability", defaultValue = "0.2")
    Double dependencyProbability;

    public void buildWorkflowForTestInstances(String testFamilyName, Long testSetNo) {
        List<TestInstance> tiList = TestInstance.list("testFamilyName=:testFamilyName and testSetNo=:testSetNo",
                Parameters.with("testFamilyName", testFamilyName).and("testSetNo", testSetNo));

        for (TestInstance ti : tiList) {
            ti.workflow = true;

            updateBidsEarliestAndLatestSlots(ti);

            ti.testCase.workflowList = generateWorkflows(ti);

        }

        TestInstance.update(tiList);
    }

    private void updateBidsEarliestAndLatestSlots(TestInstance ti) {
        TestCase tc = ti.testCase;

        for (Bid bid : tc.getBids()) {
            bid.setEarliestTime(1);
            bid.setLatestTime(tc.getPeriod());
        }
    }

    private List<Workflow> generateWorkflows(TestInstance ti) {
        List<Workflow> workflows = new ArrayList<>();

        TestCase tc = ti.testCase;
        int workFlowUpperLimit = Double.valueOf(Math.ceil(tc.getPeriod() / 10.0 * 6.0)).intValue();

        int currentTimeSlot = 0;
        Workflow wf = new Workflow();
        for (int i = 1; i <= tc.getBids().size(); i++) {
            Bid bid = tc.getBids().get(i - 1);

            wf.getBids().add(i);
            wf.setPrice(wf.getPrice().add(bid.getPrice()));
            currentTimeSlot += bid.getDuration();

            if (currentTimeSlot >= workFlowUpperLimit || i == tc.getBids().size()) {
                workflows.add(wf);

                generateBidDependenciesForWorkflow(tc, wf);
                wf = new Workflow();
                currentTimeSlot = 0;
            }

        }
        return workflows;
    }

    private void generateBidDependenciesForWorkflow(TestCase tc, Workflow wf) {
        for (int g = wf.getBids().size() - 1; g >= 0; g--) {
            for (int g_p = 0; g_p < g; g_p++) {
                Bid bid = tc.getBids().get(wf.getBids().get(g) - 1);


                if (randomG.nextDouble() < dependencyProbability) {
                    if (bid.getDependencyList() == null) {
                        bid.setDependencyList(new ArrayList<>());
                    }

                    bid.getDependencyList().add(wf.getBids().get(g_p));
                }
            }
        }
    }


}
