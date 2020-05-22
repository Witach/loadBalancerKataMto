package edu.iis.mto.serverloadbalancer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerShouldContainPredicate {
    private int serverIndex;
    private List<Integer> vmsIndexes;

    private ServerShouldContainPredicate(int serverIndex, List<Integer> vmsIndexes) {
        this.serverIndex = serverIndex;
        this.vmsIndexes = vmsIndexes;
    }

    public int getServerIndex() {
        return serverIndex;
    }

    public List<Integer> getVmsIndexes() {
        return vmsIndexes;
    }

    public String getPredicateWrongAnswer(){

        String vmsIndexes = this.vmsIndexes.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));

        String indexPrompt = String.format("The server %d should contain the vms ", serverIndex);

        return indexPrompt + vmsIndexes;
    }

    public static ServerShouldContainPredicate getPredicateFromIndexes(int serverIndex, Integer ...vmsIndexes){
        return new ServerShouldContainPredicate(serverIndex, Arrays.asList(vmsIndexes));
    }


}
