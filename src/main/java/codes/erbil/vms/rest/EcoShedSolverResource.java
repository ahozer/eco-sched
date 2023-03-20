package codes.erbil.vms.rest;


import codes.erbil.vms.config.manager.SolverManager;
import codes.erbil.vms.service.ToyTestCaseService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/solver")
public class EcoShedSolverResource {

    @Inject
    SolverManager solverManager;

    @Inject
    ToyTestCaseService toyTestCaseService;

    @GET
    @Path("/gurobi/{testFamilyName}/{testSetNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithGurobi(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo) {
        solverManager.gurobiSolverJobStarter(testFamilyName, testSetNo);

        return Response.ok("Testlerin Gurobi ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/greedy/{testFamilyName}/{testSetNo}/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithGreedy(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo,
                                             @PathParam @NotNull String order) {
        solverManager.greedySolverJobStarter(testFamilyName, testSetNo, order);

        return Response.ok("Testlerin Greedy Algoritma ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/SBPF/{testFamilyName}/{testSetNo}/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithSbpf(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo,
                                           @PathParam @NotNull String order) {

        solverManager.sbpfSolverJobStarter(testFamilyName, testSetNo, order);

        return Response.ok("Testlerin Single Bid Placement Algoritma ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/MBP/{testFamilyName}/{testSetNo}/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithMbp(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo,
                                          @PathParam @NotNull String order) {

        solverManager.mbpSolverJobStarter(testFamilyName, testSetNo, order);

        return Response.ok("Testlerin Multiple Bid Placement Algoritma ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/GENETIC/{testFamilyName}/{testSetNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithGenetic(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo) {

        solverManager.geneticSolverJobStarter(testFamilyName, testSetNo);

        return Response.ok("Testlerin Genetic Algoritma ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/gurobi/toy-test-case-1")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveToyTestCase_1() {
        return Response.ok(toyTestCaseService.solveToyTestCase1WithGurobi()).build();
    }

    @GET
    @Path("/gurobi/toy-test-case-2")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveToyTestCase_2() {
        return Response.ok(toyTestCaseService.solveToyTestCase2WithGurobi()).build();
    }

    @GET
    @Path("/remove/{testFamilyName}/{testSetNo}/{strategy}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeDuplicate(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo,
                                    @PathParam @NotNull String strategy) {
        solverManager.removeDuplicateSolutions(testFamilyName, testSetNo, strategy);
        return Response.ok("Silindi").build();
    }
}
