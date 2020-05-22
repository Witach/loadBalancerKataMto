package edu.iis.mto.serverloadbalancer;

import org.junit.jupiter.params.provider.Arguments;

import java.util.LinkedList;
import java.util.List;

public class ArgumentsForBalanceTestBuilder implements Builder<Arguments> {
    Server[] servers;
    Vm[] vms;
    List<Double> percentages;
    List<ServerShouldContainPredicate> serverShouldContainPredicates = new LinkedList<>();


    public ArgumentsForBalanceTestBuilder withServers(Server[] servers){
        this.servers = servers;
        return this;
    }

    public  ArgumentsForBalanceTestBuilder withVms(Vm[] vms) {
        this.vms = vms;
        return this;
    }

    public ArgumentsForBalanceTestBuilder withPercentages(List<Double> percentages){
        this.percentages = percentages;
        return this;
    }

    public ArgumentsForBalanceTestBuilder addServerShouldContainPredicate(ServerShouldContainPredicate serverShouldContainPredicate){
        this.serverShouldContainPredicates.add(serverShouldContainPredicate);
        return this;
    }
    public static ArgumentsForBalanceTestBuilder argumentsForBalanceTestBuilder(){
        return new ArgumentsForBalanceTestBuilder();
    }

    @Override
    public Arguments build() {
        return Arguments.of(
                servers,
                percentages,
                vms,
                serverShouldContainPredicates
        );
    }
}
