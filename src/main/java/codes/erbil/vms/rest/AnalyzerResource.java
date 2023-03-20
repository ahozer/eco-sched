package codes.erbil.vms.rest;

import codes.erbil.vms.service.AnalyzerService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/analyze")
public class AnalyzerResource {

    @Inject
    AnalyzerService analyzerService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get() throws IOException {
        analyzerService.prepareJobStarter();
        return "Analiz başlatılmıştır!";
    }

    @GET
    @Path("/{testFamilyName}/{testSetNo}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getFamilyAndSet(@PathParam @NotBlank String testFamilyName, @PathParam @NotNull Long testSetNo) throws IOException {
        analyzerService.prepareJobStarter(testFamilyName, testSetNo);
        return "Analiz başlatılmıştır!";
    }

    @GET
    @Path("/number-of-bids")
    @Produces(MediaType.TEXT_PLAIN)
    public String getNumOfBids() {
        return analyzerService.numOfBidsCsv();
    }
}
