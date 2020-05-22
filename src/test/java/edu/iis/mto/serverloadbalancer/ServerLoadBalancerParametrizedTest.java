package edu.iis.mto.serverloadbalancer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static edu.iis.mto.serverloadbalancer.CurrentLoadPercentageMatcher.hasLoadPercentageOf;
import static edu.iis.mto.serverloadbalancer.ServerBuilder.server;
import static edu.iis.mto.serverloadbalancer.VmBuilder.vm;
import static org.hamcrest.MatcherAssert.assertThat;



public class ServerLoadBalancerParametrizedTest extends ServerLoadBalancerBaseTest{

	@ParameterizedTest(name = "VM Size= {1} Server Capacity= {0} Expected Percentage= {2}")
	@CsvSource({"1,1,100.0", "2,2,100.0","3,3,100.0","2,1,50.0","4,1,25.0","5,2,40.0", "3,1,33.33"})
	public void balancingServerWitheSlotCapacity_andSlotVm_fillsTheServerWithTheVm(
			int serverCapacity, int vmSize, double expectedPercentage) {
		Server theServer = a(server().withCapacity(serverCapacity));
		Vm theVm = a(vm().ofSize(vmSize));
		balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

		assertThat(theServer, hasLoadPercentageOf(expectedPercentage));
		assertThat("the server should contain vm", theServer.contains(theVm));
	}

	@ParameterizedTest(name = "VM Size= {1} Server Capacity= {0}")
	@CsvSource({"1,2","2,3"})
	public void serverShouldNotContainVMifSizeNotFit(int serverCapacity, int vmSize){
		Server theServer = a(server().withCapacity(serverCapacity));
		Vm theVm = a(vm().ofSize(vmSize));
		balance(aListOfServersWith(theServer), aListOfVmsWith(theVm));

		assertThat(theServer, hasLoadPercentageOf(0.0));
		assertThat("the server should not contain vm", !theServer.contains(theVm));
	}
	
	
}
