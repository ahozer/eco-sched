package codes.erbil.vms.service;

import codes.erbil.vms.config.manager.TestCaseManager;
import codes.erbil.vms.config.manager.VerifierManager;
import codes.erbil.vms.entity.TestSolution;
import codes.erbil.vms.solver.EcoSchedGurobiSolver;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class ToyTestCaseService {

    @Inject
    EcoSchedGurobiSolver ecoSchedGurobiSolver;

    @Inject
    VerifierManager verifierManager;

    @Inject
    TestCaseManager testCaseManager;

    public boolean solveToyTestCase1WithGurobi() {

        TestSolution ts = ecoSchedGurobiSolver.solve(testCaseManager.getToyTestCase_1());

        return verifierManager.isFeasible(testCaseManager.getToyTestCase_1(), ts);

    }

    public boolean solveToyTestCase2WithGurobi() {
        TestSolution ts = ecoSchedGurobiSolver.solve(testCaseManager.getToyTestCase_2());

        return verifierManager.isFeasible(testCaseManager.getToyTestCase_2(), ts);
    }
}
