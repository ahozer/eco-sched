package codes.erbil.vms.rest;

import codes.erbil.vms.service.TestGeneratorService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/test-generator")
public class TestGeneratorResource {

    @Inject
    TestGeneratorService testGeneratorService;

    @GET
    @Path("/generate/{testFamilyName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateTestCases(@PathParam @NotBlank String testFamilyName) {
        testGeneratorService.generatorJobStarter(testFamilyName);
        return Response.ok("Job Başlatıldı").build();
    }

    @GET
    @Path("/count")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testCaseCount() {
        Long numOfTestCases = testGeneratorService.testInstancesCount();
        return Response.ok("Test Case sayısı:" + numOfTestCases).build();
    }

    @GET
    @Path("/delete/{testFamilyName}/{testSetNo}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTestCases(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo) {
        testGeneratorService.deletionJobStarter(testFamilyName, testSetNo);
        return Response.ok("Test Case'lerin silinme işlemi başlatıldı").build();
    }
}
