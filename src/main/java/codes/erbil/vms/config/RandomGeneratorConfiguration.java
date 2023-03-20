package codes.erbil.vms.config;

import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import java.util.Random;

public class RandomGeneratorConfiguration {
    @ConfigProperty(name = "vms.seed")
    Integer seed;

    @ApplicationScoped
    public RandomGenerator randomGenerator() {
        RandomGenerator rg = new JDKRandomGenerator();
        rg.setSeed(seed);

        return rg;
    }

    @ApplicationScoped
    public Random random() {
        Random rd = new Random(seed);

        return rd;
    }
}
