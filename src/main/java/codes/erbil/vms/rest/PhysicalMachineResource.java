package codes.erbil.vms.rest;

import codes.erbil.vms.config.manager.PhysicalMachineManager;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/physical-machine")
@Produces(MediaType.APPLICATION_JSON)
public class PhysicalMachineResource {

    @Inject
    PhysicalMachineManager physicalMachineManager;

    @GET
    @Path("/{vmCapacity}")
    public Response getPhysicalMachinesByVmCapacity(@PathParam String vmCapacity) {
        if (!("1024".equals(vmCapacity) || "2048".equals(vmCapacity)
                || "3072".equals(vmCapacity) || "4096".equals(vmCapacity))) return Response.status(404).build();
        else
            return Response.ok(physicalMachineManager.getPhysicalMachineMap().get(Integer.valueOf(vmCapacity))).build();
    }
}
