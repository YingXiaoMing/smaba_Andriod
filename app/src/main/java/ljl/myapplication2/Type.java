package ljl.myapplication2;

import java.util.ArrayList;
import java.util.List;

public class Type {
    private String TypeName;
    private List<String> MachineId;

    public Type(String title,List<String> nList) {
        this.TypeName = title;
        this.MachineId = nList;
    }

    public int size() {
        return MachineId.size() + 1;
    }
    public List<String> getChild () {
        return MachineId;
    }
    public String getName() {
        return TypeName;
    }

}
