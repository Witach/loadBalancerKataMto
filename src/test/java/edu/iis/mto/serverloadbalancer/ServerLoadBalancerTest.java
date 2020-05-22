package edu.iis.mto.serverloadbalancer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.iis.mto.serverloadbalancer.CurrentLoadPercentageMatcher.hasLoadPercentageOf;
import static edu.iis.mto.serverloadbalancer.ServerBuilder.server;
import static edu.iis.mto.serverloadbalancer.ServerShouldContainPredicate.getPredicateFromServerAndVmsIndexes;
import static edu.iis.mto.serverloadbalancer.ServerVmsCountMatcher.hasVmsCountOf;
import static edu.iis.mto.serverloadbalancer.VmBuilder.vm;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class ServerLoadBalancerTest {
    @Test
    public void itCompiles() {
        assertThat(true, equalTo(true));
    }

    @Test
    public void balancingAServer_noVms_serverStaysEmpty() {
        Server theServer = a(server().withCapacity(1));

        balance(aListOfServersWith(theServer), anEmptyListOfVms());

        assertThat(theServer, hasLoadPercentageOf(0.0d));
    }

    @Test
    public void balancingOneServerWithOneSlotCapacity_andOneSlotVm_fillsTheServerWithTheVm() {
        Server theServer = a(server().withCapacity(1));
        Vm theVm = a(vm().ofSize(1));
        balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

        assertThat(theServer, hasLoadPercentageOf(100.0d));
        assertThat("the server should contain vm", theServer.contains(theVm));
    }

    @Test
    public void balancingOneServerWithTenSlotsCapacity_andOneSlotVm_fillTheServerWithTenPercent() {
        Server theServer = a(server().withCapacity(10));
        Vm theVm = a(vm().ofSize(1));
        balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

        assertThat(theServer, hasLoadPercentageOf(10.0d));
        assertThat("the server should contain vm", theServer.contains(theVm));

    }

    @Test
    public void balancingAServerWithEnoughRoom_getsFilledWithAllVms() {
        Server theServer = a(server().withCapacity(100));
        Vm theFirstVm = a(vm().ofSize(1));
        Vm theSecondVm = a(vm().ofSize(1));
        balance(aListOfServersWith(theServer), aListOfVmsWith(theFirstVm, theSecondVm));

        assertThat(theServer, hasVmsCountOf(2));
        assertThat("the server should contain vm", theServer.contains(theFirstVm));
        assertThat("the server should contain vm", theServer.contains(theSecondVm));

    }

    @Test
    public void aVm_shouldBeBalanced_onLessLoadedServerFirst() {
        Server lessLoadedServer = a(server().withCapacity(100).withCurrentLoadOf(45.0d));
        Server moreLoadedServer = a(server().withCapacity(100).withCurrentLoadOf(50.0d));
        Vm theVm = a(vm().ofSize(10));

        balance(aListOfServersWith(moreLoadedServer, lessLoadedServer), aListOfVmsWith(theVm));

        assertThat("the less loaded server should contain vm", lessLoadedServer.contains(theVm));

    }

    @Test
    public void balanceAServerWithNotEnoughRoom_shouldNotBeFilledWithAVm() {
        Server theServer = a(server().withCapacity(10).withCurrentLoadOf(90.0d));
        Vm theVm = a(vm().ofSize(2));
        balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

        assertThat("the less loaded server should not contain vm", !theServer.contains(theVm));
    }


    @ParameterizedTest
    @MethodSource("provideArgsForBalance_serversAndVms")
    public void balance_serversAndVms(
            Server[] servers, List<Double> percentages,
            Vm[] vms, List<ServerShouldContainPredicate> serverShouldContainPredicates) {

        String answerTemplate = "The server %d should contain the vm %d";

        balance(aListOfServersWith(servers), aListOfVmsWith(vms));

        serverShouldContainPredicates.forEach(predicate -> {
            int serverIndex = predicate.getServerIndex();
            predicate.getVmsIndexes().forEach(index -> {
                String answer = String.format(answerTemplate, serverIndex, index);
                assertThat(answer, servers[serverIndex].contains(vms[index]));
            });
        });

        for (int i = 0; i < servers.length ; i++) {
            assertThat(servers[i],hasLoadPercentageOf(percentages.get(i)));
        }
    }


    private static Stream<Arguments> provideArgsForBalance_serversAndVms() {
        Server[] servers = getListOfServersWithCapacity(4, 6);
        Vm[] vms = getListOfVmsWithSize(1, 4, 2);
        List<Double> percentages = List.of(
                75.0d,
                66.66d
        );

        List<ServerShouldContainPredicate> serverShouldContainPredicates = List.of(
                getPredicateFromServerAndVmsIndexes(0, 0, 2),
                getPredicateFromServerAndVmsIndexes(1, 1)
        );


        return Stream.of(
                Arguments.of(servers, percentages, vms, serverShouldContainPredicates)
        );
    }


    private static Server[] getListOfServersWithCapacity(int... capacities) {
        return Arrays.stream(capacities)
                .mapToObj(value -> a(server().withCapacity(value)))
                .toArray(Server[]::new);
    }

    private static Vm[] getListOfVmsWithSize(int... sizes) {
        return Arrays.stream(sizes)
                .mapToObj(value -> a(vm().ofSize(value)))
                .toArray(Vm[]::new);
    }


    private void balance(Server[] servers, Vm[] vms) {
        new ServerLoadBalancer().balance(servers, vms);
    }

    private Vm[] aListOfVmsWith(Vm... vms) {
        return vms;
    }

    private Vm[] anEmptyListOfVms() {
        return new Vm[0];
    }

    private Server[] aListOfServersWith(Server... servers) {
        return servers;
    }

    private static <T> T a(Builder<T> builder) {
        return builder.build();
    }
}
