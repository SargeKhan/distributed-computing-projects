import java.util.ArrayList;
import java.util.List;

/**
 * Created by bhaiya on 1/26/15.
 */
public class ClientInfo {
    int portNo;
    List<String> keyWords;
    ClientInfo(){
        keyWords= new ArrayList<String>();
    }
    ClientInfo(int portNo){
        this.portNo= portNo;
        keyWords= new ArrayList<String>();
    }
}
