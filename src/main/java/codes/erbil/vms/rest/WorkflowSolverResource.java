package codes.erbil.vms.rest;

import codes.erbil.vms.config.manager.workflow.WorkflowSolverManager;
import codes.erbil.vms.service.WorkflowService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/wsolver")
public class WorkflowSolverResource {

    @Inject
    WorkflowService workflowService;

    @Inject
    WorkflowSolverManager workflowSolverManager;

    @GET
    @Path("/convert-workflow/{testFamilyName}/{testSetNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertWorkflow(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo) {
        workflowService.buildWorkflowForTestInstances(testFamilyName, testSetNo);

        StringBuilder sb = new StringBuilder("Test Family:");
        sb.append(testFamilyName);
        sb.append(" ,");
        sb.append("Test Set No:");
        sb.append(testSetNo);
        sb.append(" başarıyla workflow'a dönüştürülmüştür.");

        return Response.ok(sb.toString()).build();
    }

    @GET
    @Path("/gurobi/{testFamilyName}/{testSetNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithGurobi(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo) {
        workflowSolverManager.gurobiSolverJobStarter(testFamilyName, testSetNo);

        return Response.ok("Testlerin Gurobi ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/GREEDY/{testFamilyName}/{testSetNo}/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithGreedy(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo,
                                             @PathParam @NotNull String order) {

        workflowSolverManager.greedySolverJobStarter(testFamilyName, testSetNo, order);

        return Response.ok("Testlerin Greedy Placement Algoritma ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/MBP/{testFamilyName}/{testSetNo}/{order}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithMbp(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo,
                                          @PathParam @NotNull String order) {

        workflowSolverManager.mbpSolverJobStarter(testFamilyName, testSetNo, order);

        return Response.ok("Testlerin Multiple Bid Placement Algoritma ile çözümü başlatılmıştır").build();
    }

    @GET
    @Path("/GENETIC/{testFamilyName}/{testSetNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response solveTestCasesWithGenetic(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo) {

        workflowSolverManager.geneticSolverJobStarter(testFamilyName, testSetNo);

        return Response.ok("Testlerin Genetic Algoritma ile çözümü başlatılmıştır").build();
    }
}
